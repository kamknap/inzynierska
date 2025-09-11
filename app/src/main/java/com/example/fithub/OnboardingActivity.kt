package com.example.fithub

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.addCallback
import androidx.fragment.app.commit

class OnboardingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 1) Ustawienie layoutu aktywności
        setContentView(R.layout.activity_onboarding)




    // 2) Jeśli pierwszy raz tworzymy aktywność, wstaw UserDataFragment do kontenera
        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                replace(R.id.onboarding_container, UserDataFragment())
            }
        }

        //callback
        onBackPressedDispatcher.addCallback(this) {
            val fm = supportFragmentManager
            if (fm.backStackEntryCount > 0) {
                fm.popBackStack()
            } else {
                // Brak fragmentów do cofnięcia – zamknij aktywność
                finish()
            }
        }
    }

    /**
     * 3) Funkcja, która podmienia zawartość kontenera na GoalsFragment
     */
    fun showGoalsFragment() {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.onboarding_container, GoalsFragment())
            addToBackStack(null) // pozwala wrócić przyciskiem "wstecz" do poprzedniego fragmentu
        }
    }

}
