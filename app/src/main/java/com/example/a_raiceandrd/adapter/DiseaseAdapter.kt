package com.example.a_raiceandrd.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.a_raiceandrd.R
import com.example.a_raiceandrd.data.DiseaseRepo
import com.example.a_raiceandrd.data.repository.api.DiseaseDto
import com.example.a_raiceandrd.databinding.DiseaseItemBinding

class DiseaseAdapter(
    private val onClick: (DiseaseDto) -> Unit
) : ListAdapter<DiseaseDto, DiseaseAdapter.VH>(Diff) {

    class VH(val binding: DiseaseItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(DiseaseItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val disease = getItem(position)
        with(holder.binding) {
            val fallbackRes = DiseaseRepo.getBundledDrawableRes(disease.name)
            if (!disease.imageUrl.isNullOrBlank()) {
                Glide.with(root.context)
                    .load(disease.imageUrl)
                    .placeholder(fallbackRes)
                    .error(fallbackRes)
                    .fallback(fallbackRes)
                    .into(ivDisease)
            } else {
                ivDisease.setImageResource(fallbackRes)
            }

            tvName.text = disease.name.orEmpty()
            tvBrief.text = disease.description.orEmpty()

            val category = DiseaseRepo.getCategoryByName(disease.name)
            if (!category.isNullOrBlank()) {
                tvCategory.isVisible = true
                tvCategory.text = category
                val (bgRes, textRes) = chipColors(category)
                val ctx = root.context
                tvCategory.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(ctx, bgRes))
                tvCategory.setTextColor(ContextCompat.getColor(ctx, textRes))
            } else {
                tvCategory.isVisible = false
            }

            val open = View.OnClickListener { onClick(disease) }
            root.setOnClickListener(open)
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

    private object Diff : DiffUtil.ItemCallback<DiseaseDto>() {
        override fun areItemsTheSame(old: DiseaseDto, new: DiseaseDto) = old.id == new.id
        override fun areContentsTheSame(old: DiseaseDto, new: DiseaseDto) = old == new
    }
}