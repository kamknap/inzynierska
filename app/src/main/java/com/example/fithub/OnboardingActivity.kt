package com.example.fithub

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.addCallback
import androidx.fragment.app.commit
import com.example.fithub.data.OnboardingUserData

class OnboardingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                replace(R.id.onboarding_container, UserDataFragment())
            }
        }

        onBackPressedDispatcher.addCallback(this) {
            val fm = supportFragmentManager
            if (fm.backStackEntryCount > 0) {
                fm.popBackStack()
            } else {
                finish()
            }
        }
    }

    fun showGoalsFragment(userData: OnboardingUserData? = null) {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            val goalsFragment = GoalsFragment()
            if (userData != null) {
                val bundle = Bundle().apply {
                    putString("userName", userData.name)
                    putString("userSex", userData.sex)
                    putString("userBirthDate", userData.birthDate)
                    putDouble("userWeight", userData.weight ?: 0.0)
                    putDouble("userHeight", userData.height ?: 0.0)
                }
                goalsFragment.arguments = bundle
            }
            replace(R.id.onboarding_container, goalsFragment)
            addToBackStack(null)
        }
    }

}
