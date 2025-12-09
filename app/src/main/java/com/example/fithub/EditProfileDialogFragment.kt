package com.example.fithub

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.fithub.data.ComputedData
import com.example.fithub.data.UpdateProfileData
import com.example.fithub.data.UpdateUserDto
import com.example.fithub.logic.UserCalculator
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class EditProfileDialogFragment : DialogFragment() {

    private lateinit var etName: EditText
    private lateinit var etWeight: EditText
    private lateinit var etHeight: EditText
    private lateinit var etBirthDate: EditText
    private lateinit var etSex: EditText
    private lateinit var alertDialog: AlertDialog

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = layoutInflater.inflate(R.layout.fragment_edit_profile_dialog, null)

        etName = view.findViewById(R.id.etName)
        etWeight = view.findViewById(R.id.etWeight)
        etHeight = view.findViewById(R.id.etHeight)
        etBirthDate = view.findViewById(R.id.etBirthDate)
        etSex = view.findViewById(R.id.etSex)

        arguments?.let {
            etName.setText(it.getString("userName"))
            etWeight.setText(it.getDouble("userWeight").toString())
            etHeight.setText(it.getInt("userHeight").toString())

            val birthDateIso = it.getString("userBirthDate")
            if (birthDateIso != null) {
                try {
                    val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val date = if (birthDateIso.contains("T")) {
                        isoFormat.parse(birthDateIso.substring(0, 10))
                    } else {
                        isoFormat.parse(birthDateIso)
                    }
                    etBirthDate.setText(displayFormat.format(date!!))
                } catch (e: Exception) {
                    etBirthDate.setText(birthDateIso)
                }
            }

            val sex = it.getString("userSex")
            // Mapowanie z angielskiego (z API) na polski (do wyświetlenia)
            etSex.setText(when(sex) {
                "Male" -> "Mężczyzna"
                "Female" -> "Kobieta"
                else -> "Mężczyzna"
            })
        }

        setupClickListeners()

        alertDialog = AlertDialog.Builder(requireContext())
            .setTitle("Edytuj profil")
            .setView(view)
            .setPositiveButton("Zapisz", null) // Ustawione na null - obsługa niżej
            .setNegativeButton("Anuluj", null)
            .create()

        alertDialog.setOnShowListener {
            val positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                saveProfile()
            }
        }

        return alertDialog
    }

    private fun setupClickListeners() {
        etWeight.setOnClickListener { showWeightPicker() }
        etHeight.setOnClickListener { showHeightPicker() }
        etBirthDate.setOnClickListener { showDatePicker() }
        etSex.setOnClickListener { showSexPicker() }
    }

    private fun saveProfile() {
        val userId = arguments?.getString("userId") ?: return

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).isEnabled = false

        lifecycleScope.launch {
            try {
                val calculator = UserCalculator()
                val weight = etWeight.text.toString().toIntOrNull()
                val height = etHeight.text.toString().toIntOrNull()
                val birthDateDisplay = etBirthDate.text.toString()
                val sexDisplay = etSex.text.toString()
                
                // Mapowanie z polskiego (z UI) na angielski (do API)
                val sex = when(sexDisplay) {
                    "Mężczyzna" -> "Male"
                    "Kobieta" -> "Female"
                    else -> "Male"
                }

                if (weight == null || height == null) {
                    Toast.makeText(context, "Nieprawidłowa waga lub wzrost", Toast.LENGTH_SHORT).show()
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                    alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).isEnabled = true
                    return@launch
                }

                val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val birthDate = try {
                    val date = displayFormat.parse(birthDateDisplay)
                    isoFormat.format(date!!)
                } catch (e: Exception) {
                    Log.e("EditProfile", "Błąd konwersji daty: ${e.message}")
                    birthDateDisplay
                }

                val age = calculator.calculateAge(birthDate)

                Log.d("EditProfile", "Waga: $weight, Wzrost: $height, Wiek: $age, Płeć: $sex")

                val bmi = calculator.calculateBMI(weight.toDouble(), height.toDouble())
                val bmr = calculator.calculateBMR(weight.toDouble(), height.toDouble(), age.toDouble(), sex)

                Log.d("EditProfile", "BMI: $bmi, BMR: $bmr")

                val updateDto = UpdateUserDto(
                    username = etName.text.toString(),
                    profile = UpdateProfileData(
                        weightKg = weight.toDouble(),
                        heightCm = height,
                        sex = sex,
                        birthDate = birthDate
                    ),
                    computed = ComputedData(
                        bmi = bmi ?: 0.0,
                        bmr = bmr ?: 0.0
                    )
                )

                Log.d("EditProfile", "Wysyłam dane: $updateDto")

                val response = NetworkModule.api.updateUser(userId, updateDto)

                Log.d("EditProfile", "Odpowiedź: $response")

                Toast.makeText(context, "Profil zaktualizowany", Toast.LENGTH_SHORT).show()

                (parentFragment as? UserProfileFragment)?.loadDataForUser(userId)

                dismiss()

            } catch (e: Exception) {
                Log.e("EditProfile", "Błąd aktualizacji: ${e.message}", e)
                Toast.makeText(context, "Błąd: ${e.message}", Toast.LENGTH_LONG).show()

                // Włącz przyciski z powrotem w przypadku błędu
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).isEnabled = true
            }
        }
    }


    private fun showWeightPicker() {
        showNumberPicker("Waga (kg)", etWeight.text.toString().toIntOrNull() ?: 70, 30, 200) {
            etWeight.setText(it.toString())
        }
    }

    private fun showHeightPicker() {
        showNumberPicker("Wzrost (cm)", etHeight.text.toString().toIntOrNull() ?: 170, 80, 250) {
            etHeight.setText(it.toString())
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()

        // Spróbuj sparsować aktualną datę
        try {
            val displayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = displayFormat.parse(etBirthDate.text.toString())
            if (date != null) {
                calendar.time = date
            }
        } catch (e: Exception) {
            calendar.add(Calendar.YEAR, -25)
        }

        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                val date = String.format("%02d/%02d/%04d", day, month + 1, year)
                etBirthDate.setText(date)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showSexPicker() {
        val options = arrayOf("Mężczyzna", "Kobieta")
        val currentValue = etSex.text.toString()
        val currentIndex = options.indexOf(currentValue).takeIf { it >= 0 } ?: 0

        AlertDialog.Builder(requireContext())
            .setTitle("Płeć")
            .setSingleChoiceItems(options, currentIndex) { dialog, which ->
                etSex.setText(options[which])
                dialog.dismiss()
            }
            .show()
    }

    private fun showNumberPicker(title: String, current: Int, min: Int, max: Int, onSelected: (Int) -> Unit) {
        val picker = NumberPicker(requireContext()).apply {
            minValue = min
            maxValue = max
            value = current
        }
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setView(picker)
            .setPositiveButton("OK") { _, _ -> onSelected(picker.value) }
            .setNegativeButton("Anuluj", null)
            .show()
    }
}