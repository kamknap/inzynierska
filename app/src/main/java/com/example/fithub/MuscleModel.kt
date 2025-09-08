package com.example.fithub

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MuscleModel : AppCompatActivity() {
    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.muscle_model)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        webView = findViewById(R.id.webView) // Znajdź WebView w nowym layoucie
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient()
        webView.addJavascriptInterface(WebAppInterface(), "AndroidInterface")

        val url = intent.getStringExtra("url") ?: "file:///android_asset/index/front.html"
        webView.loadUrl(url)
    }

    inner class WebAppInterface {
        @JavascriptInterface
        fun onPathClicked(id: String) {
            runOnUiThread {
                Toast.makeText(this@MuscleModel, "Kliknięto w: $id", Toast.LENGTH_SHORT).show()
                // obsluga kilkniecia
            }
        }
    }

}