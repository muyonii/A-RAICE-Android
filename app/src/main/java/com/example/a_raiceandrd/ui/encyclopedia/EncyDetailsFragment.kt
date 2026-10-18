package com.example.a_raiceandrd.ui.encyclopedia

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.a_raiceandrd.R
import com.example.a_raiceandrd.data.DiseaseRepo
import com.example.a_raiceandrd.databinding.FragmentEncyDetailsBinding
import com.example.a_raiceandrd.databinding.ItemEncySectionBinding
import com.example.a_raiceandrd.model.Treatment
import com.example.a_raiceandrd.databinding.TreatmentItemBinding
import androidx.core.view.isVisible
import kotlin.collections.forEachIndexed
import android.content.res.ColorStateList
import androidx.core.content.ContextCompat

class EncyDetailsFragment : Fragment(R.layout.fragment_ency_details) {

    private var _binding: FragmentEncyDetailsBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEncyDetailsBinding.bind(view)

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }

        val id = requireArguments().getInt("diseaseId")
        val disease = DiseaseRepo.getById(id)

        if (disease == null) {
            findNavController().navigateUp()   // unknown id: go back
            return
        }

        binding.ivDisease.setImageResource(disease.imageRes)
        binding.tvTitle.text = disease.name
        binding.tvDefinition.text = disease.definition

        bindSection(binding.secHistory, getString(R.string.ency_history), disease.history)
        bindSection(binding.secCauses, getString(R.string.ency_causes), disease.causes)
        bindSection(binding.secSymptoms, getString(R.string.ency_symptoms), disease.symptoms)
        bindSection(binding.secPrevention, getString(R.string.ency_prevention), disease.prevention)

        binding.treatmentCard.isVisible = disease.treatment.isNotEmpty()
        bindTreatments(disease.treatment)

        binding.sourcesCard.isVisible = disease.sources.isNotEmpty()
        binding.tvSources.text = disease.sources
    }

    private fun bindSection(section: ItemEncySectionBinding, title: String, body: String) {
        section.root.isVisible = body.isNotBlank()
        section.tvSectionTitle.text = title
        section.tvSectionBody.text = body
    }

    private fun bindTreatments(steps: List<Treatment>) {
        binding.llTreatments.removeAllViews()
        steps.forEachIndexed { i, s ->
            addTreatmentRow(i, s.title, s.description, s.type)
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
            else         -> R.color.surface_chip to R.color.surface_chip
        }
        row.tvTag.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(requireContext(), bg))
        row.tvTag.setTextColor(ContextCompat.getColor(requireContext(), fg))

        binding.llTreatments.addView(row.root)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}