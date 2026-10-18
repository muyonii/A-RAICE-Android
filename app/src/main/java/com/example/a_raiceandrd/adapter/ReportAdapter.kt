package com.example.a_raiceandrd.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.a_raiceandrd.R
import com.example.a_raiceandrd.data.repository.api.ReportDto
import com.example.a_raiceandrd.databinding.ItemReportBinding

class ReportAdapter(
    private val onClick: (ReportDto) -> Unit
) : ListAdapter<ReportDto, ReportAdapter.VH>(Diff) {

    inner class VH(private val b: ItemReportBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: ReportDto) {
            val ctx = b.root.context
            val muni = item.municipality?.replaceFirstChar { it.uppercase() }
                ?: ctx.getString(R.string.Report_Municipality_All)
            b.tvReportMuni.text = muni

            val start = item.rangeStart?.take(10).orEmpty()
            val end = item.rangeEnd?.take(10).orEmpty()
            b.tvReportDateRange.text = ctx.getString(R.string.Report_Date_Range_Format, start, end)

            val date = item.createdAt?.take(10).orEmpty()
            val author = item.generatedBy?.name ?: ctx.getString(R.string.Drawer_RoleAdmin)
            b.tvReportMeta.text = ctx.getString(R.string.Report_Created_Format, date, author)

            val summary = item.summaryData
            b.tvReportTotalScans.text = (summary?.totalScans ?: 0).toString()
            b.tvReportConfirmed.text = (summary?.confirmedCases ?: 0).toString()
            b.tvReportTopDisease.text = summary?.topDisease?.takeIf { it.isNotBlank() } ?: "None"

            b.root.setOnClickListener {
                onClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemReportBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) =
        holder.bind(getItem(position))

    object Diff : DiffUtil.ItemCallback<ReportDto>() {
        override fun areItemsTheSame(a: ReportDto, b: ReportDto) = a.id == b.id
        override fun areContentsTheSame(a: ReportDto, b: ReportDto) = a == b
    }
}