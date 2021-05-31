package com.example.sattracker.model

import com.google.android.gms.maps.model.LatLng
import java.util.*

data class Satellite(val id: Int, val name: String)
{
    var latitude : Double = 0.0
    var longitude : Double = 0.0
    var altitude : Double = 0.0
    var nextPass : Date? = null

    fun isInOrbit() : Boolean
    {
        return altitude > 1
    }

    fun getLatLng() : LatLng
    {
        return LatLng(latitude, longitude)
    }

    fun formatLatitude() : String
    {
        var lat = latitude
        var end = "N"
        if (lat < 0)
        {
            lat = -lat
            end = "S"
        }
        return "%.2f".format(lat) + " " + end
    }

    fun formatLongitude() : String
    {
        var lng = longitude
        var end = "E"
        if (lng < 0)
        {
            lng = -lng
            end = "W"
        }
        return "%.2f".format(lng) + " " + end
    }

    fun formatAltitude() : String
    {
        return "%.2f".format(altitude) + " km"
    }
}