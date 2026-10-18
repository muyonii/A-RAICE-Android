package com.example.a_raiceandrd.ui.alerts

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.a_raiceandrd.R
import com.example.a_raiceandrd.adapter.AlertAdapter
import com.example.a_raiceandrd.databinding.FragmentAlertsBinding
import com.example.a_raiceandrd.model.AlertItem
import com.example.a_raiceandrd.data.repository.api.ApiService
import com.example.a_raiceandrd.data.repository.api.ApiClient
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.isActive
import com.example.a_raiceandrd.util.toAlertItem
import android.util.Log
import androidx.core.widget.NestedScrollView
import com.example.a_raiceandrd.ui.setupTopBar
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.delay
import com.example.a_raiceandrd.ui.showNav

class AlertsFragment : Fragment(R.layout.fragment_alerts) {

    private var _binding: FragmentAlertsBinding? = null
    private val binding get() = _binding!!

    private lateinit var api: ApiService
    private val adapter = AlertAdapter { markRead(it) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAlertsBinding.bind(view)
        api = ApiClient.service

        setupTopBar(view, view.findViewById<NestedScrollView>(R.id.alertsScroll))

        binding.rvAlerts.adapter = adapter
        adapter.submitList(lastItems)   // show the last known list instantly

        // Refresh when the screen opens, then every 30s while it is visible.
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (isActive) {
                    refresh()
                    delay(POLL_MS)
                }
            }
        }
    }

    private suspend fun refresh() {
        try {
            val items = api.getAlerts().data.orEmpty().map { it.toAlertItem() }
            if (_binding != null) adapter.submitList(items)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "load alerts failed", e)   // 401 = token expired -> back to log in
        }
    }

    private fun markRead(item: AlertItem) {
        if (!item.unread) return
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                api.markAlertRead(item.id.toInt())
                refresh()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "mark read failed", e)
            }
        }
    }

    override fun onDestroyView() {
        requireView().showNav()
        super.onDestroyView()
        _binding = null
    }

    private companion object {
        var lastItems: List<AlertItem> = emptyList()   // in-memory cache
        const val TAG = "AlertsFragment"
        const val POLL_MS = 30_000L
    }
}