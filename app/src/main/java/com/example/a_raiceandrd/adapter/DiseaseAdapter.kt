package com.example.a_raiceandrd.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import android.R.attr.onClick
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.a_raiceandrd.databinding.DiseaseItemBinding
import com.example.a_raiceandrd.model.Disease
import android.content.res.ColorStateList
import androidx.core.content.ContextCompat
import com.example.a_raiceandrd.R

class DiseaseAdapter(
    private val onClick: (Disease) -> Unit
) : ListAdapter<Disease, DiseaseAdapter.VH>(Diff) {

    class VH(val binding: DiseaseItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(DiseaseItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val disease = getItem(position)
        with(holder.binding) {
            ivDisease.setImageResource(disease.imageRes)
            tvName.text = disease.name
            tvBrief.text = disease.definition

            tvCategory.text = disease.category
            val (bgRes, textRes) = chipColors(disease.category)
            val ctx = root.context
            tvCategory.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(ctx, bgRes))
            tvCategory.setTextColor(ContextCompat.getColor(ctx, textRes))

            val open = View.OnClickListener { onClick(disease) }
            //root.setOnClickListener(open)
            tvViewMore.setOnClickListener(open)
        }
    }

    private fun chipColors(category: String): Pair<Int, Int> =
        when (category.trim().lowercase()) {
            "bacterial" -> R.color.badge_moderate_bg to R.color.badge_moderate_text
            "viral" -> R.color.badge_low_bg to R.color.badge_low_text
            "fungal" -> R.color.badge_high_bg to R.color.badge_high_text
            else -> R.color.surface_card to R.color.surface_card
        }
    }

    private object Diff : DiffUtil.ItemCallback<Disease>() {
        override fun areItemsTheSame(old: Disease, new: Disease) = old.id == new.id
        override fun areContentsTheSame(old: Disease, new: Disease) = old == new
    }