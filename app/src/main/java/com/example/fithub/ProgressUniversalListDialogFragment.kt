package com.example.fithub

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fithub.data.ActiveChallenge
import kotlinx.coroutines.launch
import java.time.Instant


enum class DisplayMode {
    BADGES, CHALLENGES, PHOTOS
}

class ProgressUniversalListDialogFragment : DialogFragment() {

    private lateinit var rvList: RecyclerView
    private lateinit var adapter: UniversalProgressAdapter
    private var mode: DisplayMode = DisplayMode.BADGES
    private val currentUserId = "68cbc06e6cdfa7faa8561f82"

    interface OnChallengeActionListener {
        fun onChallengeActionCompleted()
    }

    var onChallengeActionListener: OnChallengeActionListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(context).inflate(R.layout.fragment_progress_universal_list_dialog, null)
        rvList = view.findViewById(R.id.rvUniversalList)
        rvList.layoutManager = LinearLayoutManager(context)

        val modeStr = arguments?.getString("MODE") ?: "BADGES"
        mode = DisplayMode.valueOf(modeStr)

        val title = when (mode) {
            DisplayMode.BADGES -> "Twoja Gablota"
            DisplayMode.CHALLENGES -> "Dostępne Wyzwania"
            DisplayMode.PHOTOS -> "Historia Zdjęć"
        }

        adapter = UniversalProgressAdapter(mode,
            onChallengeAction = { challengeId, action -> handleChallengeAction(challengeId, action) },
            onPhotoClick = { photoUrl -> showPhotoPreview(photoUrl) }
        )
        rvList.adapter = adapter

        loadData()

        return AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setView(view)
            .setNegativeButton("Zamknij", null)
            .create()
    }

    private fun loadData() {
        lifecycleScope.launch {
            try {
                val userProgress = NetworkModule.api.getUserProgress(currentUserId)

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

                        val activeChallengeId = userProgress.activeChallenges
                            ?.takeIf { it.challengeId.isNotEmpty() }
                            ?.challengeId

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
                        val photos = NetworkModule.api.getAllPhotos()
                        val uiItems = photos.map { UniversalItem.PhotoItem(it) }
                        adapter.submitList(uiItems)
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Błąd ładowania: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleChallengeAction(challengeId: String, action: String) {
        lifecycleScope.launch {
            try {
                val userProgress = NetworkModule.api.getUserProgress(currentUserId)

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

                        val newActiveChallenge = ActiveChallenge(
                            challengeId = challengeId,
                            counter = 0,
                            totalToFinish = challenge.targetValue,
                            startedDate = Instant.now().toString()
                        )

                        val updatedProgress = userProgress.copy(
                            activeChallenges = newActiveChallenge
                        )

                        NetworkModule.api.updateUserProgress(currentUserId, updatedProgress)

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

                        NetworkModule.api.updateUserProgress(currentUserId, updatedProgress)

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
                Log.e("ChallengeAction", "Błąd: ${e.message}", e)
                Toast.makeText(
                    context,
                    "Błąd: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showPhotoPreview(url: String) {
        Toast.makeText(context, "Otwieranie zdjęcia...", Toast.LENGTH_SHORT).show()
    }

    companion object {
        fun newInstance(mode: DisplayMode): ProgressUniversalListDialogFragment {
            val fragment = ProgressUniversalListDialogFragment()
            val args = Bundle()
            args.putString("MODE", mode.name)
            fragment.arguments = args
            return fragment
        }
    }
}