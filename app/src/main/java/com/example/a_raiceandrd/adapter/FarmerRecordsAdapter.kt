package com.example.a_raiceandrd.adapter

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.a_raiceandrd.R
import com.example.a_raiceandrd.data.repository.api.MapFarmDto
import com.example.a_raiceandrd.databinding.ItemFarmerRecordBinding

class FarmerRecordsAdapter : ListAdapter<MapFarmDto, FarmerRecordsAdapter.VH>(Diff) {

    inner class VH(private val b: ItemFarmerRecordBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: MapFarmDto) {
            val ctx = b.root.context
            // Use getters that handle both flat-staff and nested-farmer shapes
            val farmerName = item.farmerName?.takeIf { it.isNotBlank() } ?: ctx.getString(R.string.Farmer_Unknown)
            b.tvFarmerName.text = farmerName

            val locParts = listOfNotNull(
                item.barangay?.takeIf { it.isNotBlank() }?.let { "Brgy. $it" },
                item.municipality?.replaceFirstChar { it.uppercase() }
            )
            b.tvFarmerLocation.text = if (locParts.isNotEmpty()) locParts.joinToString(", ") else ctx.getString(R.string.Location_Not_Specified)

            val scan = item.scan
            if (scan != null) {
                val disease = scan.disease?.takeIf { it.isNotBlank() } ?: ctx.getString(R.string.Scans_Healthy)
                val date = scan.scanDate?.take(10).orEmpty()
                b.tvFarmerLastScan.text = ctx.getString(R.string.Last_Scan_Format, disease, date)
            } else {
                b.tvFarmerLastScan.text = ctx.getString(R.string.No_Scans_Yet)
            }

            val phone = item.farmerContact?.trim()
            if (!phone.isNullOrEmpty()) {
                b.btnCall.isVisible = true
                b.btnCall.setOnClickListener {
                    try {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null))
                        ctx.startActivity(intent)
                    } catch (_: ActivityNotFoundException) {
                        Toast.makeText(ctx, R.string.Error_No_Dialer, Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                b.btnCall.isVisible = false
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemFarmerRecordBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) =
        holder.bind(getItem(position))

    object Diff : DiffUtil.ItemCallback<MapFarmDto>() {
        override fun areItemsTheSame(a: MapFarmDto, b: MapFarmDto) = a.id == b.id
        override fun areContentsTheSame(a: MapFarmDto, b: MapFarmDto) = a == b
    }
}
