package com.example.fithub

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import com.example.fithub.data.ChallengeType
import com.example.fithub.data.NewUserDto
import com.example.fithub.logic.UserCalculator
import kotlinx.coroutines.launch

class UserProgressFragment : Fragment(R.layout.fragment_user_progress) {

    private lateinit var tvUserLevel: TextView
    private lateinit var tvUserPoints: TextView
    private lateinit var tvUserPointsNextLevel: TextView
    private lateinit var tvUserStreak: TextView
    private lateinit var tvUserWeightChangeLabel: TextView
    private lateinit var tvUserWeightChange: TextView
    private lateinit var tvUserActiveChallengeTitle: TextView
    private lateinit var pbUserActiveChallenge: ProgressBar
    private lateinit var tvUserActiveChallengeProgress: TextView
    private lateinit var btnUserBadges: Button
    private lateinit var btnUserComparePhotos: Button
    private lateinit var btnUserChallenges: Button

    companion object {
        private const val CURRENT_USER_ID = "68cbc06e6cdfa7faa8561f82"
        private const val TAG = "UserProgress"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeViews(view)
        setupClickListeners()
        loadData()
    }

    private fun initializeViews(view: View) {
        tvUserLevel = view.findViewById(R.id.tvUserLevel)
        tvUserPoints = view.findViewById(R.id.tvUserPoints)
        tvUserPointsNextLevel = view.findViewById(R.id.tvPointsToNextLevel)
        tvUserStreak = view.findViewById(R.id.tvUserStreak)
        tvUserWeightChangeLabel = view.findViewById(R.id.tvWeightChangeLabel)
        tvUserWeightChange = view.findViewById(R.id.tvWeightChange)
        tvUserActiveChallengeTitle = view.findViewById(R.id.tvActiveChallengeTitle)
        pbUserActiveChallenge = view.findViewById(R.id.progressActiveChallenge)
        tvUserActiveChallengeProgress = view.findViewById(R.id.tvActiveChallengeProgress)
        btnUserBadges = view.findViewById(R.id.btnBadges)
        btnUserComparePhotos = view.findViewById(R.id.btnComparePhotos)
        btnUserChallenges = view.findViewById(R.id.btnChallenges)
    }

    private fun setupClickListeners() {
        btnUserBadges.setOnClickListener {
            ProgressUniversalListDialogFragment.newInstance(DisplayMode.BADGES)
                .show(childFragmentManager, "BadgesDialog")
        }

        btnUserChallenges.setOnClickListener {
            val dialog = ProgressUniversalListDialogFragment.newInstance(DisplayMode.CHALLENGES)
            dialog.onChallengeActionListener = object : ProgressUniversalListDialogFragment.OnChallengeActionListener {
                override fun onChallengeActionCompleted() {
                    loadData()
                }
            }
            dialog.show(childFragmentManager, "ChallengesDialog")
        }

        btnUserComparePhotos.setOnClickListener {
            ProgressUniversalListDialogFragment.newInstance(DisplayMode.PHOTOS)
                .show(childFragmentManager, "PhotosDialog")
        }
    }

    private fun loadData() {
        lifecycleScope.launch {
            try {
                val progress = NetworkModule.api.getUserProgress(CURRENT_USER_ID)
                val user = NetworkModule.api.getUserById(CURRENT_USER_ID)
                val userGoals = NetworkModule.api.getUserGoalsByUserId(CURRENT_USER_ID)
                val challengesApi = NetworkModule.api.getAllChallenges()


                tvUserLevel.text = progress.level.toString()
                tvUserPoints.text = progress.currentPoints.toString()
                tvUserPointsNextLevel.text = buildSpannedString{
                    append("Do poziomu ")
                    append("${progress.level + 1}")
                    append(" brakuje: ")
                    bold{
                        append("${progress.pointsToNextLevel}")
                    }
                    append(" pkt")
                }
                tvUserStreak.text = progress.loginStreak.toString()

                val activeGoal = userGoals.firstOrNull { it.status == "active" } ?: userGoals.firstOrNull()

                if(activeGoal != null && user.profile.weightKg > 0){
                    val calculator = UserCalculator()
                    val result = calculator.calculateGoalProgress(
                        currentWeight = user.profile.weightKg.toDouble(),
                        goal = activeGoal
                    )
                    tvUserWeightChangeLabel.text = result.label
                    tvUserWeightChange.text = result.value
                } else {
                    tvUserWeightChangeLabel.text = "Cel"
                    tvUserWeightChange.text = "Brak danych"
                }

                val activeChallengeProgress = progress.activeChallenges

                if (activeChallengeProgress != null && !activeChallengeProgress.challengeId.isNullOrEmpty()) {
                    val currentChallengeId = activeChallengeProgress.challengeId

                    val challengeDefinition = challengesApi.find { it.id == currentChallengeId }

                    if (challengeDefinition != null) {
                        val type = challengeDefinition.type

                        val unit = when(type) {
                            ChallengeType.STREAK -> "dni"
                            ChallengeType.MEAL_COUNT -> "posiłków"
                            ChallengeType.WEIGHT_LOSS -> "kg"
                            ChallengeType.TRAINING_COUNT -> "ćwiczeń"
                            ChallengeType.TRAINING_PLAN_COUNT -> "planów"
                        }

                        val currentValue = activeChallengeProgress.counter
                        val totalValue = activeChallengeProgress.totalToFinish

                        tvUserActiveChallengeTitle.text = challengeDefinition.name
                        tvUserActiveChallengeProgress.text = "$currentValue / $totalValue $unit"

                        pbUserActiveChallenge.max = totalValue
                        pbUserActiveChallenge.progress = currentValue

                        pbUserActiveChallenge.visibility = View.VISIBLE
                        tvUserActiveChallengeProgress.visibility = View.VISIBLE
                    } else {
                        tvUserActiveChallengeTitle.text = "Nieznane wyzwanie"
                        pbUserActiveChallenge.visibility = View.INVISIBLE
                        tvUserActiveChallengeProgress.visibility = View.INVISIBLE
                    }
                } else {
                    tvUserActiveChallengeTitle.text = "Brak aktywnych wyzwań"
                    pbUserActiveChallenge.visibility = View.INVISIBLE
                    tvUserActiveChallengeProgress.visibility = View.INVISIBLE
                }

            } catch (e: Exception){
                Log.e("UserProgress", "Błąd ładowania statystyk")
                Toast.makeText(requireContext(), "Błąd ładowania danych: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

}