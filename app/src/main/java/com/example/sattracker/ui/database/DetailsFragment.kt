package com.example.sattracker.ui.database

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TableLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.text.color
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.example.sattracker.IOnBackPressed
import com.example.sattracker.MainActivity
import com.example.sattracker.R
import com.example.sattracker.data.SatelliteDatasource
import com.example.sattracker.model.Satellite
import kotlinx.coroutines.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class DetailsFragment : Fragment(), IOnBackPressed {

    private lateinit var root : View
    private var satId : Int = 0
    lateinit var datasource : SatelliteDatasource
    private lateinit var satellite: Satellite

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreate(savedInstanceState)

        if (arguments != null) {
            satId = requireArguments().getInt("id", -1)
        }

        root = inflater.inflate(R.layout.fragment_details, container, false)

        val backButton: ImageButton = root.findViewById(R.id.back_button)
        backButton.setOnClickListener {
            val mainActivity = this.context as MainActivity
            mainActivity.showListInDatabaseFragment()
        }

        val observeButton : Button = root.findViewById(R.id.observe_button)
        if ((this.context as MainActivity).mainViewModel.hasSatelliteWithId(satId))
            observeButton.text = getString(R.string.unobserve)
        else
            observeButton.text = getString(R.string.observe)
        observeButton.setOnClickListener {
            handleObserveButtonClick(observeButton)
        }

        val addNotificationButton : Button = root.findViewById(R.id.add_notification_button)
        if ((this.context as MainActivity).mainViewModel.notificationsEnabledFor(satId))
            addNotificationButton.text = getString(R.string.remove_notification)
        else
            addNotificationButton.text = getString(R.string.add_notification)
        addNotificationButton.setOnClickListener {
            handleAddNotificationButtonClick(addNotificationButton)
        }

        datasource = SatelliteDatasource()
        getSatData()

        return root
    }

    // Adds or removes a satellite from observed after button is clicked
    private fun handleObserveButtonClick(observeButton: Button)
    {
        val observeText = getString(R.string.observe)
        val unobserveText = getString(R.string.unobserve)
        if (observeButton.text == observeText)
        {
            (this.context as MainActivity).mainViewModel.addObservedSat(satId)
            observeButton.text = unobserveText
        }
        else
        {
            (this.context as MainActivity).mainViewModel.removeObservedSat(satId)
            observeButton.text = observeText
        }
    }

    // Adds or removes a satellite from the list of observed ones after button is clicked
    private fun handleAddNotificationButtonClick(addNotificationButton: Button)
    {
        val addNotificationText = getString(R.string.add_notification)
        val removeNotificationText = getString(R.string.remove_notification)
        if (addNotificationButton.text == addNotificationText)
        {
            (this.context as MainActivity).mainViewModel.addNotifiedSat(satId)
            addNotificationButton.text = removeNotificationText
        }
        else
        {
            (this.context as MainActivity).mainViewModel.removeNotifiedSat(satId)
            addNotificationButton.text = addNotificationText
        }
    }

    // Gets all the data about current satellite
    private fun getSatData()
    {
        val location = (this.context as MainActivity).mainViewModel.location.value!!
        val satName: TextView = root.findViewById(R.id.satName)
        val noradId : TextView = root.findViewById(R.id.norad_id_text)
        val status: TextView = root.findViewById(R.id.status_text)
        val locationTable : TableLayout = root.findViewById(R.id.location_table_layout)
        val locationTitle : TextView = root.findViewById(R.id.location_title)
        val latitude : TextView = root.findViewById(R.id.latitude_text)
        val longitude : TextView = root.findViewById(R.id.longitude_text)
        val altitude : TextView = root.findViewById(R.id.altitude_text)
        val observeButton : Button = root.findViewById(R.id.observe_button)
        val nextPassTitle : TextView = root.findViewById(R.id.next_pass_title)
        val nextPassText : TextView = root.findViewById(R.id.next_pass_text)
        var nextPassTime : LocalDateTime? = null
        val addNotificationButton : Button = root.findViewById(R.id.add_notification_button)

        CoroutineScope(Dispatchers.Main + Job()).launch {
            withContext(Dispatchers.Default)
            {
                satellite = datasource.getSatData(satId, location)
                nextPassTime = datasource.getNextPass(satId, location)
            }
            withContext(Dispatchers.Main)
            {
                satName.text = satellite.name
                noradId.text = getString(R.string.norad) + ": " + satellite.id
                status.text = getStatusText()
                if (satellite.isInOrbit())
                {
                    latitude.text = satellite.formatLatitude()
                    longitude.text = satellite.formatLongitude()
                    altitude.text = satellite.formatAltitude()
                    locationTitle.visibility = View.VISIBLE
                    locationTable.visibility = View.VISIBLE
                    observeButton.visibility = View.VISIBLE
                    nextPassTitle.visibility = View.VISIBLE
                    nextPassText.visibility = View.VISIBLE
                    if (nextPassTime == null)
                    {
                        nextPassTitle.text = getString(R.string.no_passes)
                    }
                    else
                    {
                        nextPassTitle.text = getString(R.string.next_pass)
                        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy,  HH:mm")
                        nextPassText.text = nextPassTime!!.format(formatter)
                        addNotificationButton.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    // Builds the status information about current satellite
    private fun getStatusText() : SpannableStringBuilder
    {
        var status = getString(R.string.in_orbit)
        var color = ContextCompat.getColor(requireContext(), R.color.in_orbit_green)
        if (!satellite.isInOrbit())
        {
            status = getString(R.string.deorbited)
            color = ContextCompat.getColor(requireContext(), R.color.deorbit_red)
        }
       return SpannableStringBuilder()
            .append("Status: ")
            .color(color)
            { append(status) }
    }

    override fun onBackPressed() {
        val manager = (context as FragmentActivity).supportFragmentManager
        val listFragment = ListFragment()
        manager.beginTransaction()
            .replace(R.id.database_constraint, listFragment)
            .addToBackStack(null)
            .commit()
    }
}