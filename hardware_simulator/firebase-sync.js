/* =====================================================================
   HARDWARE SIMULATOR — FIREBASE DATA LAYER (PHASE 5)
   =====================================================================
   Phase 5 Scope:
   - Centralized Safety System for devices with maxActiveDuration (e.g. iron01).
   - Tracks continuous active duration using Firebase state and timestamps.
   - Automatic safety shutdown: writes state: false to Firebase on timeout.
   - Isolated SAFETY_TEST_MODE configuration for rapid automated testing.
   - Multi-switch and all single-switch Phase 1-4 capabilities preserved.
   ===================================================================== */

const FIREBASE_CONFIG = {
  apiKey: "AIzaSyAmml9SxysjxAcZnZVFA3qEoGJ7wcOuv4w",
  authDomain: "maad-mini-project-4b71e.firebaseapp.com",
  databaseURL: "https://maad-mini-project-4b71e-default-rtdb.asia-southeast1.firebasedatabase.app",
  projectId: "maad-mini-project-4b71e",
  storageBucket: "maad-mini-project-4b71e.firebasestorage.app",
  messagingSenderId: "1063487593316",
  appId: "1:1063487593316:android:daea92788cfda89fccb871"
};

const DB_FLOORS_PATH = "homes/home001/floors";

/* =====================================================================
   SAFETY SYSTEM CONFIGURATION
   =====================================================================
   - Production: maxActiveDuration is in minutes (e.g. 15 = 15 minutes).
   - Development/Test Mode: When SAFETY_TEST_MODE is true, 1 minute in
     Firebase configuration is scaled to 1 second for rapid testing
     (e.g. 15 minutes = 15 seconds).
   - Note: The database value in Firebase remains untouched (15).
   ===================================================================== */
const SAFETY_TEST_MODE = true;
const SAFETY_POLL_INTERVAL_MS = 1000;

function getMaxDurationMs(maxActiveMinutes) {
  if (!maxActiveMinutes || maxActiveMinutes <= 0) return 0;
  if (SAFETY_TEST_MODE) {
    // Scale: 1 configured minute -> 1,000 ms (1 second)
    return maxActiveMinutes * 1000;
  }
  // Production: 1 configured minute -> 60,000 ms (60 seconds)
  return maxActiveMinutes * 60 * 1000;
}

const DataLayer = (() => {
  let db = null;
  let isInitialized = false;
  let hasReceivedInitialData = false;
  let safetyIntervalId = null;
  const listeners = new Set();
  const localCache = {};      // deviceId -> normalized device object
  const deviceIndex = {};     // deviceId -> { floorId, zoneId, path, data }
  const recentActions = {};   // deviceId -> { actor, timestamp }
  let floorsData = {};        // floorId -> floor object

  function notify(deviceId, stateObj, prevStatus, extraMeta = {}) {
    const snapshot = { deviceId, ...stateObj, prevStatus, ...extraMeta };
    listeners.forEach((fn) => {
      try {
        fn(snapshot);
      } catch (err) {
        console.error("[DataLayer] Listener callback error:", err);
      }
    });
  }

  function updateConnectionBadge(isLive, labelText) {
    const connIndicator = document.getElementById("connIndicator");
    const connLabel = document.getElementById("connLabel");
    if (connIndicator) connIndicator.dataset.mode = isLive ? "live" : "mock";
    if (connLabel) connLabel.textContent = labelText || (isLive ? "LIVE — connected to Firebase" : "OFFLINE / ERROR");
  }

  /* Centralized Safety Monitor */
  function startSafetyMonitor() {
    if (safetyIntervalId) return; // Prevent duplicate timers

    safetyIntervalId = setInterval(() => {
      if (!hasReceivedInitialData || !db) return;
      const now = Date.now();
      const deviceIds = Object.keys(deviceIndex);

      deviceIds.forEach((deviceId) => {
        const device = localCache[deviceId];
        if (!device) return;

        const maxDurationMinutes = device.maxActiveDuration;
        if (!maxDurationMinutes || maxDurationMinutes <= 0) return;

        // Device is safety-critical and currently powered ON
        if (device.state === true) {
          const startTime = device.lastUpdated || now;
          const elapsedMs = now - startTime;
          const limitMs = getMaxDurationMs(maxDurationMinutes);

          if (elapsedMs >= limitMs) {
            console.warn(`[SafetySystem] Safety limit exceeded for ${deviceId} (${device.deviceName}). Elapsed: ${elapsedMs}ms, Limit: ${limitMs}ms. Initiating automatic shutdown.`);
            
            // Mark action as safety-system shutdown
            recentActions[deviceId] = {
              actor: "safety-system",
              isSafetyShutdown: true,
              timestamp: now
            };

            // Trigger atomic state write to Firebase
            DataLayer.setDeviceStatus(deviceId, false, "safety-system");
          }
        }
      });
    }, SAFETY_POLL_INTERVAL_MS);

    console.log(`[SafetySystem] Safety monitor armed (Test Mode: ${SAFETY_TEST_MODE ? "ENABLED (1m = 1s)" : "DISABLED (Realtime)"}).`);
  }

  function stopSafetyMonitor() {
    if (safetyIntervalId) {
      clearInterval(safetyIntervalId);
      safetyIntervalId = null;
    }
  }

  function processFloorsSnapshot(rawFloors) {
    if (!rawFloors) {
      console.warn("[DataLayer] Empty floors data received from Firebase.");
      return;
    }
    floorsData = rawFloors;
    const discoveredCount = { floors: 0, zones: 0, devices: 0 };
    const now = Date.now();

    Object.entries(rawFloors).forEach(([floorId, floorObj]) => {
      discoveredCount.floors++;
      const zones = floorObj?.zones || {};

      Object.entries(zones).forEach(([zoneId, zoneObj]) => {
        discoveredCount.zones++;
        const devices = zoneObj?.devices || {};

        Object.entries(devices).forEach(([deviceId, deviceData]) => {
          discoveredCount.devices++;
          const realPath = `${DB_FLOORS_PATH}/${floorId}/zones/${zoneId}/devices/${deviceId}`;

          // Index device
          deviceIndex[deviceId] = {
            floorId,
            zoneId,
            path: realPath,
            data: deviceData
          };

          // Determine power state
          const stateBool = typeof deviceData.state === "boolean"
            ? deviceData.state
            : (deviceData.state === "true" || deviceData.status === "ON");

          const prevCached = localCache[deviceId];
          const prevDisplayStatus = prevCached?.displayStatus;
          const prevStatus = prevCached?.status;
          const displayStatus = stateBool
            ? "ON"
            : (deviceData.status === "DISCONNECTED" ? "DISCONNECTED" : (deviceData.status === "ERROR" ? "ERROR" : "OFF"));

          // Check for sub-switch changes on multi-switch devices
          let changedSwitchId = null;
          if (deviceData.switches && prevCached?.switches) {
            for (const sKey of Object.keys(deviceData.switches)) {
              if (deviceData.switches[sKey] !== prevCached.switches[sKey]) {
                changedSwitchId = sKey;
                break;
              }
            }
          }

          const switchesChanged = JSON.stringify(prevCached?.switches) !== JSON.stringify(deviceData.switches);

          const isChanged = !prevCached ||
            prevCached.state !== stateBool ||
            prevCached.status !== deviceData.status ||
            switchesChanged ||
            prevCached.lastUpdated !== deviceData.lastUpdated;

          // Determine source actor
          let actor = "firebase";
          let isSafetyShutdown = false;
          const recent = recentActions[deviceId];
          if (recent && (now - recent.timestamp) < 6000) {
            actor = recent.actor;
            isSafetyShutdown = Boolean(recent.isSafetyShutdown);
          }

          localCache[deviceId] = {
            ...deviceData,
            deviceId,
            floorId,
            zoneId,
            state: stateBool,
            status: deviceData.status || "Normal",
            maxActiveDuration: deviceData.maxActiveDuration || null,
            switchCount: deviceData.switchCount || (deviceData.switches ? Object.keys(deviceData.switches).length : 0),
            switches: deviceData.switches ? { ...deviceData.switches } : null,
            displayStatus,
            updatedAt: deviceData.lastUpdated || now,
            updatedBy: actor
          };

          if (isChanged && hasReceivedInitialData) {
            console.log(`[DataLayer] Device update: ${deviceId} (${deviceData.deviceName}) -> state=${stateBool}, actor=${actor}`);
            notify(deviceId, localCache[deviceId], prevDisplayStatus || prevStatus, { changedSwitchId, actor, isSafetyShutdown });
          }
        });
      });
    });

    if (!hasReceivedInitialData) {
      console.log(`[DataLayer] Initial discovery completed: ${discoveredCount.floors} floors, ${discoveredCount.zones} zones, ${discoveredCount.devices} devices.`);
      hasReceivedInitialData = true;
      startSafetyMonitor();
    }
  }

  return {
    mode: "live",
    isTestMode: SAFETY_TEST_MODE,

    init(onReady) {
      if (isInitialized) {
        if (onReady) onReady();
        return;
      }

      try {
        if (!firebase.apps.length) {
          firebase.initializeApp(FIREBASE_CONFIG);
        }
        db = firebase.database();
        isInitialized = true;
        console.log("[DataLayer] Firebase initialized for Phase 5 (Safety System).");

        // Monitor connection status
        db.ref(".info/connected").on("value", (snap) => {
          const connected = snap.val() === true;
          if (connected) {
            console.log("[DataLayer] Connected to Firebase Realtime Database.");
            updateConnectionBadge(true, "LIVE — connected to Firebase");
          } else {
            console.warn("[DataLayer] Disconnected from Firebase Realtime Database.");
            updateConnectionBadge(false, "DISCONNECTED — trying to reconnect...");
          }
        });

        // Attach listener to floors root: homes/home001/floors
        const floorsRef = db.ref(DB_FLOORS_PATH);

        floorsRef.on(
          "value",
          (snap) => {
            const rawFloors = snap.val();
            const isFirst = !hasReceivedInitialData;
            processFloorsSnapshot(rawFloors);

            if (isFirst && onReady) {
              onReady();
            }
          },
          (error) => {
            console.error(`[DataLayer] Firebase read error at ${DB_FLOORS_PATH}:`, error);
            updateConnectionBadge(false, "FIREBASE ERROR — permission or network issue");
            if (!hasReceivedInitialData && onReady) onReady();
          }
        );
      } catch (err) {
        console.error("[DataLayer] Failed to initialize Firebase:", err);
        updateConnectionBadge(false, "FIREBASE INIT FAILED");
        if (onReady) onReady();
      }
    },

    onDeviceChange(callback) {
      listeners.add(callback);
    },

    getState(deviceId) {
      return (
        localCache[deviceId] || {
          deviceId,
          state: false,
          status: "Normal",
          displayStatus: "OFF",
          maxActiveDuration: null,
          switches: null,
          switchCount: 0,
          updatedAt: null,
          updatedBy: null
        }
      );
    },

    getAllState() {
      return localCache;
    },

    getDevice(deviceId) {
      return deviceIndex[deviceId] || null;
    },

    getDevicePath(deviceId) {
      return deviceIndex[deviceId]?.path || null;
    },

    getDiscoveredDevices() {
      return Object.keys(deviceIndex);
    },

    getDeviceIndex() {
      return deviceIndex;
    },

    getFloors() {
      return floorsData;
    },

    getFloor(floorId) {
      return floorsData[floorId] || null;
    },

    getZones(floorId) {
      return floorsData[floorId]?.zones || {};
    },

    getZone(floorId, zoneId) {
      return floorsData[floorId]?.zones?.[zoneId] || null;
    },

    getSafetyRemainingSeconds(deviceId) {
      const device = localCache[deviceId];
      if (!device || !device.maxActiveDuration || device.state !== true) return null;
      const now = Date.now();
      const startTime = device.updatedAt || now;
      const elapsedMs = now - startTime;
      const limitMs = getMaxDurationMs(device.maxActiveDuration);
      const remainingMs = Math.max(0, limitMs - elapsedMs);
      return Math.ceil(remainingMs / 1000);
    },

    setDeviceStatus(deviceId, newState, actor = "simulator-ui") {
      let boolState;
      if (typeof newState === "boolean") {
        boolState = newState;
      } else if (typeof newState === "string") {
        boolState = newState.toUpperCase() === "ON" || newState.toUpperCase() === "TRUE";
      } else {
        boolState = Boolean(newState);
      }

      recentActions[deviceId] = {
        actor,
        isSafetyShutdown: actor === "safety-system",
        timestamp: Date.now()
      };

      const deviceEntry = deviceIndex[deviceId];
      if (deviceEntry && db) {
        const realPath = deviceEntry.path;
        const updates = {
          state: boolState,
          lastUpdated: Date.now()
        };

        console.log(`[DataLayer] Writing device state (${realPath}) by [${actor}]:`, updates);

        db.ref(realPath)
          .update(updates)
          .then(() => {
            console.log(`[DataLayer] Firebase update confirmed for ${deviceId} (state=${boolState}, actor=${actor})`);
          })
          .catch((err) => {
            console.error(`[DataLayer] Error writing to Firebase (${realPath}):`, err);
          });
      } else {
        console.warn(`[DataLayer] Device ${deviceId} not found in index. Updating local fallback.`);
        const prevStatus = localCache[deviceId]?.displayStatus || (localCache[deviceId]?.state ? "ON" : "OFF");
        localCache[deviceId] = {
          ...localCache[deviceId],
          deviceId,
          state: boolState,
          status: typeof newState === "string" ? newState : "Normal",
          displayStatus: boolState ? "ON" : "OFF",
          updatedAt: Date.now(),
          updatedBy: actor
        };
        notify(deviceId, localCache[deviceId], prevStatus, { actor });
      }
    },

    setSwitchState(deviceId, switchId, newState, actor = "simulator-ui") {
      let boolState;
      if (typeof newState === "boolean") {
        boolState = newState;
      } else if (typeof newState === "string") {
        boolState = newState.toUpperCase() === "ON" || newState.toUpperCase() === "TRUE";
      } else {
        boolState = Boolean(newState);
      }

      recentActions[deviceId] = {
        actor,
        changedSwitchId: switchId,
        timestamp: Date.now()
      };

      const deviceEntry = deviceIndex[deviceId];
      if (!deviceEntry || !db) {
        console.warn(`[DataLayer] Cannot set switch state: device ${deviceId} not found.`);
        return;
      }

      const currentCached = localCache[deviceId] || deviceEntry.data || {};
      const currentSwitches = { ...(currentCached.switches || {}) };
      currentSwitches[switchId] = boolState;

      // Overall state is true if at least one switch is ON
      const overallState = Object.values(currentSwitches).some((v) => v === true);

      const realPath = deviceEntry.path;
      const updates = {
        [`switches/${switchId}`]: boolState,
        state: overallState,
        lastUpdated: Date.now()
      };

      console.log(`[DataLayer] Writing individual switch update (${realPath}):`, updates);

      db.ref(realPath)
        .update(updates)
        .then(() => {
          console.log(`[DataLayer] Switch ${switchId} on ${deviceId} updated to ${boolState} in Firebase`);
        })
        .catch((err) => {
          console.error(`[DataLayer] Error writing switch update to Firebase (${realPath}):`, err);
        });
    }
  };
})();