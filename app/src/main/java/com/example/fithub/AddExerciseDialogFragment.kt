package com.example.fithub

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.fithub.data.ExerciseDto
import android.widget.TextView

class AddExerciseDialogFragment : SearchDialogFragment<ExerciseDto>() {

    interface OnExerciseAddedListener {
        fun onExerciseAdded()
    }

    var onExerciseAddedListener: OnExerciseAddedListener? = null

    override fun getTitle(): String = "Dodaj ćwiczenie"

    override fun getSearchHint(): String = "Wyszukaj ćwiczenie.."

    override suspend fun performSearch(query: String): List<ExerciseDto> {
        return NetworkModule.api.getExercisesByName(name = query)
    }

    override fun createResultView(item: ExerciseDto): View {
        val view = LayoutInflater.from(requireContext())
            .inflate(android.R.layout.simple_list_item_2, llSearchResults, false)

        view.findViewById<TextView>(android.R.id.text1).text = item.name ?: "Bez nazwy"

        val muscleInfo = item.muscleIds?.joinToString(", ") ?: "Brak danych"
        val metsInfo = item.mets?.let { "METS: $it" } ?: ""
        view.findViewById<TextView>(android.R.id.text2).text = "$muscleInfo${if (metsInfo.isNotEmpty()) " • $metsInfo" else ""}"

        return view
    }

    override fun onItemSelected(item: ExerciseDto) {
        showSetDetailsDialog(item)
    }

    private fun showSetDetailsDialog(exercise: ExerciseDto) {
        val dialogLayout = android.widget.LinearLayout(requireContext()).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(48, 24, 48, 24)
        }


        val etWeight = android.widget.EditText(requireContext()).apply {
            hint = "Obciążenie (kg)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        }

        val etDuration = android.widget.EditText(requireContext()).apply {
            hint = "Czas trwania (min)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }


        dialogLayout.addView(etWeight)
        dialogLayout.addView(etDuration)

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Szczegóły: ${exercise.name ?: "Ćwiczenie"}")
            .setView(dialogLayout)
            .setPositiveButton("Dodaj") { _, _ ->
                val weight = etWeight.text.toString().toDoubleOrNull()
                val duration = etDuration.text.toString().toIntOrNull()

//                saveExercise(exercise.id, weight, duration)
            }
            .setNegativeButton("Anuluj", null)
            .show()
    }



}