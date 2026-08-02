const HOUSE = {
  ground_floor: {
    label: "Ground Floor",
    rooms: {
      living_room: {
        label: "Living Room",
        devices: ["living_light", "ceiling_fan", "entrance_camera"]
      },
      kitchen: {
        label: "Kitchen",
        devices: ["kitchen_light", "kitchen_smart_plug"]
      },
      dining_room: {
        label: "Dining Room",
        devices: ["dining_light", "dining_smart_plug"]
      },
      garage: {
        label: "Garage",
        devices: ["garage_light", "garage_camera"]
      },
      bathroom_gf: {
        label: "Bathroom",
        devices: ["bathroom_light_gf"]
      },
      staircase: {
        label: "Staircase",
        devices: ["stairway_light"]
      }
    }
  },
  first_floor: {
    label: "First Floor",
    rooms: {
      master_bedroom: {
        label: "Master Bedroom",
        devices: ["master_bedroom_light", "master_air_conditioner", "master_smart_plug"]
      },
      bedroom_2: {
        label: "Bedroom 2",
        devices: ["bedroom2_light", "bedroom2_air_conditioner"]
      },
      work_room: {
        label: "Work Room",
        devices: ["workroom_light", "workroom_air_conditioner", "workroom_smart_plug"]
      },
      bathroom_ff: {
        label: "Bathroom",
        devices: ["bathroom_light_ff"]
      },
      hallway: {
        label: "Hallway",
        devices: ["hallway_light", "indoor_camera"]
      }
    }
  }
};


const DEVICE_META = {
  living_light:            { name: "Living Light",        type: "light" },
  ceiling_fan:              { name: "Ceiling Fan",          type: "fan" },
  entrance_camera:          { name: "Entrance Camera",      type: "camera" },
  kitchen_light:            { name: "Kitchen Light",        type: "light" },
  kitchen_smart_plug:       { name: "Smart Plug",           type: "smart_plug" },
  dining_light:             { name: "Dining Light",         type: "light" },
  dining_smart_plug:        { name: "Smart Plug",           type: "smart_plug" },
  garage_light:             { name: "Garage Light",         type: "light" },
  garage_camera:            { name: "Garage Camera",        type: "camera" },
  bathroom_light_gf:        { name: "Bathroom Light",       type: "light" },
  stairway_light:           { name: "Stairway Light",       type: "light" },
  master_bedroom_light:     { name: "Bedroom Light",        type: "light" },
  master_air_conditioner:   { name: "Air Conditioner",      type: "air_conditioner" },
  master_smart_plug:        { name: "Smart Plug",           type: "smart_plug" },
  bedroom2_light:           { name: "Bedroom Light",        type: "light" },
  bedroom2_air_conditioner: { name: "Air Conditioner",      type: "air_conditioner" },
  workroom_light:           { name: "Work Room Light",      type: "light" },
  workroom_air_conditioner: { name: "Air Conditioner",      type: "air_conditioner" },
  workroom_smart_plug:      { name: "Smart Plug",           type: "smart_plug" },
  bathroom_light_ff:        { name: "Bathroom Light",       type: "light" },
  hallway_light:            { name: "Hallway Light",        type: "light" },
  indoor_camera:            { name: "Indoor Camera",        type: "camera" }
};

const TYPE_ICON = {
  light: "💡",
  fan: "🌀",
  camera: "📷",
  smart_plug: "🔌",
  air_conditioner: "❄️"
};

const DataLayer = (() => {
  // in-memory state 
  const state = {};
  Object.keys(DEVICE_META).forEach(id => {
    state[id] = {
      status: "OFF",
      updatedAt: Date.now(),
      updatedBy: "seed"
    };
  });

  const listeners = new Set();

  function notify(deviceId, prevStatus) {
    const snapshot = { deviceId, ...state[deviceId], prevStatus };
    listeners.forEach(fn => fn(snapshot));
  }

  return {
    mode: "mock",

    init(onReady) {
      Object.keys(state).forEach(id => notify(id, null));
      if (onReady) onReady();
    },

    /** Subscribe to every future state change, mock or real. */
    onDeviceChange(callback) {
      listeners.add(callback);
    },

    getState(deviceId) {
      return state[deviceId];
    },

    getAllState() {
      return state;
    },

    /**
     * Called by the UI when a person flips a rocker switch.
     */
    setDeviceStatus(deviceId, newStatus, actor = "simulator") {
      const prev = state[deviceId].status;
      if (prev === newStatus) return;
      state[deviceId] = { status: newStatus, updatedAt: Date.now(), updatedBy: actor };
      notify(deviceId, prev);
    }
  };
})();