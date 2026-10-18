package com.example.a_raiceandrd.ui.encyclopedia

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.a_raiceandrd.R
import com.example.a_raiceandrd.data.DiseaseRepo
import com.example.a_raiceandrd.data.repository.api.DiseaseDto
import com.example.a_raiceandrd.data.repository.api.TreatmentDto
import com.example.a_raiceandrd.databinding.FragmentEncyDetailsBinding
import com.example.a_raiceandrd.databinding.ItemEncySectionBinding
import com.example.a_raiceandrd.databinding.TreatmentItemBinding
import kotlinx.coroutines.launch

class EncyDetailsFragment : Fragment(R.layout.fragment_ency_details) {

    private var _binding: FragmentEncyDetailsBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEncyDetailsBinding.bind(view)

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }

        val id = arguments?.getInt("diseaseId") ?: 0

        // Immediate local bind from bundled or memory cache if present
        val initial = DiseaseRepo.getBundledDetailAsDto(id)
        if (initial != null) {
            bindDisease(initial)
        }

        // Fetch live / cached detail
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val disease = DiseaseRepo.getDiseaseDetail(requireContext(), id)
                if (disease != null) {
                    bindDisease(disease)
                } else if (initial == null) {
                    findNavController().navigateUp()
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
            }
        }
    }

    private fun bindDisease(disease: DiseaseDto) {
        val fallbackRes = DiseaseRepo.getBundledDrawableRes(disease.name)
        if (!disease.imageUrl.isNullOrBlank()) {
            Glide.with(this)
                .load(disease.imageUrl)
                .placeholder(fallbackRes)
                .error(fallbackRes)
                .fallback(fallbackRes)
                .into(binding.ivDisease)
        } else {
            binding.ivDisease.setImageResource(fallbackRes)
        }

        binding.tvTitle.text = disease.name.orEmpty()
        binding.tvDefinition.text = disease.description.orEmpty()

        val category = DiseaseRepo.getCategoryByName(disease.name)
        if (!category.isNullOrBlank()) {
            binding.tvCategory.isVisible = true
            binding.tvCategory.text = category
            val (bg, fg) = chipColors(category)
            binding.tvCategory.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(requireContext(), bg))
            binding.tvCategory.setTextColor(ContextCompat.getColor(requireContext(), fg))
        } else {
            binding.tvCategory.isVisible = false
        }

        bindSection(binding.secHistory, getString(R.string.ency_history), disease.history.orEmpty())
        bindSection(binding.secCauses, getString(R.string.ency_causes), disease.causes.orEmpty())
        bindSection(binding.secSymptoms, getString(R.string.ency_symptoms), disease.symptoms.orEmpty())
        bindSection(binding.secPrevention, getString(R.string.ency_prevention), disease.preventionTips.orEmpty())

        val treatments = disease.treatments.orEmpty()
        binding.treatmentCard.isVisible = treatments.isNotEmpty()
        bindTreatments(treatments)

        binding.sourcesCard.isVisible = !disease.sources.isNullOrBlank()
        binding.tvSources.text = disease.sources.orEmpty()
    }

    private fun bindSection(section: ItemEncySectionBinding, title: String, body: String) {
        section.root.isVisible = body.isNotBlank()
        section.tvSectionTitle.text = title
        section.tvSectionBody.text = body
    }

    private fun bindTreatments(steps: List<TreatmentDto>) {
        binding.llTreatments.removeAllViews()
        val grouped = steps.groupBy {
            it.type?.trim()?.replaceFirstChar { c -> c.uppercase() } ?: "General"
        }

        var isFirst = true
        for ((type, items) in grouped) {
            val groupHeader = TextView(requireContext()).apply {
                text = type.uppercase()
                textSize = 12f
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                setTextColor(ContextCompat.getColor(requireContext(), R.color.gold))
                val topPad = if (isFirst) dp(6) else dp(16)
                setPadding(0, topPad, 0, dp(4))
            }
            binding.llTreatments.addView(groupHeader)
            isFirst = false

            items.forEachIndexed { i, s ->
                addTreatmentRow(i, s.title.orEmpty(), s.description.orEmpty(), s.type.orEmpty())
            }
        }
    }

    private fun addTreatmentRow(index: Int, title: String, description: String, type: String) {
        val row = TreatmentItemBinding.inflate(layoutInflater, binding.llTreatments, false)
        row.divider.isVisible = index > 0
        row.tvTitle.text = title
        row.tvDesc.text = description
        row.tvDesc.isVisible = description.isNotBlank()

        row.tvTag.text = type.replaceFirstChar { it.uppercase() }
        val (bg, fg) = when (type.trim().lowercase()) {
            "chemical"   -> R.color.badge_high_bg to R.color.badge_high_text
            "biological" -> R.color.badge_moderate_bg to R.color.badge_moderate_text
            "cultural"   -> R.color.badge_low_bg to R.color.badge_low_text
            "organic"    -> R.color.badge_low_bg to R.color.badge_low_text
            else         -> R.color.surface_chip to R.color.text_secondary
        }
        row.tvTag.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(requireContext(), bg))
        row.tvTag.setTextColor(ContextCompat.getColor(requireContext(), fg))

        binding.llTreatments.addView(row.root)
    }

    private fun chipColors(category: String): Pair<Int, Int> =
        when (category.trim().lowercase()) {
            "bacterial" -> R.color.badge_moderate_bg to R.color.badge_moderate_text
            "viral"     -> R.color.badge_low_bg to R.color.badge_low_text
            "fungal"    -> R.color.badge_high_bg to R.color.badge_high_text
            else        -> R.color.surface_chip to R.color.text_primary
        }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}