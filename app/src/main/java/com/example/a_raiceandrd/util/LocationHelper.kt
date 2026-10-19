package com.example.a_raiceandrd.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/**
 * Item 5: Shared location helper.
 * Requests a fresh fix via getCurrentLocation(HIGH_ACCURACY), falls back to lastLocation,
 * times out after [TIMEOUT_MS] (default 10 seconds). Returns a sealed LocationResult.
 * Never throws; callers check the result and show the appropriate string.
 */
object LocationHelper {

    private const val TIMEOUT_MS = 10_000L

    sealed class LocationResult {
        data class Success(val location: Location) : LocationResult()
        object PermissionDenied : LocationResult()
        object ServicesOff : LocationResult()
        object Unavailable : LocationResult()
        object Timeout : LocationResult()
    }

    suspend fun fetchLocation(context: Context): LocationResult {
        // Check permission first
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            return LocationResult.PermissionDenied
        }

        val client = LocationServices.getFusedLocationProviderClient(context)

        // Try getCurrentLocation with a timeout (FusedLocationProviderClient requires CancellationToken)
        val cts = CancellationTokenSource()
        val fresh = withTimeoutOrNull(TIMEOUT_MS) {
            withContext(Dispatchers.Main) {
                suspendCancellableCoroutine<Location?> { cont ->
                    cont.invokeOnCancellation { cts.cancel() }
                    try {
                        @Suppress("MissingPermission")
                        client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                            .addOnSuccessListener { loc -> if (cont.isActive) cont.resume(loc) }
                            .addOnFailureListener { if (cont.isActive) cont.resume(null) }
                            .addOnCanceledListener { if (cont.isActive) cont.resume(null) }
                    } catch (_: Exception) {
                        if (cont.isActive) cont.resume(null)
                    }
                }
            }
        }.also { cts.cancel() } // always cancel the token to free resources

        if (fresh != null) return LocationResult.Success(fresh)

        // Fallback to lastLocation
        val last = withContext(Dispatchers.Main) {
            suspendCancellableCoroutine<Location?> { cont ->
                try {
                    @Suppress("MissingPermission")
                    client.lastLocation
                        .addOnSuccessListener { loc -> if (cont.isActive) cont.resume(loc) }
                        .addOnFailureListener { if (cont.isActive) cont.resume(null) }
                } catch (_: Exception) {
                    if (cont.isActive) cont.resume(null)
                }
            }
        }

        return when {
            last != null -> LocationResult.Success(last)
            fresh === null -> LocationResult.Timeout   // timed out and no last location
            else -> LocationResult.Unavailable
        }
    }
}
