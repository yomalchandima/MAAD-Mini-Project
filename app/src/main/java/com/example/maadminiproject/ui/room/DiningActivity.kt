package com.example.maadminiproject.ui.room

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.maadminiproject.R
import com.example.maadminiproject.databinding.ActivityDiningBinding
import com.example.maadminiproject.ui.dashboard.MainActivity
import com.example.maadminiproject.ui.floor.FloorActivity
import com.example.maadminiproject.ui.settings.SettingsActivity

class DiningActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDiningBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDiningBinding.inflate(layoutInflater)
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
        binding.swPendant.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.tvPendantStatus.text = getString(R.string.online)
                binding.tvPendantStatus.setTextColor(getColor(R.color.vibrant_cyan))
                binding.tvPendantStatus.backgroundTintList = android.content.res.ColorStateList.valueOf(0x2000E5FF.toInt())
                binding.ivPendantIcon.backgroundTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.vibrant_cyan))
                binding.ivPendantIcon.imageTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.deep_midnight))
            } else {
                binding.tvPendantStatus.text = getString(R.string.status_off)
                binding.tvPendantStatus.setTextColor(getColor(R.color.soft_gray))
                binding.tvPendantStatus.backgroundTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.surface_container))
                binding.ivPendantIcon.backgroundTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.surface_container))
                binding.ivPendantIcon.imageTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.soft_gray))
            }
        }

        binding.swCoffee.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.tvCoffeeStatus.text = getString(R.string.status_on)
                binding.tvCoffeeStatus.setTextColor(getColor(R.color.vibrant_cyan))
                binding.tvCoffeeStatus.backgroundTintList = android.content.res.ColorStateList.valueOf(0x2000E5FF.toInt())
                binding.ivCoffeeIcon.backgroundTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.vibrant_cyan))
                binding.ivCoffeeIcon.imageTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.deep_midnight))
            } else {
                binding.tvCoffeeStatus.text = getString(R.string.standby)
                binding.tvCoffeeStatus.setTextColor(getColor(R.color.soft_gray))
                binding.tvCoffeeStatus.backgroundTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.surface_container))
                binding.ivCoffeeIcon.backgroundTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.surface_container))
                binding.ivCoffeeIcon.imageTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.soft_gray))
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