package com.example.a_raiceandrd.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.a_raiceandrd.MainActivity
import com.example.a_raiceandrd.R
import com.example.a_raiceandrd.adapter.AlertAdapter
import com.example.a_raiceandrd.data.repository.api.ApiClient
import com.example.a_raiceandrd.data.repository.api.BroadcastAlertRequest
import com.example.a_raiceandrd.data.repository.api.MapOutbreakDto
import com.example.a_raiceandrd.databinding.FragmentBroadcastBinding
import com.example.a_raiceandrd.ui.auth.FLogin
import com.example.a_raiceandrd.ui.auth.Role
import com.example.a_raiceandrd.ui.hideNavOnScroll
import com.example.a_raiceandrd.ui.showAdminNav
import com.example.a_raiceandrd.util.errorMessage
import com.example.a_raiceandrd.util.hideKeyboard
import com.example.a_raiceandrd.util.toAlertItem
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class BroadcastFragment : Fragment() {

    private var _binding: FragmentBroadcastBinding? = null
    private val binding get() = _binding!!

    private lateinit var alertAdapter: AlertAdapter

    private data class AudienceOption(val label: String, val slug: String?)
    private data class OutbreakOption(val label: String, val id: Int?)

    private val audienceOptions = listOf(
        AudienceOption("All Municipalities (Province-wide)", null),
        AudienceOption("Abucay", "abucay"),
        AudienceOption("Bagac", "bagac"),
        AudienceOption("Balanga", "balanga"),
        AudienceOption("Dinalupihan", "dinalupihan"),
        AudienceOption("Hermosa", "hermosa"),
        AudienceOption("Limay", "limay"),
        AudienceOption("Mariveles", "mariveles"),
        AudienceOption("Morong", "morong"),
        AudienceOption("Orani", "orani"),
        AudienceOption("Orion", "orion"),
        AudienceOption("Pilar", "pilar"),
        AudienceOption("Samal", "samal")
    )

    private var outbreakOptions = mutableListOf<OutbreakOption>()

    private var selectedAudienceIndex = 0
    private var selectedOutbreakIndex = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBroadcastBinding.inflate(inflater, container, false)
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
        binding.topBar.notifBtn.isVisible = false

        binding.broadcastScroll.hideNavOnScroll(binding.topBar.root)

        setupAudienceDropdown()
        setupOutbreakDropdown()
        setupHistoryList()
        setupSendButton()

        loadMapOutbreaks()
        loadAlertsHistory()
    }

    override fun onDestroyView() {
        requireView().showAdminNav()
        _binding = null
        super.onDestroyView()
    }

    private fun setupAudienceDropdown() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            audienceOptions.map { it.label }
        )
        binding.actvAudience.setAdapter(adapter)
        binding.actvAudience.setText(audienceOptions[0].label, false)
        selectedAudienceIndex = 0

        binding.actvAudience.setOnItemClickListener { _, _, position, _ ->
            selectedAudienceIndex = position
        }
    }

    private fun setupOutbreakDropdown() {
        outbreakOptions.clear()
        outbreakOptions.add(OutbreakOption(getString(R.string.Broadcast_Outbreak_None), null))
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            outbreakOptions.map { it.label }
        )
        binding.actvOutbreak.setAdapter(adapter)
        binding.actvOutbreak.setText(outbreakOptions[0].label, false)
        selectedOutbreakIndex = 0

        binding.actvOutbreak.setOnItemClickListener { _, _, position, _ ->
            selectedOutbreakIndex = position
        }
    }

    private fun loadMapOutbreaks() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val mapResp = ApiClient.service.getMap()
                val outbreaks = mapResp.outbreaks.orEmpty()
                outbreakOptions.clear()
                outbreakOptions.add(OutbreakOption(getString(R.string.Broadcast_Outbreak_None), null))
                for (o in outbreaks) {
                    val diseaseName = o.disease ?: "Unknown disease"
                    val muni = o.municipality?.replaceFirstChar { it.uppercase() } ?: "Province"
                    outbreakOptions.add(OutbreakOption("$diseaseName in $muni", o.id))
                }
                if (_binding != null) {
                    val adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        outbreakOptions.map { it.label }
                    )
                    binding.actvOutbreak.setAdapter(adapter)
                    binding.actvOutbreak.setText(outbreakOptions[0].label, false)
                    selectedOutbreakIndex = 0
                }
            } catch (_: Exception) {
            }
        }
    }

    private fun setupHistoryList() {
        alertAdapter = AlertAdapter()
        binding.rvBroadcastHistory.adapter = alertAdapter
    }

    private fun loadAlertsHistory() {
        if (_binding == null) return
        binding.pbHistoryLoading.isVisible = true
        binding.tvNoBroadcastHistory.isVisible = false

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val alertsPage = ApiClient.service.getAlerts(page = 1)
                val items = alertsPage.data.orEmpty().map { it.toAlertItem() }
                if (_binding != null) {
                    alertAdapter.submitList(items)
                    binding.tvNoBroadcastHistory.isVisible = items.isEmpty()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (_binding != null) {
                    binding.tvNoBroadcastHistory.isVisible = true
                    binding.tvNoBroadcastHistory.text = errorMessage(e)
                }
            } finally {
                if (_binding != null) binding.pbHistoryLoading.isVisible = false
            }
        }
    }

    private fun setupSendButton() {
        binding.btnSendBroadcast.setOnClickListener {
            val message = binding.etBroadcastMessage.text?.toString()?.trim().orEmpty()
            if (message.isEmpty()) {
                binding.tilBroadcastMessage.error = getString(R.string.Broadcast_Message_Hint)
                return@setOnClickListener
            }
            binding.tilBroadcastMessage.error = null

            val severity = when (binding.cgSeverity.checkedChipId) {
                R.id.chipLow -> "low"
                R.id.chipHigh -> "high"
                else -> "medium"
            }

            val audience = audienceOptions.getOrNull(selectedAudienceIndex) ?: audienceOptions[0]
            val outbreak = outbreakOptions.getOrNull(selectedOutbreakIndex)

            hideKeyboard()
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.Broadcast_Confirm_Title)
                .setMessage(
                    getString(
                        R.string.Broadcast_Confirm_Message,
                        severity.replaceFirstChar { it.uppercase() },
                        audience.label
                    )
                )
                .setPositiveButton(R.string.Broadcast_Send_Button) { _, _ ->
                    sendBroadcast(message, severity, audience.slug, outbreak?.id)
                }
                .setNegativeButton(R.string.Cancel, null)
                .show()
        }
    }

    private fun sendBroadcast(
        message: String,
        severity: String,
        municipalitySlug: String?,
        outbreakId: Int?
    ) {
        binding.btnSendBroadcast.isEnabled = false
        binding.pbBroadcastSending.isVisible = true

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val req = BroadcastAlertRequest(
                    message = message,
                    severity = severity,
                    municipality = municipalitySlug,
                    outbreakId = outbreakId
                )
                val resp = ApiClient.service.broadcastAlert(req)
                val recipients = resp.alert?.recipientsCount ?: 0
                Toast.makeText(
                    requireContext(),
                    getString(R.string.Broadcast_Success, recipients),
                    Toast.LENGTH_LONG
                ).show()

                binding.etBroadcastMessage.setText("")
                loadAlertsHistory()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (_binding != null) {
                    val err = errorMessage(e)
                    Toast.makeText(requireContext(), err, Toast.LENGTH_LONG).show()
                }
            } finally {
                if (_binding != null) {
                    binding.btnSendBroadcast.isEnabled = true
                    binding.pbBroadcastSending.isVisible = false
                }
            }
        }
    }
}