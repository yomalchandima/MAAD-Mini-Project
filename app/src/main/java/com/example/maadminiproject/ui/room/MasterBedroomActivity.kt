package com.example.maadminiproject.ui.room

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.maadminiproject.R
import com.example.maadminiproject.databinding.ActivityMasterBedroomBinding
import com.example.maadminiproject.ui.dashboard.MainActivity
import com.example.maadminiproject.ui.floor.FloorActivity
import com.example.maadminiproject.ui.settings.SettingsActivity

class MasterBedroomActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMasterBedroomBinding
    private var currentTemp = 21
    private var ironMaxDuration = 10
    private var ironTimer: android.os.CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMasterBedroomBinding.inflate(layoutInflater)
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
        binding.swAc.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.tvAcStatus.text = getString(R.string.status_on)
                binding.tvAcStatus.setTextColor(getColor(R.color.vibrant_cyan))
                binding.ivAcIcon.backgroundTintList = android.content.res.ColorStateList.valueOf(0x2000E5FF.toInt())
                binding.tempControl.visibility = android.view.View.VISIBLE
            } else {
                binding.tvAcStatus.text = getString(R.string.status_off)
                binding.tvAcStatus.setTextColor(getColor(R.color.soft_gray))
                binding.ivAcIcon.backgroundTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.surface_container))
                binding.tempControl.visibility = android.view.View.GONE
            }
        }

        binding.btnTempMinus.setOnClickListener {
            if (currentTemp > 16) {
                currentTemp--
                updateTempUI()
            }
        }

        binding.btnTempPlus.setOnClickListener {
            if (currentTemp < 30) {
                currentTemp++
                updateTempUI()
            }
        }


        binding.swMainLights.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.tvLightsStatus.text = getString(R.string.status_on)
                binding.tvLightsStatus.setTextColor(getColor(R.color.vibrant_cyan))
                binding.ivLightsIcon.backgroundTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.vibrant_cyan))
            } else {
                binding.tvLightsStatus.text = getString(R.string.status_off)
                binding.tvLightsStatus.setTextColor(getColor(R.color.soft_gray))
                binding.ivLightsIcon.backgroundTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.surface_container))
            }
        }

        binding.swSmartPlug.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.tvPlugStatus.text = getString(R.string.status_on)
                binding.tvPlugStatus.setTextColor(getColor(R.color.vibrant_cyan))
            } else {
                binding.tvPlugStatus.text = getString(R.string.status_off)
                binding.tvPlugStatus.setTextColor(getColor(R.color.soft_gray))
            }
        }

        binding.swIron.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                startIronSafetyTimer()
                binding.ivIronIcon.imageTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.error_red))
                binding.ivIronIcon.backgroundTintList = android.content.res.ColorStateList.valueOf(0x20FF5252.toInt())
            } else {
                stopIronSafetyTimer()
                binding.tvIronStatus.text = getString(R.string.status_off)
                binding.tvIronStatus.setTextColor(getColor(R.color.soft_gray))
                binding.ivIronIcon.imageTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.soft_gray))
                binding.ivIronIcon.backgroundTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.surface_container))
            }
        }

        binding.sliderIronDuration.addOnChangeListener { _, value, _ ->
            ironMaxDuration = value.toInt()
            binding.tvIronDurationValue.text = getString(R.string.duration_format, ironMaxDuration)
            if (binding.swIron.isChecked) {
                // Restart timer if duration changed while running
                startIronSafetyTimer()
            }
        }
    }

    private fun startIronSafetyTimer() {
        ironTimer?.cancel()
        
        binding.tvIronStatus.text = getString(R.string.auto_off_status, ironMaxDuration)
        binding.tvIronStatus.setTextColor(getColor(R.color.error_red))

        // Note: Real minutes would be ironMaxDuration * 60 * 1000
        // For demonstration, we'll treat 1 slider unit as a shorter interval if needed, 
        // but for assignment compliance we'll use the logic.
        ironTimer = object : android.os.CountDownTimer((ironMaxDuration * 60 * 1000).toLong(), 60000) {
            override fun onTick(millisUntilFinished: Long) {
                val minsLeft = (millisUntilFinished / 60000).toInt() + 1
                binding.tvIronStatus.text = getString(R.string.auto_off_status, minsLeft)
            }

            override fun onFinish() {
                binding.swIron.isChecked = false
            }
        }.start()
    }

    private fun stopIronSafetyTimer() {
        ironTimer?.cancel()
        ironTimer = null
    }

    private fun updateTempUI() {
        binding.tvTemperature.text = getString(R.string.temp_format, currentTemp)
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