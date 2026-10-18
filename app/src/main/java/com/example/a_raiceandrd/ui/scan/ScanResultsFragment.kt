package com.example.a_raiceandrd.ui.scan

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.navigation.fragment.findNavController
import com.example.a_raiceandrd.R
import com.example.a_raiceandrd.databinding.FragmentScanResultsBinding
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.core.content.ContextCompat
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import android.widget.TextView
import com.example.a_raiceandrd.data.repository.api.ApiClient
import com.example.a_raiceandrd.data.repository.api.ScanResponse
import com.example.a_raiceandrd.util.errorMessage
import com.example.a_raiceandrd.databinding.TreatmentItemBinding
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface

class ScanResultsFragment : Fragment(R.layout.fragment_scan_results) {

    private var _b: FragmentScanResultsBinding? = null
    private val b get() = _b!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _b = FragmentScanResultsBinding.bind(view)
        b.btnBack.setOnClickListener { findNavController().popBackStack() }

        val scanId = requireArguments().getInt("scanId")
        val scannedAt = requireArguments().getLong("scannedAt", System.currentTimeMillis())
        loadImage(requireArguments().getString("imagePath"))
        b.tvScanned.text = SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.getDefault())
            .format(Date(scannedAt))

        b.resultCard.isVisible = false
        b.tvTreatment.isVisible = false
        b.treatmentCard.isVisible = false

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val scan = withContext(Dispatchers.IO) { ApiClient.service.getScan(scanId) }
                render(scan)
            } catch (e: Exception) {
                showMessage(errorMessage(e))
            }
        }
    }

    private fun render(scan: ScanResponse) {
        val disease = scan.disease
        if (disease == null) {
            showMessage("No disease identified. Retake the photo: one leaf, close-up, daylight, plain background.")
            return
        }

        b.resultCard.isVisible = true
        b.tvDisease.text = disease.name
        b.tvConfidence.text = scan.confidence?.let { "${(it * 100).toInt()}%" } ?: "—"

        if ((scan.confidence ?: 1f) < 0.6f) {
            showMessage("Low confidence. Consider retaking the photo in better light.")
        }

        val treatments = disease.treatments.orEmpty()
        b.tvTreatment.isVisible = true
        b.treatmentCard.isVisible = true
        b.llTreatments.removeAllViews()

        if (treatments.isEmpty()) {
            b.llTreatments.addView(TextView(requireContext()).apply {
                text = "No treatment information available."
            })
            return
        }

        // Show treatments in the order the API returns them
        treatments.forEachIndexed { i, t ->
            addTreatmentRow(i, t.title, t.description.orEmpty(), t.type)
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
            else         -> R.color.surface_chip to R.color.surface_chip
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

    private fun loadImage(path: String?) {
        if (path == null) { b.imageCard.isVisible = false; return }
        viewLifecycleOwner.lifecycleScope.launch {
            val bmp = withContext(Dispatchers.IO) { decodeScaled(path) }
            if (_b == null) return@launch
            if (bmp != null) b.ivScan.setImageBitmap(bmp) else b.imageCard.isVisible = false
        }
    }

    // decode a smaller copy (saves memory) and fix the camera rotation
    private fun decodeScaled(path: String, maxSide: Int = 1024): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / sample > maxSide * 2) sample *= 2

        val bmp = BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sample })
            ?: return null

        val degrees = when (ExifInterface(path).getAttributeInt(
            ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
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