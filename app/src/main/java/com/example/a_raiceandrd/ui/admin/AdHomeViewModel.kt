package com.example.a_raiceandrd.ui.admin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a_raiceandrd.data.repository.AdminRepo
import com.example.a_raiceandrd.model.AdminDashboard
import com.example.a_raiceandrd.model.DashboardSource
import com.example.a_raiceandrd.model.Period
import com.example.a_raiceandrd.util.errorMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class AdHomeViewModel : ViewModel() {

    private var source: DashboardSource? = null
    private var period = Period.MONTH

    private val _dashboard = MutableLiveData<AdminDashboard>()
    val dashboard: LiveData<AdminDashboard> = _dashboard

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun load(forceRefresh: Boolean = false) {
        if (!forceRefresh && source != null) {
            publish()
            return
        }
        viewModelScope.launch {
            try {
                source = AdminRepo.getDashboardSource()
                publish()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _error.value = errorMessage(e)
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