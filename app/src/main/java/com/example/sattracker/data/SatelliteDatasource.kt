package com.example.sattracker.data

import android.content.Context
import android.location.Location
import android.util.Log
import com.example.sattracker.model.Satellite
import com.opencsv.CSVReaderHeaderAware
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.Instant
import java.time.LocalDateTime
import java.util.*
import kotlin.math.min

val maxPassDays = 7
val minVisibility = 15

class SatelliteDatasource {

    private val client = OkHttpClient()
    private val apiKey = "&apiKey=XXX-XXX-XXX-XXX"
    private val searchRadius = 70
    private val categoryId = 0 // Brightest
    private val maxAboveSats = 5

    private fun getSatPositionUrl(id: Int, location: Location) : String
    {
        return "https://api.n2yo.com/rest/v1/satellite/positions/$id/${location.latitude}/${location.longitude}/${location.altitude}/1$apiKey"
    }

    private fun getNextPassUrl(id: Int, location: Location) : String
    {
        return "https://api.n2yo.com/rest/v1/satellite/visualpasses/$id/${location.latitude}/${location.longitude}/${location.altitude}/$maxPassDays/$minVisibility$apiKey"
    }

    private fun getWhatsUpUrl(location: Location) : String
    {
        return "https://api.n2yo.com/rest/v1/satellite/above/${location.latitude}/${location.longitude}/${location.altitude}/$searchRadius/$categoryId/$apiKey"
    }

    fun getSatelliteList(context: Context) : List<Satellite>
    {
        val file = context.assets.open("satcat.csv")
        val isr = BufferedReader(InputStreamReader(file))
        val reader = CSVReaderHeaderAware(isr)
        val resultList = mutableListOf<Satellite>()
        var line = reader.readMap()
        while (line != null) {
            resultList.add(Satellite(line["NORAD_CAT_ID"]!!.toInt(), line["OBJECT_NAME"]!!))
            line = reader.readMap()
        }
        return resultList
    }

    fun getSatData(id : Int, location: Location) : Satellite
    {
        val url = getSatPositionUrl(id, location)
        val request = Request.Builder()
            .url(url)
            .build()
        var satellite = Satellite(0, "N/A")
        try {
            val response = client.newCall(request).execute()
            val body = response.body()?.string()
            if (body != null) {
                val json = JSONObject(body)
                val info = json.getJSONObject("info")
                val positions = json.getJSONArray("positions")
                satellite = Satellite(id, info.getString("satname"))
                extractPositions(positions, satellite)
            }
        }
        catch (exception: Exception) {
            Log.i("exception", exception.toString())
        }

        return satellite
    }

    fun getNextPass(id: Int, location: Location) : LocalDateTime?
    {
        val url = getNextPassUrl(id, location)
        val request = Request.Builder()
                .url(url)
                .build()
        var date : LocalDateTime? = null
        try {
            val response = client.newCall(request).execute()
            val body = response.body()?.string()
            if (body != null) {
                val json = JSONObject(body)
                val pass = json.getJSONArray("passes")[0] as JSONObject
                val unixTime = pass.getInt("startUTC")
                date = Instant.ofEpochSecond(unixTime.toLong())
                        .atZone(TimeZone.getDefault().toZoneId())
                        .toLocalDateTime()
            }
        }
        catch (exception: Exception) {
            Log.i("exception", exception.toString())
        }
        return date
    }

    fun getWhatsUp(location: Location) : List<Satellite>
    {
        val url = getWhatsUpUrl(location)
        val request = Request.Builder()
                .url(url)
                .build()
        var satellites : List<Satellite> = mutableListOf<Satellite>()
        try {
            val response = client.newCall(request).execute()
            val body = response.body()?.string()
            if (body != null) {
                val json = JSONObject(body)
                val above = json.getJSONArray("above")
                satellites = extractWhatsUp(above)
            }
        }
        catch (exception: Exception) {
            Log.i("exception", exception.toString())
        }

        return satellites
    }

    private fun extractWhatsUp(above : JSONArray) : List<Satellite>
    {
        val satellites = mutableListOf<Satellite>()
        for (i in 0 until min(above.length(), maxAboveSats)) {
            val satObj = above.getJSONObject(i)
            val sat = Satellite(satObj.getInt("satid"), satObj.getString("satname"))
            satellites.add(sat)
        }
        return satellites.toList()
    }

    private fun extractPositions(positions : JSONArray, satellite: Satellite)
    {
        val position = (positions[0] as JSONObject)
        satellite.latitude = position.getDouble("satlatitude")
        satellite.longitude = position.getDouble("satlongitude")
        satellite.altitude = position.getDouble("sataltitude")
    }
}