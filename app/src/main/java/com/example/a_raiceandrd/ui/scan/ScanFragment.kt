package com.example.a_raiceandrd.ui.scan

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.a_raiceandrd.R
import com.example.a_raiceandrd.data.repository.api.ApiClient
import com.example.a_raiceandrd.databinding.FragmentScanBinding
import com.example.a_raiceandrd.util.ImageUtils
import com.example.a_raiceandrd.util.errorMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class ScanFragment : Fragment(R.layout.fragment_scan) {

    private var _b: FragmentScanBinding? = null
    private val b get() = _b!!
    private var imageCapture: ImageCapture? = null

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startCamera()
        else toast(getString(R.string.Location_Permission_Denied))
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let { submit(it) } }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _b = FragmentScanBinding.bind(view)

        b.btnBack.setOnClickListener { findNavController().popBackStack() }
        b.btnGallery.setOnClickListener {
            galleryLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }
        b.btnCapture.setOnClickListener { capture() }

        // Show AI warmup hint before the first scan
        val prefs = requireContext().getSharedPreferences("scan_prefs", Context.MODE_PRIVATE)
        val hasScannedBefore = prefs.getBoolean("has_scanned_before", false)
        if (!hasScannedBefore) {
            b.tvHint.text = getString(R.string.AI_Warmup_Hint)
        }

        cleanupTempFiles()

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) startCamera()
        else permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    private fun startCamera() {
        val future = ProcessCameraProvider.getInstance(requireContext())
        future.addListener({
            val provider = future.get()
            val preview = Preview.Builder().build()
                .also { it.setSurfaceProvider(b.previewView.surfaceProvider) }
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY).build()
            provider.unbindAll()
            val camera = provider.bindToLifecycle(
                viewLifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture
            )
            // Tap the preview to focus on the leaf
            b.previewView.setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    val point = b.previewView.meteringPointFactory
                        .createPoint(event.x, event.y)
                    camera.cameraControl.startFocusAndMetering(
                        FocusMeteringAction.Builder(point).build()
                    )
                    v.performClick()
                }
                true
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun capture() {
        val ic = imageCapture ?: return
        val file = File.createTempFile("leaf_", ".jpg", requireContext().cacheDir)
        ic.takePicture(
            ImageCapture.OutputFileOptions.Builder(file).build(),
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(r: ImageCapture.OutputFileResults) =
                    submit(Uri.fromFile(file))
                override fun onError(e: ImageCaptureException) =
                    toast(getString(R.string.Capture_Failed))
            })
    }

    private fun submit(uri: Uri) {
        setLoading(true)
        b.tvStatus.text = getString(R.string.AI_Warmup_Hint)

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val (scan, imagePath) = withContext(Dispatchers.IO) {
                    val resolver = requireContext().contentResolver

                    // send the original file unchanged unless it is over the 5 MB limit
                    val raw = resolver.openInputStream(uri)!!.use { it.readBytes() }
                    val bytes = if (raw.size <= 5_000_000) raw
                    else ImageUtils.uriToJpegBytes(requireContext(), uri)

                    val mime = if (bytes === raw) resolver.getType(uri) ?: "image/jpeg"
                    else "image/jpeg"

                    // keep a local copy to show on the results screen
                    val file = File(requireContext().cacheDir, "scan_${System.currentTimeMillis()}.jpg")
                    file.writeBytes(bytes)

                    val part = MultipartBody.Part.createFormData(
                        "image", "leaf.jpg", bytes.toRequestBody(mime.toMediaType())
                    )
                    // Farmers must NOT send GPS or farmer_id
                    ApiClient.service.submitScan(part) to file.absolutePath
                }

                // If scan status failed, show retry dialog
                if (scan.status.equals("failed", ignoreCase = true)) {
                    showFailedDialog(uri)
                    return@launch
                }

                val prefs = requireContext().getSharedPreferences("scan_prefs", Context.MODE_PRIVATE)
                prefs.edit().putBoolean("has_scanned_before", true).apply()

                val args = Bundle().apply {
                    putInt("scanId", scan.id ?: 0)
                    putLong("scannedAt", System.currentTimeMillis())
                    putString("imagePath", imagePath)
                }
                findNavController().navigate(R.id.action_scan_to_scanResultsFragment, args)
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                toast(errorMessage(e))
            } finally {
                if (_b != null) setLoading(false)
            }
        }
    }

    private fun showFailedDialog(retryUri: Uri) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.Scan_title)
            .setMessage(R.string.Scan_Failed)
            .setPositiveButton(R.string.Retry) { _, _ ->
                submit(retryUri)
            }
            .setNegativeButton(R.string.Cancel, null)
            .show()
    }

    private fun setLoading(loading: Boolean) {
        b.loadingOverlay.isVisible = loading
        b.btnCapture.isEnabled = !loading
        b.btnGallery.isEnabled = !loading
    }

    private fun toast(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()

    private fun cleanupTempFiles() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val cache = context?.cacheDir ?: return@launch
                val now = System.currentTimeMillis()
                cache.listFiles { f -> f.name.startsWith("leaf_") || f.name.startsWith("scan_") }
                    ?.filter { now - it.lastModified() > 2 * 60 * 60 * 1000 } // older than 2 hours
                    ?.forEach { it.delete() }
            } catch (_: Exception) {
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}