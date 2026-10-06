package com.example.glimpse.Location

import android.location.Location

class LocationUpdateFilter(
    private val minimumDistanceMeters: Float = 5f
) {

    private var lastUploadedLocation: Location? = null

    fun shouldUpload(location: Location): Boolean {
        val previousLocation = lastUploadedLocation

        if (previousLocation == null) {
            lastUploadedLocation = location
            return true
        }

        val distance = previousLocation.distanceTo(location)

        if (distance > minimumDistanceMeters) {
            lastUploadedLocation = location
            return true
        }

        return false
    }

    fun reset() {
        lastUploadedLocation = null
    }
}