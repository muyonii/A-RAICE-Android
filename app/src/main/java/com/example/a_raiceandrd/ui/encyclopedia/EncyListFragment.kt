package com.example.a_raiceandrd.ui.encyclopedia

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.a_raiceandrd.R
import com.example.a_raiceandrd.adapter.DiseaseAdapter
import com.example.a_raiceandrd.data.DiseaseRepo
import com.example.a_raiceandrd.data.repository.api.DiseaseDto
import com.example.a_raiceandrd.databinding.FragmentEncyListBinding
import com.example.a_raiceandrd.ui.setupTopBar
import com.example.a_raiceandrd.ui.showNav
import kotlinx.coroutines.launch

class EncyListFragment : Fragment(R.layout.fragment_ency_list) {

    private var _binding: FragmentEncyListBinding? = null
    private val binding get() = _binding!!

    private var allDiseases: List<DiseaseDto> = emptyList()

    private val adapter = DiseaseAdapter { disease ->
        val nav = findNavController()
        if (nav.currentDestination?.id == R.id.ency) {
            val bundle = Bundle().apply {
                putInt("diseaseId", disease.id ?: 0)
            }
            nav.navigate(R.id.action_ency_to_encyDetailsFragment, bundle)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEncyListBinding.bind(view)

        setupTopBar(view, view.findViewById<NestedScrollView>(R.id.encyScroll))

        binding.rvEncyclopedia.adapter = adapter

        binding.swipeRefresh.setColorSchemeResources(R.color.gold)
        binding.swipeRefresh.setOnRefreshListener {
            loadDiseases(forceRefresh = true)
        }

        binding.btnRetry.setOnClickListener {
            loadDiseases(forceRefresh = true)
        }

        binding.etSearch.doAfterTextChanged { text ->
            filter(text?.toString())
        }

        loadDiseases(forceRefresh = false)
    }

    private fun loadDiseases(forceRefresh: Boolean) {
        viewLifecycleOwner.lifecycleScope.launch {
            if (allDiseases.isEmpty()) {
                binding.loadingProgress.isVisible = true
            }
            binding.emptyState.isVisible = false

            val result = DiseaseRepo.getDiseases(requireContext(), forceRefresh = forceRefresh)
            allDiseases = result

            binding.loadingProgress.isVisible = false
            binding.swipeRefresh.isRefreshing = false

            filter(binding.etSearch.text?.toString())
        }
    }

    private fun filter(query: String?) {
        val q = query?.trim()?.lowercase().orEmpty()
        val filtered = if (q.isBlank()) {
            allDiseases
        } else {
            allDiseases.filter { d ->
                val name = d.name?.lowercase().orEmpty()
                val desc = d.description?.lowercase().orEmpty()
                val symptoms = d.symptoms?.lowercase().orEmpty()
                val causes = d.causes?.lowercase().orEmpty()
                val category = DiseaseRepo.getCategoryByName(d.name)?.lowercase().orEmpty()

                name.contains(q) || desc.contains(q) || symptoms.contains(q) || causes.contains(q) || category.contains(q)
            }
        }

        adapter.submitList(filtered)

        val isEmpty = filtered.isEmpty()
        binding.emptyState.isVisible = isEmpty
        binding.rvEncyclopedia.isVisible = !isEmpty
        if (isEmpty) {
            binding.tvEmpty.setText(
                if (allDiseases.isEmpty()) R.string.ency_load_error else R.string.ency_no_results
            )
        }
    }

    override fun onDestroyView() {
        requireView().showNav()
        super.onDestroyView()
        _binding = null
    }
}