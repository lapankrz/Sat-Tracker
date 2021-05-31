package com.example.sattracker.ui.conditions

import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.TEXT_ALIGNMENT_CENTER
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.example.sattracker.MainActivity
import com.example.sattracker.MainViewModel
import com.example.sattracker.R
import com.example.sattracker.data.SatelliteDatasource
import com.example.sattracker.data.WeatherDataSource
import com.example.sattracker.model.Satellite
import com.example.sattracker.model.Weather
import kotlinx.coroutines.*

open class ConditionsFragment : Fragment() {
    private lateinit var root : View
    private lateinit var mainViewModel: MainViewModel
    private lateinit var location: Location
    private lateinit var satelliteDatasource: SatelliteDatasource
    private lateinit var weatherDatasource : WeatherDataSource
    private lateinit var weather: Weather
    private lateinit var satellites : List<Satellite>
    private lateinit var inflater : LayoutInflater

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        this.inflater = inflater
        satelliteDatasource = SatelliteDatasource()
        weatherDatasource = WeatherDataSource()
        mainViewModel = (this.context as MainActivity).mainViewModel
        root = inflater.inflate(R.layout.fragment_conditions, container, false)
        val locationText: TextView = root.findViewById(R.id.locationTextView)
        mainViewModel.location.observe(viewLifecycleOwner, Observer {
            try {
                location = mainViewModel.location.value!!
                if (location.latitude != 0.0 || location.longitude != 0.0) {
                    locationText.text = getLocationName(location)
                    getWeatherData()
                    getWhatsUp()
                }
            }
            catch (ex : Exception)
            {
                ex.printStackTrace()
            }
        })

        return root
    }

    // Gets a list of satellites that are currently above the user
    private fun getWhatsUp()
    {
        val layout : LinearLayout = root.findViewById(R.id.visibleLayout)
        layout.removeAllViews()
        CoroutineScope(Dispatchers.Main + Job()).launch {
            withContext(Dispatchers.Default)
            {
                satellites = satelliteDatasource.getWhatsUp(location)
            }
            withContext(Dispatchers.Main)
            {
                for (i in 0 until satellites.count())
                {
                    val sat = satellites[i]
                    val view = TextView(context)
                    view.text = sat.id.toString() + ": " + sat.name
                    view.textSize = 22f
                    view.textAlignment = TEXT_ALIGNMENT_CENTER
                    val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    params.bottomMargin = 20
                    view.layoutParams = params

                    view.setOnClickListener {
                        val mainActivity = (context as MainActivity)
                        val id = sat.id
                        if (id > 0) {
                            mainActivity.showDetailsInDatabaseFragment(id)
                            mainActivity.switchNavigation(R.id.navigation_dashboard)
                        }
                    }

                    layout.addView(view)
                }
            }
        }
    }

    // Gets city name for the current location
    private fun getLocationName(location: Location): String {
        val geocoder = Geocoder(context)
        val list = geocoder.getFromLocation(location.latitude, location.longitude, 1)
        return list[0].locality
    }

    // Gets information about current weather conditions
    private fun getWeatherData()
    {
        val cloudsTextView : TextView = root.findViewById(R.id.cloudsTextView)
        val weatherTextView : TextView = root.findViewById(R.id.weatherTypeTextView)

        CoroutineScope(Dispatchers.Main + Job()).launch {
            withContext(Dispatchers.Default)
            {
                weather = weatherDatasource.getWeatherData(location)
            }
            withContext(Dispatchers.Main)
            {
                cloudsTextView.text = "${getString(R.string.cloud_coverage)}: " + weather.formatCloudiness() +"%"
                weatherTextView.text = "Weather: " + weather.type
            }
        }
    }
}