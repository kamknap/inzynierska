package pl.fithubapp

import android.app.AlertDialog
import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pl.fithubapp.data.ActiveChallenge
import pl.fithubapp.data.PhotoDto
import pl.fithubapp.data.PhotoReference
import kotlinx.coroutines.launch
import pl.fithubapp.data.ChallengeType
import java.io.File
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

enum class DisplayMode {
    BADGES, CHALLENGES, PHOTOS
}

class ProgressUniversalListDialogFragment : DialogFragment() {

    private lateinit var rvList: RecyclerView
    private lateinit var adapter: UniversalProgressAdapter
    private var mode: DisplayMode = DisplayMode.BADGES
    private var currentPhotoUri: Uri? = null
    private var currentPhotoPath: String? = null
    
    private val currentUserId: String
        get() = AuthManager.currentUserId ?: ""

    companion object {
        private const val TAG = "ProgressDialog"
        private const val ARG_MODE = "MODE"
        private const val KEY_PHOTO_PATH = "camera_photo_path"

        fun newInstance(mode: DisplayMode): ProgressUniversalListDialogFragment {
            return ProgressUniversalListDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_MODE, mode.name)
                }
            }
        }
    }

    // Launcher aparatu
    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            currentPhotoPath?.let { path ->
                savePhotoToDatabase(path)
            } ?: Toast.makeText(context, "Błąd: Zgubiono ścieżkę zdjęcia po powrocie z aparatu.", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, "Anulowano robienie zdjęcia", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.getString(KEY_PHOTO_PATH)?.let {
            currentPhotoPath = it
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_PHOTO_PATH, currentPhotoPath)
    }

    interface OnChallengeActionListener {
        fun onChallengeActionCompleted()
    }

    var onChallengeActionListener: OnChallengeActionListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(context).inflate(R.layout.fragment_progress_universal_list_dialog, null)
        rvList = view.findViewById(R.id.rvUniversalList)
        rvList.layoutManager = LinearLayoutManager(context)

        mode = arguments?.getString(ARG_MODE)?.let { DisplayMode.valueOf(it) } ?: DisplayMode.BADGES

        val title = when (mode) {
            DisplayMode.BADGES -> "Twoja Gablota"
            DisplayMode.CHALLENGES -> "Dostępne Wyzwania"
            DisplayMode.PHOTOS -> "Historia Zdjęć"
        }

        adapter = UniversalProgressAdapter(mode,
            onChallengeAction = { challengeId, action -> handleChallengeAction(challengeId, action) },
            onPhotoClick = { photoDto -> showPhotoDetailDialog(photoDto) }
        )
        rvList.adapter = adapter

        loadData()

        val builder = AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setView(view)
            .setNegativeButton("Zamknij", null)

        if (mode == DisplayMode.PHOTOS) {
            builder.setPositiveButton("Dodaj zdjęcie", null)
        }

        val dialog = builder.create()

        dialog.setOnShowListener {
            if (mode == DisplayMode.PHOTOS) {
                val button: Button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                button.setOnClickListener {
                    dispatchTakePictureIntent()
                }
            }
        }

        return dialog
    }

    private fun showPhotoDetailDialog(photo: PhotoDto) {
        val imageView = ImageView(context)
        imageView.adjustViewBounds = true
        imageView.setPadding(20, 20, 20, 20)

        try {
            imageView.setImageURI(Uri.parse(photo.photoUrl))
        } catch (e: Exception) {
            Toast.makeText(context, "Nie można wczytać zdjęcia", Toast.LENGTH_SHORT).show()
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Podgląd zdjęcia")
            .setMessage("Waga: ${photo.weightKg} kg\nData: ${photo.uploadedAt.take(10)}")
            .setView(imageView)
            .setPositiveButton("Zamknij", null)
            .setNegativeButton("Usuń") { _, _ ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Potwierdzenie")
                    .setMessage("Czy na pewno chcesz usunąć to zdjęcie?")
                    .setPositiveButton("Tak") { _, _ -> deletePhoto(photo) }
                    .setNegativeButton("Nie", null)
                    .show()
            }
            .show()
    }

    private fun deletePhoto(photo: PhotoDto) {
        lifecycleScope.launch {
            try {
                if (photo.id == null) return@launch

                Toast.makeText(context, "Usuwanie...", Toast.LENGTH_SHORT).show()

                NetworkModule.api.deletePhoto(photo.id)

                val userProgress = NetworkModule.api.getUserProgress()

                val updatedPhotosList = userProgress.photos.filter { it.photoId != photo.id }

                val updatedProgress = userProgress.copy(photos = updatedPhotosList)
                NetworkModule.api.updateUserProgress(updatedProgress)

                Toast.makeText(context, "Zdjęcie usunięte", Toast.LENGTH_SHORT).show()
                loadData()

            } catch (e: Exception) {
                Log.e("PhotoDelete", "Błąd usuwania", e)
                Toast.makeText(context, "Błąd usuwania: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun dispatchTakePictureIntent() {
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: Exception) {
            Toast.makeText(context, "Błąd tworzenia pliku: ${ex.message}", Toast.LENGTH_SHORT).show()
            null
        }

        photoFile?.also {
            try {
                val photoURI: Uri = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.fileprovider",
                    it
                )
                currentPhotoUri = photoURI
                currentPhotoPath = "file://${it.absolutePath}"

                Log.d("PhotoDebug", "Uruchamiam aparat. Ścieżka: $currentPhotoPath")

                takePictureLauncher.launch(photoURI)
            } catch (e: Exception) {
                Toast.makeText(context, "Błąd FileProvider: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun createImageFile(): File {
        val timeStamp: String = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        val storageDir: File? = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }

    private fun savePhotoToDatabase(localPath: String) {
        lifecycleScope.launch {
            try {
                Toast.makeText(context, "Wysyłanie zdjęcia...", Toast.LENGTH_SHORT).show()

                val user = NetworkModule.api.getCurrentUser()
                val currentWeight = user.profile.weightKg

                val newPhotoDto = PhotoDto(
                    photoUrl = localPath,
                    uploadedAt = Instant.now().toString(),
                    weightKg = currentWeight
                )
                val createdPhoto = NetworkModule.api.addPhoto(newPhotoDto)
                Log.d("PhotoUpload", "Utworzono zdjęcie ID: ${createdPhoto.id}")

                if (createdPhoto.id != null) {
                    val userProgress = NetworkModule.api.getUserProgress()

                    val newPhotoRef = PhotoReference(
                        photoId = createdPhoto.id,
                        tag = "other"
                    )

                    val updatedList = userProgress.photos.toMutableList()
                    updatedList.add(newPhotoRef)

                    val updatedProgress = userProgress.copy(photos = updatedList)
                    NetworkModule.api.updateUserProgress(updatedProgress)

                    Toast.makeText(context, "Zdjęcie zapisane pomyślnie!", Toast.LENGTH_SHORT).show()
                    loadData()
                }

            } catch (e: Exception) {
                Log.e("PhotoUpload", "Błąd zapisu", e)
                Toast.makeText(context, "Błąd zapisu API: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun loadData() {
        lifecycleScope.launch {
            try {
                val userProgress = NetworkModule.api.getUserProgress()

                when (mode) {
                    DisplayMode.BADGES -> {
                        val allBadges = NetworkModule.api.getAllBadges()
                        val uiItems = allBadges.filter { badge ->
                            userProgress.badges.contains(badge.id)
                        }.map { badge ->
                            UniversalItem.BadgeItem(badge, true)
                        }
                        adapter.submitList(uiItems)
                    }
                    DisplayMode.CHALLENGES -> {
                        val allChallenges = NetworkModule.api.getAllChallenges()
                        val activeChallengeId = userProgress.activeChallenges?.takeIf { it.challengeId.isNotEmpty() }?.challengeId
                        val uiItems = allChallenges.map { challenge ->
                            val state = when {
                                activeChallengeId == challenge.id -> ChallengeState.ACTIVE
                                activeChallengeId != null -> ChallengeState.LOCKED
                                else -> ChallengeState.AVAILABLE
                            }
                            UniversalItem.ChallengeItem(challenge, state)
                        }
                        adapter.submitList(uiItems)
                    }
                    DisplayMode.PHOTOS -> {
                        val userPhotoIds = userProgress.photos.map { it.photoId }
                        val allPhotos = NetworkModule.api.getAllPhotos()
                        val userPhotos = allPhotos.filter { photo ->
                            userPhotoIds.contains(photo.id)
                        }.sortedByDescending { it.uploadedAt }

                        val uiItems = userPhotos.map { UniversalItem.PhotoItem(it) }
                        adapter.submitList(uiItems)
                    }
                }
            } catch (e: Exception) {
                if(context != null) {
                    Log.e("LoadData", "Błąd: ${e.message}")
                }
            }
        }
    }

    private fun handleChallengeAction(challengeId: String, action: String) {
        lifecycleScope.launch {
            try {
                val userProgress = NetworkModule.api.getUserProgress()

                when (action) {
                    "START" -> {
                        val hasActiveChallenge = userProgress.activeChallenges?.challengeId?.isNotEmpty() == true

                        if (hasActiveChallenge) {
                            Toast.makeText(context, "Masz już aktywne wyzwanie!", Toast.LENGTH_SHORT).show()
                            return@launch
                        }

                        val challenge = NetworkModule.api.getAllChallenges()
                            .find { it.id == challengeId }

                        if (challenge == null) {
                            Toast.makeText(context, "Nie znaleziono wyzwania", Toast.LENGTH_SHORT).show()
                            return@launch
                        }

                        val totalToFinish = if (challenge.type == ChallengeType.WEIGHT_LOSS) {
                            challenge.targetValue * 10 // Dla WEIGHT_LOSS używamy dziesiętnych części kg
                        } else {
                            challenge.targetValue
                        }
                        
                        val newActiveChallenge = ActiveChallenge(
                            challengeId = challengeId,
                            counter = 0,
                            totalToFinish = totalToFinish,
                            startedDate = Instant.now().toString()
                        )

                        val updatedProgress = userProgress.copy(
                            activeChallenges = newActiveChallenge
                        )

                        NetworkModule.api.updateUserProgress(updatedProgress)

                        Toast.makeText(
                            context,
                            "Rozpoczęto wyzwanie: ${challenge.name}",
                            Toast.LENGTH_SHORT
                        ).show()

                        Log.d("ChallengeAction", "Rozpoczęto wyzwanie: ${challenge.name}")
                    }

                    "CANCEL" -> {
                        val updatedProgress = userProgress.copy(
                            activeChallenges = null
                        )

                        NetworkModule.api.updateUserProgress(updatedProgress)

                        Toast.makeText(
                            context,
                            "Anulowano wyzwanie",
                            Toast.LENGTH_SHORT
                        ).show()

                        Log.d("ChallengeAction", "Anulowano wyzwanie")
                    }
                }

                loadData()
                onChallengeActionListener?.onChallengeActionCompleted()

            } catch (e: Exception) {
                Log.e(TAG, "Błąd: ${e.message}", e)
                Toast.makeText(
                    context,
                    "Błąd: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}