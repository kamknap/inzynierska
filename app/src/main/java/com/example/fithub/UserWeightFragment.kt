package com.example.fithub

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.fithub.data.UpdateProfileData
import com.example.fithub.data.UpdateUserDto
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fithub.data.ChallengeType
import com.example.fithub.data.PointsManager
import com.example.fithub.data.UserWeightHistoryDto
import com.example.fithub.logic.ChallengeManager
import com.example.fithub.logic.UserCalculator
import java.util.Calendar

class UserWeightFragment : Fragment(R.layout.fragment_user_weight) {

    private lateinit var userWeight: TextView
    private lateinit var userProgress: TextView
    private lateinit var btnAddWeight: ImageButton
    private lateinit var weightHistoryAdapter: WeightHistoryAdapter
    private lateinit var rvWeightHistory : RecyclerView
    private lateinit var weightChartView: WeightChartView
    private var currentUserWeight: Double? = null
    private var currentGoalType: String? = null
    private var referenceWeight: Double? = null
    private lateinit var btnRangeAll: Button
    private lateinit var btnRangeYear: Button
    private lateinit var btnRangeSixMonths: Button
    private lateinit var btnRangeThreeMonths: Button
    private lateinit var btnRangeMonth: Button
    private lateinit var btnRangeWeek: Button
    private var fullHistoryList: List<UserWeightHistoryDto> = emptyList()
    private var currentRange: DateRange = DateRange.THREE_MONTHS
    enum class DateRange {
        ALL, YEAR, SIX_MONTHS, THREE_MONTHS, MONTH, WEEK
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userWeight = view.findViewById(R.id.tvCurrentWeight)
        userProgress = view.findViewById(R.id.tvProgress)
        btnAddWeight = view.findViewById(R.id.btnAddWeight)
        rvWeightHistory = view.findViewById(R.id.rvWeightHistory)
        weightChartView = view.findViewById(R.id.weightChartView)
        btnRangeAll = view.findViewById(R.id.btnRangeAll)
        btnRangeYear = view.findViewById(R.id.btnRangeYear)
        btnRangeSixMonths = view.findViewById(R.id.btnRangeSixMonths)
        btnRangeThreeMonths = view.findViewById(R.id.btnRangeThreeMonths)
        btnRangeMonth = view.findViewById(R.id.btnRangeMonth)
        btnRangeWeek = view.findViewById(R.id.btnRangeWeek)


        val userId = "68cbc06e6cdfa7faa8561f82"
        getCurrentWeight(userId)
        updateProgressStats(userId)
        fillHistory(userId)

        btnAddWeight.setOnClickListener {
            showAddWeightDialog(userId)
        }

        btnRangeAll.setOnClickListener {
            currentRange = DateRange.ALL
            updateChartRange()
            highlightSelectedButton(btnRangeAll)
        }

        btnRangeYear.setOnClickListener {
            currentRange = DateRange.YEAR
            updateChartRange()
            highlightSelectedButton(btnRangeYear)
        }

        btnRangeSixMonths.setOnClickListener {
            currentRange = DateRange.SIX_MONTHS
            updateChartRange()
            highlightSelectedButton(btnRangeSixMonths)
        }

        btnRangeThreeMonths.setOnClickListener {
            currentRange = DateRange.THREE_MONTHS
            updateChartRange()
            highlightSelectedButton(btnRangeThreeMonths)
        }

        btnRangeMonth.setOnClickListener {
            currentRange = DateRange.MONTH
            updateChartRange()
            highlightSelectedButton(btnRangeMonth)
        }

        btnRangeWeek.setOnClickListener {
            currentRange = DateRange.WEEK
            updateChartRange()
            highlightSelectedButton(btnRangeWeek)
        }

        highlightSelectedButton(btnRangeThreeMonths)
    }

    private fun getCurrentWeight(userId: String){
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val user = NetworkModule.api.getUserById(userId)
                val userGoals = NetworkModule.api.getUserGoalsByUserId(userId)

                if (userGoals.isEmpty()) {
                    return@launch
                }

                currentUserWeight = user.profile.weightKg

                val activeGoal = userGoals.firstOrNull { it.status == "active" }
                    ?: userGoals.first()

                currentGoalType = activeGoal.type
                referenceWeight = activeGoal.firstWeightKg
                Log.d("UserWeightFragment", "firstMeasured: $referenceWeight")


                userWeight.text = "Obecna waga: ${user.profile.weightKg}kg, cel: ${activeGoal.targetWeightKg}kg"
                updateAdapter()
            }
            catch (e: Exception){
                Toast.makeText(
                    requireContext(),
                    "Błąd: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun updateProgressStats(userId: String){
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val user = NetworkModule.api.getUserById(userId)
                val userGoals = NetworkModule.api.getUserGoalsByUserId(userId)

                if (userGoals.isEmpty()) {
                    userProgress.text = "Brak aktywnego celu"
                    return@launch
                }

                val currentWeight = user.profile.weightKg

                val activeGoal = userGoals.firstOrNull { it.status == "active" }
                    ?: userGoals.first()

                val goalType = activeGoal.type

                currentGoalType = goalType

                val calculator = UserCalculator()
                val result = calculator.calculateGoalProgress(currentWeight, activeGoal)

                userProgress.text = result.fullDesc
            }
            catch (e: Exception){
                Log.e("UserWeightFragment", "Błąd: ${e.message}", e)
                Toast.makeText(
                    requireContext(),
                    "Błąd: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showAddWeightDialog(userId: String) {
        val dialogLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 24)
        }

        val inputLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
        }

        val etWeight = EditText(requireContext()).apply {
            hint = "Waga"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText("")
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val unitTextView = TextView(requireContext()).apply {
            text = " kg"
            textSize = 18F
            setPadding(8, 0, 0, 0)
        }

        inputLayout.addView(etWeight)
        inputLayout.addView(unitTextView)

        val dateLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            setPadding(0, 16, 0, 0)
        }

        val tvDateLabel = TextView(requireContext()).apply {
            text = "Data pomiaru: "
            textSize = 16F
        }

        val currentDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
        val etDate = TextView(requireContext()).apply {
            text = currentDate
            textSize = 16F
            setPadding(8, 0, 0, 0)
            setTextColor(resources.getColor(android.R.color.holo_blue_dark, null))
        }

        dateLayout.addView(tvDateLabel)
        dateLayout.addView(etDate)

        dialogLayout.addView(inputLayout)
        dialogLayout.addView(dateLayout)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Dodaj pomiar wagi")
            .setView(dialogLayout)
            .setPositiveButton("Dodaj") { _, _ ->
                val weight = etWeight.text.toString().toDoubleOrNull()

                if (weight == null || weight <= 0) {
                    Toast.makeText(requireContext(), "Podaj prawidłową wagę", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                addWeight(userId, weight)
            }
            .setNegativeButton("Anuluj", null)
            .create()

        dialog.setOnShowListener {
            etWeight.requestFocus()
            etWeight.postDelayed({
                val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE)
                        as android.view.inputmethod.InputMethodManager
                imm.showSoftInput(etWeight, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
            }, 100)
        }

        dialog.show()
    }

    private fun addWeight(userId: String, weight: Double){
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val currentUser = NetworkModule.api.getUserById(userId)

                val oldWeight = currentUser.profile.weightKg
                val diff = oldWeight - weight
                if (diff > 0){
                    ChallengeManager.checkChallengeProgress(userId, ChallengeType.WEIGHT_LOSS, diff)
                }

                val updateUserDto = UpdateUserDto(
                    profile = UpdateProfileData(
                        sex = currentUser.profile.sex,
                        birthDate = currentUser.profile.birthDate,
                        heightCm = currentUser.profile.heightCm,
                        weightKg = weight
                    )
                )
                NetworkModule.api.updateUser(userId, updateUserDto)

                try {
                    Log.d("AddWeight", "Waga dodana, przyznaję punkty...")
                    val leveledUp = PointsManager.addPoints(userId, PointsManager.ActionType.WEIGHT)

                    if (leveledUp) {
                        (activity as? UserMainActivity)?.showLevelUpDialog()
                    }                } catch (e: Exception) {
                    Log.e("AddWeight", "Nie udało się dodać punktów: ${e.message}")
                }

                Toast.makeText(
                    requireContext(),
                    "Waga zaktualizowana: ${String.format("%.1f", weight)}kg",
                    Toast.LENGTH_SHORT
                ).show()

                getCurrentWeight(userId)
                updateProgressStats(userId)
                fillHistory(userId)
            }
            catch (e: Exception){
                Log.e("UserWeightFragment", "Błąd dodawania pomiaru", e)
                Toast.makeText(
                    requireContext(),
                    "Błąd: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun fillHistory(userId: String){
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val userHistoryList = NetworkModule.api.getUserWeightHistory(userId)

                fullHistoryList = userHistoryList

                val sortedList = userHistoryList.sortedByDescending { it.measuredAt }

                weightHistoryAdapter = WeightHistoryAdapter(referenceWeight, currentGoalType)
                rvWeightHistory.apply {
                    layoutManager = LinearLayoutManager(requireContext())
                    adapter = weightHistoryAdapter
                }
                weightHistoryAdapter.submitList(sortedList)

                updateChartRange()
                Log.d("WeightHistory", "Załadowano ${sortedList.size} pomiarów")
            }
            catch(e: Exception){
                Log.e("UserWeightFragment", "Błąd", e)
            }
        }
    }

    private fun updateAdapter() {
        if (::weightHistoryAdapter.isInitialized) {
            weightHistoryAdapter = WeightHistoryAdapter(referenceWeight, currentGoalType)
            rvWeightHistory.adapter = weightHistoryAdapter
            fillHistory("68cbc06e6cdfa7faa8561f82")
        }
    }

    private fun highlightSelectedButton(selectedButton: Button) {
        listOf(
            btnRangeAll,
            btnRangeYear,
            btnRangeSixMonths,
            btnRangeThreeMonths,
            btnRangeMonth,
            btnRangeWeek
        ).forEach { button ->
            button.setBackgroundColor(resources.getColor(android.R.color.transparent, null))
            button.setTextColor(resources.getColor(android.R.color.black, null))
        }

        selectedButton.setBackgroundColor(resources.getColor(android.R.color.holo_blue_light, null))
        selectedButton.setTextColor(resources.getColor(android.R.color.white, null))
    }
    private fun updateChartRange() {
        val filteredData = filterDataByRange(fullHistoryList, currentRange)

        if (filteredData.isEmpty()) {
            Log.d("WeightChart", "Brak danych dla zakresu $currentRange")
            weightChartView.setData(emptyList())
        } else {
            weightChartView.setData(filteredData)
            Log.d("WeightChart", "Pokazuję ${filteredData.size} pomiarów dla zakresu $currentRange")
        }
    }

    private fun filterDataByRange(data: List<UserWeightHistoryDto>, range: DateRange): List<UserWeightHistoryDto> {
        if (range == DateRange.ALL) {
            return data
        }

        val calendar = Calendar.getInstance()
        val now = calendar.time

        calendar.time = now
        when (range) {
            DateRange.YEAR -> calendar.add(Calendar.YEAR, -1)
            DateRange.SIX_MONTHS -> calendar.add(Calendar.MONTH, -6)
            DateRange.THREE_MONTHS -> calendar.add(Calendar.MONTH, -3)
            DateRange.MONTH -> calendar.add(Calendar.MONTH, -1)
            DateRange.WEEK -> calendar.add(Calendar.DAY_OF_YEAR, -7)
            else -> return data
        }

        val cutoffDate = calendar.time
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
            timeZone = java.util.TimeZone.getTimeZone("UTC")
        }

        return data.filter { history ->
            try {
                val measureDate = isoFormat.parse(history.measuredAt)
                measureDate != null && measureDate.after(cutoffDate)
            } catch (e: Exception) {
                false
            }
        }
    }
}