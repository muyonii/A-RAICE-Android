package com.example.a_raiceandrd.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.a_raiceandrd.R
import com.example.a_raiceandrd.databinding.ItemOutbreakRowBinding
import java.util.Locale

data class OutbreakItem(
    val id: Int?,
    val diseaseName: String?,
    val municipality: String?,
    val severity: String?,
    val status: String?,
    val createdAt: String?,
    val scansCount: Int? = null
)

class OutbreakAdapter(
    private val onClick: (OutbreakItem) -> Unit
) : ListAdapter<OutbreakItem, OutbreakAdapter.VH>(Diff) {

    class VH(val binding: ItemOutbreakRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemOutbreakRowBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        with(holder.binding) {
            tvDiseaseName.text = item.diseaseName ?: root.context.getString(R.string.Disease)

            val muni = item.municipality?.replaceFirstChar { it.uppercase() }
            val dateStr = item.createdAt?.take(10)
            tvLocationAndDate.text = listOfNotNull(muni, dateStr).joinToString(" · ")

            val sev = item.severity?.lowercase(Locale.ROOT)
            val stat = item.status?.lowercase(Locale.ROOT)

            val isResolved = stat == "resolved"
            val badgeText = if (isResolved) "RESOLVED" else (sev?.uppercase() ?: "ACTIVE")
            tvBadge.text = badgeText

            val (colorRes, bgRes) = when {
                isResolved -> R.color.ok_green to R.color.badge_low_bg
                sev == "high" -> R.color.rust to R.color.badge_high_bg
                sev == "medium" -> R.color.gold to R.color.badge_moderate_bg
                sev == "low" -> R.color.sky to R.color.surface_chip
                else -> R.color.rust to R.color.badge_high_bg
            }

            val ctx = root.context
            val color = ContextCompat.getColor(ctx, colorRes)
            val bg = ContextCompat.getColor(ctx, bgRes)

            severityDot.backgroundTintList = ColorStateList.valueOf(color)
            tvBadge.setTextColor(color)
            tvBadge.backgroundTintList = ColorStateList.valueOf(bg)

            root.setOnClickListener { onClick(item) }
        }
    }

    private object Diff : DiffUtil.ItemCallback<OutbreakItem>() {
        override fun areItemsTheSame(old: OutbreakItem, new: OutbreakItem) = old.id == new.id
        override fun areContentsTheSame(old: OutbreakItem, new: OutbreakItem) = old == new
    }
}
