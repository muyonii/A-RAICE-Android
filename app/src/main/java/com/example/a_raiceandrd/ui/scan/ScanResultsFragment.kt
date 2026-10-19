package com.example.a_raiceandrd.ui.scan

import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.a_raiceandrd.R
import com.example.a_raiceandrd.data.repository.api.ApiClient
import com.example.a_raiceandrd.data.repository.api.ScanDto
import com.example.a_raiceandrd.databinding.FragmentScanResultsBinding
import com.example.a_raiceandrd.databinding.ItemTopDiseaseBinding
import com.example.a_raiceandrd.databinding.TreatmentItemBinding
import com.example.a_raiceandrd.util.errorMessage
import com.example.a_raiceandrd.util.formatTime
import com.example.a_raiceandrd.util.isHealthy
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScanResultsFragment : Fragment(R.layout.fragment_scan_results) {

    private var _b: FragmentScanResultsBinding? = null
    private val b get() = _b!!

    private var scanId: Int = 0
    private var localImagePath: String? = null
    private var scannedAtTimestamp: Long = 0L

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _b = FragmentScanResultsBinding.bind(view)

        b.btnBack.setOnClickListener { findNavController().popBackStack() }

        scanId = requireArguments().getInt("scanId", 0)
        scannedAtTimestamp = requireArguments().getLong("scannedAt", System.currentTimeMillis())
        localImagePath = requireArguments().getString("imagePath")

        // Load local thumbnail immediately
        loadLocalImage(localImagePath)

        b.tvScanned.text = SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.getDefault())
            .format(Date(scannedAtTimestamp))

        b.resultCard.isVisible = false
        b.tvTop3Title.isVisible = false
        b.cardTop3.isVisible = false
        b.tvTreatment.isVisible = false
        b.treatmentCard.isVisible = false
        b.btnRetryScan.setOnClickListener { loadScanDetails() }

        loadScanDetails()
    }

    private fun loadScanDetails() {
        b.scanResultsProgress.isVisible = true
        b.tvMessage.isVisible = false
        b.btnRetryScan.isVisible = false

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val scan = withContext(Dispatchers.IO) { ApiClient.service.getScan(scanId) }
                if (_b != null) {
                    render(scan)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (_b != null) {
                    showMessage(errorMessage(e))
                    b.btnRetryScan.isVisible = true
                }
            } finally {
                if (_b != null) {
                    b.scanResultsProgress.isVisible = false
                }
            }
        }
    }

    private fun render(scan: ScanDto) {
        // Load image from image_url via Glide if present
        if (!scan.imageUrl.isNullOrBlank()) {
            b.imageCard.isVisible = true
            Glide.with(this)
                .load(scan.imageUrl)
                .placeholder(b.ivScan.drawable)
                .error(b.ivScan.drawable)
                .into(b.ivScan)
        }

        // Formatted scan date
        val dateText = formatTime(scan.createdAt).ifEmpty {
            SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.getDefault())
                .format(Date(scannedAtTimestamp))
        }
        b.tvScanned.text = dateText

        if (scan.status.equals("failed", ignoreCase = true)) {
            showMessage(getString(R.string.Scan_Failed))
            b.btnRetryScan.isVisible = true
            return
        }

        val disease = scan.disease
        val isHealthy = disease?.name.equals("Healthy Rice Plant", ignoreCase = true)
        val isUnrecognised = disease == null

        if (isUnrecognised) {
            showMessage(getString(R.string.Scan_Unrecognised_Desc))
            b.resultCard.isVisible = true
            b.tvDisease.text = getString(R.string.Scan_Unrecognised)
            b.tvConfidence.text = scan.confidence?.let { "${Math.round(it * 100)}%" } ?: "—"
            b.btnViewEncyclopedia.isVisible = false
            b.tvTreatment.isVisible = false
            b.treatmentCard.isVisible = false
            b.cardOutbreakNotice.isVisible = false
            return
        }

        b.resultCard.isVisible = true
        b.tvDisease.text = disease.name ?: getString(R.string.Scan_Unrecognised)
        b.tvConfidence.text = scan.confidence?.let { "${Math.round(it * 100)}%" } ?: "—"

        // Outbreak association notice — NOT shown for healthy
        if (!isHealthy && (scan.outbreakId != null || scan.outbreak != null)) {
            b.cardOutbreakNotice.isVisible = true
        } else {
            b.cardOutbreakNotice.isVisible = false
        }

        // For healthy plants, skip the treatment card entirely (keep encyclopedia link)
        if (isHealthy) {
            b.tvTreatment.isVisible = false
            b.treatmentCard.isVisible = false
            // Still show encyclopedia link for healthy scans
            val hId = disease?.id
            if (hId != null && hId > 0) {
                b.btnViewEncyclopedia.isVisible = true
                b.btnViewEncyclopedia.setOnClickListener {
                    val args = Bundle().apply { putInt("diseaseId", hId) }
                    findNavController().navigate(R.id.action_scanResultsFragment_to_encyDetailsFragment, args)
                }
            } else {
                b.btnViewEncyclopedia.isVisible = false
            }
            return
        }

        // Link to matching encyclopedia entry
        val diseaseId = disease.id
        if (diseaseId != null && diseaseId > 0) {
            b.btnViewEncyclopedia.isVisible = true
            b.btnViewEncyclopedia.setOnClickListener {
                val args = Bundle().apply { putInt("diseaseId", diseaseId) }
                findNavController().navigate(
                    R.id.action_scanResultsFragment_to_encyDetailsFragment,
                    args
                )
            }
        } else {
            b.btnViewEncyclopedia.isVisible = false
        }

        // Low confidence notice (< 0.6)
        if ((scan.confidence ?: 1.0) < 0.6) {
            showMessage(getString(R.string.Hint))
        }

        // Top 3 predictions
        val top3 = scan.top3.orEmpty().filter {
            !it.diseaseName.isNullOrBlank() && !it.diseaseName.equals(disease.name, ignoreCase = true)
        }
        if (top3.isNotEmpty()) {
            b.tvTop3Title.isVisible = true
            b.cardTop3.isVisible = true
            b.llTop3.removeAllViews()
            top3.forEach { item ->
                val row = ItemTopDiseaseBinding.inflate(layoutInflater, b.llTop3, false)
                row.tvTopDiseaseName.text = item.diseaseName
                row.tvTopDiseaseConfidence.text = item.confidence?.let { "${Math.round(it * 100)}%" } ?: "—"
                b.llTop3.addView(row.root)
            }
        } else {
            b.tvTop3Title.isVisible = false
            b.cardTop3.isVisible = false
        }

        // Treatments
        val treatments = disease.treatments.orEmpty()
        b.tvTreatment.isVisible = true
        b.treatmentCard.isVisible = true
        b.llTreatments.removeAllViews()

        if (treatments.isEmpty()) {
            b.llTreatments.addView(TextView(requireContext()).apply {
                text = getString(R.string.No_Treatment_Info)
                setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
            })
            return
        }

        // Show treatments in the order the API returns them
        treatments.forEachIndexed { i, t ->
            addTreatmentRow(i, t.title.orEmpty(), t.description.orEmpty(), t.type.orEmpty())
        }
    }

    private fun addTreatmentRow(index: Int, title: String, description: String, type: String) {
        val row = TreatmentItemBinding.inflate(layoutInflater, b.llTreatments, false)
        row.divider.isVisible = index > 0
        row.tvTitle.text = title
        row.tvDesc.text = description
        row.tvDesc.isVisible = description.isNotBlank()

        row.tvTag.text = type.replaceFirstChar { it.uppercase() }
        val (bg, fg) = when (type.trim().lowercase()) {
            "chemical"   -> R.color.badge_high_bg to R.color.badge_high_text
            "biological" -> R.color.badge_moderate_bg to R.color.badge_moderate_text
            "cultural"   -> R.color.badge_low_bg to R.color.badge_low_text
            "organic"    -> R.color.badge_low_bg to R.color.badge_low_text
            else         -> R.color.surface_chip to R.color.text_primary
        }
        row.tvTag.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(requireContext(), bg))
        row.tvTag.setTextColor(ContextCompat.getColor(requireContext(), fg))

        b.llTreatments.addView(row.root)
    }

    private fun showMessage(msg: String) {
        b.tvMessage.text = msg
        b.tvMessage.isVisible = true
    }

    private fun loadLocalImage(path: String?) {
        if (path == null) {
            b.imageCard.isVisible = false
            return
        }
        viewLifecycleOwner.lifecycleScope.launch {
            val bmp = withContext(Dispatchers.IO) { decodeScaled(path) }
            if (_b == null) return@launch
            if (bmp != null) {
                b.imageCard.isVisible = true
                b.ivScan.setImageBitmap(bmp)
            }
        }
    }

    private fun decodeScaled(path: String, maxSide: Int = 1024): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / sample > maxSide * 2) sample *= 2

        val bmp = BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sample })
            ?: return null

        val degrees = when (ExifInterface(path).getAttributeInt(
            ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
        )) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
        if (degrees == 0f) return bmp
        val m = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, m, true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}