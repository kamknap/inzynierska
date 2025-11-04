package com.example.fithub

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.ui.graphics.Path
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.fithub.data.UserWeightHistoryDto
import java.text.SimpleDateFormat
import java.util.*

class WeightHistoryAdapter(private val currentWeight: Double? = null, private val goalType: String? = null) : ListAdapter<UserWeightHistoryDto, WeightHistoryAdapter.WeightHistoryViewHolder>(WeightHistoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeightHistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_weight_history, parent, false)
        return WeightHistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: WeightHistoryViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, currentWeight, goalType)
    }

    class WeightHistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvWeight: TextView = itemView.findViewById(R.id.tvHistoryWeight)
        private val tvDate: TextView = itemView.findViewById(R.id.tvHistoryDate)

        fun bind(history: UserWeightHistoryDto, currentWeight: Double?, goalType: String?) {
            tvWeight.text = "${history.weightKg} kg"

            if (currentWeight != null && goalType != null) {
                when (goalType) {
                    "lose_weight" -> {
                        when {
                            history.weightKg < currentWeight -> {
                                tvWeight.setTextColor(Color.parseColor("#4CAF50")) // Zielony
                            }
                            history.weightKg > currentWeight -> {
                                tvWeight.setTextColor(Color.parseColor("#F44336")) // Czerwony
                            }
                            else -> {
                                tvWeight.setTextColor(Color.parseColor("#2196F3")) // Niebieski
                            }
                        }
                    }
                    "gain_weight" -> {
                        when {
                            history.weightKg > currentWeight -> {
                                tvWeight.setTextColor(Color.parseColor("#4CAF50")) // Zielony
                            }
                            history.weightKg < currentWeight -> {
                                tvWeight.setTextColor(Color.parseColor("#F44336")) // Czerwony
                            }
                            else -> {
                                tvWeight.setTextColor(Color.parseColor("#2196F3")) // Niebieski
                            }
                        }
                    }
                    "maintain" -> {
                        tvWeight.setTextColor(Color.parseColor("#2196F3")) // Niebieski
                    }
                    else -> {
                        tvWeight.setTextColor(Color.BLACK)
                    }
                }
            } else {
                tvWeight.setTextColor(Color.BLACK)
            }

            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                inputFormat.timeZone = TimeZone.getTimeZone("UTC")
                val outputFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

                val date = inputFormat.parse(history.measuredAt)
                tvDate.text = date?.let { outputFormat.format(it) } ?: history.measuredAt.substring(0, 10)
            } catch (e: Exception) {
                tvDate.text = history.measuredAt.substring(0, 10)
            }
        }
    }

    class WeightHistoryDiffCallback : DiffUtil.ItemCallback<UserWeightHistoryDto>() {
        override fun areItemsTheSame(oldItem: UserWeightHistoryDto, newItem: UserWeightHistoryDto): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: UserWeightHistoryDto, newItem: UserWeightHistoryDto): Boolean {
            return oldItem == newItem
        }
    }
}