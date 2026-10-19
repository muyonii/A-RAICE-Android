package com.example.a_raiceandrd.ui.alerts

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.a_raiceandrd.MainActivity
import com.example.a_raiceandrd.R
import com.example.a_raiceandrd.adapter.AlertAdapter
import com.example.a_raiceandrd.data.repository.api.ApiClient
import com.example.a_raiceandrd.data.repository.api.ApiService
import com.example.a_raiceandrd.databinding.FragmentAlertsBinding
import com.example.a_raiceandrd.model.AlertItem
import com.example.a_raiceandrd.ui.setupTopBar
import com.example.a_raiceandrd.ui.showNav
import com.example.a_raiceandrd.util.errorMessage
import com.example.a_raiceandrd.util.toAlertItem
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AlertsFragment : Fragment(R.layout.fragment_alerts) {

    private var _binding: FragmentAlertsBinding? = null
    private val binding get() = _binding!!

    private lateinit var api: ApiService
    private val alertList = mutableListOf<AlertItem>()

    private var unreadOnly: Boolean = false
    private var currentPage: Int = 1
    private var lastPage: Int = 1
    private var isLoading: Boolean = false

    private val adapter = AlertAdapter { item ->
        markRead(item)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAlertsBinding.bind(view)
        api = ApiClient.service

        setupTopBar(view, view.findViewById<NestedScrollView>(R.id.alertsScroll))

        binding.rvAlerts.adapter = adapter

        binding.swipeRefresh.setColorSchemeResources(R.color.gold)
        binding.swipeRefresh.setOnRefreshListener {
            loadAlerts(reset = true)
        }

        binding.chipUnreadOnly.setOnCheckedChangeListener { _, isChecked ->
            unreadOnly = isChecked
            loadAlerts(reset = true)
        }

        binding.alertsScroll.setOnScrollChangeListener(
            NestedScrollView.OnScrollChangeListener { v, _, scrollY, _, _ ->
                if (scrollY >= (v.getChildAt(0).measuredHeight - v.measuredHeight - 300)) {
                    if (!isLoading && currentPage < lastPage) {
                        loadAlerts(reset = false)
                    }
                }
            }
        )

        // Show cached items if any
        if (alertList.isNotEmpty()) {
            adapter.submitList(alertList.toList())
        }

        // Refresh on open and every 60 seconds while visible
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                var firstRun = true
                while (isActive) {
                    val atTop = !binding.alertsScroll.canScrollVertically(-1)
                    if (firstRun || atTop) {
                        loadAlerts(reset = true)
                        firstRun = false
                    }
                    delay(POLL_MS)
                }
            }
        }
    }


    private fun loadAlerts(reset: Boolean) {
        if (isLoading) return
        isLoading = true

        val pageToLoad = if (reset) 1 else currentPage + 1

        if (!reset) {
            binding.loadingMoreProgress.isVisible = true
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val unreadParam = if (unreadOnly) 1 else null
                val resp = api.getAlerts(unread = unreadParam, page = pageToLoad)

                currentPage = resp.currentPage ?: pageToLoad
                lastPage = resp.lastPage ?: 1

                val newItems = resp.data.orEmpty().map { it.toAlertItem() }

                if (reset) {
                    alertList.clear()
                    alertList.addAll(newItems)
                } else {
                    alertList.addAll(newItems)
                }

                adapter.submitList(alertList.toList())

                // Update unread count badge
                if (unreadOnly) {
                    val unreadCount = resp.total ?: 0
                    (activity as? MainActivity)?.setAlertsUnreadCount(unreadCount)
                } else {
                    // Update badge from dedicated unread query
                    updateBadgeAsync()
                }

                updateEmptyState()

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load alerts", e)
                if (_binding != null) {
                    if (alertList.isEmpty()) {
                        // Show error state; user can pull-to-refresh to retry
                        binding.emptyAlertsState.isVisible = true
                        binding.rvAlerts.isVisible = false
                        binding.tvEmptyAlerts.text = errorMessage(e)
                    } else {
                        Toast.makeText(requireContext(), errorMessage(e), Toast.LENGTH_SHORT).show()
                    }
                }
            } finally {
                isLoading = false
                _binding?.let {
                    it.swipeRefresh.isRefreshing = false
                    it.loadingMoreProgress.isVisible = false
                }
            }
        }
    }

    private fun updateBadgeAsync() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val unreadResp = api.getAlerts(unread = 1, page = 1)
                val unreadTotal = unreadResp.total ?: 0
                (activity as? MainActivity)?.setAlertsUnreadCount(unreadTotal)
            } catch (e: Exception) {
                // Ignore badge update failure
            }
        }
    }

    private fun updateEmptyState() {
        val b = _binding ?: return
        val isEmpty = alertList.isEmpty()
        b.emptyAlertsState.isVisible = isEmpty
        b.rvAlerts.isVisible = !isEmpty
        if (isEmpty) {
            b.tvEmptyAlerts.setText(
                if (unreadOnly) R.string.No_Unread_Alerts else R.string.No_Alerts_Found
            )
        }
    }

    private fun markRead(item: AlertItem) {
        if (!item.unread) return

        // Optimistically mark as read in local list
        val idx = alertList.indexOfFirst { it.id == item.id }
        if (idx != -1) {
            alertList[idx] = item.copy(unread = false)
            adapter.submitList(alertList.toList())
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                api.markAlertRead(item.id.toInt())
                updateBadgeAsync()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "mark read failed", e)
                // Roll back optimistic change
                if (idx != -1 && _binding != null) {
                    alertList[idx] = item
                    adapter.submitList(alertList.toList())
                    Toast.makeText(requireContext(), getString(R.string.Mark_Read_Failed), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        requireView().showNav()
        super.onDestroyView()
        _binding = null
    }

    private companion object {
        const val TAG = "AlertsFragment"
        const val POLL_MS = 60_000L
    }
}