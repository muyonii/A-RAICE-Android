package com.example.a_raiceandrd.ui.map

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.a_raiceandrd.R
import com.example.a_raiceandrd.adapter.OutbreakAdapter
import com.example.a_raiceandrd.adapter.OutbreakItem
import com.example.a_raiceandrd.data.repository.api.ApiClient
import com.example.a_raiceandrd.data.repository.api.MapOutbreakDto
import com.example.a_raiceandrd.databinding.FragmentDiseaseMapBinding
import com.example.a_raiceandrd.ui.auth.FLogin
import com.example.a_raiceandrd.ui.setupTopBar
import com.example.a_raiceandrd.ui.showNav
import kotlinx.coroutines.launch
import java.util.Locale

class DiseaseMapFragment : Fragment(R.layout.fragment_disease_map) {

    private var _b: FragmentDiseaseMapBinding? = null
    private val b get() = _b!!

    private var allOutbreaks: List<MapOutbreakDto> = emptyList()
    private var farmerMunicipality: String? = null

    private val outbreakAdapter = OutbreakAdapter {
        // Tapping row in farmer view shows info (handled inline)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _b = FragmentDiseaseMapBinding.bind(view)

        setupTopBar(view, view.findViewById<NestedScrollView>(R.id.DMapScroll))

        b.bataanMapView.isFarmerMode = true
        b.rvNearby.layoutManager = LinearLayoutManager(requireContext())
        b.rvNearby.adapter = outbreakAdapter

        b.bataanMapView.onMunicipalitySelected = { slug ->
            showOutbreaksForArea(slug)
        }

        loadData()
    }

    private fun loadData() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // 1. Get farmer user info
                val user = try {
                    ApiClient.service.getUser()
                } catch (e: Exception) {
                    null
                }

                farmerMunicipality = user?.farmerProfile?.municipality?.trim()?.lowercase(Locale.ROOT)
                val lat = user?.farmerProfile?.farmLat
                val lng = user?.farmerProfile?.farmLong

                farmerMunicipality?.let {
                    b.bataanMapView.setHighlightedMunicipality(it)
                }
                b.bataanMapView.setFarmerLocation(lat, lng)

                // 2. Load map response
                val mapResp = ApiClient.service.getMap()

                val severities = mutableMapOf<String, String?>()
                mapResp.municipalities.orEmpty().forEach { m ->
                    m.slug?.let { s ->
                        severities[s.lowercase(Locale.ROOT)] = m.severity
                    }
                }
                b.bataanMapView.setSeverities(severities)

                allOutbreaks = mapResp.outbreaks.orEmpty()

                // Default view: farmer's own municipality
                showOutbreaksForArea(b.bataanMapView.selectedSlug ?: farmerMunicipality)

            } catch (e: Exception) {
                // If offline or network error, show empty gracefully
                showOutbreaksForArea(farmerMunicipality)
            }
        }
    }

    private fun showOutbreaksForArea(areaSlug: String?) {
        val targetSlug = areaSlug?.trim()?.lowercase(Locale.ROOT)
        val areaName = targetSlug?.replaceFirstChar { it.uppercase() } ?: "Bataan"

        if (targetSlug != null && targetSlug != farmerMunicipality) {
            b.nearbyLabel.text = getString(R.string.Selected_Area, areaName)
        } else {
            b.nearbyLabel.text = getString(R.string.Nearby)
        }

        val matching = if (targetSlug == null) {
            allOutbreaks
        } else {
            allOutbreaks.filter {
                it.municipality?.trim()?.lowercase(Locale.ROOT) == targetSlug
            }
        }

        val items = matching.map { o ->
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

        val isEmpty = items.isEmpty()
        b.tvNoOutbreaks.isVisible = isEmpty
        b.rvNearby.isVisible = !isEmpty

        if (isEmpty) {
            b.tvNoOutbreaks.text = getString(R.string.No_Outbreaks_In_Area, areaName)
        }
    }

    override fun onDestroyView() {
        requireView().showNav()
        super.onDestroyView()
        _b = null
    }
}