package com.example.sattracker.ui.map

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.example.sattracker.MainActivity
import com.example.sattracker.MainViewModel
import com.example.sattracker.R
import com.example.sattracker.model.Satellite
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.*


data class MarkerObject(val id : Int, val marker : Marker)

class MapFragment : Fragment() {

    private lateinit var mainViewModel: MainViewModel
    private val markers : MutableList<MarkerObject> = mutableListOf<MarkerObject>()
    lateinit var mMapView : MapView
    var googleMap : GoogleMap? = null

    private var mapInitialized = false
    private val iconScale = 1.5

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_map, container, false)
        mMapView = root.findViewById(R.id.map) as MapView
        mMapView.onCreate(savedInstanceState)
        mMapView.onResume()
        try {
            MapsInitializer.initialize(requireActivity().applicationContext)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        mMapView.getMapAsync { mMap ->
            googleMap = mMap
            val location = mainViewModel.location.value
            if (!mapInitialized && location != null &&
                    (location.latitude != 0.0 || location.longitude != 0.0))
                setupGoogleMap()
            onObservedSatellitesChanged()
        }

        mainViewModel = (this.context as MainActivity).mainViewModel
        mainViewModel.location.observe(viewLifecycleOwner, Observer {
            try {
                if (googleMap != null && !mapInitialized) {
                    val location = mainViewModel.location.value!!
                    if (location.latitude != 0.0 || location.longitude != 0.0) {
                        setupGoogleMap()
                    }
                }
            }
            catch (ex : Exception)
            {
                ex.printStackTrace()
            }
        })

        mainViewModel.observedSatellites.observe(viewLifecycleOwner, Observer {
            onObservedSatellitesChanged()
        })

        return root
    }

    // Updates markers on the map after a change to the list of observed satellites
    private fun onObservedSatellitesChanged()
    {
        val observedSats = mainViewModel.observedSatellites.value!!
        markers.clear()
        if (googleMap != null)
        {
            googleMap!!.clear()
            addMarkerForUser()
            observedSats.forEach {
                addMarkerForSatellite(it)
            }
        }
    }

    // Sets up Google Map to be displayed to the user
    @SuppressLint("MissingPermission")
    private fun setupGoogleMap()
    {
        mapInitialized = true

        // For showing a move to my location button
        googleMap!!.isMyLocationEnabled = true

        // For dropping a marker at a point on the Map
        addMarkerForUser()

        val observedSats = mainViewModel.observedSatellites.value!!
        for (sat in observedSats) {
            addMarkerForSatellite(sat)
        }

        // For zooming automatically to the location of the marker
        val loc = getLatLngForUser()
        val cameraPosition =
                CameraPosition.Builder().target(loc).zoom(0f).build()
        googleMap!!.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

        googleMap!!.setOnInfoWindowClickListener { marker ->
            val mainActivity = (this.context as MainActivity)
            val id = markers.find { m -> m.marker == marker }?.id
            if (id != null && id > 0) {
                mainActivity.showDetailsInDatabaseFragment(id)
                mainActivity.switchNavigation(R.id.navigation_dashboard)
            }
        }
    }

    // Gets location data about the user
    private fun getLatLngForUser() : LatLng
    {
        val location = mainViewModel.location.value!!
        return LatLng(location.latitude, location.longitude)
    }

    // Adds a marker for the user to the map
    private fun addMarkerForUser()
    {
        val loc = getLatLngForUser()
        if (loc.latitude != 0.0 || loc.longitude != 0.0) {
            val marker = googleMap!!.addMarker(
                    MarkerOptions().position(loc).title("Your location")
            )
            markers.add(MarkerObject(0, marker))
        }
    }

    // Adds a marker for a satellite to the map
    private fun addMarkerForSatellite(satellite: Satellite)
    {
        if (satellite.id <= 0 || !satellite.isInOrbit())
        {
            return
        }
        val satLoc = satellite.getLatLng()
        var markerExists = false
        for (marker in markers)
        {
            if (marker.id == satellite.id)
            {
                markerExists = true
                break
            }
        }

        if (!markerExists) {
            var icon = getBitmapDescriptor(R.drawable.satellite)
            if (satellite.id == 25544)
                icon = getBitmapDescriptor(R.drawable.space_station)

            val marker = googleMap!!.addMarker(
                    MarkerOptions()
                            .position(satLoc)
                            .title(satellite.name)
                            .snippet("NORAD ID: " + satellite.id)
                            .icon(icon)
            )
            markers.add(MarkerObject(satellite.id, marker))
        }
    }

    // Transforms the icon for a satellite to a format used by the map
    private fun getBitmapDescriptor(drawableRes: Int): BitmapDescriptor? {
        val drawable = resources.getDrawable(drawableRes)
        val canvas = Canvas()
        val width = drawable.intrinsicWidth
        val height = drawable.intrinsicHeight
        var bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap = Bitmap.createScaledBitmap(bitmap, (iconScale * width).toInt(), (iconScale * height).toInt(), false)
        canvas.setBitmap(bitmap)
        drawable.setBounds(0, 0, (iconScale * width).toInt(), (iconScale * height).toInt())
        drawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    override fun onResume() {
        super.onResume()
        mMapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mMapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mMapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mMapView.onLowMemory()
    }
}