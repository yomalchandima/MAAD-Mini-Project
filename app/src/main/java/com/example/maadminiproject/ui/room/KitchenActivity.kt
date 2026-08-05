package com.example.maadminiproject.ui.room

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.maadminiproject.R
import com.example.maadminiproject.databinding.ActivityKitchenBinding
import com.example.maadminiproject.ui.dashboard.MainActivity
import com.example.maadminiproject.ui.floor.FloorActivity
import com.example.maadminiproject.ui.settings.SettingsActivity

class KitchenActivity : AppCompatActivity() {
    private lateinit var binding: ActivityKitchenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityKitchenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        setupSwitchListeners()
        setupBottomNav()
    }

    private fun setupSwitchListeners() {
        binding.swMainLighting.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.tvLightStatus.text = getString(R.string.online_caps)
                binding.tvLightStatus.setTextColor(getColor(R.color.vibrant_cyan))
                binding.tvLightStatus.compoundDrawableTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.vibrant_cyan))
                binding.ivLightIcon.backgroundTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.vibrant_cyan))
            } else {
                binding.tvLightStatus.text = getString(R.string.status_off)
                binding.tvLightStatus.setTextColor(getColor(R.color.soft_gray))
                binding.tvLightStatus.compoundDrawableTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.soft_gray))
                binding.ivLightIcon.backgroundTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.surface_container))
            }
        }

        binding.swEspresso.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.tvEspressoStatus.text = getString(R.string.status_on)
                binding.tvEspressoStatus.setTextColor(getColor(R.color.vibrant_cyan))
                binding.tvEspressoStatus.compoundDrawableTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.vibrant_cyan))
                binding.ivPlugIcon.backgroundTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.vibrant_cyan))
                binding.ivPlugIcon.imageTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.deep_midnight))
            } else {
                binding.tvEspressoStatus.text = getString(R.string.status_off)
                binding.tvEspressoStatus.setTextColor(getColor(R.color.soft_gray))
                binding.tvEspressoStatus.compoundDrawableTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.soft_gray))
                binding.ivPlugIcon.backgroundTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.surface_container))
                binding.ivPlugIcon.imageTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.soft_gray))
            }
        }
    }

    private fun setupBottomNav() {
        binding.bottomNav.selectedItemId = 0 // Not a main nav item, or choose a related one
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