package com.example.glimpse.Location

import android.util.Printer
import com.google.android.gms.location.Priority
import android.location.Location

data class LocationStrategy(
    val intervalMillis:Long,
    val priority:Int
)

enum class MovementState{
    STATIONARY,
    FAST,
    SLOW
}

class AdaptiveLocationEngine{

    fun getMovementState(speedMetersPerSecond: Float): MovementState {
        return when {
            speedMetersPerSecond < 1f -> MovementState.STATIONARY
            speedMetersPerSecond > 5f -> MovementState.FAST
            else -> MovementState.SLOW
        }
    }
    fun getMovementState(location: Location): MovementState {
        return getMovementState(location.speed)
    }
    fun getStrategy(
        speedMetersPerSecond: Float,
        batteryLevel:Int
    ): LocationStrategy{
        val isStationary=speedMetersPerSecond<1f
        val isFast=speedMetersPerSecond>5f
        val isLowBattery=batteryLevel<=30

        return when {
            isStationary && isLowBattery -> {
                LocationStrategy(
                    intervalMillis = 60_000L,
                    priority = Priority.PRIORITY_BALANCED_POWER_ACCURACY
                )
            }
            isStationary->{
                LocationStrategy(
                    intervalMillis = 30_000L,
                    priority = Priority.PRIORITY_BALANCED_POWER_ACCURACY
                )
            }
            isFast && isLowBattery->{
                LocationStrategy(
                    intervalMillis = 15_000L,
                    priority = Priority.PRIORITY_BALANCED_POWER_ACCURACY
                )
            }
            isFast->{
                LocationStrategy(
                    intervalMillis = 5_000L,
                    priority = Priority.PRIORITY_HIGH_ACCURACY
                )
            }
            isLowBattery->{
                LocationStrategy(
                    intervalMillis = 30_000L,
                    priority = Priority.PRIORITY_BALANCED_POWER_ACCURACY
                )
            }
            else->{
                LocationStrategy(
                    intervalMillis = 15_000L,
                    priority = Priority.PRIORITY_BALANCED_POWER_ACCURACY
                )
            }

        }
    }
}