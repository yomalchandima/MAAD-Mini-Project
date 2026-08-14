package com.example.maadminiproject.ui.floor

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.example.maadminiproject.R
import com.example.maadminiproject.data.models.Floor
import com.example.maadminiproject.databinding.ItemFloorCardBinding

class FloorAdapter(
    private var floors: List<Floor>,
    private val onEnterFloor: (Floor) -> Unit,
    private val onRenameFloor: (Floor) -> Unit,
    private val onDeleteFloor: (Floor) -> Unit,
) : RecyclerView.Adapter<FloorAdapter.FloorViewHolder>() {

    fun updateFloors(newFloors: List<Floor>) {
        this.floors = newFloors
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FloorViewHolder {
        val binding = ItemFloorCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        )
        return FloorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FloorViewHolder, position: Int) {
        holder.bind(floors[position])
    }

    override fun getItemCount(): Int = floors.size

    inner class FloorViewHolder(private val binding: ItemFloorCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(floor: Floor) {
            val context = binding.root.context

            binding.tvFloorTitle.text = floor.floorName.ifBlank { floor.floorId }

            val roomCount = floor.zones.size
            binding.tvRoomCount.text = context.getString(R.string.rooms_count_dynamic, roomCount)

            var deviceCount = 0
            for (zone in floor.zones.values) {
                deviceCount += zone.devices.size
            }
            binding.tvDeviceCount.text = context.getString(R.string.devices_count_dynamic, deviceCount)

            val layoutName = when (floor.floorPlanImage?.lowercase()) {
                "first_floor_map", "first_floor" -> "First Floor Layout"
                "ground_floor", "studio" -> "Modern Studio Layout"
                else -> "Ground Floor Layout"
            }
            binding.tvFloorPlanTag.text = "Plan: $layoutName"

            binding.btnEnterFloor.setOnClickListener {
                onEnterFloor(floor)
            }

            binding.btnFloorOptions.setOnClickListener { view ->
                val popup = PopupMenu(context, view)
                popup.menu.add(0, 1, 0, context.getString(R.string.rename_floor))
                popup.menu.add(0, 2, 1, context.getString(R.string.delete_floor))
                popup.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        1 -> {
                            onRenameFloor(floor)
                            true
                        }
                        2 -> {
                            onDeleteFloor(floor)
                            true
                        }
                        else -> false
                    }
                }
                popup.show()
            }
        }
    }
}
