package com.example.fithub

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ReturnThis
import androidx.compose.ui.graphics.ImageBitmap
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.fithub.data.MealWithFoodsDto
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class UserDiaryFragment : Fragment(R.layout.fragment_user_diary) {

    private lateinit var llDaysContainer: LinearLayout
    private lateinit var hsvWeek: HorizontalScrollView
    private var selectedDate = Calendar.getInstance()
    private lateinit var svDiary: ScrollView
    private lateinit var btnBreakfast: ImageButton
    private lateinit var llBreakfastMeals: LinearLayout
    private lateinit var llLunchMeals: LinearLayout
    private lateinit var llDinnerMeals: LinearLayout
    private lateinit var tvMacros: TextView
    private var totalMacroKcal = 0.0

    private val currentUserId = "68cbc06e6cdfa7faa8561f82"

    //TODO: pobierac dane z bazy


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        llDaysContainer = view.findViewById(R.id.llDaysContainer)
        hsvWeek = view.findViewById(R.id.hsvWeek)
        llBreakfastMeals = view.findViewById(R.id.llBreakfastMeals)
        llLunchMeals = view.findViewById(R.id.llLunchMeals)
        llDinnerMeals = view.findViewById(R.id.llDinnerMeals)
        tvMacros = view.findViewById<TextView>(R.id.tvMacros)


        initDaysView()
        btnBreakfast = view.findViewById(R.id.btnAddBreakfast)
        btnBreakfast.setOnClickListener {
            addMealToList(llBreakfastMeals, "Kurczak", 25, 10,8,300)
        }
    }


    private fun initDiary() {
        // TODO; widok dziennika kalorii

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

        // Edycja posilku TODO
        mealView.setOnClickListener {
            Toast.makeText(requireContext(), "Edytuj: $mealName", Toast.LENGTH_SHORT).show()
        }

        container.addView(mealView)
    }

    private fun initDaysView() {
        val currentMonth = selectedDate.get(Calendar.MONTH)
        val currentYear = selectedDate.get(Calendar.YEAR)

        // Pobierz pierwszy dzień miesiąca
        val firstDayOfMonth = Calendar.getInstance().apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.MONTH, currentMonth)
            set(Calendar.DAY_OF_MONTH, 1)
        }

        // Pobierz ostatni dzień miesiąca
        val lastDayOfMonth = firstDayOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH)

        // Wygeneruj wszystkie dni miesiąca
        for (day in 1..lastDayOfMonth) {
            val dayCalendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, currentYear)
                set(Calendar.MONTH, currentMonth)
                set(Calendar.DAY_OF_MONTH, day)
            }

            addDayView(dayCalendar, day)
        }

        // Automatycznie zaznacz dzisiejszy dzień i przewiń do niego
        selectTodayAndScroll()
    }

    private fun addDayView(dayCalendar: Calendar, dayNumber: Int) {
        val dayView = LayoutInflater.from(requireContext())
            .inflate(R.layout.item_day, llDaysContainer, false)

        val tvDayLetter = dayView.findViewById<TextView>(R.id.tvDayLetter)
        val tvDayNumber = dayView.findViewById<TextView>(R.id.tvDayNumber)

        // Ustaw literę dnia tygodnia
        val dayOfWeek = dayCalendar.get(Calendar.DAY_OF_WEEK)
        tvDayLetter.text = getDayLetter(dayOfWeek)

        // Ustaw numer dnia
        tvDayNumber.text = dayNumber.toString()

        // Obsługa kliknięcia
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
        // Usuń selekcję z poprzedniego dnia
        for (i in 0 until llDaysContainer.childCount) {
            llDaysContainer.getChildAt(i).isSelected = false
        }

        // Zaznacz nowy dzień
        dayView.isSelected = true
        selectedDate = dayCalendar.clone() as Calendar

        // Tutaj możesz załadować dane dla wybranego dnia
        loadDataForDate(selectedDate)
    }

    private fun selectTodayAndScroll() {
        val today = Calendar.getInstance()

        // Znajdź dzisiejszy dzień w kontenerze
        for (i in 0 until llDaysContainer.childCount) {
            val dayView = llDaysContainer.getChildAt(i)
            val dayCalendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, today.get(Calendar.YEAR))
                set(Calendar.MONTH, today.get(Calendar.MONTH))
                set(Calendar.DAY_OF_MONTH, i + 1)
            }

            if (isSameDay(dayCalendar, today)) {
                // Zaznacz dzisiejszy dzień
                dayView.isSelected = true
                selectedDate = dayCalendar.clone() as Calendar

                // Przewiń do tego dnia po wyrenderowaniu widoku
                dayView.post {
                    scrollToDay(dayView)
                }

                // Załaduj dane dla dzisiaj
                loadDataForDate(selectedDate)
                break
            }
        }
    }

    private fun scrollToDay(dayView: View) {
        // Oblicz pozycję do przewinięcia - wycentruj dzień na ekranie
        val scrollViewWidth = hsvWeek.width
        val dayViewLeft = dayView.left
        val dayViewWidth = dayView.width

        // Wycentruj dzień na ekranie
        val scrollX = dayViewLeft - (scrollViewWidth / 2) + (dayViewWidth / 2)

        // Przewiń płynnie do obliczonej pozycji
        hsvWeek.smoothScrollTo(scrollX.coerceAtLeast(0), 0)
    }


    private fun loadMealsForUser(userId: String, date: String, mealType: String) {
        lifecycleScope.launch {
            try {
                // 1. Pobierz dane z API
                val dailyNutrition = NetworkModule.api.getDailyNutrition(userId, date)

                // 2. Znajdź posiłki tego typu (np. "śniadanie")
                val matchingMeals = dailyNutrition.meals.filter { meal ->
                    meal.name.lowercase().contains(mealType.lowercase())
                }

                // 3. Wyświetl je
                displayMeals(matchingMeals, mealType)

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Błąd: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayMeals(meals: List<MealWithFoodsDto>, mealType: String) {
        // Znajdź odpowiedni kontener (śniadanie/obiad/kolacja)
        val container = when(mealType.lowercase()) {
            "śniadanie" -> llBreakfastMeals
            "obiad" -> llLunchMeals
            "kolacja" -> llDinnerMeals
            else -> llBreakfastMeals
        }

        // Wyczyść stare posiłki
        container.removeAllViews()

        // zmienne do sumowania
        var totalCalories = 0.0
        var totalProtein = 0.0
        var totalFat = 0.0
        var totalCarbs = 0.0

        // Dodawanie pojedynczego posilku
        meals.forEach { meal ->

            meal.foods.forEach { foodItem ->

                var mealCalories = 0.0
                var mealProtein = 0.0
                var mealFat = 0.0
                var mealCarbs = 0.0

                val food = foodItem.foodId
                val quantity = foodItem.quantity / 100.0

                mealCalories += food.nutritionPer100g.calories * quantity
                mealProtein += food.nutritionPer100g.protein * quantity
                mealFat += food.nutritionPer100g.fat * quantity
                mealCarbs += food.nutritionPer100g.carbs * quantity

                addMealToList(
                    container = container,
                    mealName = "- ${food.name}",
                    protein = kotlin.math.round(mealProtein).toInt(),
                    fat = kotlin.math.round(mealFat).toInt(),
                    carbs = kotlin.math.round(mealCarbs).toInt(),
                    calories = kotlin.math.round(mealCalories).toInt()
                )

                totalCalories += mealCalories
                totalProtein += mealProtein
                totalFat += mealFat
                totalCarbs += mealCarbs
            }


        }
        if (meals.isNotEmpty()) {
            addMealToList(
                container = container,
                mealName = "Razem:",
                protein = kotlin.math.round(totalProtein).toInt(),
                fat = kotlin.math.round(totalFat).toInt(),
                carbs = kotlin.math.round(totalCarbs).toInt(),
                calories = kotlin.math.round(totalCalories).toInt()
            )
            totalMacroKcal += totalCalories
        }
    }

    // Wywołaj funkcję np. przy zmianie daty:
    private fun loadDataForDate(date: Calendar) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formattedDate = dateFormat.format(date.time)

        // Załaduj posiłki dla każdej pory dnia
        loadMealsForUser(currentUserId, formattedDate, "śniadanie")
        loadMealsForUser(currentUserId, formattedDate, "obiad")
        loadMealsForUser(currentUserId, formattedDate, "kolacja")
        updateMacros()
    }

    private fun updateMacros() {
        val macrosText = "Suma z całego dnia: ${kotlin.math.round(totalMacroKcal)} kcal"
        tvMacros.text = macrosText
        totalMacroKcal = 0.0
    }

}