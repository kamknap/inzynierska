package com.example.fithub

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.fithub.data.MealWithFoodsDto
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class UserDiaryFragment : Fragment(R.layout.fragment_user_diary) {

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

    private val currentUserId = "68cbc06e6cdfa7faa8561f82"


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        llDaysContainer = view.findViewById(R.id.llDaysContainer)
        hsvWeek = view.findViewById(R.id.hsvWeek)
        llBreakfastMeals = view.findViewById(R.id.llBreakfastMeals)
        llLunchMeals = view.findViewById(R.id.llLunchMeals)
        llDinnerMeals = view.findViewById(R.id.llDinnerMeals)
        tvDailyTotal = view.findViewById<TextView>(R.id.tvDailyTotal)
        tvBreakfast = view.findViewById(R.id.tvBreakfast)
        tvLunch = view.findViewById(R.id.tvLunch)
        tvDinner = view.findViewById(R.id.tvDinner)
        btnBreakfast = view.findViewById(R.id.btnAddBreakfast)
        btnLunch = view.findViewById(R.id.btnAddLunch)
        btnDinner = view.findViewById(R.id.btnAddDinner)

        initDaysView()

        // przyciski dodawania jedzenia
        btnBreakfast.setOnClickListener {
            addFoodToDb(
                mealName = "Śniadanie",
                foodId = "66feabcd1234567890abcdb2",
                quantityGrams = 220.0
            )
        }
        btnLunch.setOnClickListener {
            addFoodToDb(
                mealName = "Obiad",
                foodId = "66feabcd1234567890abcdb2",
                quantityGrams = 220.0
            )
        }
        btnDinner.setOnClickListener {
            addFoodToDb(
                mealName = "Kolacja",
                foodId = "66feabcd1234567890abcdb2",
                quantityGrams = 220.0
            )
        }
    }


    private fun addMealToList(container: LinearLayout, mealName: String, protein: Int = 0, fat: Int = 0, carbs: Int = 0, calories: Int = 0) {
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

        // Usuwanie posilku
        btnDeleteMeal.setOnClickListener {
            container.removeView(mealView)
        }

        // TODO: Edycja posilku
        mealView.setOnClickListener {
            Toast.makeText(requireContext(), "Edytuj: $mealName", Toast.LENGTH_SHORT).show()
        }

        container.addView(mealView)
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


    private fun loadMealsForUser(userId: String, date: String, mealType: String) {
        val loadIdAtStart = currentLoadId

        lifecycleScope.launch {
            try {
                val dailyNutrition = NetworkModule.api.getDailyNutrition(userId, date)

                // jesli w miedzyczasie kliknieto inny dzien – porzuc te dane
                if (loadIdAtStart != currentLoadId) return@launch

                val matchingMeals = dailyNutrition.meals.filter { meal ->
                    meal.name.lowercase().contains(mealType.lowercase())
                }

                displayMeals(matchingMeals, mealType)

                // flaga dopiero po narysowaniu wszystkiego
                when (mealType.lowercase()) {
                    "śniadanie" -> loadedBreakfast = true
                    "obiad"     -> loadedLunch = true
                    "kolacja"   -> loadedDinner = true
                }

                if (loadIdAtStart == currentLoadId) maybeUpdateDailyTotals()

            } catch (e: Exception) {
                if (loadIdAtStart != currentLoadId) return@launch
                Toast.makeText(requireContext(), "Błąd: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayMeals(meals: List<MealWithFoodsDto>, mealType: String) {
        // szukanie odpowiedniego kontenera
        val container = when(mealType.lowercase()) {
            "śniadanie" -> llBreakfastMeals
            "obiad" -> llLunchMeals
            "kolacja" -> llDinnerMeals
            else -> return
        }

        // czyszczenie starych posilkow
        container.removeAllViews()

        // dodawanie pojedynczego posilku
        meals.forEach { meal ->
            meal.foods.forEach { foodItem ->


                val food = foodItem.foodId
                val quantity = foodItem.quantity / 100.0
                val qtyLabel = foodItem.quantity.asGramsLabel()

                val mealCalories = food.nutritionPer100g.calories * quantity
                val mealProtein = food.nutritionPer100g.protein * quantity
                val mealFat = food.nutritionPer100g.fat * quantity
                val mealCarbs = food.nutritionPer100g.carbs * quantity

                addMealToList(
                    container = container,
                    mealName = "- ${food.name} (${qtyLabel})",
                    protein = kotlin.math.round(mealProtein).toInt(),
                    fat = kotlin.math.round(mealFat).toInt(),
                    carbs = kotlin.math.round(mealCarbs).toInt(),
                    calories = kotlin.math.round(mealCalories).toInt()
                )

                // sumowanie makroskladnikow
                when (mealType.lowercase()){
                    "śniadanie"->{
                        breakfastCalories += mealCalories
                        breakfastProtein += mealProtein
                        breakfastFat += mealFat
                        breakfastCarbs += mealCarbs
                    }
                    "obiad"->{
                        lunchCalories += mealCalories
                        lunchProtein += mealProtein
                        lunchFat += mealFat
                        lunchCarbs += mealCarbs
                    }
                    "kolacja"->{
                        dinnerCalories += mealCalories
                        dinnerProtein += mealProtein
                        dinnerFat += mealFat
                        dinnerCarbs += mealCarbs
                    }
                }
            }
        }

        when (mealType.lowercase()) {
            "śniadanie" -> tvBreakfast.text = "Śniadanie (${breakfastCalories.roundToInt()} kcal, ${breakfastProtein.roundToInt()} P, ${breakfastFat.roundToInt()} F, ${breakfastCarbs.roundToInt()} C)"
            "obiad" -> tvLunch.text = "Obiad (${lunchCalories.roundToInt()} kcal, ${lunchProtein.roundToInt()} P, ${lunchFat.roundToInt()} F, ${lunchCarbs.roundToInt()} C)"
            "kolacja" -> tvDinner.text = "Kolacja (${dinnerCalories.roundToInt()} kcal, ${dinnerProtein.roundToInt()} P, ${dinnerFat.roundToInt()} F, ${dinnerCarbs.roundToInt()} C)"
        }

    }
    private fun loadDataForDate(date: Calendar) {
        currentLoadId++

        clearAllMacrosAndUi()

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formattedDate = dateFormat.format(date.time)

        loadMealsForUser(currentUserId, formattedDate, "śniadanie")
        loadMealsForUser(currentUserId, formattedDate, "obiad")
        loadMealsForUser(currentUserId, formattedDate, "kolacja")
    }

    private fun clearAllMacrosAndUi() {
        breakfastCalories = 0.0; breakfastProtein = 0.0; breakfastFat = 0.0; breakfastCarbs = 0.0
        lunchCalories     = 0.0; lunchProtein     = 0.0; lunchFat     = 0.0; lunchCarbs     = 0.0
        dinnerCalories    = 0.0; dinnerProtein    = 0.0; dinnerFat    = 0.0; dinnerCarbs    = 0.0
        dailyTotalCalories = 0.0; dailyTotalProtein = 0.0; dailyTotalFat = 0.0; dailyTotalCarbs = 0.0

        loadedBreakfast = false
        loadedLunch = false
        loadedDinner = false

        llBreakfastMeals.removeAllViews()
        llLunchMeals.removeAllViews()
        llDinnerMeals.removeAllViews()

        tvBreakfast.text = "Śniadanie"
        tvLunch.text = "Obiad"
        tvDinner.text = "Kolacja"
        tvDailyTotal.text = ""
    }

    private fun maybeUpdateDailyTotals() {
        if (loadedBreakfast && loadedLunch && loadedDinner) {
            dailyTotalCalories = breakfastCalories + lunchCalories + dinnerCalories
            dailyTotalProtein  = breakfastProtein  + lunchProtein  + dinnerProtein
            dailyTotalFat      = breakfastFat      + lunchFat      + dinnerFat
            dailyTotalCarbs    = breakfastCarbs    + lunchCarbs    + dinnerCarbs

            tvDailyTotal.text = "Dzisiaj: ${dailyTotalCalories.roundToInt()} kcal, " +
                    "${dailyTotalProtein.roundToInt()} P, ${dailyTotalFat.roundToInt()} F, ${dailyTotalCarbs.roundToInt()} C"
        }
    }

    private fun addFoodToDb(mealName: String, foodId: String, quantityGrams: Double) {
        // 1) Sformatuj datę na "yyyy-MM-dd" (wymagane przez backend)
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val dateStr = dateFormat.format(selectedDate.time)

        // 2) Zbuduj DTO posiłku z jedną pozycją
        val mealDto = com.example.fithub.data.MealDto(
            name = mealName,
            foods = listOf(
                com.example.fithub.data.FoodItemDto(
                    foodId = foodId,
                    quantity = quantityGrams
                )
            )
        )

        val payload = com.example.fithub.data.AddMealDto(meal = mealDto)

        // 3) Wyślij do API i odśwież ekran
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                NetworkModule.api.addMeal(
                    userId = currentUserId,
                    date = dateStr,
                    addMealDto = payload
                ) // zwraca aktualny dokument daily nutrition z posiłkami

                // Po sukcesie odśwież dane dla wybranego dnia
                loadDataForDate(selectedDate)
                Toast.makeText(requireContext(), "Dodano produkt do: $mealName", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Błąd dodawania: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun Double.asGramsLabel(): String {
        return if (this % 1.0 == 0.0) "${this.toInt()} g" else "${"%.1f".format(this)} g"
    }

}