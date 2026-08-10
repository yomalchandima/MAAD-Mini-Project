/* 
   1. In index.html, replace:
        <script src="mock-data-layer.js"></script>
      with:
        <script type="module" src="firebase-sync.js"></script>
        <script type="module" src="app.js"></script>
      (app.js needs `type="module"` too once this file uses ES modules,
      OR simplest: keep this file non-module and use the Firebase
      "compat" SDK tags shown below — pick ONE approach and stay
      consistent with what Member 2 used for the Android app's config.)

   2. Ask Member 2 for the firebaseConfig object (from Firebase console
      → Project settings → General → Your apps → Web app). Paste it
      into FIREBASE_CONFIG below. Never commit real API keys to a
      public repo without checking with your team/instructor first —
      for a Realtime DB demo project this is normally fine, but ask.

   3. Confirm the schema path matches exactly what Member 2 built:
        smart_home/devices/{deviceId}/status
        smart_home/devices/{deviceId}/updatedAt
        smart_home/devices/{deviceId}/updatedBy
      If their path differs, change DB_PATH below — nothing else in
      this file should need to change.

   4. DataLayer.mode is read by app.js purely to flip the "MOCK MODE"
      badge in the header to "LIVE" — set it to "live" once this file
      is wired in.
   ===================================================================== */

// ---- Using the Firebase compat SDK (simplest, no bundler needed) ----
// Add these two lines to index.html <head>, BEFORE this script tag:
//
// <script src="https://www.gstatic.com/firebasejs/10.12.0/firebase-app-compat.js"></script>
// <script src="https://www.gstatic.com/firebasejs/10.12.0/firebase-database-compat.js"></script>

const FIREBASE_CONFIG = {
  apiKey: "PASTE_FROM_MEMBER_2",
  authDomain: "PASTE_FROM_MEMBER_2",
  databaseURL: "PASTE_FROM_MEMBER_2", // critical for Realtime Database — often missed
  projectId: "PASTE_FROM_MEMBER_2",
  appId: "PASTE_FROM_MEMBER_2"
};

const DB_PATH = "smart_home/devices";

const DataLayer = (() => {
  let db = null;
  const listeners = new Set();
  const localCache = {};

  function notify(deviceId, statusObj, prevStatus) {
    const snapshot = { deviceId, ...statusObj, prevStatus };
    listeners.forEach(fn => fn(snapshot));
  }

  return {
    mode: "live",

    init(onReady) {
      firebase.initializeApp(FIREBASE_CONFIG);
      db = firebase.database();

      db.ref(DB_PATH).on("value", (snap) => {
        const data = snap.val() || {};
        Object.entries(data).forEach(([deviceId, statusObj]) => {
          const prev = localCache[deviceId]?.status ?? null;
          localCache[deviceId] = statusObj;
          notify(deviceId, statusObj, prev);
        });
      });

      if (onReady) onReady();
    },

    onDeviceChange(callback) {
      listeners.add(callback);
    },

    getState(deviceId) {
      return localCache[deviceId] || { status: "DISCONNECTED", updatedAt: null, updatedBy: null };
    },

    getAllState() {
      return localCache;
    },

    setDeviceStatus(deviceId, newStatus, actor = "simulator") {
      db.ref(`${DB_PATH}/${deviceId}`).update({
        status: newStatus,
        updatedAt: Date.now(),
        updatedBy: actor
      });
    }
  };
})();