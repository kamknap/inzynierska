package com.example.fithub

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

abstract class SearchDialogFragment<T> : DialogFragment() {

    protected var searchJob: Job? = null
    protected lateinit var llSearchResults: LinearLayout

    abstract fun getTitle(): String
    abstract fun getSearchHint(): String
    abstract suspend fun performSearch(query: String): List<T>
    abstract fun createResultView(item: T): android.view.View
    abstract fun onItemSelected(item: T)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val mainLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        val etSearch = EditText(requireContext()).apply {
            hint = getSearchHint()
            setSingleLine(true)
        }

        val scrollView = ScrollView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                400
            )
        }

        llSearchResults = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
        }

        scrollView.addView(llSearchResults)
        mainLayout.addView(etSearch)
        mainLayout.addView(scrollView)

        etSearch.doAfterTextChanged { text ->
            val query = text.toString().trim()
            searchJob?.cancel()

            if (query.length >= 2) {
                searchJob = lifecycleScope.launch {
                    delay(500)
                    try {
                        val results = performSearch(query)
                        displayResults(results)
                    } catch (e: Exception) {
                        android.util.Log.e(this@SearchDialogFragment::class.simpleName, "Bład wyszukiwania", e)
                    }
                }
            } else {
                llSearchResults.removeAllViews()
            }
        }

        return AlertDialog.Builder(requireContext())
            .setTitle(getTitle())
            .setView(mainLayout)
            .setNegativeButton("Zamknij", null)
            .create()
    }

    private fun displayResults(results: List<T>) {
        llSearchResults.removeAllViews()

        results.forEach { item ->
            val view = createResultView(item)
            view.setOnClickListener {
                onItemSelected(item)
            }
            llSearchResults.addView(view)
        }
    }
}