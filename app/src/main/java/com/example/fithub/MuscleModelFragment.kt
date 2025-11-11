package com.example.fithub

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.fragment.app.Fragment
import kotlin.math.abs

class MuscleModelFragment : Fragment() {
    private lateinit var webView: WebView
    private lateinit var gestureDetector: GestureDetector
    private var isFrontView = true

    companion object {
        private const val SWIPE_THRESHOLD = 100
        private const val SWIPE_VELOCITY_THRESHOLD = 100
        private const val URL = "file:///android_asset/index/"
        private const val ARG_URL = "url"

        fun newInstance(url: String? = null): MuscleModelFragment {
            return MuscleModelFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_URL, url)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_muscle_model, container, false)
    }

    @SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        webView = view.findViewById(R.id.webView)
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient()
        webView.addJavascriptInterface(WebAppInterface(), "AndroidInterface")

        gestureDetector = GestureDetector(requireContext(), SwipeGestureListener())
        webView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            false
        }

        loadView()
    }

    private fun loadView() {
        val url = arguments?.getString(ARG_URL) ?: URL

        if (isFrontView) {
            webView.loadUrl(url + "front.html")
        } else {
            webView.loadUrl(url + "back.html")
        }
    }

    private fun switchView() {
        isFrontView = !isFrontView
        loadView()
    }

    inner class SwipeGestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            if (e1 == null) return false

            val diffX = e2.x - e1.x
            val diffY = e2.y - e1.y

            if (abs(diffX) > abs(diffY) &&
                abs(diffX) > SWIPE_THRESHOLD &&
                abs(velocityX) > SWIPE_VELOCITY_THRESHOLD
            ) {
                switchView()
                return true
            }
            return false
        }
    }

    inner class WebAppInterface {
        @JavascriptInterface
        fun onPathClicked(id: String) {
            activity?.runOnUiThread {
                Toast.makeText(requireContext(), "Kliknięto w: $id", Toast.LENGTH_SHORT).show()
                // Możesz tutaj dodać callback do UserTrainingFragment
                (parentFragment as? UserTrainingFragment)?.onMuscleClicked(id)
            }
        }
    }
}