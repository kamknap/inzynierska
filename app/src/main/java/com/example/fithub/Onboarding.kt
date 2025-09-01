package com.example.fithub

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class Onboarding : AppCompatActivity() {
    private lateinit var name: EditText
    private lateinit var weight: EditText
    private lateinit var height: EditText
    private lateinit var btnCalculate: Button
    private lateinit var result: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.onboarding)

        name = findViewById(R.id.etName)
        weight = findViewById(R.id.etWeigth)
        height = findViewById(R.id.etHeight)
        btnCalculate = findViewById(R.id.btnCalculate)
        result = findViewById(R.id.etResult)

        btnCalculate.setOnClickListener {
            val weightVal = weight.text.toString().toDouble()
            val heightVal = height.text.toString().toDouble()
            val bmi = calculateBMI(weightVal, heightVal)
            result.setText(bmi.toString())
        }
    }

    private fun calculateBMI(weight: Double, height: Double): Double {
        val heightInMeters = height / 100
        return weight / (heightInMeters * heightInMeters)
    }
}
