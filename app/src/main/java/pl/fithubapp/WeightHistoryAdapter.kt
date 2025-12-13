package pl.fithubapp

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import pl.fithubapp.data.UserWeightHistoryDto
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

class WeightHistoryAdapter(private val referenceWeight: Double? = null, private val goalType: String? = null) : ListAdapter<UserWeightHistoryDto, WeightHistoryAdapter.WeightHistoryViewHolder>(WeightHistoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeightHistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_weight_history, parent, false)
        return WeightHistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: WeightHistoryViewHolder, position: Int) {
        val item = getItem(position)
        val previousItem = if (position < itemCount - 1) getItem(position + 1) else null
        holder.bind(item, previousItem, referenceWeight, goalType)
    }

    class WeightHistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvWeight: TextView = itemView.findViewById(R.id.tvHistoryWeight)
        private val tvDate: TextView = itemView.findViewById(R.id.tvHistoryDate)
        private val tvWeightChange: TextView = itemView.findViewById(R.id.tvHistoryWeightChange)

        fun bind(history: UserWeightHistoryDto, previousHistory: UserWeightHistoryDto?, currentWeight: Double?, goalType: String?) {
            tvWeight.text = "${history.weightKg} kg"

            if (previousHistory != null) {
                val difference = history.weightKg - previousHistory.weightKg
                val formattedDiff = String.format(Locale.getDefault(), "%.1f", abs(difference))

                when {
                    difference > 0 -> {
                        tvWeightChange.text = "+$formattedDiff"
//                        tvWeightChange.setTextColor(Color.parseColor("#F44336"))
                    }
                    difference < 0 -> {
                        tvWeightChange.text = "-$formattedDiff"
//                        tvWeightChange.setTextColor(Color.parseColor("#4CAF50"))
                    }
                    else -> {
                        tvWeightChange.text = "0.0"
//                        tvWeightChange.setTextColor(Color.parseColor("#2196F3"))
                    }
                }
                tvWeightChange.visibility = View.VISIBLE
            } else {
                tvWeightChange.visibility = View.GONE
            }

            if (currentWeight != null && goalType != null) {
                when (goalType) {
                    "lose_weight" -> {
                        when {
                            history.weightKg < currentWeight -> {
                                tvWeight.setTextColor(Color.parseColor("#4CAF50"))
                            }
                            history.weightKg > currentWeight -> {
                                tvWeight.setTextColor(Color.parseColor("#F44336"))
                            }
                            else -> {
                                tvWeight.setTextColor(Color.parseColor("#2196F3"))
                            }
                        }
                    }
                    "gain_weight" -> {
                        when {
                            history.weightKg > currentWeight -> {
                                tvWeight.setTextColor(Color.parseColor("#4CAF50"))
                            }
                            history.weightKg < currentWeight -> {
                                tvWeight.setTextColor(Color.parseColor("#F44336"))
                            }
                            else -> {
                                tvWeight.setTextColor(Color.parseColor("#2196F3"))
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
                val zonedDateTime = ZonedDateTime.parse(history.measuredAt)
                val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
                tvDate.text = zonedDateTime.format(formatter)
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