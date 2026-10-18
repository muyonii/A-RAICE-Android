package com.example.a_raiceandrd.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.a_raiceandrd.R
import com.example.a_raiceandrd.databinding.AlertItemBinding
import com.example.a_raiceandrd.model.AlertItem
import com.example.a_raiceandrd.model.AlertLevel

class AlertAdapter(
    private val onClick: ((AlertItem) -> Unit)? = null
) : ListAdapter<AlertItem, AlertAdapter.VH>(Diff) {

    inner class VH(private val b: AlertItemBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: AlertItem) {
            b.alertTitle.text = item.title
            b.alertTime.text = item.timeLabel

            val colorRes = when (item.level) {
                AlertLevel.HIGH -> R.color.risk_high
                AlertLevel.MODERATE -> R.color.risk_moderate
                AlertLevel.WEATHER -> R.color.risk_weather
                AlertLevel.GOOD -> R.color.risk_good
            }
            b.alertDot.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(b.root.context, colorRes))

            // Unread indicator: unread alerts have full opacity and prominent dot
            b.alertDot.isVisible = item.unread
            b.alertTitle.alpha = if (item.unread) 1.0f else 0.7f
            b.alertTime.alpha = if (item.unread) 1.0f else 0.7f

            b.root.strokeColor = ContextCompat.getColor(
                b.root.context,
                if (item.unread) R.color.gold else R.color.stroke_default
            )

            b.root.setOnClickListener {
                onClick?.invoke(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(AlertItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) =
        holder.bind(getItem(position))

    object Diff : DiffUtil.ItemCallback<AlertItem>() {
        override fun areItemsTheSame(a: AlertItem, b: AlertItem) = a.id == b.id
        override fun areContentsTheSame(a: AlertItem, b: AlertItem) = a == b
    }
}