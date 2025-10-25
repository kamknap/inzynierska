package com.example.fithub

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import android.widget.Toast
import androidx.compose.material3.Text
import androidx.compose.ui.semantics.text
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.example.fithub.NetworkModule
import com.example.fithub.data.CreateFoodDto
import com.example.fithub.data.FoodDto
import com.example.fithub.data.NutritionData
import com.example.fithub.data.OpenFoodFactsProduct
import kotlinx.coroutines.launch
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import android.view.inputmethod.InputMethodManager


class AddMealDialogFragment : DialogFragment() {


    interface OnMealAddedListener {
        fun onMealAdded()
    }

    var onMealAddedListener: OnMealAddedListener? = null
    private var searchJob: kotlinx.coroutines.Job? = null

    private val barcodeLauncher = registerForActivityResult(ScanContract()){ result ->
        if(result.contents != null){
            searchByBarcode(result.contents)
        }
        else{
            Toast.makeText(requireContext(), "Nie znaleziono kodu kreskowego", Toast.LENGTH_SHORT).show()
        }

    }


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
                val options = ScanOptions().apply {
                    setDesiredBarcodeFormats(ScanOptions.PRODUCT_CODE_TYPES)
                    setPrompt("Zeskanuj kod kreskowy")
                    setBeepEnabled(false)
                    setOrientationLocked(true)
                }
                barcodeLauncher.launch(options)
            }
        }

        val scrollView = ScrollView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                400
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
            //anulowanie poprzedniego wyszukiwania
            searchJob?.cancel()
            if (query.length >= 2) {
//                opoznienie 500ms przed wyszukiwaniem
                searchJob = lifecycleScope.launch {
                    kotlinx.coroutines.delay(500)
                    searchFoods(query, llSearchResults)
                }
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
        Log.d("AddMealDialog", "Wyszukiwanie: $query")
        lifecycleScope.launch {
            try {
                val localFoods = NetworkModule.api.getFoods(search = query)
                val offFoods = NetworkModule.offApi.searchProducts(query = query, limit = 10)
                var foodDtoList = listOf<FoodDto>()

                if(offFoods.products?.isNotEmpty() == true){
                    foodDtoList = offFoods.products.map { product ->
                        // DODAJ LOGI DO DEBUGOWANIA
                        Log.d("OpenFoodFacts", "===== Produkt =====")
                        Log.d("OpenFoodFacts", "Nazwa: ${product.product_name}")
                        Log.d("OpenFoodFacts", "Nutriments: ${product.nutriments}")
                        Log.d("OpenFoodFacts", "Kalorie: ${product.nutriments?.energy_kcal_100g}")
                        Log.d("OpenFoodFacts", "Białko: ${product.nutriments?.proteins_100g}")

                        val mapped = mapOpenFoodFactsToFood(product)
                        mapped
                    }
                }

                val foodList = localFoods.foods + foodDtoList

                container.removeAllViews()

                foodList.forEach { food ->
                    val foodView = LinearLayout(requireContext()).apply {
                        orientation = LinearLayout.HORIZONTAL
                        setPadding(16, 16, 16, 16)
                        setBackgroundResource(android.R.drawable.list_selector_background)
                        isClickable = true
                        setOnClickListener {
                            showQuantityDialog(food.name, food.id, food)
                        }
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
                        val caloriesText = if (food.nutritionPer100g.calories == 0.0) {
                            "Brak danych"
                        } else {
                            "${food.nutritionPer100g.calories} kcal/100g"
                        }
                        text = caloriesText
                        textSize = 12f
                        setTextColor(resources.getColor(android.R.color.darker_gray, null))

                        //wymuszenie widocznosci
                        visibility = View.VISIBLE
                    }



                    foodInfo.addView(tvName)
                    foodInfo.addView(tvNutrition)
                    foodView.addView(foodInfo)

                    container.addView(foodView)
                }
            }
            catch(e: Exception) {
                Log.e("AddMealDialog", "Błąd wyszukiwania", e)
                Toast.makeText(requireContext(), "Błąd wyszukiwania: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mapOpenFoodFactsToFood(offProduct: OpenFoodFactsProduct): FoodDto {
        val nutrition = offProduct.nutriments

        val calories = nutrition?.energyKcal100g
            ?: nutrition?.energy_kcal_100g
            ?: nutrition?.energy_100g
            ?: nutrition?.energy_kj_100g?.div(4.184)
            ?: run {
                val protein = nutrition?.proteins_100g ?: 0.0
                val carbs = nutrition?.carbohydrates_100g ?: 0.0
                val fat = nutrition?.fat_100g ?: 0.0
                (protein * 4) + (carbs * 4) + (fat * 9)
            }

        Log.d("OpenFoodFacts", "Obliczone kalorie: $calories")

        return FoodDto(
            id = offProduct.code ?: "",
            name = offProduct.product_name ?: "Nieznany produkt",
            brand = offProduct.brands,
            barcode = offProduct.code,
            nutritionPer100g = NutritionData(
                calories = calories,
                protein = nutrition?.proteins_100g ?: 0.0,
                fat = nutrition?.fat_100g ?: 0.0,
                carbs = nutrition?.carbohydrates_100g ?: 0.0,
                fiber = nutrition?.fiber_100g ?: 0.0,
                sugar = nutrition?.sugars_100g ?: 0.0,
                sodium = nutrition?.sodium_100g ?: 0.0
            ),
            category = "OpenFoodFacts",
            verified = false,
            addedBy = "OpenFoodFacts",
            createdAt = "",
            updatedAt = ""
        )
    }

    private fun showQuantityDialog(foodName: String, foodId: String, food: FoodDto) {

        val dialogLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48,24,48,24)
        }

        val inputLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
        }

        val etQuantity = EditText(requireContext()).apply {
            hint = "Ilość"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText("100")
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val unitTextView = TextView(requireContext()).apply {
            text = "g"
            textSize = 18F
        }

        inputLayout.addView(etQuantity)
        inputLayout.addView(unitTextView)

        val tvNutritionInfo = TextView(requireContext()).apply {
            textSize = 14f
            setPadding(0, 24, 0, 0) // Odstęp od góry
            setTextColor(resources.getColor(android.R.color.darker_gray, null))
        }

        val updateNutritionInfo = {
            val quantity = etQuantity.text.toString().toDoubleOrNull() ?: 0.0
            val factor = quantity / 100.0

            val calories = food.nutritionPer100g.calories * factor
            val protein = food.nutritionPer100g.protein * factor
            val carbs = food.nutritionPer100g.carbs * factor
            val fat = food.nutritionPer100g.fat * factor

            tvNutritionInfo.text = String.format(
                "Kalorie: %.1f kcal\nBiałko: %.1f g\nWęglowodany: %.1f g\nTłuszcze: %.1f g",
                calories, protein, carbs, fat
            )
        }

        etQuantity.doAfterTextChanged {
            updateNutritionInfo()
        }

        updateNutritionInfo()

        dialogLayout.addView(inputLayout)
        dialogLayout.addView(tvNutritionInfo)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Dodaj $foodName")
            .setView(dialogLayout)
            .setPositiveButton("Dodaj") { _, _ ->
                val quantity = etQuantity.text.toString().toDoubleOrNull() ?: 100.0
                addFoodToMeal(foodId, quantity, food)
            }
            .setNegativeButton("Anuluj", null)
            .create()

        dialog.setOnShowListener {
            etQuantity.requestFocus()
            etQuantity.selectAll()

            etQuantity.postDelayed({
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(etQuantity, InputMethodManager.SHOW_IMPLICIT)
            }, 100)
        }

        dialog.show()
    }

    private fun addFoodToMeal(foodId: String, quantity: Double, food: FoodDto) {
        val userId = arguments?.getString("userId")
        val date = arguments?.getString("date")
        val mealType = arguments?.getString("mealType")

        if(userId.isNullOrEmpty() || date.isNullOrEmpty() || mealType.isNullOrEmpty()){
            Toast.makeText(requireContext(), "Bład danych użytkownika", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                // sprawdzanie czy produkt istnieje w lokalnej bazie
                var actualFoodId = foodId
                val existingProduct = try{
                    NetworkModule.api.getFoodById(actualFoodId)
                } catch (e: Exception){
                    null
                }
                // jesli produktu nie ma w bazie to dodaje go
                    if(existingProduct == null || existingProduct.id.isNullOrEmpty()){
                        val createFoodDto = CreateFoodDto(
                            name = food.name,
                            brand = food.brand,
                            barcode = food.barcode,
                            nutritionPer100g = food.nutritionPer100g,
                            category = food.category,
                            addedBy = userId
                        )
                        val createdFood = NetworkModule.api.createFood(createFoodDto)
                        actualFoodId = createdFood.id
                        Log.d("AddMealDialog", "Produkt dodany z ID: $actualFoodId")
                    } else {
                        Log.d("AddMealDialog", "Produkt już istnieje z ID: ${existingProduct.id}")
                        actualFoodId = existingProduct.id
        }

                // jesli produkt znajduje sie w lokalnej bazie to dodaje go do posiłku
                    val foodItem = com.example.fithub.data.FoodItemDto(
                        foodId = actualFoodId,
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
                    Toast.makeText(
                        requireContext(),
                        "Dodano ${food.name} do $mealType",
                        Toast.LENGTH_SHORT
                    ).show()
                    onMealAddedListener?.onMealAdded()
                    dismiss()
                }
            catch (e: Exception){
                android.util.Log.e("AddMealDialog", "Błąd dodawania posiłku", e)
                Toast.makeText(requireContext(), "Błąd dodawania posiłku: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // TODO DODAC WALIDACJE DO SKANERA ORAZ NAPRAWIC MODEL 3D W INKSCAPE
    private fun searchByBarcode(barcode: String) {
        lifecycleScope.launch {
            try {
                Toast.makeText(requireContext(), "Szukam produktu: $barcode", Toast.LENGTH_SHORT).show()

                try {
                    val localProduct = NetworkModule.api.getFoodByBarcode(barcode)
                    showQuantityDialog(localProduct.name, localProduct.id, localProduct)
                    return@launch
                } catch (e: Exception) {
                    Log.d("BarcodeScanner", "Produkt nie znaleziony lokalnie, sprawdzam OFF")
                }

                val offProduct = NetworkModule.offBarcodeApi.getProductByBarcode(barcode)

                if (offProduct.status == 1 && offProduct.product != null) {
                    val mappedFood = mapOpenFoodFactsToFood(offProduct.product)
                    if (mappedFood.name == "Nieznany produkt"){
                        AlertDialog.Builder(requireContext())
                            .setTitle("Błąd")
                            .setMessage("Nie znaleziono produktu dla kodu kreskowego: $barcode. Spróbuj ponownie lub dodaj produkt przy pomocy ręcznego dodawania")
                            .setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .show()
                    }
                    else{
                        showQuantityDialog(mappedFood.name, mappedFood.id, mappedFood)
                    }
                } else {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Błąd")
                        .setMessage("Nie znaleziono produktu dla kodu kreskowego: $barcode")
                        .setPositiveButton("OK") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                }

            } catch (e: Exception) {
                Log.e("BarcodeScanner", "Błąd skanowania", e)
                Toast.makeText(
                    requireContext(),
                    "Błąd: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

}



