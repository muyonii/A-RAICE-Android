package com.example.a_raiceandrd.ui.admin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a_raiceandrd.data.repository.AdminRepo
import com.example.a_raiceandrd.model.AdminDashboard
import com.example.a_raiceandrd.model.DashboardSource
import com.example.a_raiceandrd.model.Period
import kotlinx.coroutines.launch

class AdHomeViewModel : ViewModel() {

    private var source: DashboardSource? = null
    private var period = Period.MONTH

    private val _dashboard = MutableLiveData<AdminDashboard>()
    val dashboard: LiveData<AdminDashboard> = _dashboard

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun load() {
        viewModelScope.launch {
            try {
                source = AdminRepo.getDashboardSource()
                publish()
            } catch (e: Exception) {
                _error.value = e.message ?: "Couldn't load the dashboard"
            }
        }
    }

    // Called by the dropdown (stage 2). No new network call is needed.
    fun setPeriod(newPeriod: Period) {
        period = newPeriod
        publish()
    }

    private fun publish() {
        val data = source ?: return
        _dashboard.value = AdminRepo.buildDashboard(data, period)
    }
}