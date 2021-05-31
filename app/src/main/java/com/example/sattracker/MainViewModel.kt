package com.example.sattracker

import android.location.Location
import android.location.LocationManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sattracker.data.SatelliteDatasource
import com.example.sattracker.model.Satellite
import com.example.sattracker.notifications.NotificationUtils
import kotlinx.coroutines.*
import java.time.OffsetDateTime
import java.util.*

class MainViewModel : ViewModel() {

    var datasource : SatelliteDatasource
    private val notificationUtils: NotificationUtils = NotificationUtils()
    lateinit var activity : MainActivity

    private val _observedSatellites = MutableLiveData<MutableList<Satellite>>()
    val observedSatellites : MutableLiveData<MutableList<Satellite>>
        get() = _observedSatellites
    fun setObservedSatellites(satellites : MutableList<Satellite>)
    {
        _observedSatellites.value?.clear()
        _observedSatellites.value?.addAll(satellites)
    }

    private val _location = MutableLiveData<Location>()
    val location: LiveData<Location>
        get() = _location
    fun setLocation(location: Location)
    {
        _location.value = location
    }

    private val _notifiedSatellites = MutableLiveData<MutableList<Satellite>>()
    val notifiedSatellites : MutableLiveData<MutableList<Satellite>>
        get() = _notifiedSatellites
    fun setNotifiedSatellites(satellites : MutableList<Satellite>)
    {
        _notifiedSatellites.value?.clear()
        _notifiedSatellites.value?.addAll(satellites)
    }

    init {
        setLocation(Location(LocationManager.GPS_PROVIDER))
        _observedSatellites.value = mutableListOf<Satellite>()
        _notifiedSatellites.value = mutableListOf<Satellite>()
        datasource = SatelliteDatasource()
    }

    // Adds satellite to the list of observed
    fun addObservedSat(id: Int)
    {
        if (id <= 0)
        {
            return
        }
        var sat: Satellite? = null
        CoroutineScope(Dispatchers.Main + Job()).launch {
            withContext(Dispatchers.Default)
            {
                try {
                    val ret = datasource.getSatData(id, location.value!!)
                    sat = ret
                }
                catch (ex : Exception)
                {
                    ex.printStackTrace()
                }
            }
            withContext(Dispatchers.Main)
            {
                if (sat != null) {
                    _observedSatellites.value?.add(sat!!)
                    _observedSatellites.notifyObserver()
                }
            }
        }
    }

    // Checks if satellite with his ID is already observed
    fun hasSatelliteWithId(id : Int): Boolean {
        return observedSatellites.value!!.any {s -> s.id == id}
    }

    // Turns notifications on for the satellite with this ID
    fun addNotifiedSat(id: Int)
    {
        if (id <= 0)
        {
            return
        }
        var sat: Satellite? = null
        var notificationTime: Long? = null
        CoroutineScope(Dispatchers.Main + Job()).launch {
            withContext(Dispatchers.Default)
            {
                try {
                    sat = datasource.getSatData(id, location.value!!)
                    if (sat != null)
                    {
                        notificationTime = datasource
                            .getNextPass(id, location.value!!)!!
                            .toEpochSecond(OffsetDateTime.now().offset) - 60 * 1000 // 1 minute before visual pass
                    }
                }
                catch (ex : Exception)
                {
                    ex.printStackTrace()
                }
            }
            withContext(Dispatchers.Main)
            {
                if (sat != null && notificationTime != null) {

                    sat!!.nextPass = Date(notificationTime!!)
                    _notifiedSatellites.value?.add(sat!!)
                    notificationUtils.setNotification(notificationTime!!, activity, sat)
                    _notifiedSatellites.notifyObserver()
                }
            }
        }
    }

    // Checks if notification are on for satellite with this ID
    fun notificationsEnabledFor(id: Int) : Boolean {
        val sat : Satellite? = _notifiedSatellites.value!!.find { s -> s.id == id }
        if (sat == null)
        {
            return false
        }
        else
        {
            if (sat.nextPass == null || Calendar.getInstance().time.after(sat.nextPass))
            {
                _notifiedSatellites.value!!.remove(sat)
                return false
            }
        }
        return true
    }

    // Turns off notifications for satellite with this ID
    fun removeNotifiedSat(id: Int)
    {
        _notifiedSatellites.value?.removeIf { t -> t.id == id}
        notificationUtils.cancelNotification(activity, id)
        _notifiedSatellites.notifyObserver()
    }

    // Removes satellite with his ID from the list of observed ones
    fun removeObservedSat(id : Int)
    {
        _observedSatellites.value?.removeIf { t -> t.id == id }
        _observedSatellites.notifyObserver()
    }

    // Gets the list of all observed satellites
    fun getObservedSatIds() : List<Int>?
    {
        return observedSatellites.value?.map { s -> s.id }
    }

    fun <T> MutableLiveData<T>.notifyObserver() {
        this.value = this.value
    }
}