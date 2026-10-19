package com.example.a_raiceandrd.ui.admin

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.a_raiceandrd.MainActivity
import com.example.a_raiceandrd.R
import com.example.a_raiceandrd.adapter.ReportAdapter
import com.example.a_raiceandrd.data.DiseaseRepo
import com.example.a_raiceandrd.data.repository.api.ApiClient
import com.example.a_raiceandrd.data.repository.api.CreateReportRequest
import com.example.a_raiceandrd.data.repository.api.DiseaseDto
import com.example.a_raiceandrd.data.repository.api.ReportDto
import com.example.a_raiceandrd.databinding.DialogGenerateReportBinding
import com.example.a_raiceandrd.databinding.FragmentReportsBinding
import com.example.a_raiceandrd.ui.auth.FLogin
import com.example.a_raiceandrd.ui.auth.Role
import com.example.a_raiceandrd.ui.hideNavOnScroll
import com.example.a_raiceandrd.ui.showAdminNav
import com.example.a_raiceandrd.util.errorMessage
import com.example.a_raiceandrd.util.hideKeyboard
import com.example.a_raiceandrd.util.parseFieldErrors
import com.example.a_raiceandrd.util.BATAAN_MUNICIPALITIES
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class ReportsFragment : Fragment() {

    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!

    private lateinit var reportAdapter: ReportAdapter
    private val reportsList = mutableListOf<ReportDto>()

    private var currentPage = 1
    private var lastPage = 1
    private var isLoading = false

    private data class DiseaseOption(val label: String, val id: Int?)

    // Use shared municipality list (Group 9)
    private val muniOptions = BATAAN_MUNICIPALITIES

    private var diseaseList = listOf<DiseaseDto>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (FLogin.getCurrentRole() != Role.ADMIN) {
            findNavController().navigate(R.id.action_global_loginFragment)
            return
        }

        binding.topBar.menuBtn.setOnClickListener {
            (requireActivity() as MainActivity).openDrawer()
        }
        binding.topBar.notifBtn.setOnClickListener {
            findNavController().navigate(R.id.adnotif)
        }

        binding.reportsScroll.hideNavOnScroll(binding.topBar.root)

        setupList()
        setupListeners()

        loadReports(reset = true)
        preloadDiseases()
    }

    override fun onDestroyView() {
        requireView().showAdminNav()
        _binding = null
        super.onDestroyView()
    }

    private fun setupList() {
        reportAdapter = ReportAdapter { report ->
            ReportDetailDialog.show(requireContext(), report)
        }
        binding.rvReports.adapter = reportAdapter

        binding.swipeRefresh.setColorSchemeResources(R.color.gold)
        binding.swipeRefresh.setOnRefreshListener {
            loadReports(reset = true)
        }

        binding.reportsScroll.setOnScrollChangeListener(
            NestedScrollView.OnScrollChangeListener { v, _, scrollY, _, _ ->
                if (scrollY >= (v.getChildAt(0).measuredHeight - v.measuredHeight - 300)) {
                    if (!isLoading && currentPage < lastPage) {
                        loadReports(reset = false)
                    }
                }
            }
        )
    }

    private fun setupListeners() {
        binding.btnOpenGenerateReport.setOnClickListener {
            showGenerateReportDialog()
        }
    }

    private fun preloadDiseases() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                diseaseList = DiseaseRepo.getDiseases(requireContext())
            } catch (_: Exception) {
            }
        }
    }

    private fun loadReports(reset: Boolean) {
        if (isLoading) return
        isLoading = true

        val pageToLoad = if (reset) 1 else currentPage + 1

        if (reset && !binding.swipeRefresh.isRefreshing) {
            binding.pbReportsLoading.isVisible = true
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val pageResp = ApiClient.service.getReports(page = pageToLoad)
                currentPage = pageResp.currentPage ?: pageToLoad
                lastPage = pageResp.lastPage ?: 1

                val items = pageResp.data.orEmpty()
                if (reset) {
                    reportsList.clear()
                    reportsList.addAll(items)
                } else {
                    reportsList.addAll(items)
                }

                reportAdapter.submitList(reportsList.toList())
                binding.tvNoReports.isVisible = reportsList.isEmpty()
            } catch (e: Exception) {
                if (reportsList.isEmpty()) {
                    binding.tvNoReports.isVisible = true
                    binding.tvNoReports.text = errorMessage(e)
                }
            } finally {
                isLoading = false
                if (_binding != null) {
                    binding.pbReportsLoading.isVisible = false
                    binding.swipeRefresh.isRefreshing = false
                }
            }
        }
    }

    private fun showGenerateReportDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        val genBinding = DialogGenerateReportBinding.inflate(layoutInflater)
        dialog.setContentView(genBinding.root)

        dialog.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        genBinding.btnGenClose.setOnClickListener {
            dialog.dismiss()
        }

        // Municipality dropdown - no default selection; user must pick one
        val muniAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            muniOptions.map { it.displayName }
        )
        genBinding.actvGenMuni.setAdapter(muniAdapter)
        genBinding.actvGenMuni.setText("", false)
        var selectedMuniIndex = -1   // -1 = nothing selected
        genBinding.actvGenMuni.setOnItemClickListener { _, _, position, _ ->
            selectedMuniIndex = position
            genBinding.tilGenMuni.error = null
        }

        // Disease dropdown
        val diseaseOptions = mutableListOf(DiseaseOption(getString(R.string.Report_Disease_All), null))
        for (d in diseaseList) {
            d.id?.let { id ->
                diseaseOptions.add(DiseaseOption(d.name ?: "Disease $id", id))
            }
        }
        val diseaseAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            diseaseOptions.map { it.label }
        )
        genBinding.actvGenDisease.setAdapter(diseaseAdapter)
        genBinding.actvGenDisease.setText(diseaseOptions[0].label, false)
        var selectedDiseaseIndex = 0
        genBinding.actvGenDisease.setOnItemClickListener { _, _, position, _ ->
            selectedDiseaseIndex = position
        }

        // Date range picker
        var selectedStart: String? = null
        var selectedEnd: String? = null

        val openDatePicker = View.OnClickListener {
            val picker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText(R.string.Report_Date_Range_Label)
                .build()

            picker.addOnPositiveButtonClickListener { selection ->
                val startMs = selection.first
                val endMs = selection.second
                if (startMs != null && endMs != null) {
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                        timeZone = TimeZone.getTimeZone("UTC")
                    }
                    val sStr = sdf.format(Date(startMs))
                    val eStr = sdf.format(Date(endMs))
                    selectedStart = sStr
                    selectedEnd = eStr
                    genBinding.etGenDateRange.setText("$sStr to $eStr")
                    genBinding.tilGenDateRange.error = null
                }
            }
            picker.show(childFragmentManager, "REPORT_DATE_RANGE")
        }

        genBinding.etGenDateRange.setOnClickListener(openDatePicker)
        genBinding.tilGenDateRange.setEndIconOnClickListener(openDatePicker)

        // Submit
        genBinding.btnGenSubmit.setOnClickListener {
            // Validate municipality selection
            if (selectedMuniIndex < 0) {
                genBinding.tilGenMuni.error = getString(R.string.Report_Select_Municipality)
                return@setOnClickListener
            }
            genBinding.tilGenMuni.error = null

            if (selectedStart == null || selectedEnd == null) {
                genBinding.tilGenDateRange.error = getString(R.string.Report_Date_Range_Hint)
                return@setOnClickListener
            }
            genBinding.tilGenDateRange.error = null

            val chosenMuni = muniOptions.getOrNull(selectedMuniIndex)?.slug
            if (chosenMuni.isNullOrBlank()) {
                genBinding.tilGenMuni.error = getString(R.string.Report_Select_Municipality)
                return@setOnClickListener
            }
            val chosenDisease = diseaseOptions.getOrNull(selectedDiseaseIndex)?.id

            genBinding.root.hideKeyboard()
            genBinding.btnGenSubmit.isEnabled = false
            genBinding.pbGenLoading.isVisible = true

            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val req = CreateReportRequest(
                        municipality = chosenMuni,
                        rangeStart = selectedStart,
                        rangeEnd = selectedEnd,
                        diseaseId = chosenDisease
                    )
                    val newReport = ApiClient.service.createReport(req)
                    dialog.dismiss()

                    loadReports(reset = true)
                    ReportDetailDialog.show(requireContext(), newReport)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: HttpException) {
                    if (e.code() == 422) {
                        val fieldErrors = com.example.a_raiceandrd.util.parseFieldErrors(e)
                        fieldErrors["municipality"]?.let { genBinding.tilGenMuni.error = it }
                        fieldErrors["range_start"]?.let { genBinding.tilGenDateRange.error = it }
                        fieldErrors["range_end"]?.let { genBinding.tilGenDateRange.error = (genBinding.tilGenDateRange.error?.let { err -> "$err / $it" } ?: it) }
                        val unmapped = fieldErrors.filterKeys { it !in setOf("municipality", "range_start", "range_end", "disease_id") }
                        if (unmapped.isNotEmpty() || (fieldErrors.isEmpty())) {
                            Toast.makeText(requireContext(), errorMessage(e), Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(requireContext(), errorMessage(e), Toast.LENGTH_LONG).show()
                    }
                    genBinding.btnGenSubmit.isEnabled = true
                    genBinding.pbGenLoading.isVisible = false
                } catch (e: Exception) {
                    val err = errorMessage(e)
                    Toast.makeText(requireContext(), err, Toast.LENGTH_LONG).show()
                    genBinding.btnGenSubmit.isEnabled = true
                    genBinding.pbGenLoading.isVisible = false
                }
            }
        }

        dialog.show()
    }
}