package com.example.glimpse.Location

import android.location.Location
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.Priority

class AdaptiveLocationTracker(
    private val locationRepository: LocationRepository,
    private val locationEngine: AdaptiveLocationEngine
) {

    private var currentCallback: LocationCallback? = null
    private var currentInterval: Long? = null
    private var currentPriority: Int? = null

    private val locationUpdateFilter = LocationUpdateFilter()

    fun start(
        batteryLevelProvider: () -> Int,
        onLocationReceived: (Location) -> Unit
    ) {
        locationUpdateFilter.reset()

        requestUpdates(
            intervalMillis = 30_000L,
            priority = Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            batteryLevelProvider = batteryLevelProvider,
            onLocationReceived = onLocationReceived
        )
    }

    private fun requestUpdates(
        intervalMillis: Long,
        priority: Int,
        batteryLevelProvider: () -> Int,
        onLocationReceived: (Location) -> Unit
    ) {
        currentCallback?.let {
            locationRepository.stopLocationUpdates(it)
        }

        currentInterval = intervalMillis
        currentPriority = priority

        currentCallback = locationRepository.startLocationUpdates(
            intervalMillis = intervalMillis,
            priority = priority
        ) { location ->
            val batteryLevel = batteryLevelProvider()
            val strategy = locationEngine.getStrategy(
                speedMetersPerSecond = location.speed,
                batteryLevel = batteryLevel
            )
            val strategyChanged =
                strategy.intervalMillis != currentInterval ||
                        strategy.priority != currentPriority

            if (strategyChanged) {
                requestUpdates(
                    intervalMillis = strategy.intervalMillis,
                    priority = strategy.priority,
                    batteryLevelProvider = batteryLevelProvider,
                    onLocationReceived = onLocationReceived
                )
            }
            if (locationUpdateFilter.shouldUpload(location)) {
                onLocationReceived(location)
            }
        }
    }

    fun stop() {
        currentCallback?.let {
            locationRepository.stopLocationUpdates(it)
        }
        currentCallback = null
        currentInterval = null
        currentPriority = null
        locationUpdateFilter.reset()
    }
}