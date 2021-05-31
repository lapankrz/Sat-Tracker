package com.example.sattracker.model

import android.location.Location

data class Weather(val location: Location)
{
    var cloudiness = 0f
    var visibility = 1f
    var type = "Clear"

    fun formatCloudiness() : String
    {
        return cloudiness.toInt().toString()
    }
}