package com.example.fithub

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import android.widget.Toast
import androidx.compose.ui.semantics.text
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.example.fithub.NetworkModule
import kotlinx.coroutines.launch


class AddMealDialogFragment : DialogFragment() {

    interface OnMealAddedListener {
        fun onMealAdded()
    }

    var onMealAddedListener: OnMealAddedListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val mealType = arguments?.getString("mealType") ?: "Posiłek"

        val mainLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }
        val etSearch = EditText(requireContext()).apply {
            hint = "Wyszukaj produkt.."
            setSingleLine(true)
        }

        val btnScanner = Button(requireContext()).apply {
            text = "Zeskanuj kod kreskowy"
            setOnClickListener{
                Toast.makeText(requireContext(), "Uruchamiam skaner..", Toast.LENGTH_SHORT).show()
            }
        }

        val scrollView = ScrollView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                400 // maksymalna wysokość w px
            )
        }

        val llSearchResults = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
        }

        scrollView.addView(llSearchResults)

        mainLayout.addView(btnScanner)
        mainLayout.addView(etSearch)
        mainLayout.addView(scrollView)

        etSearch.doAfterTextChanged { text ->
            val query = text.toString().trim()
            if (query.length >= 2) {
                searchFoods(query, llSearchResults)
            } else {
                llSearchResults.removeAllViews()    }
        }

        return AlertDialog.Builder(requireContext())
            .setTitle("Dodaj $mealType")
            .setView(mainLayout)
            .setNegativeButton("Gotowe") { _, _ ->
            }
            .create()
    }

    private fun searchFoods(query: String, container: LinearLayout) {
        lifecycleScope.launch {
            try {
                val foods = NetworkModule.api.getFoods(search = query)
                container.removeAllViews()

                foods.foods.forEach { food ->
                    val foodView = LinearLayout(requireContext()).apply {
                        orientation = LinearLayout.HORIZONTAL
                        setPadding(16, 16, 16, 16)
                        setBackgroundResource(android.R.drawable.list_selector_background)
                        isClickable = true
                    }

                    val foodInfo = LinearLayout(requireContext()).apply {
                        orientation = LinearLayout.VERTICAL
                        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    }

                    val tvName = TextView(requireContext()).apply {
                        text = food.name
                        textSize = 16f
                        setTypeface(null, android.graphics.Typeface.BOLD)
                    }

                    val tvNutrition = TextView(requireContext()).apply {
                        text = "${food.nutritionPer100g.calories} kcal/100g"
                        textSize = 12f
                        setTextColor(resources.getColor(android.R.color.darker_gray, null))
                    }

                    val btnAdd = Button(requireContext()).apply {
                        text = "+"
                        layoutParams = LinearLayout.LayoutParams(100, LinearLayout.LayoutParams.WRAP_CONTENT)
                        setOnClickListener {
                            showQuantityDialog(food.name, food.id)
                        }
                    }



                    foodInfo.addView(tvName)
                    foodInfo.addView(tvNutrition)
                    foodView.addView(foodInfo)
                    foodView.addView(btnAdd)

                    container.addView(foodView)
                }
            }
            catch(e: Exception) {
                Toast.makeText(requireContext(), "Błąd wyszukiwania: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showQuantityDialog(foodName: String, foodId: String) {
        val etQuantity = EditText(requireContext()).apply {
            hint = "Ilość w gramach"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText("100")
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Dodaj $foodName")
            .setMessage("Podaj ilość:")
            .setView(etQuantity)
            .setPositiveButton("Dodaj") { _, _ ->
                val quantity = etQuantity.text.toString().toDoubleOrNull() ?: 100.0
                addFoodToMeal(foodId, quantity)
            }
            .setNegativeButton("Anuluj", null)
            .show()
    }

    private fun addFoodToMeal(foodId: String, quantity: Double) {
        val userId = arguments?.getString("userId")
        val date = arguments?.getString("date")
        val mealType = arguments?.getString("mealType")

        if(userId.isNullOrEmpty() || date.isNullOrEmpty() || mealType.isNullOrEmpty()){
            Toast.makeText(requireContext(), "Bład danych użytkownika", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try{
                val foodItem = com.example.fithub.data.FoodItemDto(
                    foodId = foodId,
                    quantity = quantity
                )

                val meal = com.example.fithub.data.MealDto(
                    name = mealType,
                    foods = listOf(foodItem)
                )

                val addMealData = com.example.fithub.data.AddMealDto(meal = meal)

                NetworkModule.api.addMeal(
                    userId = userId,
                    date = date,
                    addMealDto = addMealData
                )
                Toast.makeText(requireContext(), "Dodano $quantity g produktu", Toast.LENGTH_SHORT).show()
                onMealAddedListener?.onMealAdded()
                dismiss()
            }
            catch (e: Exception){
                android.util.Log.e("AddMealDialog", "Błąd dodawania posiłku", e)
                Toast.makeText(requireContext(), "Błąd dodawania posiłku: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}



