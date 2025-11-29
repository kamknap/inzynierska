package com.example.fithub

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fithub.data.BadgeDto
import com.example.fithub.data.ChallengeDto
import com.example.fithub.data.PhotoDto

enum class ChallengeState { AVAILABLE, ACTIVE, LOCKED }

sealed class UniversalItem {
    data class BadgeItem(val dto: BadgeDto, val isUnlocked: Boolean) : UniversalItem()
    data class ChallengeItem(val dto: ChallengeDto, val state: ChallengeState) : UniversalItem()
    data class PhotoItem(val dto: PhotoDto) : UniversalItem()
}

class UniversalProgressAdapter(
    private val mode: DisplayMode,
    private val onChallengeAction: (String, String) -> Unit,
    private val onPhotoClick: (String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items: List<UniversalItem> = emptyList()

    fun submitList(newItems: List<UniversalItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return mode.ordinal
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (DisplayMode.values()[viewType]) {
            DisplayMode.BADGES -> BadgeViewHolder(inflater.inflate(R.layout.item_universal_badge, parent, false))
            DisplayMode.CHALLENGES -> ChallengeViewHolder(inflater.inflate(R.layout.item_universal_challenge, parent, false))
            DisplayMode.PHOTOS -> PhotoViewHolder(inflater.inflate(R.layout.item_universal_photo, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is UniversalItem.BadgeItem -> (holder as BadgeViewHolder).bind(item)
            is UniversalItem.ChallengeItem -> (holder as ChallengeViewHolder).bind(item, onChallengeAction)
            is UniversalItem.PhotoItem -> (holder as PhotoViewHolder).bind(item, onPhotoClick)
        }
    }

    override fun getItemCount(): Int = items.size

    class BadgeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.ivBadgeIcon)
        val name: TextView = view.findViewById(R.id.tvBadgeName)
        val desc: TextView = view.findViewById(R.id.tvBadgeDesc)

        fun bind(item: UniversalItem.BadgeItem) {
            name.text = item.dto.name
            desc.text = item.dto.desc

            if (item.isUnlocked) {
                itemView.alpha = 1.0f
                name.setTextColor(Color.BLACK)
                icon.setColorFilter(null)
            } else {
                itemView.alpha = 0.5f
                name.setTextColor(Color.GRAY)
                icon.setColorFilter(Color.GRAY) // Szara ikona
                desc.text = "Zablokowane"
            }
        }
    }

    class ChallengeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvChallengeName)
        val details: TextView = view.findViewById(R.id.tvChallengeDetails)
        val btnAction: Button = view.findViewById(R.id.btnChallengeAction)

        fun bind(item: UniversalItem.ChallengeItem, actionListener: (String, String) -> Unit) {
            name.text = item.dto.name
            details.text = "${item.dto.desc}\n | Nagroda: ${item.dto.pointsForComplete} pkt"

            when (item.state) {
                ChallengeState.ACTIVE -> {
                    btnAction.text = "Anuluj"
                    btnAction.isEnabled = true
                    btnAction.setBackgroundColor(Color.RED)
                    btnAction.setOnClickListener { actionListener(item.dto.id ?: "", "CANCEL") }
                }
                ChallengeState.AVAILABLE -> {
                    btnAction.text = "Rozpocznij"
                    btnAction.isEnabled = true
                    btnAction.setBackgroundColor(Color.parseColor("#4CAF50"))
                    btnAction.setOnClickListener { actionListener(item.dto.id ?: "", "START") }
                }
                ChallengeState.LOCKED -> {
                    btnAction.text = "Niedostępne"
                    btnAction.isEnabled = false
                    btnAction.setBackgroundColor(Color.GRAY)
                }
            }
        }
    }

    class PhotoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val date: TextView = view.findViewById(R.id.tvPhotoDate)
        val weight: TextView = view.findViewById(R.id.tvPhotoWeight)
        // val image: ImageView = ...

        fun bind(item: UniversalItem.PhotoItem, clickListener: (String) -> Unit) {
            date.text = item.dto.uploadedAt.take(10)
            weight.text = "${item.dto.weightKg} kg"

            itemView.setOnClickListener {
                clickListener(item.dto.photoUrl)
            }
        }
    }
}