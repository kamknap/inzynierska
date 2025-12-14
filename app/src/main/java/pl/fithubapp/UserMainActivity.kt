package pl.fithubapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import pl.fithubapp.data.ChallengeType
import pl.fithubapp.data.PointsManager
import pl.fithubapp.data.ChallengeManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class UserMainActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView
    private var isNavigating = false // Flaga zapobiegająca wielokrotnej nawigacji
    private var activeDialogs = mutableListOf<AlertDialog>() // Lista aktywnych dialogów

    // 1. Launcher do zapytania o uprawnienia powiadomień
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Tutaj można obsłużyć reakcję na decyzję użytkownika, np. logiem
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Włącz tryb immersive (ukryj przyciski systemowe)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        
        setContentView(R.layout.activity_user_main)

        bottomNavigation = findViewById(R.id.bottom_navigation)

        // 2. Sprawdzenie i prośba o uprawnienia (tylko dla Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        ChallengeManager.onChallengeCompleted = { challengeName, badgeName, points ->
            showChallengeCompleteDialog(challengeName, badgeName, points)
        }

        // Pobierz ID zalogowanego użytkownika z Firebase
        val currentUserId = AuthManager.currentUserId
        if (currentUserId == null) {
            // Użytkownik nie jest zalogowany - przekieruj do logowania
            startActivity(Intent(this, SplashActivity::class.java))
            finish()
            return
        }

        lifecycleScope.launch {
            try {
                // Próbuj sprawdzić daily login (może fail dla nowych użytkowników bez UserProgress)
                try {
                    val result = PointsManager.checkDailyLogin()
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
                        ChallengeManager.checkChallengeProgress(ChallengeType.STREAK)
                    }

                    if (result.levelUp) {
                        showLevelUpDialog()
                    }
                } catch (e: Exception) {
                    // Nowy użytkownik może nie mieć UserProgress - to OK
                    Log.d("UserMainActivity", "Pomijam checkDailyLogin dla nowego użytkownika: ${e.message}")
                }

                // 3. Konfiguracja powiadomień z zabezpieczeniem przed nullem w bazie
                val user = NetworkModule.api.getCurrentUser()

                // Używamy bezpiecznego wywołania ?. bo settings może być null
                val notifSettings = user.settings?.notifications

                ReminderScheduler.scheduleAllReminders(
                    context = this@UserMainActivity,
                    userId = user.id,
                    // Jeśli ustawienia są null, domyślnie włączamy powiadomienia (true)
                    workoutEnabled = notifSettings?.types?.workoutReminders ?: true,
                    mealEnabled = notifSettings?.types?.mealReminders ?: true,
                    measureEnabled = notifSettings?.types?.measureReminders ?: true
                )

            } catch (e: Exception) {
                e.printStackTrace() // Log błędu, żeby aplikacja nie padła przy braku sieci
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
        // Zamknij wszystkie aktywne dialogi przed zniszczeniem Activity
        activeDialogs.forEach { dialog ->
            if (dialog.isShowing) {
                dialog.dismiss()
            }
        }
        activeDialogs.clear()
        ChallengeManager.onChallengeCompleted = null
    }

    private fun setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener { item ->
            // Zapobiegaj wielokrotnej nawigacji lub nawigacji podczas wyświetlania dialogów
            if (isNavigating || activeDialogs.isNotEmpty()) {
                return@setOnItemSelectedListener false
            }
            
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
        // Sprawdź czy już trwa transakcja
        if (isNavigating) return
        
        // Sprawdź czy fragment tego samego typu już jest załadowany
        val currentFragment = supportFragmentManager.findFragmentById(R.id.main_container)
        if (currentFragment != null && currentFragment::class == fragment::class) {
            return // Ten sam fragment już jest wyświetlany
        }
        
        isNavigating = true
        
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.main_container, fragment)
            runOnCommit {
                // Odblokuj nawigację po zakończeniu transakcji
                isNavigating = false
            }
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

        // Dodaj dialog do listy aktywnych
        activeDialogs.add(dialog)
        
        dialog.setOnDismissListener {
            // Usuń dialog z listy po zamknięciu
            activeDialogs.remove(dialog)
        }

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    fun showLevelUpDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.level_up_dialog, null)
        val btnOk = dialogView.findViewById<Button>(R.id.btnOkLevel)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        // Dodaj dialog do listy aktywnych
        activeDialogs.add(dialog)
        
        dialog.setOnDismissListener {
            // Usuń dialog z listy po zamknięciu
            activeDialogs.remove(dialog)
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnOk.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    fun showChallengeCompleteDialog(challengeName: String, badgeName: String, points: Int) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.challenge_complete_dialog, null)

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

        // Dodaj dialog do listy aktywnych
        activeDialogs.add(dialog)
        
        dialog.setOnDismissListener {
            // Usuń dialog z listy po zamknięciu
            activeDialogs.remove(dialog)
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnOk.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}