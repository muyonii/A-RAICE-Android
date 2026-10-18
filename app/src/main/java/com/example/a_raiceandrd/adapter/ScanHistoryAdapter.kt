package com.example.a_raiceandrd.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.a_raiceandrd.R
import com.example.a_raiceandrd.data.repository.api.ScanDto
import com.example.a_raiceandrd.databinding.ItemScanHistoryBinding
import com.example.a_raiceandrd.util.formatTime
import java.util.Locale

class ScanHistoryAdapter(
    private val isAdmin: Boolean,
    private val onItemClick: (ScanDto) -> Unit
) : ListAdapter<ScanDto, ScanHistoryAdapter.VH>(DiffCallback) {

    class VH(val binding: ItemScanHistoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemScanHistoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        val ctx = holder.itemView.context

        with(holder.binding) {
            val diseaseName = item.disease?.name ?: ctx.getString(R.string.Scans_Healthy)
            tvScanDisease.text = diseaseName

            val timeLabel = formatTime(item.createdAt).ifEmpty { "Recently" }
            val confLabel = item.confidence?.let { " · ${(it * 100).toInt()}%" }.orEmpty()
            tvScanMeta.text = "$timeLabel$confLabel"

            if (isAdmin && !item.farmer?.name.isNullOrBlank()) {
                tvScanFarmer.isVisible = true
                tvScanFarmer.text = "Farmer: ${item.farmer?.name}"
            } else {
                tvScanFarmer.isVisible = false
            }

            // Status Badge
            val status = item.status?.lowercase(Locale.ROOT) ?: "completed"
            val (badgeText, bgRes, fgRes) = when {
                item.outbreakId != null -> Triple(
                    ctx.getString(R.string.Badge_Outbreak),
                    R.color.badge_high_bg,
                    R.color.badge_high_text
                )
                status == "processing" -> Triple(
                    "PROCESSING",
                    R.color.badge_low_bg,
                    R.color.badge_low_text
                )
                status == "failed" -> Triple(
                    "FAILED",
                    R.color.badge_high_bg,
                    R.color.badge_high_text
                )
                item.disease == null -> Triple(
                    ctx.getString(R.string.Badge_Healthy),
                    R.color.badge_low_bg,
                    R.color.badge_low_text
                )
                else -> Triple(
                    ctx.getString(R.string.Badge_Detected),
                    R.color.badge_moderate_bg,
                    R.color.badge_moderate_text
                )
            }

            tvScanStatus.text = badgeText
            tvScanStatus.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(ctx, bgRes))
            tvScanStatus.setTextColor(ContextCompat.getColor(ctx, fgRes))

            // Thumbnail loading
            if (!item.imageUrl.isNullOrBlank()) {
                Glide.with(ctx)
                    .load(item.imageUrl)
                    .placeholder(R.drawable.ic_scan)
                    .error(R.drawable.ic_scan)
                    .into(ivScanThumb)
            } else {
                ivScanThumb.setImageResource(R.drawable.ic_scan)
            }

            root.setOnClickListener { onItemClick(item) }
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<ScanDto>() {
        override fun areItemsTheSame(oldItem: ScanDto, newItem: ScanDto): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: ScanDto, newItem: ScanDto): Boolean =
            oldItem == newItem
    }
}