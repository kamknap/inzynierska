package com.example.fithub

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.fithub.data.CreateUserExercisePlanDto
import kotlinx.coroutines.launch

class SelectExercisePlanDialogFragment : DialogFragment() {

    interface OnPlanSelectedListener {
        fun onPlanSelected(planId: String, planName: String)
    }

    var onPlanSelectedListener: OnPlanSelectedListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val userId = arguments?.getString("userId") ?: return super.onCreateDialog(savedInstanceState)

        val mainLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        val plansContainer = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
        }

        mainLayout.addView(plansContainer)

        lifecycleScope.launch {
            try {
                val plans = NetworkModule.api.getUserExercisePlans(userId)

                plans.forEach { plan ->
                    val planView = TextView(requireContext()).apply {
                        text = plan.planName
                        textSize = 18f
                        setPadding(24, 24, 24, 24)
                        setOnClickListener {
                            onPlanSelectedListener?.onPlanSelected( plan.id,plan.planName)
                            dismiss()
                        }
                    }
                    plansContainer.addView(planView)
                }

                if (plans.isEmpty()) {
                    val emptyView = TextView(requireContext()).apply {
                        text = "Brak planów treningowych"
                        setPadding(24, 24, 24, 24)
                    }
                    plansContainer.addView(emptyView)
                }

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Błąd: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        return AlertDialog.Builder(requireContext())
            .setTitle("Wybierz plan treningowy")
            .setView(mainLayout)
            .setPositiveButton("Nowy plan") { _, _ ->
                showCreatePlanDialog(userId)
            }
            .setNegativeButton("Anuluj", null)
            .create()
    }

    private fun showCreatePlanDialog(userId: String) {
        val input = EditText(requireContext()).apply {
            hint = "Nazwa planu"
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Nowy plan treningowy")
            .setView(input)
            .setPositiveButton("Utwórz") { _, _ ->
                val planName = input.text.toString().trim()
                if (planName.isNotEmpty()) {
                    createPlan(userId, planName)
                } else {
                    Toast.makeText(requireContext(), "Podaj nazwę planu", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Anuluj", null)
            .show()
    }

    private fun createPlan(userId: String, planName: String) {
        lifecycleScope.launch {
            try {
                val newPlan = NetworkModule.api.createUserExercisePlan(
                    CreateUserExercisePlanDto(
                        userId = userId,
                        planName = planName
                    )
                )
                onPlanSelectedListener?.onPlanSelected(newPlan.id, newPlan.planName)
                Toast.makeText(requireContext(), "Utworzono: ${newPlan.planName}", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Błąd: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        fun newInstance(userId: String): SelectExercisePlanDialogFragment {
            return SelectExercisePlanDialogFragment().apply {
                arguments = Bundle().apply {
                    putString("userId", userId)
                }
            }
        }
    }
}