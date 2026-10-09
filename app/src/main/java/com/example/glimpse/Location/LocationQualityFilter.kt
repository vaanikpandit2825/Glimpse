package com.example.glimpse.Location

import android.location.Location
import android.os.SystemClock

class LocationQualityFilter(
    private val maximumAccuracyMeters: Float = 50f,
    private val maximumAgeMillis: Long = 30_000L
) {

    fun isValid(location: Location): Boolean {
        val ageMillis =
            (SystemClock.elapsedRealtimeNanos() - location.elapsedRealtimeNanos) /
                    1_000_000L

        if (ageMillis < 0 || ageMillis > maximumAgeMillis) {
            return false
        }

        if (!location.hasAccuracy()) {
            return false
        }

        if (location.accuracy > maximumAccuracyMeters) {
            return false
        }

        return true
    }
}
