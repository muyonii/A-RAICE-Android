package com.example.a_raiceandrd.ui.map

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.a_raiceandrd.R
import com.example.a_raiceandrd.adapter.OutbreakAdapter
import com.example.a_raiceandrd.adapter.OutbreakItem
import com.example.a_raiceandrd.data.repository.api.ApiClient
import com.example.a_raiceandrd.data.repository.api.MapFarmDto
import com.example.a_raiceandrd.data.repository.api.MapOutbreakDto
import com.example.a_raiceandrd.data.repository.api.MapResponseDto
import com.example.a_raiceandrd.databinding.FragmentProvinceMapBinding
import com.example.a_raiceandrd.ui.setupTopBar
import com.example.a_raiceandrd.ui.showAdminNav
import com.example.a_raiceandrd.util.errorMessage
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.util.Locale

class ProvinceMap : Fragment(R.layout.fragment_province_map) {

    private var _b: FragmentProvinceMapBinding? = null
    private val b get() = _b!!

    private var currentMapData: MapResponseDto? = null
    private var isHistoryMode = false
    private var historyStatusFilter: String? = null

    private val outbreakAdapter = OutbreakAdapter { item ->
        showOutbreakDetailDialog(item)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _b = FragmentProvinceMapBinding.bind(view)

        setupTopBar(view, view.findViewById<NestedScrollView>(R.id.adMapScroll))

        b.adBataanMapView.isFarmerMode = false
        b.rvAdnearby.layoutManager = LinearLayoutManager(requireContext())
        b.rvAdnearby.adapter = outbreakAdapter

        // Farm dot tap
        b.adBataanMapView.onFarmSelected = { farm ->
            showFarmDetails(farm)
        }

        b.btnCloseFarmDetails.setOnClickListener {
            b.cardFarmDetails.isVisible = false
        }

        // Municipality polygon tap — null means deselect → show all hotspots
        b.adBataanMapView.onMunicipalitySelected = { slug ->
            if (!isHistoryMode) {
                filterHotspotsByMunicipality(slug)
            }
        }

        // Mode toggles
        b.toggleMode.check(R.id.btnModeHotspots)
        b.toggleMode.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btnModeHotspots -> {
                        isHistoryMode = false
                        b.historyFilterGroup.isVisible = false
                        b.adnearbyLabel.setText(R.string.Ad_HPots)
                        displayHotspots()
                    }
                    R.id.btnModeHistory -> {
                        isHistoryMode = true
                        b.historyFilterGroup.isVisible = true
                        b.adnearbyLabel.setText(R.string.Ad_Outbreak_History)
                        loadOutbreakHistory()
                    }
                }
            }
        }

        // History filter chips
        b.historyFilterGroup.check(R.id.chipFilterAll)
        b.chipFilterAll.setOnClickListener {
            historyStatusFilter = null
            loadOutbreakHistory()
        }
        b.chipFilterActive.setOnClickListener {
            historyStatusFilter = "active"
            loadOutbreakHistory()
        }
        b.chipFilterResolved.setOnClickListener {
            historyStatusFilter = "resolved"
            loadOutbreakHistory()
        }

        loadMapData()
    }

    private fun loadMapData() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val mapResp = ApiClient.service.getMap()
                currentMapData = mapResp

                val severities = mutableMapOf<String, String?>()
                mapResp.municipalities.orEmpty().forEach { m ->
                    m.slug?.let { s -> severities[s.lowercase(Locale.ROOT)] = m.severity }
                }
                b.adBataanMapView.setSeverities(severities)
                b.adBataanMapView.setFarms(mapResp.farms.orEmpty())

                if (!isHistoryMode) {
                    displayHotspots()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (_b != null) {
                    Toast.makeText(requireContext(), errorMessage(e), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun displayHotspots() {
        val outbreaks = currentMapData?.outbreaks.orEmpty()
        val sorted = outbreaks.sortedWith(
            compareByDescending<MapOutbreakDto> { severityRank(it.severity) }
                .thenByDescending { it.createdAt.orEmpty() }
        )

        val items = sorted.map { o ->
            OutbreakItem(
                id = o.id,
                diseaseName = o.disease,
                municipality = o.municipality,
                severity = o.severity,
                status = "Active",
                createdAt = o.createdAt
            )
        }

        outbreakAdapter.submitList(items)
        b.tvAdNoOutbreaks.isVisible = items.isEmpty()
        b.rvAdnearby.isVisible = items.isNotEmpty()
    }

    private fun filterHotspotsByMunicipality(slug: String?) {
        val outbreaks = currentMapData?.outbreaks.orEmpty()
        val target = slug?.lowercase(Locale.ROOT)
        val filtered = if (target.isNullOrBlank()) {
            outbreaks
        } else {
            outbreaks.filter { it.municipality?.lowercase(Locale.ROOT) == target }
        }

        val sorted = filtered.sortedWith(
            compareByDescending<MapOutbreakDto> { severityRank(it.severity) }
                .thenByDescending { it.createdAt.orEmpty() }
        )

        val items = sorted.map { o ->
            OutbreakItem(
                id = o.id,
                diseaseName = o.disease,
                municipality = o.municipality,
                severity = o.severity,
                status = "Active",
                createdAt = o.createdAt
            )
        }

        outbreakAdapter.submitList(items)
        b.tvAdNoOutbreaks.isVisible = items.isEmpty()
        b.rvAdnearby.isVisible = items.isNotEmpty()
    }

    private fun loadOutbreakHistory() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val page = ApiClient.service.getOutbreaks(page = 1, status = historyStatusFilter)
                val items = page.data.orEmpty().map { o ->
                    OutbreakItem(
                        id = o.id,
                        diseaseName = o.disease?.name,
                        municipality = o.municipality,
                        severity = o.severity,
                        status = o.status,
                        createdAt = o.createdAt,
                        scansCount = o.scansCount
                    )
                }
                outbreakAdapter.submitList(items)
                b.tvAdNoOutbreaks.isVisible = items.isEmpty()
                b.rvAdnearby.isVisible = items.isNotEmpty()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (_b != null) {
                    Toast.makeText(requireContext(), errorMessage(e), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showFarmDetails(farm: MapFarmDto) {
        b.cardFarmDetails.isVisible = true

        // farmerName handles both flat-staff (name field) and nested-farmer shapes
        val farmerName = farm.farmerName ?: getString(R.string.Farmer_Unknown)
        b.tvFarmFarmerName.text = farmerName

        val loc = listOfNotNull(
            farm.barangay?.let { "Brgy. $it" },
            farm.municipality?.replaceFirstChar { it.uppercase() }
        ).joinToString(", ")
        b.tvFarmLocation.text = if (loc.isNotBlank()) loc else getString(R.string.Location_Not_Specified)

        val scan = farm.scan
        if (scan != null) {
            val dName = scan.disease ?: getString(R.string.Badge_Healthy)
            val dDate = scan.scanDate?.take(10) ?: ""
            b.tvFarmLastScan.text = getString(R.string.Last_Scan_Format, dName, dDate)
        } else {
            b.tvFarmLastScan.text = getString(R.string.No_Scans_Yet)
        }

        val phone = farm.farmerContact
        if (!phone.isNullOrBlank()) {
            b.btnCallFarmer.isVisible = true
            b.btnCallFarmer.text = getString(R.string.Tap_To_Call, phone)
            b.btnCallFarmer.setOnClickListener {
                try {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null))
                    startActivity(intent)
                } catch (_: ActivityNotFoundException) {
                    if (_b != null) Toast.makeText(requireContext(), R.string.Error_No_Dialer, Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            b.btnCallFarmer.isVisible = false
        }
    }

    private fun showOutbreakDetailDialog(item: OutbreakItem) {
        val isResolved = item.status.equals("resolved", ignoreCase = true)
        val muni = item.municipality?.replaceFirstChar { it.uppercase() } ?: "Unknown"

        val msg = StringBuilder()
            .append("Municipality: $muni\n")
            .append("Severity: ${item.severity?.uppercase(Locale.ROOT) ?: "N/A"}\n")
            .append("Status: ${item.status?.uppercase(Locale.ROOT) ?: "ACTIVE"}\n")
            .append("Reported: ${item.createdAt?.take(10) ?: "N/A"}")

        item.scansCount?.let {
            msg.append("\nAssociated Scans: $it")
        }

        val builder = MaterialAlertDialogBuilder(requireContext())
            .setTitle(item.diseaseName ?: "Outbreak Details")
            .setMessage(msg.toString())
            .setNegativeButton(R.string.Close, null)

        if (!isResolved && item.id != null) {
            builder.setPositiveButton(R.string.Mark_As_Resolved) { _, _ ->
                confirmResolveOutbreak(item.id, muni)
            }
        }

        builder.show()
    }

    private fun confirmResolveOutbreak(outbreakId: Int, municipalityName: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.Confirm_Resolve_Title)
            .setMessage(getString(R.string.Confirm_Resolve_Msg, municipalityName))
            .setNegativeButton(R.string.Cancel, null)
            .setPositiveButton(R.string.Mark_As_Resolved) { _, _ ->
                executeResolveOutbreak(outbreakId)
            }
            .show()
    }

    private fun executeResolveOutbreak(outbreakId: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                ApiClient.service.closeOutbreak(outbreakId)
                Toast.makeText(requireContext(), R.string.Resolve_Success, Toast.LENGTH_SHORT).show()
                // Refresh data
                loadMapData()
                if (isHistoryMode) {
                    loadOutbreakHistory()
                }
            } catch (e: HttpException) {
                if (_b == null) return@launch
                if (e.code() == 409) {
                    Toast.makeText(requireContext(), R.string.Resolve_Conflict, Toast.LENGTH_SHORT).show()
                    // Refresh to reflect already-resolved state
                    loadMapData()
                    if (isHistoryMode) loadOutbreakHistory()
                } else {
                    Toast.makeText(requireContext(), R.string.Resolve_Error, Toast.LENGTH_SHORT).show()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (_b != null) {
                    Toast.makeText(requireContext(), R.string.Resolve_Error, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun severityRank(severity: String?): Int = when (severity?.lowercase(Locale.ROOT)) {
        "high" -> 3
        "medium" -> 2
        "low" -> 1
        else -> 0
    }

    override fun onDestroyView() {
        requireView().showAdminNav()
        super.onDestroyView()
        _b = null
    }
}