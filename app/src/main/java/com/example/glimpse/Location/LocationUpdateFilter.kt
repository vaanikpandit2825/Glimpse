
package com.example.glimpse.Location

import android.location.Location

class LocationUpdateFilter(
    private val minimumDistanceMeters: Float = 5f
) {

    private var lastUploadedLocation: Location? = null

    fun shouldUpload(location: Location): Boolean {
        val previousLocation = lastUploadedLocation

        return previousLocation == null ||
                previousLocation.distanceTo(location) > minimumDistanceMeters
    }

    fun markUploaded(location: Location) {
        lastUploadedLocation = Location(location)
    }

    fun reset() {
        lastUploadedLocation = null
    }
}
