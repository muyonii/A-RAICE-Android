package com.example.a_raiceandrd.ui.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a_raiceandrd.R
import com.example.a_raiceandrd.adapter.ScanHistoryAdapter
import com.example.a_raiceandrd.data.repository.api.ApiClient
import com.example.a_raiceandrd.data.repository.api.DiseaseDto
import com.example.a_raiceandrd.data.repository.api.MunicipalityDto
import com.example.a_raiceandrd.data.repository.api.ScanDto
import com.example.a_raiceandrd.databinding.FragmentScanHistoryBinding
import com.example.a_raiceandrd.ui.auth.FLogin
import com.example.a_raiceandrd.ui.auth.Role
import com.example.a_raiceandrd.util.errorMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ScanHistoryFragment : Fragment() {

    private var _binding: FragmentScanHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ScanHistoryAdapter
    private val allScans = mutableListOf<ScanDto>()

    private var currentPage = 1
    private var lastPage = 1
    private var isLoading = false

    private var selectedMunicipalitySlug: String? = null
    private var selectedDiseaseId: Int? = null

    private var municipalitiesList = listOf<MunicipalityDto>()
    private var diseasesList = listOf<DiseaseDto>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScanHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val isAdmin = FLogin.getCurrentRole() == Role.ADMIN
        binding.tvHistoryTitle.setText(if (isAdmin) R.string.All_Scans else R.string.Scan_History)
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        adapter = ScanHistoryAdapter(isAdmin) { scan ->
            val id = scan.id ?: return@ScanHistoryAdapter
            val args = Bundle().apply {
                putInt("scanId", id)
            }
            findNavController().navigate(
                R.id.action_scanHistoryFragment_to_scanResultsFragment,
                args
            )
        }

        val layoutManager = LinearLayoutManager(requireContext())
        binding.rvScanHistory.layoutManager = layoutManager
        binding.rvScanHistory.adapter = adapter

        // Infinite scroll listener
        binding.rvScanHistory.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy <= 0) return

                val visibleCount = layoutManager.childCount
                val totalCount = layoutManager.itemCount
                val firstVisible = layoutManager.findFirstVisibleItemPosition()

                if (!isLoading && currentPage < lastPage && (visibleCount + firstVisible) >= totalCount - 3) {
                    loadPage(currentPage + 1, append = true)
                }
            }
        })

        binding.swipeRefresh.setOnRefreshListener {
            loadPage(1, append = false)
        }

        binding.btnEmptyRetry.setOnClickListener {
            loadPage(1, append = false)
        }

        if (isAdmin) {
            binding.adminFiltersLayout.isVisible = true
            setupFilters()
        } else {
            binding.adminFiltersLayout.isVisible = false
        }

        loadPage(1, append = false)
    }

    private fun setupFilters() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                municipalitiesList = withContext(Dispatchers.IO) {
                    ApiClient.service.getMunicipalities()
                }
                diseasesList = withContext(Dispatchers.IO) {
                    ApiClient.service.getDiseases()
                }

                if (_binding == null) return@launch

                // Municipality Filter
                val munNames = mutableListOf(getString(R.string.Filter_All_Municipalities))
                munNames.addAll(municipalitiesList.map { it.name.orEmpty() })
                val munAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, munNames)
                binding.actvFilterMunicipality.setAdapter(munAdapter)
                binding.actvFilterMunicipality.setText(munNames[0], false)
                binding.actvFilterMunicipality.setOnItemClickListener { _, _, position, _ ->
                    selectedMunicipalitySlug = if (position == 0) null else municipalitiesList.getOrNull(position - 1)?.slug
                    loadPage(1, append = false)
                }

                // Disease Filter
                val disNames = mutableListOf(getString(R.string.Filter_All_Diseases))
                disNames.addAll(diseasesList.map { it.name.orEmpty() })
                val disAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, disNames)
                binding.actvFilterDisease.setAdapter(disAdapter)
                binding.actvFilterDisease.setText(disNames[0], false)
                binding.actvFilterDisease.setOnItemClickListener { _, _, position, _ ->
                    selectedDiseaseId = if (position == 0) null else diseasesList.getOrNull(position - 1)?.id
                    loadPage(1, append = false)
                }
            } catch (_: Exception) {
                // If filter data load fails, filter defaults to all
            }
        }
    }

    private fun loadPage(page: Int, append: Boolean) {
        if (isLoading) return
        isLoading = true

        if (!append) {
            if (!binding.swipeRefresh.isRefreshing) {
                binding.initialProgress.isVisible = true
            }
            binding.layoutEmpty.isVisible = false
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.service.getScans(
                        page = page,
                        municipality = selectedMunicipalitySlug,
                        diseaseId = selectedDiseaseId
                    )
                }

                currentPage = response.currentPage ?: page
                lastPage = response.lastPage ?: page

                val items = response.data.orEmpty()
                if (append) {
                    allScans.addAll(items)
                } else {
                    allScans.clear()
                    allScans.addAll(items)
                }

                adapter.submitList(allScans.toList())
                binding.layoutEmpty.isVisible = allScans.isEmpty()
            } catch (e: Exception) {
                if (!append && allScans.isEmpty()) {
                    binding.layoutEmpty.isVisible = true
                }
                Toast.makeText(requireContext(), errorMessage(e), Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
                if (_binding != null) {
                    binding.initialProgress.isVisible = false
                    binding.swipeRefresh.isRefreshing = false
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}