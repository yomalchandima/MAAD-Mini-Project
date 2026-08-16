/* =====================================================================
   HARDWARE SIMULATOR — FIREBASE DATA LAYER (PHASE 5.5)
   =====================================================================
   - Firebase Realtime Database is the SINGLE SOURCE OF TRUTH.
   - Robust dynamic floor, zone, and device discovery.
   - Strictly ignores primitive or corrupt non-device nodes.
   - Centralized Safety System for devices with maxActiveDuration (e.g. iron01).
   - Tracks continuous active duration using Firebase state and timestamps.
   - Automatic safety shutdown: writes state: false to Firebase on timeout.
   - Multi-switch and single-switch bidirectional synchronization.
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
const DB_SCHEDULES_PATH = "homes/home001/schedules";
const SCHEDULE_POLL_INTERVAL_MS = 1000;

/* =====================================================================
   SAFETY SYSTEM CONFIGURATION
   =====================================================================
   - Production: maxActiveDuration is in minutes (e.g. 15 = 15 minutes).
   - Development/Test Mode: When SAFETY_TEST_MODE is true, 1 minute in
     Firebase configuration is scaled to 1 second for rapid testing
     (e.g. 15 minutes = 15 seconds).
   ===================================================================== */
const SAFETY_TEST_MODE = true;
const SAFETY_POLL_INTERVAL_MS = 1000;

function getMaxDurationMs(maxActiveMinutes) {
  if (!maxActiveMinutes || maxActiveMinutes <= 0) return 0;
  if (SAFETY_TEST_MODE) {
    // Scale: 1 configured minute -> 1,000 ms (1 second)
    return maxActiveMinutes * 1000;
  }
  return maxActiveMinutes * 60 * 1000;
}

const DataLayer = (() => {
  let db = null;
  let isInitialized = false;
  let hasReceivedInitialData = false;
  let safetyIntervalId = null;
  let schedulerIntervalId = null;
  const deviceChangeListeners = new Set();
  const structureChangeListeners = new Set();
  const scheduleChangeListeners = new Set();
  const localCache = {};           // deviceId -> normalized device object
  const deviceIndex = {};          // deviceId -> { floorId, zoneId, path, data }
  const recentActions = {};        // deviceId -> { actor, timestamp }
  const executedOccurrences = new Set(); // Set of "scheduleId_YYYY-MM-DD_HH:mm"
  let floorsData = {};             // floorId -> clean floor object
  let schedulesData = {};          // scheduleId -> clean schedule object

  function notifyDeviceChange(deviceId, stateObj, prevStatus, extraMeta = {}) {
    const snapshot = { deviceId, ...stateObj, prevStatus, ...extraMeta };
    deviceChangeListeners.forEach((fn) => {
      try {
        fn(snapshot);
      } catch (err) {
        console.error("[DataLayer] Device listener callback error:", err);
      }
    });
  }

  function notifyStructureChange() {
    structureChangeListeners.forEach((fn) => {
      try {
        fn(floorsData);
      } catch (err) {
        console.error("[DataLayer] Structure listener callback error:", err);
      }
    });
  }

  function notifyScheduleChange() {
    scheduleChangeListeners.forEach((fn) => {
      try {
        fn(schedulesData);
      } catch (err) {
        console.error("[DataLayer] Schedule listener callback error:", err);
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
    if (safetyIntervalId) return;

    safetyIntervalId = setInterval(() => {
      if (!hasReceivedInitialData || !db) return;
      const now = Date.now();
      const deviceIds = Object.keys(deviceIndex);

      deviceIds.forEach((deviceId) => {
        const device = localCache[deviceId];
        if (!device) return;

        const maxDurationMinutes = device.maxActiveDuration;
        if (!maxDurationMinutes || maxDurationMinutes <= 0) return;

        if (device.state === true) {
          const startTime = device.lastUpdated || device.updatedAt || now;
          const elapsedMs = now - startTime;
          const limitMs = getMaxDurationMs(maxDurationMinutes);

          if (elapsedMs >= limitMs) {
            console.warn(`[SafetySystem] Safety limit exceeded for ${deviceId} (${device.deviceName}). Elapsed: ${elapsedMs}ms, Limit: ${limitMs}ms. Initiating automatic shutdown.`);
            
            recentActions[deviceId] = {
              actor: "safety-system",
              isSafetyShutdown: true,
              timestamp: now
            };

            DataLayer.setDeviceStatus(deviceId, false, "safety-system");
          }
        }
      });
    }, SAFETY_POLL_INTERVAL_MS);

    console.log(`[SafetySystem] Safety monitor armed (Test Mode: ${SAFETY_TEST_MODE ? "ENABLED (1m = 1s)" : "DISABLED (Realtime)"}).`);
  }

  /* Centralized Schedule Executor (Phase 7) */
  function startScheduleExecutor() {
    if (schedulerIntervalId) return;

    schedulerIntervalId = setInterval(() => {
      if (!hasReceivedInitialData || !db) return;

      const now = new Date();
      const year = now.getFullYear();
      const month = String(now.getMonth() + 1).padStart(2, "0");
      const day = String(now.getDate()).padStart(2, "0");
      const todayStr = `${year}-${month}-${day}`; // "YYYY-MM-DD"

      const hours = String(now.getHours()).padStart(2, "0");
      const minutes = String(now.getMinutes()).padStart(2, "0");
      const currentTimeStr = `${hours}:${minutes}`; // "HH:mm"

      const dayOfWeek = now.getDay(); // 0 = Sun, 1 = Mon, ..., 5 = Fri, 6 = Sat
      const isWeekday = dayOfWeek >= 1 && dayOfWeek <= 5;

      Object.values(schedulesData).forEach((schedule) => {
        if (!schedule.enabled) return;
        if (!schedule.deviceId || !schedule.startTime) return;

        let isApplicableToday = false;
        const rep = (schedule.repeat || "NONE").toUpperCase();

        if (rep === "DAILY") {
          // Daily applies every day
          if (!schedule.startDate || todayStr >= schedule.startDate) {
            isApplicableToday = true;
          }
        } else if (rep === "WEEKDAYS") {
          // Weekdays applies Monday to Friday
          if (isWeekday && (!schedule.startDate || todayStr >= schedule.startDate)) {
            isApplicableToday = true;
          }
        } else if (rep === "NONE" || rep === "ONCE" || rep === "") {
          // One-time schedule applies strictly on matching startDate
          if (schedule.startDate === todayStr) {
            isApplicableToday = true;
          }
        }

        if (!isApplicableToday) return;

        // Check if start time matches current HH:mm
        if (schedule.startTime !== currentTimeStr) return;

        // Deduplication key: scheduleId + scheduledDate + scheduledTime
        const occurrenceKey = `${schedule.scheduleId}_${todayStr}_${currentTimeStr}`;
        if (executedOccurrences.has(occurrenceKey)) return;

        // Mark occurrence as executed
        executedOccurrences.add(occurrenceKey);

        const targetState = schedule.action === "ON";
        console.log(`[ScheduleExecutor] Executing schedule ${schedule.scheduleId} (${schedule.deviceName || schedule.deviceId}) -> ${schedule.action} at ${currentTimeStr} on ${todayStr}`);

        // Set device status via existing DataLayer
        DataLayer.setDeviceStatus(schedule.deviceId, targetState, "schedule-executor");

        // Handle one-time vs recurring
        if (rep === "NONE" || rep === "ONCE" || rep === "") {
          console.log(`[ScheduleExecutor] One-time schedule ${schedule.scheduleId} completed. Auto-disabling in Firebase.`);
          db.ref(`${DB_SCHEDULES_PATH}/${schedule.scheduleId}`)
            .update({ enabled: false })
            .then(() => {
              console.log(`[ScheduleExecutor] Schedule ${schedule.scheduleId} successfully disabled in Firebase.`);
            })
            .catch((err) => {
              console.error(`[ScheduleExecutor] Failed to auto-disable schedule ${schedule.scheduleId}:`, err);
            });
        }
      });
    }, SCHEDULE_POLL_INTERVAL_MS);

    console.log("[ScheduleExecutor] Centralized schedule executor armed and running.");
  }

  function processSchedulesSnapshot(rawSchedules) {
    if (!rawSchedules || typeof rawSchedules !== "object") {
      schedulesData = {};
      notifyScheduleChange();
      return;
    }

    const clean = {};
    Object.entries(rawSchedules).forEach(([sId, sObj]) => {
      if (sObj && typeof sObj === "object") {
        clean[sId] = {
          scheduleId: sObj.scheduleId || sId,
          deviceId: sObj.deviceId || "",
          deviceName: sObj.deviceName || sObj.deviceId || "",
          action: (sObj.action || "ON").toUpperCase(),
          startDate: sObj.startDate || "",
          startTime: sObj.startTime || "",
          endTime: sObj.endTime || null,
          repeat: (sObj.repeat || "NONE").toUpperCase(),
          enabled: sObj.enabled === true || sObj.enabled === "true",
          switchId: sObj.switchId || null,
          createdAt: sObj.createdAt || Date.now()
        };
      }
    });

    schedulesData = clean;
    console.log(`[DataLayer] Schedules updated: ${Object.keys(clean).length} schedule(s) loaded.`);
    notifyScheduleChange();
  }

  function processFloorsSnapshot(rawFloors) {
    if (!rawFloors || typeof rawFloors !== "object") {
      console.warn("[DataLayer] Empty or invalid floors data received from Firebase.");
      floorsData = {};
      return;
    }

    const cleanFloors = {};
    const discoveredDeviceIds = new Set();
    const discoveredCount = { floors: 0, zones: 0, devices: 0 };
    const now = Date.now();
    let structureChanged = false;

    // Previous state keys for diffing
    const prevFloorKeys = Object.keys(floorsData).sort().join(",");
    const prevDeviceKeys = Object.keys(deviceIndex).sort().join(",");

    Object.entries(rawFloors).forEach(([floorId, rawFloorObj]) => {
      if (!rawFloorObj || typeof rawFloorObj !== "object" || Array.isArray(rawFloorObj)) {
        return; // Skip invalid floor node
      }

      discoveredCount.floors++;
      const cleanFloor = {
        floorId: rawFloorObj.floorId || floorId,
        floorName: rawFloorObj.floorName || floorId,
        floorPlanImage: rawFloorObj.floorPlanImage || null,
        zones: {}
      };

      const rawZones = rawFloorObj.zones || {};
      if (typeof rawZones === "object" && !Array.isArray(rawZones)) {
        Object.entries(rawZones).forEach(([zoneId, rawZoneObj]) => {
          if (!rawZoneObj || typeof rawZoneObj !== "object" || Array.isArray(rawZoneObj)) {
            return; // Skip invalid zone node
          }

          discoveredCount.zones++;
          const cleanZone = {
            zoneId: rawZoneObj.zoneId || zoneId,
            zoneName: rawZoneObj.zoneName || zoneId,
            floorId: rawZoneObj.floorId || floorId,
            devices: {}
          };

          const rawDevices = rawZoneObj.devices || {};
          if (typeof rawDevices === "object" && !Array.isArray(rawDevices)) {
            Object.entries(rawDevices).forEach(([deviceId, rawDevObj]) => {
              // Ensure this child is a genuine device object, not a corrupt primitive or stub
              if (!rawDevObj || typeof rawDevObj !== "object" || Array.isArray(rawDevObj)) {
                return;
              }

              // Filter out corrupt entries that lack minimum identification
              const effectiveId = rawDevObj.deviceId || deviceId;
              const effectiveName = rawDevObj.deviceName || deviceId;
              const effectiveType = (rawDevObj.type || "LIGHT").toUpperCase();

              discoveredCount.devices++;
              discoveredDeviceIds.add(effectiveId);

              const realPath = `${DB_FLOORS_PATH}/${floorId}/zones/${zoneId}/devices/${effectiveId}`;

              // Determine power state
              const stateBool = typeof rawDevObj.state === "boolean"
                ? rawDevObj.state
                : (rawDevObj.state === "true" || rawDevObj.status === "ON");

              const prevCached = localCache[effectiveId];
              const prevDisplayStatus = prevCached?.displayStatus;
              const prevStatus = prevCached?.status;
              const displayStatus = stateBool
                ? "ON"
                : (rawDevObj.status === "DISCONNECTED" ? "DISCONNECTED" : (rawDevObj.status === "ERROR" ? "ERROR" : "OFF"));

              // Check for sub-switch changes on multi-switch devices
              let changedSwitchId = null;
              if (rawDevObj.switches && prevCached?.switches) {
                for (const sKey of Object.keys(rawDevObj.switches)) {
                  if (rawDevObj.switches[sKey] !== prevCached.switches[sKey]) {
                    changedSwitchId = sKey;
                    break;
                  }
                }
              }

              const switchesChanged = JSON.stringify(prevCached?.switches) !== JSON.stringify(rawDevObj.switches);

              const isChanged = !prevCached ||
                prevCached.state !== stateBool ||
                prevCached.status !== rawDevObj.status ||
                switchesChanged ||
                prevCached.lastUpdated !== rawDevObj.lastUpdated;

              // Determine source actor
              let actor = "firebase";
              let isSafetyShutdown = false;
              const recent = recentActions[effectiveId];
              if (recent && (now - recent.timestamp) < 6000) {
                actor = recent.actor;
                isSafetyShutdown = Boolean(recent.isSafetyShutdown);
              }

              const normalizedDevice = {
                ...rawDevObj,
                deviceId: effectiveId,
                deviceName: effectiveName,
                type: effectiveType,
                floorId,
                zoneId,
                state: stateBool,
                status: rawDevObj.status || "Normal",
                maxActiveDuration: rawDevObj.maxActiveDuration || null,
                switchCount: rawDevObj.switchCount || (rawDevObj.switches ? Object.keys(rawDevObj.switches).length : 0),
                switches: rawDevObj.switches ? { ...rawDevObj.switches } : null,
                displayStatus,
                updatedAt: rawDevObj.lastUpdated || now,
                updatedBy: actor
              };

              cleanZone.devices[effectiveId] = normalizedDevice;
              localCache[effectiveId] = normalizedDevice;

              deviceIndex[effectiveId] = {
                floorId,
                zoneId,
                path: realPath,
                data: normalizedDevice
              };

              if (isChanged && hasReceivedInitialData) {
                console.log(`[DataLayer] Device update: ${effectiveId} (${effectiveName}) -> state=${stateBool}, actor=${actor}`);
                notifyDeviceChange(effectiveId, localCache[effectiveId], prevDisplayStatus || prevStatus, { changedSwitchId, actor, isSafetyShutdown });
              }
            });
          }

          cleanFloor.zones[zoneId] = cleanZone;
        });
      }

      cleanFloors[floorId] = cleanFloor;
    });

    // Remove any stale devices that were deleted from Firebase
    Object.keys(deviceIndex).forEach((devId) => {
      if (!discoveredDeviceIds.has(devId)) {
        delete deviceIndex[devId];
        delete localCache[devId];
        structureChanged = true;
      }
    });

    floorsData = cleanFloors;

    const currentFloorKeys = Object.keys(cleanFloors).sort().join(",");
    const currentDeviceKeys = Object.keys(deviceIndex).sort().join(",");
    if (prevFloorKeys !== currentFloorKeys || prevDeviceKeys !== currentDeviceKeys) {
      structureChanged = true;
    }

    if (!hasReceivedInitialData) {
      console.log(`[DataLayer] Initial discovery completed: ${discoveredCount.floors} floors, ${discoveredCount.zones} zones, ${discoveredCount.devices} devices.`);
      hasReceivedInitialData = true;
      startSafetyMonitor();
      startScheduleExecutor();
    } else if (structureChanged) {
      console.log(`[DataLayer] Structure changed. Re-notifying UI.`);
      notifyStructureChange();
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
        console.log("[DataLayer] Firebase initialized for Phase 7.");

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

        const floorsRef = db.ref(DB_FLOORS_PATH);
        const schedulesRef = db.ref(DB_SCHEDULES_PATH);

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

        schedulesRef.on(
          "value",
          (snap) => {
            const rawSchedules = snap.val();
            processSchedulesSnapshot(rawSchedules);
          },
          (error) => {
            console.error(`[DataLayer] Firebase read error at ${DB_SCHEDULES_PATH}:`, error);
          }
        );
      } catch (err) {
        console.error("[DataLayer] Failed to initialize Firebase:", err);
        updateConnectionBadge(false, "FIREBASE INIT FAILED");
        if (onReady) onReady();
      }
    },

    onDeviceChange(callback) {
      deviceChangeListeners.add(callback);
    },

    onStructureChange(callback) {
      structureChangeListeners.add(callback);
    },

    onScheduleChange(callback) {
      scheduleChangeListeners.add(callback);
    },

    getSchedules() {
      return schedulesData;
    },

    getState(deviceId) {
      return (
        localCache[deviceId] || {
          deviceId,
          deviceName: deviceId,
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
        notifyDeviceChange(deviceId, localCache[deviceId], prevStatus, { actor });
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
    },

    setScheduleEnabled(scheduleId, enabled) {
      if (!db || !scheduleId) return;
      db.ref(`${DB_SCHEDULES_PATH}/${scheduleId}`)
        .update({ enabled: Boolean(enabled) })
        .then(() => {
          console.log(`[DataLayer] Schedule ${scheduleId} enabled set to ${enabled}`);
        })
        .catch((err) => {
          console.error(`[DataLayer] Error updating schedule ${scheduleId}:`, err);
        });
    },

    deleteSchedule(scheduleId) {
      if (!db || !scheduleId) return;
      db.ref(`${DB_SCHEDULES_PATH}/${scheduleId}`)
        .remove()
        .then(() => {
          console.log(`[DataLayer] Schedule ${scheduleId} deleted`);
        })
        .catch((err) => {
          console.error(`[DataLayer] Error deleting schedule ${scheduleId}:`, err);
        });
    }
  };
})();