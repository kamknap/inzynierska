package com.example.fithub

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.Button
import android.view.Gravity
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.fithub.data.CreateUserExercisePlanDto
import kotlinx.coroutines.launch

class SelectExercisePlanDialogFragment : DialogFragment() {

    interface OnPlanSelectedListener {
        fun onPlanSelected(planId: String, planName: String)
    }

    var onPlanSelectedListener: OnPlanSelectedListener? = null
    private var plansContainer: LinearLayout? = null
    private var userId: String? = null
    private var currentPlanId: String? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        userId = arguments?.getString("userId") ?: return super.onCreateDialog(savedInstanceState)
        currentPlanId = arguments?.getString("currentPlanId")

        val mainLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        plansContainer = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
        }

        mainLayout.addView(plansContainer)

        loadPlans()

        return AlertDialog.Builder(requireContext())
            .setTitle("Wybierz plan treningowy")
            .setView(mainLayout)
            .setPositiveButton("Nowy plan") { _, _ ->
                showCreatePlanDialog(userId!!)
            }
            .setNegativeButton("Anuluj", null)
            .create()
    }

    private fun loadPlans() {
        lifecycleScope.launch {
            try {
                val plans = NetworkModule.api.getUserExercisePlans(userId!!)

                plansContainer?.removeAllViews()

                plans.forEach { plan ->
                    val planItemLayout = LinearLayout(requireContext()).apply {
                        orientation = LinearLayout.HORIZONTAL
                        setPadding(16, 16, 16, 16)
                        gravity = Gravity.CENTER_VERTICAL
                    }

                    val planView = TextView(requireContext()).apply {
                        text = plan.planName
                        textSize = 18f
                        setPadding(8, 8, 8, 8)
                        layoutParams = LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            1f
                        )
                        setOnClickListener {
                            onPlanSelectedListener?.onPlanSelected(plan.id, plan.planName)
                            dismiss()
                        }
                    }

                    val deleteButton = Button(requireContext()).apply {
                        text = "Usuń"
                        textSize = 14f
                        setPadding(16, 8, 16, 8)
                        setOnClickListener {
                            showDeleteConfirmationDialog(plan.id, plan.planName)
                        }
                    }

                    planItemLayout.addView(planView)
                    planItemLayout.addView(deleteButton)
                    plansContainer?.addView(planItemLayout)
                }

                if (plans.isEmpty()) {
                    val emptyView = TextView(requireContext()).apply {
                        text = "Brak planów treningowych"
                        setPadding(24, 24, 24, 24)
                    }
                    plansContainer?.addView(emptyView)
                }

            } catch (e: Exception) {
                if (isAdded && context != null) {
                    Toast.makeText(requireContext(), "Błąd: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showDeleteConfirmationDialog(planId: String, planName: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Usuwanie planu")
            .setMessage("Czy na pewno chcesz usunąć plan \"$planName\"?")
            .setPositiveButton("Usuń") { _, _ ->
                deletePlan(planId, planName)
            }
            .setNegativeButton("Anuluj", null)
            .show()
    }

    private fun deletePlan(planId: String, planName: String) {
        lifecycleScope.launch {
            try {
                NetworkModule.api.deleteUserExercisePlan(planId)

                if (isAdded && context != null) {
                    Toast.makeText(requireContext(), "Usunięto plan: $planName", Toast.LENGTH_SHORT).show()

                    // Jeśli usunięto aktualnie wybrany plan, wybierz inny
                    if (planId == currentPlanId) {
                        val remainingPlans = NetworkModule.api.getUserExercisePlans(userId!!)
                        if (remainingPlans.isNotEmpty()) {
                            // Automatycznie wybierz pierwszy dostępny plan
                            onPlanSelectedListener?.onPlanSelected(remainingPlans[0].id, remainingPlans[0].planName)
                        } else {
                            // Brak planów - UserTrainingFragment utworzy domyślny
                            onPlanSelectedListener?.onPlanSelected("", "")
                        }
                    }

                    loadPlans() // Odśwież listę planów
                }
            } catch (e: Exception) {
                if (isAdded && context != null) {
                    Toast.makeText(requireContext(), "Błąd usuwania: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showCreatePlanDialog(userId: String) {
        val input = EditText(requireContext()).apply {
            hint = "Nazwa planu"
        }

        val createDialog = AlertDialog.Builder(requireContext())
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
            .create()

        createDialog.show()
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

                if (isAdded && context != null) {
                    Toast.makeText(requireContext(), "Utworzono: ${newPlan.planName}", Toast.LENGTH_SHORT).show()
                    onPlanSelectedListener?.onPlanSelected(newPlan.id, newPlan.planName)
                    dismiss()
                }
            } catch (e: Exception) {
                if (isAdded && context != null) {
                    Toast.makeText(requireContext(), "Błąd: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        fun newInstance(userId: String, currentPlanId: String? = null): SelectExercisePlanDialogFragment {
            return SelectExercisePlanDialogFragment().apply {
                arguments = Bundle().apply {
                    putString("userId", userId)
                    putString("currentPlanId", currentPlanId)
                }
            }
        }
    }
}