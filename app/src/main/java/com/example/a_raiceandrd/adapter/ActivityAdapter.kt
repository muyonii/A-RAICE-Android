package com.example.a_raiceandrd.adapter

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.a_raiceandrd.R
import com.example.a_raiceandrd.ui.home.ActivityItem

class ActivityAdapter : ListAdapter<ActivityItem, ActivityAdapter.VH>(Diff) {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
            val dot: View = view.findViewById(R.id.alertDot)
            val title: TextView = view.findViewById(R.id.alertTitle)
            val time: TextView = view.findViewById(R.id.alertTime)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        // flat row layout (the card + dividers in fragment_home.xml)
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recent_act, parent, false)
        return VH(v)
    }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = getItem(position)
            holder.title.text = item.title
            holder.time.text = item.timeAgo.uppercase()
            holder.dot.background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(ContextCompat.getColor(holder.itemView.context, item.type.color))
            }

        }

    object Diff : DiffUtil.ItemCallback<ActivityItem>() {
        override fun areItemsTheSame(a: ActivityItem, b: ActivityItem) =
            if (a.id.isNotEmpty() || b.id.isNotEmpty()) a.id == b.id else a == b

        override fun areContentsTheSame(a: ActivityItem, b: ActivityItem) = a == b
    }
}