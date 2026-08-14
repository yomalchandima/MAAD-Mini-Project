package com.example.maadminiproject.ui.floor

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.maadminiproject.R
import com.example.maadminiproject.data.models.Zone
import com.example.maadminiproject.databinding.ItemZoneCardBinding

class ZoneAdapter(
    private var zones: List<Zone>,
    private val onZoneClick: (Zone) -> Unit,
) : RecyclerView.Adapter<ZoneAdapter.ZoneViewHolder>() {

    fun updateZones(newZones: List<Zone>) {
        this.zones = newZones
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ZoneViewHolder {
        val binding = ItemZoneCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        )
        return ZoneViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ZoneViewHolder, position: Int) {
        holder.bind(zones[position])
    }

    override fun getItemCount(): Int = zones.size

    inner class ZoneViewHolder(private val binding: ItemZoneCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(zone: Zone) {
            binding.tvZoneName.text = zone.zoneName.ifBlank { zone.zoneId }
            val count = zone.devices.size
            binding.tvZoneDeviceCount.text = binding.root.context.getString(R.string.devices_count_dynamic, count)

            binding.ivZoneIcon.setImageResource(resolveZoneIcon(zone.zoneId))

            binding.cardZone.setOnClickListener {
                onZoneClick(zone)
            }
        }

        private fun resolveZoneIcon(zoneId: String): Int {
            return when (zoneId.lowercase()) {
                "livingroom", "living" -> R.drawable.ic_sofa
                "kitchen" -> R.drawable.ic_kitchen
                "diningroom", "dining" -> R.drawable.ic_dining
                "garage" -> R.drawable.ic_garage
                "bathroomgf", "bathroomff", "bathroom" -> R.drawable.ic_bath
                "staircase" -> R.drawable.ic_stairs
                "masterbedroom", "bedroom2", "bedroom" -> R.drawable.ic_bed
                "workroom", "office" -> R.drawable.ic_briefcase
                "hallway" -> R.drawable.ic_door
                else -> R.drawable.ic_home
            }
        }
    }
}
