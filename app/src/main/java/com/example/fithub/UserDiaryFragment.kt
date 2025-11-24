package com.example.fithub

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.fithub.data.FoodDto
import com.example.fithub.data.MealWithFoodsDto
import com.example.fithub.data.PointsManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class UserDiaryFragment : Fragment(R.layout.fragment_user_diary), AddMealDialogFragment.OnMealAddedListener {

    private lateinit var llDaysContainer: LinearLayout
    private lateinit var hsvWeek: HorizontalScrollView
    private var selectedDate = Calendar.getInstance()
    private lateinit var btnBreakfast: ImageButton
    private lateinit var btnLunch: ImageButton
    private lateinit var btnDinner: ImageButton
    private lateinit var llBreakfastMeals: LinearLayout
    private lateinit var llLunchMeals: LinearLayout
    private lateinit var llDinnerMeals: LinearLayout
    private lateinit var tvDailyTotal: TextView
    private lateinit var tvBreakfast: TextView
    private lateinit var tvLunch: TextView
    private lateinit var tvDinner: TextView
    private lateinit var tvTraining: TextView
    private lateinit var llTraining: LinearLayout
    private lateinit var btnTraining: ImageButton
    private var breakfastCalories = 0.0
    private var breakfastProtein = 0.0
    private var breakfastFat = 0.0
    private var breakfastCarbs = 0.0
    private var lunchCalories = 0.0
    private var lunchProtein = 0.0
    private var lunchFat = 0.0
    private var lunchCarbs = 0.0
    private var dinnerCalories = 0.0
    private var dinnerProtein = 0.0
    private var dinnerFat = 0.0
    private var dinnerCarbs = 0.0
    private var dailyTotalCalories = 0.0
    private var dailyTotalProtein = 0.0
    private var dailyTotalFat = 0.0
    private var dailyTotalCarbs = 0.0
    private var loadedBreakfast = false
    private var loadedLunch = false
    private var loadedDinner = false
    private var currentLoadId = 0
    private var trainingCalories = 0.0
    private val currentUserId = "68cbc06e6cdfa7faa8561f82"
    enum class TrainingDeleteItemType{
        NONE,
        SINGLE_EXERCISE,
        FULL_TRAINING
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        llDaysContainer = view.findViewById(R.id.llDaysContainer)
        hsvWeek = view.findViewById(R.id.hsvWeek)
        llBreakfastMeals = view.findViewById(R.id.llBreakfastMeals)
        llLunchMeals = view.findViewById(R.id.llLunchMeals)
        llDinnerMeals = view.findViewById(R.id.llDinnerMeals)
        llTraining = view.findViewById(R.id.llTraining)
        tvDailyTotal = view.findViewById<TextView>(R.id.tvDailyTotal)
        tvBreakfast = view.findViewById(R.id.tvBreakfast)
        tvLunch = view.findViewById(R.id.tvLunch)
        tvDinner = view.findViewById(R.id.tvDinner)
        tvTraining = view.findViewById(R.id.tvTraining)
        btnBreakfast = view.findViewById(R.id.btnAddBreakfast)
        btnLunch = view.findViewById(R.id.btnAddLunch)
        btnDinner = view.findViewById(R.id.btnAddDinner)
        btnTraining = view.findViewById(R.id.btnTraining)


        initDaysView()

        btnBreakfast.setOnClickListener {
            openMealDialog("Śniadanie")

        }
        btnLunch.setOnClickListener {
            openMealDialog("Obiad")
        }
        btnDinner.setOnClickListener {
            openMealDialog("Kolacja")
        }
        btnTraining.setOnClickListener {
            openExerciseDialog()
        }
    }

    private fun openMealDialog(mealType: String){
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formattedDate = dateFormat.format(selectedDate.time)

        val dialog = AddMealDialogFragment().apply{
            arguments = Bundle().apply {
                putString("mealType", mealType)
                putString("userId", currentUserId)
                putString("date", formattedDate)
            }
        }
        dialog.onMealAddedListener = this
        dialog.show(parentFragmentManager, "AddMealDialog")
    }

    override fun onMealAdded() {
        loadDataForDate(selectedDate)
    }

    private fun addMealToList
                (container: LinearLayout,
                 mealName: String,
                 itemId: String,
                 protein: Int = 0,
                 fat: Int = 0,
                 carbs: Int = 0,
                 calories: Int = 0,
                 currentQuantity: Double,
                 foodDto: FoodDto,
                 trainingType: TrainingDeleteItemType = TrainingDeleteItemType.NONE
    ) {
        val mealView = LayoutInflater.from(requireContext())
            .inflate(R.layout.item_meal, container, false)

        val tvMealName = mealView.findViewById<TextView>(R.id.tvMealName)
        val tvMealProtein = mealView.findViewById<TextView>(R.id.tvProtein)
        val tvMealFat = mealView.findViewById<TextView>(R.id.tvFat)
        val tvMealCarbs = mealView.findViewById<TextView>(R.id.tvCarbs)
        val tvMealCalories = mealView.findViewById<TextView>(R.id.tvMealCalories)
        val btnDeleteMeal = mealView.findViewById<ImageButton>(R.id.btnDeleteMeal)

        tvMealName.text = mealName
        tvMealProtein.text = "$protein P"
        tvMealFat.text = "$fat F"
        tvMealCarbs.text = "$carbs C"
        tvMealCalories.text = "$calories Kcal"

        btnDeleteMeal.setOnClickListener {
            deleteFoodByItemId(itemId, trainingType)
        }


        mealView.setOnClickListener {
            when (trainingType) {
                TrainingDeleteItemType.NONE ->
                    showEditQuantityDialog(mealName, itemId, currentQuantity, foodDto)
                else ->
                    Toast.makeText(requireContext(), "Nie można edytować treningu", Toast.LENGTH_SHORT).show()
            }
        }

        container.addView(mealView)
    }

    private fun showEditQuantityDialog(foodName: String, itemId: String, currentQuantity: Double, food: FoodDto) {
        val dialogLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 24)
        }

        val inputLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
        }

        val etQuantity = android.widget.EditText(requireContext()).apply {
            hint = "Ilość"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText(String.format("%.0f", currentQuantity))
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
            setPadding(0, 24, 0, 0)
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

        val dialog = android.app.AlertDialog.Builder(requireContext())
            .setTitle("Edytuj $foodName")
            .setView(dialogLayout)
            .setPositiveButton("Zapisz") { _, _ ->
                val newQuantity = etQuantity.text.toString().toDoubleOrNull() ?: currentQuantity
                updateFoodQuantity(itemId, newQuantity)
            }
            .setNegativeButton("Anuluj", null)
            .create()

        dialog.setOnShowListener {
            etQuantity.requestFocus()
            etQuantity.selectAll()

            etQuantity.postDelayed({
                val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                imm.showSoftInput(etQuantity, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
            }, 100)
        }

        dialog.show()
    }

    private fun updateFoodQuantity(itemId: String, newQuantity: Double) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateStr = dateFormat.format(selectedDate.time)

        lifecycleScope.launch {
            try {
                val updateDto = com.example.fithub.data.UpdateFoodQuantityDto(quantity = newQuantity)
                NetworkModule.api.updateFoodQuantity(
                    userId = currentUserId,
                    date = dateStr,
                    itemId = itemId,
                    request = updateDto
                )
                Toast.makeText(requireContext(), "Zaktualizowano ilość", Toast.LENGTH_SHORT).show()
                loadDataForDate(selectedDate)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Błąd aktualizacji: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initDaysView() {
        val currentMonth = selectedDate.get(Calendar.MONTH)
        val currentYear = selectedDate.get(Calendar.YEAR)

        val firstDayOfMonth = Calendar.getInstance().apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.MONTH, currentMonth)
            set(Calendar.DAY_OF_MONTH, 1)
        }

        val lastDayOfMonth = firstDayOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH)

        for (day in 1..lastDayOfMonth) {
            val dayCalendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, currentYear)
                set(Calendar.MONTH, currentMonth)
                set(Calendar.DAY_OF_MONTH, day)
            }

            addDayView(dayCalendar, day)
        }

        selectTodayAndScroll()
    }

    private fun addDayView(dayCalendar: Calendar, dayNumber: Int) {
        val dayView = LayoutInflater.from(requireContext())
            .inflate(R.layout.item_day, llDaysContainer, false)

        val tvDayLetter = dayView.findViewById<TextView>(R.id.tvDayLetter)
        val tvDayNumber = dayView.findViewById<TextView>(R.id.tvDayNumber)

        val dayOfWeek = dayCalendar.get(Calendar.DAY_OF_WEEK)
        tvDayLetter.text = getDayLetter(dayOfWeek)

        tvDayNumber.text = dayNumber.toString()

        dayView.setOnClickListener {
            selectDay(dayView, dayCalendar)
        }

        llDaysContainer.addView(dayView)
    }

    private fun getDayLetter(dayOfWeek: Int): String {
        return when (dayOfWeek) {
            Calendar.MONDAY -> "P"
            Calendar.TUESDAY -> "W"
            Calendar.WEDNESDAY -> "Ś"
            Calendar.THURSDAY -> "C"
            Calendar.FRIDAY -> "P"
            Calendar.SATURDAY -> "S"
            Calendar.SUNDAY -> "N"
            else -> "?"
        }
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun selectDay(dayView: View, dayCalendar: Calendar) {
        for (i in 0 until llDaysContainer.childCount) {
            llDaysContainer.getChildAt(i).isSelected = false
        }

        dayView.isSelected = true
        selectedDate = dayCalendar.clone() as Calendar

        loadDataForDate(selectedDate)
    }

    private fun selectTodayAndScroll() {
        val today = Calendar.getInstance()

        for (i in 0 until llDaysContainer.childCount) {
            val dayView = llDaysContainer.getChildAt(i)
            val dayCalendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, today.get(Calendar.YEAR))
                set(Calendar.MONTH, today.get(Calendar.MONTH))
                set(Calendar.DAY_OF_MONTH, i + 1)
            }

            if (isSameDay(dayCalendar, today)) {
                dayView.isSelected = true
                selectedDate = dayCalendar.clone() as Calendar

                dayView.post {
                    scrollToDay(dayView)
                }

                loadDataForDate(selectedDate)
                break
            }
        }
    }

    private fun scrollToDay(dayView: View) {
        val scrollViewWidth = hsvWeek.width
        val dayViewLeft = dayView.left
        val dayViewWidth = dayView.width

        val scrollX = dayViewLeft - (scrollViewWidth / 2) + (dayViewWidth / 2)

        hsvWeek.smoothScrollTo(scrollX.coerceAtLeast(0), 0)
    }


    private fun loadAllMealsForUser(userId: String, date: String) {
        val loadIdAtStart = currentLoadId

        lifecycleScope.launch {
            try {
                val dailyNutrition = NetworkModule.api.getDailyNutrition(userId, date)

                if (loadIdAtStart != currentLoadId) return@launch

                val breakfastMeals = dailyNutrition.meals.filter { meal ->
                    meal.name.lowercase().contains("śniadanie")
                }
                val lunchMeals = dailyNutrition.meals.filter { meal ->
                    meal.name.lowercase().contains("obiad")
                }
                val dinnerMeals = dailyNutrition.meals.filter { meal ->
                    meal.name.lowercase().contains("kolacja")
                }
                val trainingMeals = dailyNutrition.meals.filter { meal ->
                    meal.name.lowercase().contains("trening")
                }

                // wyswietlanie posilkow
                displayMeals(breakfastMeals, "śniadanie")
                displayMeals(lunchMeals, "obiad")
                displayMeals(dinnerMeals, "kolacja")
                displayMeals(trainingMeals, "trening")

                // flagi
                loadedBreakfast = true
                loadedLunch = true
                loadedDinner = true

                // aktualizacja totals
                if (loadIdAtStart == currentLoadId) {
                    updateDailyTotals(dailyNutrition.dailyTotals.calorieGoal)
                }

            } catch (e: Exception) {
                if (loadIdAtStart != currentLoadId) return@launch
                Toast.makeText(requireContext(), "Błąd: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayMeals(meals: List<MealWithFoodsDto>, mealType: String) {
        val container = when(mealType.lowercase()) {
            "śniadanie" -> llBreakfastMeals
            "obiad" -> llLunchMeals
            "kolacja" -> llDinnerMeals
            "trening" -> llTraining
            else -> return
        }

        container.removeAllViews()

        when (mealType.lowercase()) {
            "śniadanie" -> {
                breakfastCalories = 0.0; breakfastProtein = 0.0; breakfastFat = 0.0; breakfastCarbs = 0.0
            }
            "obiad" -> {
                lunchCalories = 0.0; lunchProtein = 0.0; lunchFat = 0.0; lunchCarbs = 0.0
            }
            "kolacja" -> {
                dinnerCalories = 0.0; dinnerProtein = 0.0; dinnerFat = 0.0; dinnerCarbs = 0.0
            }
            "trening" -> {
                trainingCalories = 0.0
            }
        }

        meals.forEach { meal ->
            meal.foods.forEach { foodItem ->
                val food = foodItem.foodId
                val quantity = foodItem.quantity / 100.0

                val protein = (food.nutritionPer100g.protein * quantity).roundToInt()
                val fat = (food.nutritionPer100g.fat * quantity).roundToInt()
                val carbs = (food.nutritionPer100g.carbs * quantity).roundToInt()
                val calories = (food.nutritionPer100g.calories * quantity).roundToInt()

                when (mealType.lowercase()) {
                    "śniadanie" -> {
                        breakfastCalories += calories
                        breakfastProtein += protein
                        breakfastFat += fat
                        breakfastCarbs += carbs
                    }

                    "obiad" -> {
                        lunchCalories += calories
                        lunchProtein += protein
                        lunchFat += fat
                        lunchCarbs += carbs
                    }

                    "kolacja" -> {
                        dinnerCalories += calories
                        dinnerProtein += protein
                        dinnerFat += fat
                        dinnerCarbs += carbs
                    }

                    "trening" -> {
                        trainingCalories += calories
                    }
                }

                val itemTrainingType = if (mealType.lowercase() == "trening") {
                    when (foodItem.foodId.brand) {
                        "TrainingPlan" -> TrainingDeleteItemType.FULL_TRAINING
                        "SingleExercise" -> TrainingDeleteItemType.SINGLE_EXERCISE
                        else -> TrainingDeleteItemType.SINGLE_EXERCISE //stare wpisy
                    }
                } else {
                    TrainingDeleteItemType.NONE
                }

                if (mealType.lowercase() == "trening") {
                    addMealToList(
                        container = container,
                        mealName = food.name,
                        itemId = foodItem.itemId,
                        calories = calories,
                        currentQuantity = foodItem.quantity,
                        foodDto = food,
                        trainingType = itemTrainingType
                    )
                } else {
                    addMealToList(
                        container = container,
                        mealName = "${food.name} (${foodItem.quantity.toInt()}g)",
                        itemId = foodItem.itemId,
                        protein = protein,
                        fat = fat,
                        carbs = carbs,
                        calories = calories,
                        currentQuantity = foodItem.quantity,
                        foodDto = food,
                        trainingType = TrainingDeleteItemType.NONE
                    )
                }
            }
        }

        when (mealType.lowercase()) {
            "śniadanie" -> tvBreakfast.text = "Śniadanie (${breakfastCalories.roundToInt()} kcal, ${breakfastProtein.roundToInt()} P, ${breakfastFat.roundToInt()} F, ${breakfastCarbs.roundToInt()} C)"
            "obiad" -> tvLunch.text = "Obiad (${lunchCalories.roundToInt()} kcal, ${lunchProtein.roundToInt()} P, ${lunchFat.roundToInt()} F, ${lunchCarbs.roundToInt()} C)"
            "kolacja" -> tvDinner.text = "Kolacja (${dinnerCalories.roundToInt()} kcal, ${dinnerProtein.roundToInt()} P, ${dinnerFat.roundToInt()} F, ${dinnerCarbs.roundToInt()} C)"
            "trening" -> tvTraining.text = "Trening (${kotlin.math.abs(trainingCalories).roundToInt()} kcal spalono)"
        }
    }


    private fun loadDataForDate(date: Calendar) {
        currentLoadId++
        clearAllMacrosAndUi()

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formattedDate = dateFormat.format(date.time)

        loadAllMealsForUser(currentUserId, formattedDate)
    }

    private fun clearAllMacrosAndUi() {
        breakfastCalories = 0.0; breakfastProtein = 0.0; breakfastFat = 0.0; breakfastCarbs = 0.0
        lunchCalories     = 0.0; lunchProtein     = 0.0; lunchFat     = 0.0; lunchCarbs     = 0.0
        dinnerCalories    = 0.0; dinnerProtein    = 0.0; dinnerFat    = 0.0; dinnerCarbs    = 0.0
        dailyTotalCalories = 0.0; dailyTotalProtein = 0.0; dailyTotalFat = 0.0; dailyTotalCarbs = 0.0
        trainingCalories = 0.0


        loadedBreakfast = false
        loadedLunch = false
        loadedDinner = false

        llBreakfastMeals.removeAllViews()
        llLunchMeals.removeAllViews()
        llDinnerMeals.removeAllViews()
        llTraining.removeAllViews()

        tvBreakfast.text = "Śniadanie"
        tvLunch.text = "Obiad"
        tvDinner.text = "Kolacja"
        tvDailyTotal.text = ""
        tvTraining.text = "Trening"
    }

    private fun updateDailyTotals(calorieGoal: Double) {
        if (loadedBreakfast && loadedLunch && loadedDinner) {
            dailyTotalCalories = breakfastCalories + lunchCalories + dinnerCalories + trainingCalories
            dailyTotalProtein  = breakfastProtein  + lunchProtein  + dinnerProtein
            dailyTotalFat      = breakfastFat      + lunchFat      + dinnerFat
            dailyTotalCarbs    = breakfastCarbs    + lunchCarbs    + dinnerCarbs

            tvDailyTotal.text = "${dailyTotalCalories.roundToInt()} / ${calorieGoal.roundToInt()} kcal, " +
                    "${dailyTotalProtein.roundToInt()} P, ${dailyTotalFat.roundToInt()} F, ${dailyTotalCarbs.roundToInt()} C"
        }
    }

    private fun deleteFoodByItemId(itemId: String, trainingType: TrainingDeleteItemType) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateStr = dateFormat.format(selectedDate.time)

        lifecycleScope.launch {
            try {
                NetworkModule.api.deleteFoodByItemId(
                    userId = currentUserId,
                    date = dateStr,
                    itemId = itemId
                )

                try {
                    val actionType = when (trainingType) {
                        TrainingDeleteItemType.FULL_TRAINING -> PointsManager.ActionType.TRAINING_FULL
                        TrainingDeleteItemType.SINGLE_EXERCISE -> PointsManager.ActionType.TRAINING
                        TrainingDeleteItemType.NONE -> PointsManager.ActionType.MEAL
                    }

                    PointsManager.removePoints(currentUserId, actionType)
                    Log.d("UserDiary", "Odjęto punkty za: $actionType")

                } catch (e: Exception) {
                    Log.e("UserDiary", "Błąd odejmowania punktów: ${e.message}")
                }

                loadDataForDate(selectedDate)
                val message = when(trainingType) {
                    TrainingDeleteItemType.FULL_TRAINING -> "Usunięto plan treningowy"
                    TrainingDeleteItemType.SINGLE_EXERCISE -> "Usunięto ćwiczenie"
                    TrainingDeleteItemType.NONE -> "Usunięto produkt"
                }
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Błąd usuwania: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun openExerciseDialog() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formattedDate = dateFormat.format(selectedDate.time)

        val dialog = AddExerciseDialogFragment().apply {
            arguments = Bundle().apply {
                putString("userId", currentUserId)
                putString("date", formattedDate)
            }
        }
        dialog.onExerciseAddedListener = object : AddExerciseDialogFragment.OnExerciseAddedListener {
            override fun onExerciseAdded() {
                loadDataForDate(selectedDate)
            }
        }
        dialog.show(parentFragmentManager, "AddExerciseDialog")
    }

}