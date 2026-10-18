package com.example.a_raiceandrd.ui.scan

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.navigation.fragment.findNavController
import com.example.a_raiceandrd.R
import com.example.a_raiceandrd.databinding.FragmentScanBinding
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.core.view.isVisible
import androidx.activity.result.PickVisualMediaRequest
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.Manifest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.core.Preview
import androidx.camera.core.CameraSelector
import java.io.File
import android.net.Uri
import androidx.camera.core.ImageCaptureException
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import okhttp3.MultipartBody
import androidx.core.os.bundleOf
import okhttp3.MediaType.Companion.toMediaType
import android.widget.Toast
import com.example.a_raiceandrd.data.repository.api.ApiClient
import com.example.a_raiceandrd.util.ImageUtils
import okhttp3.RequestBody.Companion.toRequestBody
import com.example.a_raiceandrd.util.errorMessage
import android.view.MotionEvent
import androidx.camera.core.FocusMeteringAction

class ScanFragment : Fragment(R.layout.fragment_scan) {

    private var _b: FragmentScanBinding? = null
    private val b get() = _b!!
    private var imageCapture: ImageCapture? = null

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startCamera()
        else toast("Camera permission is needed to scan. You can still use the gallery.")
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let { submit(it) } }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _b = FragmentScanBinding.bind(view)

        b.btnBack.setOnClickListener { findNavController().popBackStack() }
        b.btnGallery.setOnClickListener {
            galleryLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }
        b.btnCapture.setOnClickListener { capture() }

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
            // tap the preview to focus on the leaf
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
                    toast("Capture failed: ${e.message}")
            })
    }

    private fun submit(uri: Uri) {
        setLoading(true)
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
                    ApiClient.service.submitScan(part) to file.absolutePath
                }
                findNavController().navigate(
                    R.id.action_scan_to_scanResultsFragment,
                    bundleOf("scanId" to scan.id, "scannedAt" to System.currentTimeMillis(), "imagePath" to imagePath)
                )
            } catch (e: Exception) {
                toast(errorMessage(e))
            } finally {
                if (_b != null) setLoading(false)
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        b.loadingOverlay.isVisible = loading
        b.btnCapture.isEnabled = !loading
        b.btnGallery.isEnabled = !loading
    }

    private fun toast(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}