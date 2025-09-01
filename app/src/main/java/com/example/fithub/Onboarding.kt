package com.example.fithub

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.fithub.logic.UserCalculator

class Onboarding : AppCompatActivity() {
    // BMI elements
    private lateinit var layoutBMISection: LinearLayout
    private lateinit var btnShowBMI: Button
    private lateinit var etWeight: EditText
    private lateinit var etHeight: EditText
    private lateinit var btnCalculateBMI: Button
    private lateinit var etBMIResult: EditText
    private lateinit var tvBMICategory: TextView

    // Other elements
    private lateinit var etName: EditText
    private lateinit var btnSave: Button

    private val userCalculator = UserCalculator()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.onboarding)

        initViews()
        setupClickListeners()
    }

    private fun initViews() {
        // BMI views
        layoutBMISection = findViewById(R.id.layoutBMISection)
        btnShowBMI = findViewById(R.id.btnShowBMI)
        etWeight = findViewById(R.id.etWeight)
        etHeight = findViewById(R.id.etHeight)
        btnCalculateBMI = findViewById(R.id.btnCalculateBMI)
        etBMIResult = findViewById(R.id.etBMIResult)
        tvBMICategory = findViewById(R.id.tvBMICategory)

        // Other views
        etName = findViewById(R.id.etName)
        btnSave = findViewById(R.id.btnSave)
    }

    private fun setupClickListeners() {
        // Show/hide BMI section
        btnShowBMI.setOnClickListener {
            toggleSection(layoutBMISection, btnShowBMI, "Ukryj kalkulator BMI", "Wprowadź dane by obliczyć BMI")
        }

        // Weight picker
        etWeight.setOnClickListener { showWeightPicker() }

        // Height picker
        etHeight.setOnClickListener { showHeightPicker() }

        // Calculate BMI
        btnCalculateBMI.setOnClickListener { calculateBMI() }

        // Save data - DO ROZBUDOWY
        btnSave.setOnClickListener {
            Toast.makeText(this, "Dane zapisane!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleSection(layout: LinearLayout, button: Button, hideText: String, showText: String) {
        if (layout.visibility == View.GONE) {
            layout.visibility = View.VISIBLE
            button.text = hideText
        } else {
            layout.visibility = View.GONE
            button.text = showText
        }
    }

    private fun calculateBMI() {
        val weightVal = etWeight.text.toString().toDoubleOrNull()
        val heightVal = etHeight.text.toString().toDoubleOrNull()

        if (weightVal != null && heightVal != null) {
            val bmi = userCalculator.calculateBMI(weightVal, heightVal)
            if (bmi != null) {
                val roundedBMI = String.format("%.1f", bmi)
                etBMIResult.setText(roundedBMI)
                tvBMICategory.text = userCalculator.getBMICategory(bmi)
            }
        } else {
            Toast.makeText(this, "Wprowadź wagę i wzrost", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showWeightPicker() {
        val currentValue = etWeight.text.toString().toIntOrNull() ?: 70
        val numberPicker = NumberPicker(this).apply {
            minValue = 30
            maxValue = 200
            value = currentValue.coerceIn(minValue, maxValue)
            wrapSelectorWheel = false
        }

        AlertDialog.Builder(this)
            .setTitle("Wybierz wagę (kg)")
            .setView(numberPicker)
            .setPositiveButton("OK") { _, _ ->
                etWeight.setText(numberPicker.value.toString())
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showHeightPicker() {
        val currentValue = etHeight.text.toString().toIntOrNull() ?: 180
        val numberPicker = NumberPicker(this).apply {
            minValue = 80
            maxValue = 250
            value = currentValue.coerceIn(minValue, maxValue)
            wrapSelectorWheel = false
        }

        AlertDialog.Builder(this)
            .setTitle("Wybierz wzrost (cm)")
            .setView(numberPicker)
            .setPositiveButton("OK") { _, _ ->
                etHeight.setText(numberPicker.value.toString())
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}