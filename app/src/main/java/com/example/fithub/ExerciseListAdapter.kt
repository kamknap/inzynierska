package com.example.fithub

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.fithub.data.ExerciseDto

class ExerciseListAdapter(
    private val onExerciseClick: (ExerciseDto) -> Unit = {},
    private val onDeleteClick: ((ExerciseDto) -> Unit)? = null,
    private val showDeleteButton: Boolean = true
) : ListAdapter<ExerciseListAdapter.ExerciseItem, ExerciseListAdapter.ExerciseViewHolder>(ExerciseDiffCallback()) {

    data class ExerciseItem(
        val exercise: ExerciseDto,
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_exercise, parent, false)
        return ExerciseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvExerciseName: TextView = itemView.findViewById(R.id.tvExerciseName)
        private val tvExerciseDescription: TextView = itemView.findViewById(R.id.tvExerciseDescription)
        private val tvExerciseInstructions: TextView = itemView.findViewById(R.id.tvExerciseInstructions)
        private val tvVideoPlaceholder: TextView = itemView.findViewById(R.id.tvVideoPlaceholder)
        private val flYoutubeContainer: View = itemView.findViewById(R.id.flYoutubeContainer)
        private val llExpandedDetails: LinearLayout = itemView.findViewById(R.id.llExpandedDetails)
        private val btnExpandExercise: ImageButton = itemView.findViewById(R.id.btnExpandExercise)
        private val btnDeleteExercise: ImageButton = itemView.findViewById(R.id.btnDeleteExercise)

        fun bind(item: ExerciseItem) {
            val exercise = item.exercise

            // Nazwa
            tvExerciseName.text = exercise.name ?: "Ćwiczenie"

            // Opis i instrukcje
            tvExerciseDescription.text = exercise.desc ?: "Brak opisu"
            tvExerciseInstructions.text = exercise.instructions?.mapIndexed { index, instruction ->
                "${index + 1}. $instruction"
            }?.joinToString("\n") ?: "Brak instrukcji"

            // Video YouTube
            if (exercise.videoUrl != null) {
                tvVideoPlaceholder.text = "Kliknij aby obejrzeć instrukcję"
                flYoutubeContainer.setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(exercise.videoUrl))
                    itemView.context.startActivity(intent)
                }
            } else {
                tvVideoPlaceholder.text = "Brak video"
                flYoutubeContainer.isClickable = false
            }

            // Kliknięcie w całe ćwiczenie
            var isExpanded = false
            itemView.setOnClickListener {
                isExpanded = !isExpanded
                llExpandedDetails.visibility = if (isExpanded) View.VISIBLE else View.GONE
                btnExpandExercise.rotation = if (isExpanded) 180f else 0f            }

            // Przycisk usuwania
            if (showDeleteButton && onDeleteClick != null) {
                btnDeleteExercise.visibility = View.VISIBLE
                btnDeleteExercise.setOnClickListener {
                    onDeleteClick.invoke(exercise)
                }
            } else {
                btnDeleteExercise.visibility = View.GONE
            }
        }
    }

    class ExerciseDiffCallback : DiffUtil.ItemCallback<ExerciseItem>() {
        override fun areItemsTheSame(oldItem: ExerciseItem, newItem: ExerciseItem): Boolean {
            return oldItem.exercise.id == newItem.exercise.id
        }

        override fun areContentsTheSame(oldItem: ExerciseItem, newItem: ExerciseItem): Boolean {
            return oldItem == newItem
        }
    }
}