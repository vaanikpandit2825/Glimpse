package com.example.glimpse.util

import android.content.Context
import android.location.Geocoder
import android.os.Build
import java.util.Locale

object LocationPlaceUtils {
    fun getPlaceName(
        context: Context,
        latitude: Double,
        longitude: Double,
        onResult: (String?) -> Unit
    ) {
        if (!Geocoder.isPresent()) {
            onResult(null)
            return
        }

        val geocoder = Geocoder(
            context,
            Locale.getDefault()
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            geocoder.getFromLocation(
                latitude,
                longitude,
                1
            ) { addresses ->

                val address = addresses.firstOrNull()

                onResult(
                    address?.let {
                        it.locality
                            ?: it.subAdminArea
                            ?: it.adminArea
                    }
                )
            }

        } else {

            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(
                latitude,
                longitude,
                1
            )

            val address = addresses?.firstOrNull()

            onResult(
                address?.locality
                    ?: address?.subAdminArea
                    ?: address?.adminArea
            )
        }
    }
}