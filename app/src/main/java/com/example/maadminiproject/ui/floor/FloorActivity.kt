package com.example.maadminiproject.ui.floor

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.maadminiproject.R
import com.example.maadminiproject.data.models.Floor
import com.example.maadminiproject.databinding.ActivityFloorBinding
import com.example.maadminiproject.ui.dashboard.MainActivity
import com.example.maadminiproject.ui.settings.SettingsActivity
import com.example.maadminiproject.viewmodel.floor.FloorViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText

class FloorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFloorBinding
    private lateinit var floorViewModel: FloorViewModel
    private lateinit var floorAdapter: FloorAdapter

    private val homeId = "home001"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityFloorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        floorViewModel = ViewModelProvider(this)[FloorViewModel::class.java]

        setupRecyclerView()
        setupBottomNav()
        setupAddFloorAction()
        observeFloors()

        floorViewModel.observeFloors(homeId)
    }

    private fun setupRecyclerView() {
        floorAdapter = FloorAdapter(
            floors = emptyList(),
            onEnterFloor = { floor ->
                val intent = Intent(this, FloorDetailActivity::class.java).apply {
                    putExtra("floorId", floor.floorId)
                }
                startActivity(intent)
            },
            onRenameFloor = { floor ->
                showRenameFloorDialog(floor)
            },
            onDeleteFloor = { floor ->
                showDeleteFloorDialog(floor)
            },
        )

        binding.rvFloors.layoutManager = LinearLayoutManager(this)
        binding.rvFloors.adapter = floorAdapter
    }

    private fun setupAddFloorAction() {
        binding.btnAddFloor.setOnClickListener {
            showAddFloorDialog()
        }
    }

    private fun showAddFloorDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_floor, null)
        val etFloorName = dialogView.findViewById<TextInputEditText>(R.id.etFloorName)
        val rgFloorPlans = dialogView.findViewById<RadioGroup>(R.id.rgFloorPlans)

        MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setPositiveButton(getString(R.string.create)) { dialog, _ ->
                val name = etFloorName.text?.toString()?.trim().orEmpty()
                val selectedPlan = when (rgFloorPlans.checkedRadioButtonId) {
                    R.id.rbFirstPlan -> "first_floor_map"
                    R.id.rbStudioPlan -> "ground_floor"
                    else -> "ground_floor_map"
                }

                floorViewModel.createFloor(
                    homeId = homeId,
                    floorName = name,
                    floorPlanImage = selectedPlan,
                    onSuccess = {
                        Toast.makeText(this, "Floor added successfully", Toast.LENGTH_SHORT).show()
                    },
                    onFailure = { err ->
                        Toast.makeText(this, "Error: ${err.message}", Toast.LENGTH_LONG).show()
                    },
                )
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showRenameFloorDialog(floor: Floor) {
        val input = EditText(this).apply {
            setText(floor.floorName)
            setSelection(text.length)
            setTextColor(getColor(R.color.white))
            setHintTextColor(getColor(R.color.soft_gray))
            setPadding(48, 32, 48, 32)
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.rename_floor))
            .setView(input)
            .setPositiveButton(getString(R.string.save)) { dialog, _ ->
                val newName = input.text.toString().trim()
                if (newName.isNotEmpty()) {
                    floorViewModel.updateFloorName(
                        homeId = homeId,
                        floorId = floor.floorId,
                        newName = newName,
                        onSuccess = {
                            Toast.makeText(this, "Floor renamed", Toast.LENGTH_SHORT).show()
                        },
                        onFailure = { err ->
                            Toast.makeText(this, "Error: ${err.message}", Toast.LENGTH_LONG).show()
                        },
                    )
                }
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showDeleteFloorDialog(floor: Floor) {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.delete_floor))
            .setMessage(getString(R.string.delete_floor_confirm, floor.floorName))
            .setPositiveButton(getString(R.string.delete)) { dialog, _ ->
                floorViewModel.deleteFloor(
                    homeId = homeId,
                    floorId = floor.floorId,
                    onSuccess = {
                        Toast.makeText(this, "${floor.floorName} deleted", Toast.LENGTH_SHORT).show()
                    },
                    onFailure = { err ->
                        Toast.makeText(this, "Error: ${err.message}", Toast.LENGTH_LONG).show()
                    },
                )
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun observeFloors() {
        floorViewModel.floors.observe(this) { floors ->
            floorAdapter.updateFloors(floors)

            if (floors.isEmpty()) {
                binding.layoutEmptyFloors.visibility = View.VISIBLE
                binding.rvFloors.visibility = View.GONE
            } else {
                binding.layoutEmptyFloors.visibility = View.GONE
                binding.rvFloors.visibility = View.VISIBLE
            }
        }
    }

    private fun setupBottomNav() {
        binding.bottomNav.selectedItemId = R.id.nav_floors
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_floors -> true
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_reports -> {
                    startActivity(Intent(this, com.example.maadminiproject.ui.report.ReportsActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                else -> false
            }
        }
    }
}