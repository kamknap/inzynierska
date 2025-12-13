package pl.fithubapp

import android.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import pl.fithubapp.data.AddExerciseToPlanDto
import pl.fithubapp.data.ExerciseDto
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException

class AddExerciseToPlanDialogFragment : SearchDialogFragment<ExerciseDto>() {

    interface OnExerciseAddedToPlanListener {
        fun onExerciseAddedToPlan()
    }

    var onExerciseAddedToPlanListener: OnExerciseAddedToPlanListener? = null

    override fun getTitle(): String {
        val muscleId = arguments?.getString("muscleId")
        return if (muscleId != null) {
            "Ćwiczenia dla: ${MuscleTranslator.translate(muscleId)}"
        } else {
            "Dodaj ćwiczenie"
        }
    }

    override fun getSearchHint(): String = "Wyszukaj ćwiczenie.."

    override fun onDialogCreated() {
        val muscleId = arguments?.getString("muscleId")
        if (muscleId != null) {
            lifecycleScope.launch {
                try {
                    val results = performSearch("")
                    displayResults(results)
                } catch (e: Exception) {
                    Log.e("AddExerciseToPlan", "Błąd ładowania ćwiczeń", e)
                    Toast.makeText(requireContext(), "Błąd: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun shouldShowSearchField(): Boolean {
        return arguments?.getString("muscleId") == null
    }
    override suspend fun performSearch(query: String): List<ExerciseDto> {
        val muscleId = arguments?.getString("muscleId")
        return if (muscleId != null){
            NetworkModule.api.getExercisesByMuscleId(muscleId)
        } else{
            NetworkModule.api.getExercisesByName(query)
        }
    }

    override fun createResultView(item: ExerciseDto): View {
        val view = LayoutInflater.from(requireContext())
            .inflate(android.R.layout.simple_list_item_2, llSearchResults, false)

        view.findViewById<TextView>(android.R.id.text1).text = item.name ?: "Bez nazwy"

        val muscleInfo = item.muscleIds?.joinToString(", ") { muscleId ->
            MuscleTranslator.translate(muscleId)
        } ?: "Brak danych"

        val metsInfo = item.mets?.let { "METS: $it" } ?: ""
        view.findViewById<TextView>(android.R.id.text2).text =
            "$muscleInfo${if (metsInfo.isNotEmpty()) " • $metsInfo" else ""}"

        return view
    }

    override fun onItemSelected(item: ExerciseDto) {
        showExerciseDetailsDialog(item)
    }

    private fun showExerciseDetailsDialog(exercise: ExerciseDto) {
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.item_exercise, null)

        val tvName = view.findViewById<TextView>(R.id.tvExerciseName)
        val tvDesc = view.findViewById<TextView>(R.id.tvExerciseDescription)
        val tvInstructions = view.findViewById<TextView>(R.id.tvExerciseInstructions)
        val llExpandedDetails = view.findViewById<View>(R.id.llExpandedDetails)
        val btnExpand = view.findViewById<View>(R.id.btnExpandExercise)
        val btnDelete = view.findViewById<View>(R.id.btnDeleteExercise)
        val webViewVideo = view.findViewById<WebView>(R.id.webViewYoutube)

        tvName.text = exercise.name ?: "Ćwiczenie"
        tvDesc.text = exercise.desc ?: "Brak opisu"
        tvInstructions.text = exercise.instructions?.mapIndexed { index, instruction ->
            "${index + 1}. $instruction"
        }?.joinToString("\n") ?: "Brak instrukcji"

        llExpandedDetails.visibility = View.VISIBLE
        btnExpand.visibility = View.GONE
        btnDelete.visibility = View.GONE

        setupWebView(webViewVideo)

        if (exercise.videoUrl != null) {
            webViewVideo.visibility = View.VISIBLE
            playVideo(webViewVideo, exercise.videoUrl)
        } else {
            webViewVideo.visibility = View.GONE
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Szczegóły ćwiczenia")
            .setView(view)
            .setPositiveButton("Dodaj") { _, _ ->
                addExerciseToPlan(exercise)
            }
            .setNegativeButton("Anuluj", null)
            .create()

        dialog.setOnDismissListener {
            stopVideo(webViewVideo)
        }

        dialog.show()
    }

    private fun setupWebView(webView: WebView) {
        webView.settings.apply {
            javaScriptEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            mediaPlaybackRequiresUserGesture = false
            domStorageEnabled = true
            cacheMode = WebSettings.LOAD_NO_CACHE
        }
        webView.setBackgroundColor(0)
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
    }

    private fun playVideo(webView: WebView, videoUrl: String) {
        val embedHtml = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    html, body { 
                        width: 100%; 
                        height: 100%; 
                        background: transparent;
                        overflow: hidden; 
                    }
                    .video-container { 
                        position: relative; 
                        width: 100%; 
                        height: 0; 
                        padding-bottom: 56.25%;
                        background: transparent;
                    }
                    .video-container iframe { 
                        position: absolute; 
                        top: 0; 
                        left: 0; 
                        width: 100%; 
                        height: 100%; 
                        border: none;
                        display: block;
                    }
                </style>
            </head>
            <body>
                <div class="video-container">
                    <iframe 
                        src="$videoUrl?badge=0&autopause=1&autoplay=0&loop=0" 
                        allow="autoplay; fullscreen; picture-in-picture; clipboard-write" 
                        allowfullscreen
                        frameborder="0"
                        title="Vimeo Video Player">
                    </iframe>
                </div>
            </body>
            </html>
        """.trimIndent()

        webView.loadDataWithBaseURL(
            "https://player.vimeo.com",
            embedHtml,
            "text/html",
            "UTF-8",
            null
        )
    }

    private fun stopVideo(webView: WebView) {
        webView.loadUrl("about:blank")
        webView.clearHistory()
        webView.clearCache(true)
    }

    private fun addExerciseToPlan(exercise: ExerciseDto) {
        val userId = arguments?.getString("userId") ?: return
        val planName = arguments?.getString("planName") ?: "Plan Treningowy 1"

        lifecycleScope.launch {
            try {
                val userExercisePlans = NetworkModule.api.getUserExercisePlans(userId)

                Log.d("AddExerciseToPlan", "Pobrano ${userExercisePlans.size} planów")

                val existingPlan = userExercisePlans.find { it.planName == planName }

                if (existingPlan != null) {
                    Log.d("AddExerciseToPlan", "Znaleziono plan: ${existingPlan.planName}, ID: ${existingPlan.id}")
                    Log.d("AddExerciseToPlan", "Dodawanie ćwiczenia: ${exercise.id}")

                    val exerciseIdDto = AddExerciseToPlanDto(exerciseId = exercise.id)

                    NetworkModule.api.addExerciseToPlan(
                        existingPlan.id,
                        exerciseIdDto
                    )

                    Toast.makeText(
                        requireContext(),
                        "Dodano: ${exercise.name}",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Nie znaleziono planu: $planName",
                        Toast.LENGTH_LONG
                    ).show()
                }

                onExerciseAddedToPlanListener?.onExerciseAddedToPlan()
                dismiss()

            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("AddExerciseToPlan", "HTTP Error ${e.code()}: $errorBody", e)

                val errorMessage = when (e.code()) {
                    400 -> errorBody?.let {
                        try {
                            JSONObject(it).getString("message")
                        } catch (_: Exception) {
                            "To ćwiczenie zostało już dodane do planu"
                        }
                    } ?: "Błąd: nieprawidłowe dane"

                    404 -> "Plan treningowy nie został znaleziony"
                    else -> "Błąd serwera: ${e.code()}"
                }

                Toast.makeText(
                    requireContext(),
                    errorMessage,
                    Toast.LENGTH_LONG
                ).show()
            }
            catch (e: Exception) {
                Log.e("AddExerciseToPlan", "Error", e)
                Toast.makeText(
                    requireContext(),
                    "Błąd: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}

object MuscleTranslator {
    private val translations = mapOf(
// Brzuch
        "abs" to "Brzuch",
        "abdominals" to "Brzuch",
        "external_obliques" to "Skośne brzucha",
        "obliques" to "Skośne brzucha",

        // Klatka
        "chest" to "Klatka piersiowa",
        "pectorals" to "Klatka piersiowa",
        "pectoralis" to "Klatka piersiowa",
        "serratus_anterior" to "Zębaty przedni",

        // Plecy
        "back" to "Plecy",
        "lats" to "Najszerszy grzbietu",
        "latissimus_dorsi" to "Najszerszy grzbietu",
        "traps" to "Czworoboczny (Kaptury)",
        "trapezius" to "Czworoboczny (Kaptury)",
        "lower_back" to "Lędźwia",
        "middle_back" to "Środek pleców",
        "upper_back" to "Góra pleców",
        "neck" to "Szyja",

        // Ramiona
        "shoulders" to "Barki",
        "deltoid" to "Barki",
        "biceps" to "Biceps",
        "triceps" to "Triceps",
        "forearm" to "Przedramiona",
        "brachialis" to "Ramienny",

        // Nogi - Uda
        "quadriceps" to "Czworogłowe uda",
        "quads" to "Czworogłowe uda",
        "hamstrings" to "Dwugłowe uda",
        "sartorius" to "Krawiecki",
        "adductors" to "Przywodziciele",
        "abductors" to "Odwodziciele",
        "glutes" to "Pośladki",
        "gluteus_maximus" to "Pośladki",

        // Nogi - Łydki
        "calves" to "Łydki",
        "gastrocnemius" to "Brzuchaty łydki",
        "soleus" to "Płaszczkowaty",
        "tibialis_anterior" to "Piszczelowy przedni",

        // Inne
        "cardio" to "Kardio"
    )

    fun translate(englishName: String?): String {
        if (englishName == null) return ""
        return translations[englishName.lowercase()] ?: englishName.replaceFirstChar { it.uppercase() }
    }
}