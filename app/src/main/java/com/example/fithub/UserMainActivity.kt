package com.example.fithub

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Button
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import com.example.fithub.data.ChallengeType
import com.example.fithub.data.PointsManager
import com.example.fithub.logic.ChallengeManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class UserMainActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_main)

        bottomNavigation = findViewById(R.id.bottom_navigation)

        ChallengeManager.onChallengeCompleted = { challengeName, badgeName, points ->
            showChallengeCompleteDialog(challengeName, badgeName, points)
        }

        //check daily login
        val currentUserId = "68cbc06e6cdfa7faa8561f82"

        lifecycleScope.launch {
            val result = PointsManager.checkDailyLogin(currentUserId)
            if(result.isNewLogin){
                if (result.streakBonus){
                    showStreakDialog(result.currentStreak, result.pointsAdded)
                }
                else{
                    Snackbar.make(
                        bottomNavigation,
                        "Witaj ponownie! Zdobyto +${result.pointsAdded} pkt.",
                        Snackbar.LENGTH_LONG
                    ).setAnchorView(bottomNavigation).show()
                }
                ChallengeManager.checkChallengeProgress(currentUserId, ChallengeType.STREAK)
            }

            if (result.levelUp) {
                showLevelUpDialog()
            }

        }

        if (savedInstanceState == null) {
            loadFragment(UserDiaryFragment())
        }

        setupBottomNavigation()

        onBackPressedDispatcher.addCallback(this) {
            val fm = supportFragmentManager
            if (fm.backStackEntryCount > 0) {
                fm.popBackStack()
            } else {
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ChallengeManager.onChallengeCompleted = null // Wyczyść callback
    }

    private fun setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_diary -> {
                    loadFragment(UserDiaryFragment())
                    true
                }
                R.id.nav_training -> {
                    loadFragment(UserTrainingFragment())
                    true
                }
                R.id.nav_weight -> {
                    loadFragment(UserWeightFragment())
                    true
                }
                R.id.nav_stats -> {
                    loadFragment(UserProgressFragment())
                    true
                }
                R.id.nav_profile -> {
                    loadFragment(UserProfileFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.main_container, fragment)
        }
    }

    private fun showStreakDialog(days: Int, points: Int){
        val dialogView = LayoutInflater.from(this).inflate(R.layout.login_streak_dialog, null)
        val tvDays = dialogView.findViewById<TextView>(R.id.tvStreakDays)
        val tvPoints = dialogView.findViewById<TextView>(R.id.tvStreakPoints)
        val btnClose = dialogView.findViewById<Button>(R.id.btnCloseDialog)

        tvDays.text = "$days dni z rzędu!"
        tvPoints.text = "Zdobyto +$points pkt"

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        // przezroczyste tło
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialog.show()
    }

    fun showLevelUpDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.level_up_dialog, null)
        val btnOk = dialogView.findViewById<Button>(R.id.btnOkLevel)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnOk.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    fun showChallengeCompleteDialog(challengeName: String, badgeName: String, points: Int) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.challenge_complete_dialog, null)

        // Ustaw teksty (musisz utworzyć layout challenge_complete_dialog.xml)
        val tvChallengeName = dialogView.findViewById<TextView>(R.id.tvChallengeName)
        val tvBadgeName = dialogView.findViewById<TextView>(R.id.tvBadgeName)
        val tvPoints = dialogView.findViewById<TextView>(R.id.tvPoints)
        val btnOk = dialogView.findViewById<Button>(R.id.btnOk)

        tvChallengeName.text = "Ukończono: $challengeName"
        tvBadgeName.text = "Zdobyto odznakę: $badgeName"
        tvPoints.text = "+$points pkt"

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnOk.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}