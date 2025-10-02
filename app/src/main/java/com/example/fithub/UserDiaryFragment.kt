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
import java.text.SimpleDateFormat
import java.util.*

class UserDiaryFragment : Fragment(R.layout.fragment_user_diary) {

    private lateinit var llDaysContainer: LinearLayout
    private lateinit var hsvWeek: HorizontalScrollView
    private var selectedDate = Calendar.getInstance()
    private lateinit var svDiary: ScrollView
    private lateinit var btnBreakfast: ImageButton
    private lateinit var llBreakfastMeals: LinearLayout

    //TODO: pobierac dane z bazy


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        llDaysContainer = view.findViewById(R.id.llDaysContainer)
        hsvWeek = view.findViewById(R.id.hsvWeek)
        llBreakfastMeals = view.findViewById(R.id.llBreakfastMeals)


        initDaysView()
        llBreakfastMeals = view.findViewById<LinearLayout>(R.id.llBreakfastMeals)
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


    private fun loadDataForDate(date: Calendar) {
        // TODO: Załaduj posiłki dla wybranego dnia
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        println("Wybrano datę: ${dateFormat.format(date.time)}")
    }
}