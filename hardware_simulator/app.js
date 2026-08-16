/* =====================================================================
   HARDWARE SIMULATOR — APPLICATION CONTROLLER (PHASE 5.5)
   =====================================================================
   - Pure UI controller consuming DataLayer.
   - Dynamic floor, zone, and device rendering from Firebase.
   - Real-time bi-directional synchronization with Firebase & Android.
   - Independent multi-switch unit controls.
   - Safety badge display for appliances with maxActiveDuration.
   - Distinct safety-system activity logging.
   ===================================================================== */

/* Presentation mappings: Firebase keys -> CSS layout classes/areas */
const FLOOR_LAYOUT_MAP = {
  floor1: "ground_floor",
  floor2: "first_floor"
};

const ZONE_LAYOUT_MAP = {
  livingRoom:     "living_room",
  kitchen:        "kitchen",
  diningRoom:     "dining_room",
  garage:         "garage",
  bathroomGF:     "bathroom_gf",
  staircase:      "staircase",
  masterBedroom:  "master_bedroom",
  bedroom2:       "bedroom_2",
  workRoom:       "work_room",
  bathroomFF:     "bathroom_ff",
  hallway:        "hallway"
};

const FLOOR_ORDER = ["floor1", "floor2"];

const ZONE_ORDER = {
  floor1: ["livingRoom", "kitchen", "diningRoom", "garage", "bathroomGF", "staircase"],
  floor2: ["masterBedroom", "bedroom2", "workRoom", "bathroomFF", "hallway"]
};

const TYPE_ICON = {
  LIGHT: "💡",
  FAN: "🌀",
  CAMERA: "📷",
  SMART_PLUG: "🔌",
  AIR_CONDITIONER: "❄️",
  IRON: "👔",
  MULTI_SWITCH: "🎛️"
};

let activeFloor = "floor1";

/* Power state evaluations */
function isDeviceOn(deviceId) {
  const st = DataLayer.getState(deviceId);
  return Boolean(st && st.state === true);
}

function getDeviceDisplayStatus(deviceId) {
  const st = DataLayer.getState(deviceId);
  if (!st) return "OFF";
  if (st.status === "DISCONNECTED") return "DISCONNECTED";
  if (st.status === "ERROR") return "ERROR";
  return st.state === true ? "ON" : "OFF";
}

function isRoomLit(zone) {
  if (!zone || !zone.devices) return false;
  return Object.entries(zone.devices).some(([devId, d]) => {
    if (!d || typeof d !== "object") return false;
    const typeUpper = (d.type || "").toUpperCase();
    const resolvedId = d.deviceId || devId;
    return typeUpper === "LIGHT" && isDeviceOn(resolvedId);
  });
}

function areAllFloorLightsOn(floorId) {
  const zones = DataLayer.getZones(floorId);
  const zoneEntries = Object.values(zones || {}).filter((z) => z && typeof z === "object");
  if (zoneEntries.length === 0) return false;

  let hasLights = false;
  for (const zone of zoneEntries) {
    const devs = Object.entries(zone.devices || {});
    for (const [devId, d] of devs) {
      if (!d || typeof d !== "object") continue;
      const typeUpper = (d.type || "").toUpperCase();
      if (typeUpper === "LIGHT") {
        hasLights = true;
        const resolvedId = d.deviceId || devId;
        if (!isDeviceOn(resolvedId)) {
          return false;
        }
      }
    }
  }
  return hasLights;
}

function areAllLightsOn() {
  const floors = DataLayer.getFloors();
  const floorKeys = Object.keys(floors || {});
  if (floorKeys.length === 0) return false;

  let hasAnyFloorWithLights = false;
  for (const floorId of floorKeys) {
    const zones = DataLayer.getZones(floorId);
    const hasFloorLights = Object.values(zones || {}).some((zone) =>
      Object.values(zone.devices || {}).some((d) => (d?.type || "").toUpperCase() === "LIGHT")
    );
    if (hasFloorLights) {
      hasAnyFloorWithLights = true;
      if (!areAllFloorLightsOn(floorId)) {
        return false;
      }
    }
  }
  return hasAnyFloorWithLights;
}

function updateLightingStates() {
  const board = document.querySelector(".board");
  const floorTabs = document.getElementById("floorTabs");
  const roomsContainer = document.getElementById("roomsContainer");
  const floors = DataLayer.getFloors();
  const activeFloorObj = floors[activeFloor] || {};

  if (!board) return;
  const houseLit = areAllLightsOn();
  const floorLit = areAllFloorLightsOn(activeFloor);

  board.dataset.houseLit = String(houseLit);

  if (floorTabs) {
    Object.keys(floors).forEach((floorId) => {
      const tab = floorTabs.querySelector(`[data-floor-id="${floorId}"]`);
      if (tab) {
        tab.dataset.lit = String(areAllFloorLightsOn(floorId));
      }
    });
  }

  if (roomsContainer) {
    roomsContainer.dataset.floor = FLOOR_LAYOUT_MAP[activeFloor] || activeFloor;
    roomsContainer.dataset.houseLit = String(houseLit);
    roomsContainer.dataset.floorLit = String(floorLit);
    roomsContainer.dataset.floorLabel = (activeFloorObj.floorName || activeFloor).toUpperCase();
  }
}

/* CLOCK */
function tickClock() {
  const el = document.getElementById("clock");
  if (el) el.textContent = new Date().toLocaleTimeString("en-GB", { hour12: false });
}
setInterval(tickClock, 1000);
tickClock();

/* FLOOR TABS */
function renderFloorTabs() {
  const nav = document.getElementById("floorTabs");
  if (!nav) return;
  nav.innerHTML = "";

  const floors = DataLayer.getFloors();
  const floorKeys = Object.keys(floors);
  if (!floorKeys.includes(activeFloor) && floorKeys.length > 0) {
    activeFloor = floorKeys[0];
  }

  const sortedFloorIds = FLOOR_ORDER.filter((id) => floorKeys.includes(id))
    .concat(floorKeys.filter((id) => !FLOOR_ORDER.includes(id)));

  sortedFloorIds.forEach((floorId) => {
    const floor = floors[floorId];
    if (!floor || typeof floor !== "object") return;
    const btn = document.createElement("button");
    btn.className = "floor-tab" + (floorId === activeFloor ? " active" : "");
    btn.dataset.floorId = floorId;
    btn.dataset.lit = String(areAllFloorLightsOn(floorId));
    btn.textContent = floor.floorName || floorId;
    btn.onclick = () => {
      activeFloor = floorId;
      renderFloorTabs();
      renderRooms();
    };
    nav.appendChild(btn);
  });
}

/* ROOMS + DEVICES */
function renderRooms() {
  const container = document.getElementById("roomsContainer");
  if (!container) return;
  container.innerHTML = "";
  updateLightingStates();

  const zones = DataLayer.getZones(activeFloor);
  const zoneKeys = Object.keys(zones || {});
  const preferredOrder = ZONE_ORDER[activeFloor] || [];
  const sortedZoneIds = preferredOrder.filter((id) => zoneKeys.includes(id))
    .concat(zoneKeys.filter((id) => !preferredOrder.includes(id)));

  sortedZoneIds.forEach((zoneId) => {
    const zone = zones[zoneId];
    if (!zone || typeof zone !== "object") return;

    const cssRoomId = ZONE_LAYOUT_MAP[zoneId] || zoneId;
    const devEntries = Object.entries(zone.devices || {}).filter(([_, d]) => d && typeof d === "object");

    const roomEl = document.createElement("div");
    roomEl.className = "room";
    roomEl.id = `room-${zoneId}`;
    roomEl.dataset.roomId = cssRoomId;
    if (isRoomLit(zone)) roomEl.classList.add("room--lit");

    const head = document.createElement("div");
    head.className = "room__label";
    head.innerHTML = `<h3>${zone.zoneName || zoneId}</h3><span class="room__count">${devEntries.length} device${devEntries.length !== 1 ? "s" : ""}</span>`;
    roomEl.appendChild(head);

    devEntries.forEach(([devId, devData]) => {
      const effectiveId = devData.deviceId || devId;
      const devRow = renderDeviceRow(effectiveId);
      if (devRow) {
        roomEl.appendChild(devRow);
      }
    });

    container.appendChild(roomEl);
  });
}

function renderDeviceRow(deviceId) {
  const device = DataLayer.getState(deviceId);
  const displayStatus = getDeviceDisplayStatus(deviceId);
  const isOn = isDeviceOn(deviceId);
  const deviceType = (device.type || "LIGHT").toUpperCase();
  const iconChar = TYPE_ICON[deviceType] || TYPE_ICON[(device.icon || "").toUpperCase()] || "💡";

  // Check if device is a multi-switch unit
  const isMultiSwitch = deviceType === "MULTI_SWITCH" || (device.switches && (device.switchCount > 1 || Object.keys(device.switches).length > 1));

  if (isMultiSwitch) {
    const row = document.createElement("div");
    row.className = "device device--multi";
    row.id = `device-${deviceId}`;

    const main = document.createElement("div");
    main.className = "device__main";

    const icon = document.createElement("div");
    icon.className = "device__icon";
    icon.textContent = iconChar;
    main.appendChild(icon);

    const info = document.createElement("div");
    info.className = "device__info";

    const switchesObj = device.switches || {};
    const switchKeys = Object.keys(switchesObj);
    const activeCount = switchKeys.filter((k) => switchesObj[k] === true).length;
    const totalCount = device.switchCount || switchKeys.length || 3;

    info.innerHTML = `
      <div class="device__name">${device.deviceName || deviceId}</div>
      <div class="device__meta" data-status="${isOn ? "ON" : "OFF"}">${activeCount}/${totalCount} ACTIVE</div>
    `;
    main.appendChild(info);
    row.appendChild(main);

    const switchesContainer = document.createElement("div");
    switchesContainer.className = "device__switches";

    const switchIdsToRender = switchKeys.length > 0
      ? switchKeys.sort()
      : Array.from({ length: totalCount }, (_, i) => `switch_${i + 1}`);

    switchIdsToRender.forEach((sKey) => {
      const sVal = Boolean(switchesObj[sKey]);
      const sStatus = sVal ? "ON" : "OFF";
      const sNumber = sKey.replace("switch_", "");

      const sRow = document.createElement("div");
      sRow.className = "sub-switch";
      sRow.id = `switch-${deviceId}-${sKey}`;

      const sLabel = document.createElement("span");
      sLabel.className = "sub-switch__label";
      sLabel.textContent = `SWITCH ${sNumber}`;
      sRow.appendChild(sLabel);

      const sMeta = document.createElement("span");
      sMeta.className = "sub-switch__meta";
      sMeta.dataset.status = sStatus;
      sMeta.textContent = sStatus;
      sRow.appendChild(sMeta);

      const sRocker = document.createElement("div");
      sRocker.className = "rocker rocker--small";
      sRocker.dataset.status = sStatus;
      sRocker.dataset.switchId = sKey;
      sRocker.onclick = () => {
        const currentDev = DataLayer.getState(deviceId);
        const currentVal = Boolean(currentDev.switches?.[sKey]);
        const nextVal = !currentVal;
        DataLayer.setSwitchState(deviceId, sKey, nextVal, "simulator-ui");
      };
      sRow.appendChild(sRocker);

      switchesContainer.appendChild(sRow);
    });

    row.appendChild(switchesContainer);
    return row;
  }

  // Standard single-switch device or camera
  const row = document.createElement("div");
  row.className = "device" + (deviceType === "CAMERA" ? " device--camera" : "");
  row.id = `device-${deviceId}`;

  const icon = document.createElement("div");
  icon.className = "device__icon";
  icon.textContent = iconChar;
  row.appendChild(icon);

  const info = document.createElement("div");
  info.className = "device__info";

  const safetyTagHtml = device.maxActiveDuration
    ? `<div class="device__safety-tag ${isOn ? "active" : ""}">MAX: ${device.maxActiveDuration} MIN${isOn ? " • ACTIVE" : ""}</div>`
    : "";

  info.innerHTML = `
    <div class="device__name">${device.deviceName || deviceId}</div>
    <div class="device__meta" data-status="${displayStatus}">${displayStatus}</div>
    ${safetyTagHtml}
  `;
  row.appendChild(info);

  if (deviceType === "CAMERA") {
    const btn = document.createElement("button");
    btn.className = "device__action";
    btn.textContent = "VIEW";
    btn.onclick = () => openCameraModal(deviceId);
    row.appendChild(btn);
  } else {
    const rocker = document.createElement("div");
    rocker.className = "rocker";
    rocker.dataset.status = displayStatus;
    rocker.onclick = () => {
      const currentStatus = getDeviceDisplayStatus(deviceId);
      if (currentStatus === "DISCONNECTED") return;
      const currentOn = isDeviceOn(deviceId);
      const nextBool = !currentOn;
      DataLayer.setDeviceStatus(deviceId, nextBool, "simulator-ui");
    };
    row.appendChild(rocker);
  }

  return row;
}

/* REACTIVE UPDATES */
DataLayer.onDeviceChange((snapshot) => {
  updateDeviceRowUI(snapshot.deviceId);
  updateRoomLightState(snapshot.deviceId);
  updateLightingStates();
  logActivity(snapshot);
});

DataLayer.onStructureChange(() => {
  renderFloorTabs();
  renderRooms();
  populateUplinkDeviceList();
  updateLightingStates();
});

function updateRoomLightState(deviceId) {
  const deviceEntry = DataLayer.getDevice(deviceId);
  if (!deviceEntry) return;
  if (deviceEntry.floorId !== activeFloor) return;

  const zone = DataLayer.getZone(deviceEntry.floorId, deviceEntry.zoneId);
  if (!zone) return;

  const roomEl = document.getElementById(`room-${deviceEntry.zoneId}`);
  if (roomEl) {
    roomEl.classList.toggle("room--lit", isRoomLit(zone));
  }
}

function updateDeviceRowUI(deviceId) {
  const row = document.getElementById(`device-${deviceId}`);
  if (!row) return;

  const device = DataLayer.getState(deviceId);
  const isMultiSwitch = row.classList.contains("device--multi");
  const isOn = isDeviceOn(deviceId);

  if (isMultiSwitch && device.switches) {
    const switchesObj = device.switches || {};
    const switchKeys = Object.keys(switchesObj);
    const activeCount = switchKeys.filter((k) => switchesObj[k] === true).length;
    const totalCount = device.switchCount || switchKeys.length || 3;

    const mainMeta = row.querySelector(".device__main .device__meta");
    if (mainMeta) {
      mainMeta.textContent = `${activeCount}/${totalCount} ACTIVE`;
      mainMeta.dataset.status = isOn ? "ON" : "OFF";
    }

    switchKeys.forEach((sKey) => {
      const sVal = Boolean(switchesObj[sKey]);
      const sStatus = sVal ? "ON" : "OFF";
      const sRow = document.getElementById(`switch-${deviceId}-${sKey}`);
      if (sRow) {
        const sMeta = sRow.querySelector(".sub-switch__meta");
        if (sMeta) {
          sMeta.textContent = sStatus;
          sMeta.dataset.status = sStatus;
        }
        const sRocker = sRow.querySelector(".rocker");
        if (sRocker) {
          sRocker.dataset.status = sStatus;
        }
      }
    });
  } else {
    const displayStatus = getDeviceDisplayStatus(deviceId);
    const metaEl = row.querySelector(".device__meta");
    if (metaEl) {
      metaEl.textContent = displayStatus;
      metaEl.dataset.status = displayStatus;
    }
    const rocker = row.querySelector(".rocker");
    if (rocker) rocker.dataset.status = displayStatus;

    const safetyTag = row.querySelector(".device__safety-tag");
    if (safetyTag && device.maxActiveDuration) {
      safetyTag.className = `device__safety-tag ${isOn ? "active" : ""}`;
      safetyTag.textContent = `MAX: ${device.maxActiveDuration} MIN${isOn ? " • ACTIVE" : ""}`;
    }
  }
}

/* ACTIVITY LOG */
function logActivity(snapshot) {
  const log = document.getElementById("activityLog");
  if (!log) return;
  const deviceName = snapshot.deviceName || DataLayer.getDevice(snapshot.deviceId)?.data?.deviceName || snapshot.deviceId;
  const time = new Date(snapshot.updatedAt || Date.now()).toLocaleTimeString("en-GB", { hour12: false });

  let statusText = snapshot.displayStatus || (snapshot.state ? "ON" : "OFF");
  let labelText = deviceName;
  let actorLabel = snapshot.updatedBy || snapshot.actor || "system";
  let cls = { ON: "on", OFF: "off", ERROR: "error", DISCONNECTED: "disc" }[statusText] || (snapshot.state ? "on" : "off");

  if (snapshot.actor === "safety-system" || snapshot.isSafetyShutdown) {
    statusText = "OFF (SAFETY TIMEOUT)";
    actorLabel = "safety-system";
    cls = "error";
  } else if (snapshot.changedSwitchId) {
    const sNumber = snapshot.changedSwitchId.replace("switch_", "");
    const sVal = snapshot.switches?.[snapshot.changedSwitchId];
    statusText = sVal ? "ON" : "OFF";
    labelText = `${deviceName} (Switch ${sNumber})`;
    cls = sVal ? "on" : "off";
  }

  const entry = document.createElement("div");
  entry.className = "log__entry " + cls;
  entry.innerHTML = `<span class="t">${time}</span>${labelText} → <strong>${statusText}</strong> <span class="t">(${actorLabel})</span>`;
  log.appendChild(entry);

  while (log.children.length > 60) log.removeChild(log.firstChild);
}

/* CAMERA MODAL */
let cameraTimer = null;

function openCameraModal(deviceId) {
  const device = DataLayer.getState(deviceId);
  const deviceName = device.deviceName || deviceId;

  document.getElementById("cameraModalTitle").textContent = deviceName;
  document.getElementById("cameraModal").classList.add("open");

  const canvas = document.getElementById("cameraCanvas");
  const draw = () => drawFakeFrame(canvas, deviceName);
  draw();
  cameraTimer = setInterval(draw, 2000);

  const statusEl = document.getElementById("cameraModalStatus");
  statusEl.textContent = device.status === "DISCONNECTED" ? "OFFLINE" : (device.status || "ONLINE");
}

document.getElementById("cameraModalClose").onclick = () => {
  document.getElementById("cameraModal").classList.remove("open");
  clearInterval(cameraTimer);
};

function drawFakeFrame(canvas, label) {
  const ctx = canvas.getContext("2d");
  const w = canvas.width, h = canvas.height;

  ctx.fillStyle = "#05070a";
  ctx.fillRect(0, 0, w, h);

  const imgData = ctx.createImageData(w, h);
  for (let i = 0; i < imgData.data.length; i += 4) {
    const v = Math.random() * 14;
    imgData.data[i] = v; imgData.data[i + 1] = v + 4; imgData.data[i + 2] = v + 8;
    imgData.data[i + 3] = 255;
  }
  ctx.putImageData(imgData, 0, 0);

  const sweepY = (Date.now() / 8) % h;
  ctx.fillStyle = "rgba(76,195,138,0.08)";
  ctx.fillRect(0, sweepY, w, 2);

  ctx.font = "11px monospace";
  ctx.fillStyle = "rgba(234,237,242,0.65)";
  ctx.fillText(label.toUpperCase(), 10, 18);
  ctx.fillText(new Date().toLocaleString(), 10, h - 10);

  document.getElementById("cameraModalTime").textContent = "Frame captured " + new Date().toLocaleTimeString("en-GB", { hour12: false });
}

/* UPLINK SIMULATOR */
function populateUplinkDeviceList() {
  const select = document.getElementById("uplinkDevice");
  if (!select) return;
  select.innerHTML = "";
  const deviceIds = DataLayer.getDiscoveredDevices();
  deviceIds.forEach((id) => {
    const st = DataLayer.getState(id);
    const opt = document.createElement("option");
    opt.value = id;
    opt.textContent = `${st.deviceName || id} (${id})`;
    select.appendChild(opt);
  });
}

document.getElementById("uplinkPush").onclick = () => {
  const deviceId = document.getElementById("uplinkDevice").value;
  const status = document.getElementById("uplinkStatus").value;
  DataLayer.setDeviceStatus(deviceId, status, "cloud-uplink");
};

document.getElementById("uplinkChaos").onclick = () => {
  const ids = DataLayer.getDiscoveredDevices();
  if (ids.length === 0) return;
  const randomId = ids[Math.floor(Math.random() * ids.length)];
  DataLayer.setDeviceStatus(randomId, "ERROR", "safety-worker");
};

/* SCHEDULES UI */
function renderSchedulesList(schedules) {
  const container = document.getElementById("schedulesList");
  if (!container) return;

  const schedMap = schedules || DataLayer.getSchedules() || {};
  const list = Object.values(schedMap);

  if (list.length === 0) {
    container.innerHTML = '<div class="schedules-empty">No schedules configured</div>';
    return;
  }

  // Sort by startTime
  list.sort((a, b) => (a.startTime || "").localeCompare(b.startTime || ""));

  container.innerHTML = "";
  list.forEach((s) => {
    const isActionOn = (s.action || "ON").toUpperCase() === "ON";
    const item = document.createElement("div");
    item.className = `schedule-item ${s.enabled ? "" : "disabled"}`;

    let repeatLabel = s.repeat || "Once";
    if (repeatLabel === "NONE" || repeatLabel === "ONCE") {
      repeatLabel = s.startDate ? `Once (${s.startDate})` : "Once";
    } else if (repeatLabel === "WEEKDAYS") {
      repeatLabel = "Weekdays (Mon-Fri)";
    } else if (repeatLabel === "DAILY") {
      repeatLabel = "Every Day";
    }

    item.innerHTML = `
      <div class="schedule-item__main">
        <div class="schedule-item__time">
          ${s.startTime || "--:--"}
          <span class="schedule-badge ${isActionOn ? 'schedule-badge--on' : 'schedule-badge--off'}">${isActionOn ? 'ON' : 'OFF'}</span>
        </div>
        <div class="schedule-item__device">${s.deviceName || s.deviceId}</div>
        <div class="schedule-item__freq">${repeatLabel}</div>
      </div>
      <button class="schedule-item__toggle ${s.enabled ? 'active' : ''}" title="Toggle enabled">
        ${s.enabled ? 'ACTIVE' : 'PAUSED'}
      </button>
    `;

    const toggleBtn = item.querySelector(".schedule-item__toggle");
    toggleBtn.onclick = () => {
      DataLayer.setScheduleEnabled(s.scheduleId, !s.enabled);
    };

    container.appendChild(item);
  });
}

DataLayer.onScheduleChange((schedules) => {
  renderSchedulesList(schedules);
});

/* BOOTSTRAP */
DataLayer.init(() => {
  renderFloorTabs();
  renderRooms();
  populateUplinkDeviceList();
  renderSchedulesList();
  updateLightingStates();

  const connIndicator = document.getElementById("connIndicator");
  const connLabel = document.getElementById("connLabel");
  if (connIndicator) connIndicator.dataset.mode = DataLayer.mode;
  if (connLabel) {
    connLabel.textContent = DataLayer.mode === "live"
      ? "LIVE — connected to Firebase"
      : "MOCK MODE — no cloud link";
  }
});