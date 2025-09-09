package com.example.fithub

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.fithub.data.AddUserDto
import com.example.fithub.data.UserData
import com.example.fithub.logic.UserCalculator
import kotlinx.coroutines.launch

class UserDataFragment : Fragment(R.layout.fragment_user_data) {
    private lateinit var etWeight: EditText
    private lateinit var etHeight: EditText
    private lateinit var etAge: EditText
    private lateinit var etSex: EditText
    private lateinit var tvBMIResult: TextView
    private lateinit var tvBMRResult: TextView
    private lateinit var etName: EditText
    private lateinit var btnSave: Button

    private val userCalculator = UserCalculator()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupClickListeners()
        setupTextWatchers()
    }

    private fun initViews(view: View) {
        etWeight = view.findViewById(R.id.etWeight)
        etHeight = view.findViewById(R.id.etHeight)
        etAge = view.findViewById(R.id.etAge)
        etSex = view.findViewById(R.id.etSex)
        tvBMIResult = view.findViewById(R.id.tvBMIResult)
        tvBMRResult = view.findViewById(R.id.tvBMRResult)
        etName = view.findViewById(R.id.etName)
        btnSave = view.findViewById(R.id.btnSave)
    }

    private fun setupClickListeners() {
        etWeight.setOnClickListener { showWeightPicker() }
        etHeight.setOnClickListener { showHeightPicker() }
        etAge.setOnClickListener { showAgePicker() }
        etSex.setOnClickListener { showSexPicker() }

        btnSave.setOnClickListener {
            val userData = getUserData()
            if (userData.isComplete()) {
                saveUserData(userData)
            } else {
                Toast.makeText(requireContext(), "Wypełnij wszystkie pola", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupTextWatchers() {
        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = updateCalculations()
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        listOf(etWeight, etHeight, etAge, etSex).forEach {
            it.addTextChangedListener(watcher)
        }
    }

    private fun getUserData(): UserData {
        return UserData(
            name = etName.text.toString().trim(),
            weight = etWeight.text.toString().toDoubleOrNull(),
            height = etHeight.text.toString().toDoubleOrNull(),
            age = etAge.text.toString().toIntOrNull(),
            sex = etSex.text.toString()
        )
    }

    private fun updateCalculations() {
        val userData = getUserData()

        tvBMIResult.visibility = View.INVISIBLE
        tvBMRResult.visibility = View.INVISIBLE

        if (userData.isValidForBMI()) {
            calculateAndShowBMI(userData.weight!!, userData.height!!)
        }

        if (userData.isValidForBMR()) {
            calculateAndShowBMR(userData.weight!!, userData.height!!, userData.age!!, userData.sex)
        }
    }

    private fun calculateAndShowBMI(weight: Double, height: Double) {
        val bmi = userCalculator.calculateBMI(weight, height)
        if (bmi != null) {
            val roundedBMI = String.format("%.1f", bmi)
            val bmiCategory = userCalculator.getBMICategory(bmi)
            tvBMIResult.apply {
                visibility = View.VISIBLE
                text = "Twoje BMI wynosi: $roundedBMI ($bmiCategory)"
            }
        }
    }

    private fun calculateAndShowBMR(weight: Double, height: Double, age: Int, sex: String) {
        val bmr = userCalculator.calculateBMR(weight, height, age.toDouble(), sex)
        if (bmr != null) {
            val roundedBMR = String.format("%.0f", bmr)
            tvBMRResult.apply {
                visibility = View.VISIBLE
                text = "Twoje dzienne zapotrzebowanie kaloryczne wynosi około: $roundedBMR kcal"
            }
        }
    }

    private fun saveUserData(userData: UserData) {
        val bmi = userCalculator.calculateBMI(userData.weight!!, userData.height!!)
        val roundedBMI = String.format("%.1f", bmi).toDouble()
        val bmr = userCalculator.calculateBMR(
            userData.weight!!, userData.height!!, userData.age!!.toDouble(), userData.sex
        )
        val roundedBMR = String.format("%.0f", bmr).toDouble()
        val dto = AddUserDto(
            username = userData.name,
            sex = userData.sex,
            age = userData.age!!,
            weight = userData.weight!!.toInt(),
            height = userData.height!!.toInt(),
            bmr = roundedBMR ?: 0.0,
            bmi = roundedBMI ?: 0.0
        )
        lifecycleScope.launch {
            try {
                NetworkModule.api.createUser(dto)
                Toast.makeText(requireContext(), "Dane zapisane", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Błąd: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }



    //Picker methods
    private fun showWeightPicker() {
        showNumberPicker(
            title = "Wybierz wagę (kg)",
            currentValue = etWeight.text.toString().toIntOrNull() ?: 70,
            minValue = 30,
            maxValue = 200
        ) { value ->
            etWeight.setText(value.toString())
        }
    }

    private fun showHeightPicker() {
        showNumberPicker(
            title = "Wybierz wzrost (cm)",
            currentValue = etHeight.text.toString().toIntOrNull() ?: 180,
            minValue = 80,
            maxValue = 250
        ) { value ->
            etHeight.setText(value.toString())
        }
    }

    private fun showAgePicker() {
        showNumberPicker(
            title = "Wybierz wiek",
            currentValue = etAge.text.toString().toIntOrNull() ?: 25,
            minValue = 16,
            maxValue = 120
        ) { value ->
            etAge.setText(value.toString())
        }
    }

    private fun showSexPicker() {
        val values = arrayOf("Male", "Female")
        val currentValue = etSex.text.toString()
        val currentIndex = values.indexOf(currentValue).takeIf { it >= 0 } ?: 0

        val numberPicker = NumberPicker(requireContext()).apply {
            minValue = 0
            maxValue = values.size - 1
            displayedValues = values
            value = currentIndex
            wrapSelectorWheel = false
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Wybierz płeć")
            .setView(numberPicker)
            .setPositiveButton("OK") { _, _ ->
                etSex.setText(values[numberPicker.value])
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showNumberPicker(
        title: String,
        currentValue: Int,
        minValue: Int,
        maxValue: Int,
        onValueSelected: (Int) -> Unit
    ) {
        val numberPicker = NumberPicker(requireContext()).apply {
            this.minValue = minValue
            this.maxValue = maxValue
            value = currentValue.coerceIn(minValue, maxValue)
            wrapSelectorWheel = false
        }

        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setView(numberPicker)
            .setPositiveButton("OK") { _, _ ->
                onValueSelected(numberPicker.value)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}