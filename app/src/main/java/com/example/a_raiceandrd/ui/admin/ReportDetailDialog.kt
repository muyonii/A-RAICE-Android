package com.example.a_raiceandrd.ui.admin

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import com.example.a_raiceandrd.R
import com.example.a_raiceandrd.data.repository.api.ReportDto
import com.example.a_raiceandrd.databinding.DialogReportDetailBinding

object ReportDetailDialog {

    fun show(context: Context, report: ReportDto) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

        val binding = DialogReportDetailBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(binding.root)

        dialog.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        binding.btnDetailClose.setOnClickListener {
            dialog.dismiss()
        }

        val muni = report.municipality?.replaceFirstChar { it.uppercase() }
            ?: context.getString(R.string.Report_Municipality_All)
        val start = report.rangeStart?.take(10).orEmpty()
        val end = report.rangeEnd?.take(10).orEmpty()
        val dateRange = context.getString(R.string.Report_Date_Range_Format, start, end)

        binding.tvDetailSubtitle.text = "$muni · $dateRange"

        val created = report.createdAt?.take(10).orEmpty()
        val author = report.generatedBy?.name ?: context.getString(R.string.Drawer_RoleAdmin)
        binding.tvDetailMeta.text = context.getString(R.string.Report_Created_Format, created, author)

        val summary = report.summaryData
        val totalScans = summary?.totalScans ?: 0
        val confirmedCases = summary?.confirmedCases ?: 0
        val topDisease = summary?.topDisease?.takeIf { it.isNotBlank() } ?: "None"

        binding.tvDetailTotalScans.text = totalScans.toString()
        binding.tvDetailConfirmed.text = confirmedCases.toString()
        binding.tvDetailTopDisease.text = topDisease

        // Per-disease breakdown
        val breakdown = summary?.perDisease.orEmpty()
        binding.llDiseaseBreakdown.removeAllViews()

        if (breakdown.isEmpty()) {
            binding.tvNoDiseaseBreakdown.isVisible = true
        } else {
            binding.tvNoDiseaseBreakdown.isVisible = false
            val maxCount = (breakdown.maxOfOrNull { it.count ?: 0 } ?: 0).coerceAtLeast(1)

            val inflater = LayoutInflater.from(context)
            for (item in breakdown) {
                val row = inflater.inflate(R.layout.item_surveillance_row, binding.llDiseaseBreakdown, false)
                val count = item.count ?: 0

                row.findViewById<TextView>(R.id.rowName).text = item.disease ?: "Unknown"
                row.findViewById<TextView>(R.id.rowSub).text = "$count cases"
                row.findViewById<TextView>(R.id.rowCount).text = count.toString()

                val fill = row.findViewById<View>(R.id.rowFill)
                (fill.layoutParams as LinearLayout.LayoutParams).weight = count * 100f / maxCount

                binding.llDiseaseBreakdown.addView(row)
            }
        }

        binding.btnShareReport.setOnClickListener {
            val shareText = context.getString(
                R.string.Report_Summary_Share_Text,
                muni,
                start,
                end,
                totalScans,
                confirmedCases,
                topDisease,
                author
            )
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, shareText)
                type = "text/plain"
            }
            val chooser = Intent.createChooser(sendIntent, context.getString(R.string.Report_Share_Chooser))
            context.startActivity(chooser)
        }

        dialog.show()
    }
}
