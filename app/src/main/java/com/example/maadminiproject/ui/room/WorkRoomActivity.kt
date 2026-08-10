package com.example.maadminiproject.ui.room

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.maadminiproject.R
import com.example.maadminiproject.databinding.ActivityWorkRoomBinding
import com.example.maadminiproject.ui.dashboard.MainActivity
import com.example.maadminiproject.ui.floor.FloorActivity
import com.example.maadminiproject.ui.settings.SettingsActivity

class WorkRoomActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWorkRoomBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityWorkRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        setupControls()
        setupBottomNav()
    }

    private fun setupControls() {
        binding.swMainLight.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.tvLightStatus.text = getString(R.string.active_85)
                binding.tvLightStatus.setTextColor(getColor(R.color.vibrant_cyan))
                binding.ivLightIcon.backgroundTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.vibrant_cyan))
            } else {
                binding.tvLightStatus.text = getString(R.string.status_off)
                binding.tvLightStatus.setTextColor(getColor(R.color.soft_gray))
                binding.ivLightIcon.backgroundTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.surface_container))
            }
        }

        binding.swDesk.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.tvDeskStatus.text = getString(R.string.status_on)
                binding.tvDeskStatus.setTextColor(getColor(R.color.vibrant_cyan))
            } else {
                binding.tvDeskStatus.text = getString(R.string.standby_12w)
                binding.tvDeskStatus.setTextColor(getColor(R.color.soft_gray))
            }
        }

        binding.swAc.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.tvAcStatus.text = getString(R.string.cooling_22)
                binding.tvAcStatus.setTextColor(getColor(R.color.vibrant_cyan))
                binding.ivAcIcon.backgroundTintList = android.content.res.ColorStateList.valueOf(0x2000E5FF.toInt())
            } else {
                binding.tvAcStatus.text = getString(R.string.status_off)
                binding.tvAcStatus.setTextColor(getColor(R.color.soft_gray))
                binding.ivAcIcon.backgroundTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.surface_container))
            }
        }
    }

    private fun setupBottomNav() {
        binding.bottomNav.selectedItemId = 0
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_floors -> {
                    startActivity(Intent(this, FloorActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_reports -> {
                    true
                }
                else -> false
            }
        }
    }
}