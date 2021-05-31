package com.example.sattracker.data

import android.location.Location
import android.util.Log
import com.example.sattracker.model.Weather
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject


class WeatherDataSource {

    private val client = OkHttpClient()
    private val apiKey = "&appid=XXXXXXXXXXXXXX"

    private fun getCurrentWeatherUrl(location: Location) : String
    {
        return "https://api.openweathermap.org/data/2.5/weather?lat=${location.latitude}&lon=${location.longitude}$apiKey"
    }

    fun getWeatherData(location: Location) : Weather
    {
        val url = getCurrentWeatherUrl(location)
        val request = Request.Builder()
                .url(url)
                .build()
        val weather = Weather(location)
        try {
            val response = client.newCall(request).execute()
            val body = response.body()?.string()
            if (body != null) {
                val json = JSONObject(body)
                val weatherInfo = json.getJSONArray("weather")[0] as JSONObject
                weather.type = weatherInfo.getString("main")
                weather.visibility = json.getDouble("visibility").toFloat()
                val cloudsInfo = json.getJSONObject("clouds")
                weather.cloudiness = cloudsInfo.getDouble("all").toFloat()
            }
        }
        catch (exception: Exception) {
            Log.i("exception", exception.toString())
        }
        return weather
    }
}