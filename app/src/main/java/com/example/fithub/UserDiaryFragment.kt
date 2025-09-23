package com.example.fithub

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.*

class UserDiaryFragment : Fragment(R.layout.fragment_user_diary) {

    private lateinit var llDaysContainer: LinearLayout
    private var selectedDate = Calendar.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        llDaysContainer = view.findViewById(R.id.llDaysContainer)

        initDaysView()
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

        // Przewiń do obecnego tygodnia
//        scrollToCurrentWeek()
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

        // Oznacz dzisiejszy dzień
        val today = Calendar.getInstance()
        if (isSameDay(dayCalendar, today)) {
            dayView.isSelected = true
        }

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

//    private fun scrollToCurrentWeek() {
//        // Przewiń do obecnego dnia po krótkim opóźnieniu
//        llDaysContainer.post {
//            val today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
//            val todayView = llDaysContainer.getChildAt(today - 1)
//
//            val scrollX = todayView.left - (llDaysContainer.width / 2) + (todayView.width / 2)
//            findViewById<HorizontalScrollView>(R.id.hsvWeek)?.smoothScrollTo(scrollX, 0)
//        }
//    }

    private fun loadDataForDate(date: Calendar) {
        // TODO: Załaduj posiłki dla wybranego dnia
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        println("Wybrano datę: ${dateFormat.format(date.time)}")
    }
}