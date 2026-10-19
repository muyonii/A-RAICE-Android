package com.example.a_raiceandrd.ui.admin

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import com.example.a_raiceandrd.R
import com.example.a_raiceandrd.adapter.FarmerRecordsAdapter
import com.example.a_raiceandrd.data.repository.api.ApiClient
import com.example.a_raiceandrd.data.repository.api.MapFarmDto
import com.example.a_raiceandrd.databinding.DialogFarmerRecordsBinding
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.a_raiceandrd.util.errorMessage

object FarmerRecordsDialog {

    fun show(context: Context) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        val binding = DialogFarmerRecordsBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(binding.root)

        dialog.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        val adapter = FarmerRecordsAdapter()
        binding.rvFarmers.adapter = adapter

        binding.btnClose.setOnClickListener {
            dialog.dismiss()
        }

        var allFarms = listOf<MapFarmDto>()

        fun filter(query: String) {
            val q = query.trim().lowercase()
            val filtered = if (q.isEmpty()) {
                allFarms
            } else {
                allFarms.filter { farm ->
                    // farmerName getter handles flat-staff and nested-farmer shapes
                    val name = farm.farmerName.orEmpty().lowercase()
                    val brgy = farm.barangay.orEmpty().lowercase()
                    val muni = farm.municipality.orEmpty().lowercase()
                    name.contains(q) || brgy.contains(q) || muni.contains(q)
                }
            }
            adapter.submitList(filtered)
            binding.tvNoFarmers.isVisible = filtered.isEmpty()
            binding.tvNoFarmers.setText(R.string.No_Farmers_Found)
        }

        binding.etSearchFarmers.doOnTextChanged { text, _, _, _ ->
            filter(text?.toString().orEmpty())
        }

        binding.loadingFarmers.isVisible = true
        binding.tvNoFarmers.isVisible = false

        // Item 11: Use a Job so we can cancel it on dismiss; never touch views after dismiss.
        val job = Job()
        val scope = CoroutineScope(Dispatchers.Main + job)

        dialog.setOnDismissListener {
            job.cancel()
        }

        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.service.getMap()
                }
                if (!job.isCancelled) {
                    allFarms = response.farms.orEmpty()
                    binding.loadingFarmers.isVisible = false
                    filter(binding.etSearchFarmers.text?.toString().orEmpty())
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (!job.isCancelled) {
                    binding.loadingFarmers.isVisible = false
                    binding.tvNoFarmers.isVisible = true
                    binding.tvNoFarmers.text = errorMessage(e)
                }
            }
        }

        dialog.show()
    }
}
