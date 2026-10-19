package com.example.a_raiceandrd.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.IOException

object ImageUtils {
    private const val MAX_BYTES = 4_500_000   // stay under the API's 5 MB limit
    private const val MAX_SIDE = 1280

    fun uriToJpegBytes(ctx: Context, uri: Uri): ByteArray {
        val resolver = ctx.contentResolver

        // read the image size only
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }

        // decode a smaller version to save memory
        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / sample > MAX_SIDE * 2) sample *= 2
        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        val decoded = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
            ?: throw IOException("Cannot read image")

        // fix rotation from EXIF
        val orientation = resolver.openInputStream(uri)?.use {
            ExifInterface(it).getAttributeInt(
                ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
            )
        } ?: ExifInterface.ORIENTATION_NORMAL
        val degrees = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }

        // scale down and rotate
        val scale = MAX_SIDE.toFloat() / maxOf(decoded.width, decoded.height)
        val matrix = Matrix().apply {
            if (scale < 1f) postScale(scale, scale)
            if (degrees != 0f) postRotate(degrees)
        }
        val bitmap = Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)

        // compress until under the size limit (start at 85 as specified)
        var quality = 85
        var bytes: ByteArray
        do {
            bytes = ByteArrayOutputStream().also {
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, it)
            }.toByteArray()
            quality -= 10
        } while (bytes.size > MAX_BYTES && quality >= 40)
        return bytes
    }
}