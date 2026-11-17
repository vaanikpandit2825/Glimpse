
package com.example.glimpse.firebase

import com.example.glimpse.BuildConfig
import com.google.firebase.functions.FirebaseFunctions

object GlimpseFunctions {

    val instance: FirebaseFunctions by lazy {

        FirebaseFunctions
            .getInstance("us-central1")
            .apply {
                if (BuildConfig.DEBUG) {
                    useEmulator("127.0.0.1", 5001)
                }
            }
    }
}
