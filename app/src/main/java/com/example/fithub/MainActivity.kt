package com.example.fithub

import android.content.Intent
import android.os.Bundle
import android.webkit.WebView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import android.util.Log
import androidx.fragment.app.commit

class MainActivity : AppCompatActivity() {

    lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //onboarding
        val buttonOnboarding = findViewById<Button>(R.id.btnNavigateBMI)
        buttonOnboarding.setOnClickListener {
            val intent = Intent(this, OnboardingActivity::class.java)
            startActivity(intent)
        }

        //muscle model
        val buttonMuscle = findViewById<Button>(R.id.btnMuscle)
        buttonMuscle.setOnClickListener {
            val intent = Intent(this, MuscleModel::class.java)
            intent.putExtra("url", "file:///android_asset/index/back.html")
            startActivity(intent)
        }

        val buttonDiary = findViewById<Button>(R.id.btnDiary)
        buttonDiary.setOnClickListener {
            val intent = Intent(this, UserMainActivity::class.java)
            startActivity(intent)
        }

        //api
        val btnApi = findViewById<Button>(R.id.btnApi)
        btnApi.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val users = NetworkModule.api.getUsers()
                    val userGoals = NetworkModule.api.getUserGoals()

                    if (users.isNotEmpty() && userGoals.isNotEmpty()) {
                        val username = users[0].username
                        val birthDate = users[0].profile.birthDate.replace(Regex("T.*$"), "")
                        val calorieTarget = userGoals[0].plan.calorieTarget ?: "Brak celu"
                        val goalOwner = userGoals[0].userId.username
                        val message = "User: $username, Data ur: $birthDate, Kalorie: $calorieTarget, Cel należy do: $goalOwner"
                        Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
                        Log.d("ApiInfo", message)
                    } else {
                        val usersCount = users.size
                        val goalsCount = userGoals.size
                        val message = "Users: $usersCount, Goals: $goalsCount"
                        Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
                        Log.d("ApiInfo", message)
                    }
                } catch (e: Exception) {
                    val errorMessage = "Błąd: ${e.message}"
                    Toast.makeText(this@MainActivity, errorMessage, Toast.LENGTH_LONG).show()
                    Log.e("ApiError", errorMessage, e)
                }
            }
        }

    }
}