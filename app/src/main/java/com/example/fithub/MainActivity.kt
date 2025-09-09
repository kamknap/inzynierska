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
            val intent = Intent(this, Onboarding::class.java)
            startActivity(intent)
        }

        //muscle model
        val buttonMuscle = findViewById<Button>(R.id.btnMuscle)
        buttonMuscle.setOnClickListener {
            val intent = Intent(this, MuscleModel::class.java)
            intent.putExtra("url", "file:///android_asset/index/back.html")
            startActivity(intent)
        }

        //api
        val btnApi = findViewById<Button>(R.id.btnApi)
        btnApi.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val users = NetworkModule.api.getUsers()
                    if (users.isNotEmpty()) {
                        val age = users[0].bmi
                        Toast.makeText(this@MainActivity, "Wiek: $age", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this@MainActivity, "Brak użytkowników", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@MainActivity, "Błąd: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

    }
}