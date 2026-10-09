
package com.example.glimpse.Location

import android.location.Location
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.Priority

class AdaptiveLocationTracker(
    private val locationRepository: LocationRepository,
    private val locationEngine: AdaptiveLocationEngine
) {

    companion object {
        private const val TAG = "AdaptiveLocationTracker"
    }

    private val handler = Handler(Looper.getMainLooper())
    private val locationUpdateFilter = LocationUpdateFilter()
    private val locationQualityFilter = LocationQualityFilter()

    private var currentCallback: LocationCallback? = null
    private var currentInterval: Long? = null
    private var currentPriority: Int? = null
    private var isTracking = false
    private var generation = 0

    fun start(
        batteryLevelProvider: () -> Int,
        onLocationReceived: (Location) -> Unit
    ) {
        stop()
        isTracking = true
        val trackingGeneration = generation

        Log.d(TAG, "Tracking started")

        applyStrategy(
            intervalMillis = 30_000L,
            priority = Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            batteryLevelProvider = batteryLevelProvider,
            onLocationReceived = onLocationReceived,
            trackingGeneration = trackingGeneration
        )
    }

    private fun applyStrategy(
        intervalMillis: Long,
        priority: Int,
        batteryLevelProvider: () -> Int,
        onLocationReceived: (Location) -> Unit,
        trackingGeneration: Int
    ) {
        if (!isTracking || trackingGeneration != generation) return

        if (intervalMillis == currentInterval && priority == currentPriority) {
            return
        }

        currentCallback?.let {
            locationRepository.stopLocationUpdates(it)
        }
        currentCallback = null

        currentInterval = intervalMillis
        currentPriority = priority

        Log.d(
            TAG,
            "Requesting updates: interval=${intervalMillis / 1000}s, priority=$priority"
        )

        currentCallback = locationRepository.startLocationUpdates(
            intervalMillis = intervalMillis,
            priority = priority
        ) { location ->
            handler.post {
                if (!isTracking || trackingGeneration != generation) {
                    return@post
                }

                if (!locationQualityFilter.isValid(location)) {
                    Log.d(TAG, "Location rejected by quality filter")
                    return@post
                }

                val strategy = locationEngine.getStrategy(
                    speedMetersPerSecond = location.speed,
                    batteryLevel = batteryLevelProvider()
                )

                if (
                    strategy.intervalMillis != currentInterval ||
                    strategy.priority != currentPriority
                ) {
                    handler.post {
                        applyStrategy(
                            intervalMillis = strategy.intervalMillis,
                            priority = strategy.priority,
                            batteryLevelProvider = batteryLevelProvider,
                            onLocationReceived = onLocationReceived,
                            trackingGeneration = trackingGeneration
                        )
                    }
                }

                if (locationUpdateFilter.shouldUpload(location)) {
                    Log.d(
                        TAG,
                        "Location accepted: accuracy=${location.accuracy}m, speed=${location.speed}m/s"
                    )
                    onLocationReceived(location)
                } else {
                    Log.d(TAG, "Location filtered: movement <= 5m")
                }
            }
        }
    }

    fun stop() {
        generation++
        isTracking = false

        currentCallback?.let {
            locationRepository.stopLocationUpdates(it)
        }

        currentCallback = null
        currentInterval = null
        currentPriority = null
        locationUpdateFilter.reset()

        handler.removeCallbacksAndMessages(null)
        Log.d(TAG, "Tracking stopped")
    }
}
