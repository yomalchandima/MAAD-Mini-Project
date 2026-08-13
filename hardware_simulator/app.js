let activeFloor = "ground_floor";

function isRoomLit(room) {
  return room.devices.some((deviceId) => {
    const meta = DEVICE_META[deviceId];
    return meta && meta.type === "light" && DataLayer.getState(deviceId).status === "ON";
  });
}

function areAllLightsOn() {
  return Object.keys(DEVICE_META).every((deviceId) => {
    const meta = DEVICE_META[deviceId];
    return !meta || meta.type !== "light" || DataLayer.getState(deviceId).status === "ON";
  });
}

function areAllFloorLightsOn(floorId) {
  const floor = HOUSE[floorId];
  return Object.values(floor.rooms).every((room) =>
    room.devices.every((deviceId) => {
      const meta = DEVICE_META[deviceId];
      return !meta || meta.type !== "light" || DataLayer.getState(deviceId).status === "ON";
    })
  );
}

function updateLightingStates() {
  const board = document.querySelector(".board");
  const floorTabs = document.getElementById("floorTabs");
  const roomsContainer = document.getElementById("roomsContainer");
  const floor = HOUSE[activeFloor];

  if (!board) return;
  const houseLit = areAllLightsOn();

  board.dataset.houseLit = String(houseLit);
  if (floorTabs) {
    Object.entries(HOUSE).forEach(([floorId]) => {
      const tab = floorTabs.querySelector(`[data-floor-id="${floorId}"]`);
      if (!tab) return;
      tab.dataset.lit = String(areAllFloorLightsOn(floorId));
    });
  }

  if (roomsContainer) {
    roomsContainer.dataset.floor = activeFloor;
    roomsContainer.dataset.houseLit = String(houseLit);
    roomsContainer.dataset.floorLit = String(areAllFloorLightsOn(activeFloor));
    roomsContainer.dataset.floorLabel = floor.label.toUpperCase();
  }
}

/*CLOCK*/
function tickClock() {
  const el = document.getElementById("clock");
  el.textContent = new Date().toLocaleTimeString("en-GB", { hour12: false });
}
setInterval(tickClock, 1000);
tickClock();

/*FLOOR TABS*/
function renderFloorTabs() {
  const nav = document.getElementById("floorTabs");
  nav.innerHTML = "";
  Object.entries(HOUSE).forEach(([floorId, floor]) => {
    const btn = document.createElement("button");
    btn.className = "floor-tab" + (floorId === activeFloor ? " active" : "");
    btn.dataset.floorId = floorId;
    btn.dataset.lit = String(areAllFloorLightsOn(floorId));
    btn.textContent = floor.label;
    btn.onclick = () => { activeFloor = floorId; renderFloorTabs(); renderRooms(); };
    nav.appendChild(btn);
  });
}

/*ROOMS + DEVICES*/
function renderRooms() {
  const container = document.getElementById("roomsContainer");
  container.innerHTML = "";
  updateLightingStates();

  const floor = HOUSE[activeFloor];
  Object.entries(floor.rooms).forEach(([roomId, room]) => {
    const roomEl = document.createElement("div");
    roomEl.className = "room";
    roomEl.id = `room-${roomId}`;
    roomEl.dataset.roomId = roomId;
    if (isRoomLit(room)) roomEl.classList.add("room--lit");

    const head = document.createElement("div");
    head.className = "room__label";
    head.innerHTML = `<h3>${room.label}</h3><span class="room__count">${room.devices.length} device${room.devices.length > 1 ? "s" : ""}</span>`;
    roomEl.appendChild(head);

    room.devices.forEach(deviceId => {
      roomEl.appendChild(renderDeviceRow(deviceId));
    });

    container.appendChild(roomEl);
  });
}

function renderDeviceRow(deviceId) {
  const meta = DEVICE_META[deviceId];
  const current = DataLayer.getState(deviceId);

  const row = document.createElement("div");
  row.className = "device" + (meta.type === "camera" ? " device--camera" : "");
  row.id = `device-${deviceId}`;

  const icon = document.createElement("div");
  icon.className = "device__icon";
  icon.textContent = TYPE_ICON[meta.type] || "⬤";
  row.appendChild(icon);

  const info = document.createElement("div");
  info.className = "device__info";
  info.innerHTML = `
    <div class="device__name">${meta.name}</div>
    <div class="device__meta" data-status="${current.status}">${current.status}</div>
  `;
  row.appendChild(info);

  if (meta.type === "camera") {
    const btn = document.createElement("button");
    btn.className = "device__action";
    btn.textContent = "VIEW";
    btn.onclick = () => openCameraModal(deviceId);
    row.appendChild(btn);
  } else {
    const rocker = document.createElement("div");
    rocker.className = "rocker";
    rocker.dataset.status = current.status;
    rocker.onclick = () => {
      if (current.status === "DISCONNECTED") return; // can't toggle a dead device
      const next = DataLayer.getState(deviceId).status === "ON" ? "OFF" : "ON";
      DataLayer.setDeviceStatus(deviceId, next, "simulator-ui");
    };
    row.appendChild(rocker);
  }

  return row;
}

/*REACTIVE UPDATES*/
DataLayer.onDeviceChange((snapshot) => {
  updateDeviceRowUI(snapshot.deviceId, snapshot.status);
  updateRoomLightState(snapshot.deviceId);
  updateLightingStates();
  logActivity(snapshot);
});

function updateRoomLightState(deviceId) {
  const floor = HOUSE[activeFloor];
  for (const [roomId, room] of Object.entries(floor.rooms)) {
    if (!room.devices.includes(deviceId)) continue;
    const roomEl = document.getElementById(`room-${roomId}`);
    if (!roomEl) return;
    roomEl.classList.toggle("room--lit", isRoomLit(room));
    return;
  }
}

function updateDeviceRowUI(deviceId, status) {
  const row = document.getElementById(`device-${deviceId}`);
  if (!row) return; // device belongs to the floor that's not currently shown

  const metaEl = row.querySelector(".device__meta");
  if (metaEl) {
    metaEl.textContent = status;
    metaEl.dataset.status = status;
  }
  const rocker = row.querySelector(".rocker");
  if (rocker) rocker.dataset.status = status;
}

/*ACTIVITY LOG*/
function logActivity(snapshot) {
  const log = document.getElementById("activityLog");
  const meta = DEVICE_META[snapshot.deviceId];
  const time = new Date(snapshot.updatedAt).toLocaleTimeString("en-GB", { hour12: false });

  const entry = document.createElement("div");
  const cls = { ON: "on", OFF: "off", ERROR: "error", DISCONNECTED: "disc" }[snapshot.status] || "";
  entry.className = "log__entry " + cls;
  entry.innerHTML = `<span class="t">${time}</span>${meta.name} → <strong>${snapshot.status}</strong> <span class="t">(${snapshot.updatedBy})</span>`;
  log.appendChild(entry);

  // cap log length
  while (log.children.length > 60) log.removeChild(log.firstChild);
}

/*CAMERA MODAL*/
let cameraTimer = null;

function openCameraModal(deviceId) {
  const meta = DEVICE_META[deviceId];
  const current = DataLayer.getState(deviceId);

  document.getElementById("cameraModalTitle").textContent = meta.name;
  document.getElementById("cameraModal").classList.add("open");

  const canvas = document.getElementById("cameraCanvas");
  const draw = () => drawFakeFrame(canvas, meta.name);
  draw();
  cameraTimer = setInterval(draw, 2000);

  const statusEl = document.getElementById("cameraModalStatus");
  statusEl.textContent = current.status === "DISCONNECTED" ? "OFFLINE" : "ONLINE";
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

  // static noise
  const imgData = ctx.createImageData(w, h);
  for (let i = 0; i < imgData.data.length; i += 4) {
    const v = Math.random() * 14; 
    imgData.data[i] = v; imgData.data[i + 1] = v + 4; imgData.data[i + 2] = v + 8;
    imgData.data[i + 3] = 255;
  }
  ctx.putImageData(imgData, 0, 0);

  // scanline sweep
  const sweepY = (Date.now() / 8) % h;
  ctx.fillStyle = "rgba(76,195,138,0.08)";
  ctx.fillRect(0, sweepY, w, 2);

  // label + timestamp overlay, like a real camera OSD
  ctx.font = "11px monospace";
  ctx.fillStyle = "rgba(234,237,242,0.65)";
  ctx.fillText(label.toUpperCase(), 10, 18);
  ctx.fillText(new Date().toLocaleString(), 10, h - 10);

  document.getElementById("cameraModalTime").textContent = "Frame captured " + new Date().toLocaleTimeString("en-GB", { hour12: false });
}

/*UPLINK SIMULATOR*/
function populateUplinkDeviceList() {
  const select = document.getElementById("uplinkDevice");
  select.innerHTML = "";
  Object.entries(DEVICE_META).forEach(([id, meta]) => {
    const opt = document.createElement("option");
    opt.value = id;
    opt.textContent = `${meta.name} (${id})`;
    select.appendChild(opt);
  });
}

document.getElementById("uplinkPush").onclick = () => {
  const deviceId = document.getElementById("uplinkDevice").value;
  const status = document.getElementById("uplinkStatus").value;
  DataLayer.setDeviceStatus(deviceId, status, "cloud-uplink");
};

document.getElementById("uplinkChaos").onclick = () => {
  const ids = Object.keys(DEVICE_META);
  const randomId = ids[Math.floor(Math.random() * ids.length)];
  DataLayer.setDeviceStatus(randomId, "ERROR", "safety-worker");
};

/*BOOT*/
DataLayer.init(() => {
  renderFloorTabs();
  renderRooms();
  populateUplinkDeviceList();
  updateLightingStates();

  const connIndicator = document.getElementById("connIndicator");
  const connLabel = document.getElementById("connLabel");
  connIndicator.dataset.mode = DataLayer.mode;
  connLabel.textContent = DataLayer.mode === "live"
    ? "LIVE — connected to Firebase"
    : "MOCK MODE — no cloud link";
});