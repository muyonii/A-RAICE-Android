package com.example.a_raiceandrd.ui.admin

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.a_raiceandrd.R
import com.example.a_raiceandrd.model.AdminDashboard
import com.example.a_raiceandrd.model.ChartBar
import com.example.a_raiceandrd.ui.auth.FLogin
import com.example.a_raiceandrd.ui.auth.Role
import androidx.core.widget.NestedScrollView
import com.example.a_raiceandrd.ui.hideNavOnScroll
import com.example.a_raiceandrd.ui.showAdminNav
import android.widget.PopupMenu
import com.google.android.material.button.MaterialButton
import com.example.a_raiceandrd.model.Period
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import android.content.res.ColorStateList
import com.example.a_raiceandrd.model.RecentScan
import com.example.a_raiceandrd.MainActivity
import com.example.a_raiceandrd.util.isHealthy
import com.example.a_raiceandrd.util.isUnrecognised
import com.example.a_raiceandrd.data.repository.api.DiseaseDto

class AdHomeFragment : Fragment(R.layout.fragment_ad_home) {

    override fun onDestroyView() {
        requireView().showAdminNav()
        super.onDestroyView()
    }

    private lateinit var viewModel: AdHomeViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<NestedScrollView>(R.id.adScroll)
            .hideNavOnScroll(view.findViewById(R.id.adTopBar))

        // Only admins may be here
        if (FLogin.getCurrentRole() != Role.ADMIN) {
            findNavController().navigate(R.id.action_global_loginFragment)
            return
        }

        // Buttons
        view.findViewById<View>(R.id.profileBtn).setOnClickListener {
            (requireActivity() as MainActivity).openDrawer()
        }
        view.findViewById<View>(R.id.btnGenerateConsolidated)?.setOnClickListener {
            findNavController().navigate(R.id.adreport)
        }

        view.findViewById<View>(R.id.tvViewMap).setOnClickListener {
            findNavController().navigate(R.id.admap)
        }

        view.findViewById<View>(R.id.btnViewAllScans).setOnClickListener {
            findNavController().navigate(R.id.action_adhome_to_scanHistoryFragment)
        }

        view.findViewById<View>(R.id.cardFarmers).setOnClickListener {
            FarmerRecordsDialog.show(requireContext())
        }

        // Data: scope ViewModel to activity so it survives navigation (Group 7: no re-fetch on return)
        viewModel = ViewModelProvider(requireActivity())[AdHomeViewModel::class.java]
        val periodBtn = view.findViewById<MaterialButton>(R.id.btnPeriod)
        periodBtn.setOnClickListener { anchor ->
            val popup = PopupMenu(requireContext(), anchor)
            popup.menu.add(0, 0, 0, R.string.Period_Week)
            popup.menu.add(0, 1, 1, R.string.Period_Month)
            popup.menu.add(0, 2, 2, R.string.Period_Year)
            popup.setOnMenuItemClickListener { item ->
                periodBtn.text = item.title
                viewModel.setPeriod(Period.values()[item.itemId])
                true
            }
            popup.show()
        }
        viewModel.dashboard.observe(viewLifecycleOwner) { data ->
            showStats(view, data)
            showChart(view, data.chartBars)
            showRecentScans(view, data.recentScans)
        }

        viewModel.error.observe(viewLifecycleOwner) { message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
        styleCard(view, R.id.cardScans, R.drawable.ic_scan, R.color.tile_gold, R.color.gold)
        styleCard(view, R.id.cardConfirmed, R.drawable.ic_warning, R.color.tile_rust, R.color.rust)
        styleCard(view, R.id.cardActive, R.drawable.ic_pin, R.color.tile_sky, R.color.sky)
        styleCard(view, R.id.cardFarmers, R.drawable.ic_people, R.color.tile_green, R.color.ok_green)
        // load() uses forceRefresh=false by default: no re-fetch if data is already loaded
        viewModel.load()
    }

    private fun styleCard(root: View, cardId: Int, icon: Int, tile: Int, tint: Int) {
        val card = root.findViewById<View>(cardId)
        card.findViewById<MaterialCardView>(R.id.statTile)
            .setCardBackgroundColor(ContextCompat.getColor(requireContext(), tile))
        card.findViewById<ImageView>(R.id.statIcon).apply {
            setImageResource(icon)
            setColorFilter(ContextCompat.getColor(requireContext(), tint))
        }
    }

    private fun setCard(root: View, cardId: Int, label: String, value: String, caption: String) {
        val card = root.findViewById<View>(cardId)
        card.findViewById<TextView>(R.id.statLabel).text = label
        card.findViewById<TextView>(R.id.statValue).text = value
        card.findViewById<TextView>(R.id.statCaption).text = caption
    }

    private fun showStats(root: View, data: AdminDashboard) {
        val rate = if (data.totalScans > 0) data.confirmedCases * 100.0 / data.totalScans else 0.0
        setCard(root, R.id.cardScans, getString(R.string.Stat_Scans),
            data.totalScans.toString(), getString(R.string.Stat_Scans_Sub))
        setCard(root, R.id.cardConfirmed, getString(R.string.Stat_Confirmed),
            data.confirmedCases.toString(),
            getString(R.string.Stat_Confirmed_Sub, String.format("%.1f", rate)))
        setCard(root, R.id.cardActive, getString(R.string.Stat_Active),
            data.activeOutbreaks.toString(),
            getString(R.string.Stat_Active_Sub, data.municipalitiesAffected))
        setCard(root, R.id.cardFarmers, getString(R.string.Stat_Farmers),
            data.farmersMonitored.toString(), getString(R.string.Stat_Farmers_Sub))
    }

    private fun showChart(root: View, data: List<ChartBar>) {
        val rows = root.findViewById<LinearLayout>(R.id.surveillanceRows)
        rows.removeAllViews()

        val top = data.sortedByDescending { it.count }.take(5)
        val max = (top.maxOfOrNull { it.count } ?: 0).coerceAtLeast(1)   // never divide by 0

        for (item in top) {
            val row = layoutInflater.inflate(R.layout.item_surveillance_row, rows, false)
            row.findViewById<TextView>(R.id.rowName).text = item.label
            row.findViewById<TextView>(R.id.rowSub).text = item.sub
            row.findViewById<TextView>(R.id.rowCount).text = item.count.toString()

            val fill = row.findViewById<View>(R.id.rowFill)
            (fill.layoutParams as LinearLayout.LayoutParams).weight = item.count * 100f / max

            rows.addView(row)
        }

        root.findViewById<TextView>(R.id.tvSurveillanceTotal).text =
            getString(R.string.Surveillance_Total, data.sumOf { it.count })
    }

    private class Look(
        val tile: Int, val tint: Int,
        val badgeBg: Int, val badgeText: Int, val label: Int
    )

    private fun showRecentScans(root: View, scans: List<RecentScan>) {
        val rows = root.findViewById<LinearLayout>(R.id.recentScansRows)
        rows.removeAllViews()
        val ctx = requireContext()

        for (scan in scans) {
            val row = layoutInflater.inflate(R.layout.item_recent_scan, rows, false)

            // Determine scan classification using shared helpers
            val healthy = isHealthy(scan.disease)
            val unrecognised = scan.disease == null && !healthy

            val look = when {
                healthy -> Look(R.color.tile_green, R.color.ok_green,
                    R.color.badge_low_bg, R.color.badge_low_text, R.string.Badge_Healthy)
                unrecognised -> Look(R.color.surface_chip, R.color.text_secondary,
                    R.color.surface_chip, R.color.text_secondary, R.string.Badge_Unrecognised)
                scan.inOutbreak -> Look(R.color.tile_rust, R.color.rust,
                    R.color.badge_high_bg, R.color.badge_high_text, R.string.Badge_Outbreak)
                else -> Look(R.color.tile_gold, R.color.gold,
                    R.color.badge_moderate_bg, R.color.badge_moderate_text, R.string.Badge_Detected)
            }

            row.findViewById<TextView>(R.id.scanTitle).text = when {
                healthy -> getString(R.string.Scans_Healthy)
                unrecognised -> getString(R.string.Scan_Unrecognised)
                else -> scan.disease ?: getString(R.string.Scan_Unrecognised)
            }
            row.findViewById<TextView>(R.id.scanSub).text =
                getString(R.string.Scans_Sub, scan.farmer, scan.confidence)

            row.findViewById<MaterialCardView>(R.id.scanTile)
                .setCardBackgroundColor(ContextCompat.getColor(ctx, look.tile))
            row.findViewById<ImageView>(R.id.scanIcon).apply {
                setImageResource(R.drawable.ic_scan)
                setColorFilter(ContextCompat.getColor(ctx, look.tint))
            }

            row.findViewById<TextView>(R.id.scanBadge).apply {
                setText(look.label)
                setTextColor(ContextCompat.getColor(ctx, look.badgeText))
                backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(ctx, look.badgeBg))
            }

            row.setOnClickListener {
                scan.id?.let { scanId ->
                    val bundle = Bundle().apply { putInt("scanId", scanId) }
                    findNavController().navigate(R.id.action_adhome_to_scanResultsFragment, bundle)
                }
            }

            rows.addView(row)
        }
    }
}