package pl.fithubapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

/**
 * SplashActivity - Entry point aplikacji
 * Decyduje o routingu użytkownika na podstawie stanu autentykacji i onboardingu
 */
class SplashActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Sprawdzamy stan autentykacji
        if (AuthManager.isUserLoggedIn()) {
            // Użytkownik zalogowany - sprawdź czy ukończył onboarding
            AuthManager.currentUserId?.let { uid ->
                checkUserStatusAndRedirect(uid)
            }
        } else {
            // Użytkownik niezalogowany - idź do logowania
            navigateToLogin()
        }
    }

    /**
     * Sprawdza status użytkownika w backendzie i przekierowuje odpowiednio
     */
    private fun checkUserStatusAndRedirect(userId: String) {
        lifecycleScope.launch {
            try {
                // Pobieramy usera z API
                val userDto = NetworkModule.api.getUserById(userId)
                
                // Sprawdzamy czy użytkownik ma ukończony onboarding
                // Warunek: ma wypełnione dane podstawowe (waga, wzrost)
                val hasCompletedOnboarding = userDto.profile?.weightKg != null && 
                                            userDto.profile.heightCm != null
                
                if (hasCompletedOnboarding) {
                    // Ma ukończony onboarding -> idź do głównej aplikacji
                    navigateToMainApp()
                } else {
                    // Jest w Firebase, ale nie ma pełnych danych -> dokończ onboarding
                    navigateToOnboarding(isCompletion = true)
                }
            } catch (e: Exception) {
                // Błąd (np. 404 - user nie istnieje w backendzie)
                // User jest w Firebase, ale nie w MongoDB -> musi przejść onboarding
                navigateToOnboarding(isCompletion = false)
            }
        }
    }

    /**
     * Przekierowanie do ekranu logowania
     */
    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    /**
     * Przekierowanie do onboardingu
     * @param isCompletion - czy to dokończenie rejestracji (user istnieje w Firebase)
     */
    private fun navigateToOnboarding(isCompletion: Boolean) {
        val intent = Intent(this, OnboardingActivity::class.java).apply {
            putExtra("IS_COMPLETION", isCompletion)
        }
        startActivity(intent)
        finish()
    }

    /**
     * Przekierowanie do głównej aplikacji (UserMainActivity)
     */
    private fun navigateToMainApp() {
        startActivity(Intent(this, UserMainActivity::class.java))
        finish()
    }
}
