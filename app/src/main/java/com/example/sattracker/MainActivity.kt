package com.example.sattracker

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.sattracker.ui.conditions.ConditionsFragment
import com.example.sattracker.ui.database.DatabaseFragment
import com.example.sattracker.ui.map.MapFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.IOException
import java.io.OutputStreamWriter


interface IOnBackPressed {
    fun onBackPressed()
}

class MainActivity : AppCompatActivity(), LocationListener {

    private var mapFragment: MapFragment = MapFragment()
    private val databaseFragment: DatabaseFragment = DatabaseFragment()
    private val conditionsFragment: ConditionsFragment = ConditionsFragment()
    val fm: FragmentManager = supportFragmentManager
    var active: Fragment = mapFragment
    lateinit var navView: BottomNavigationView

    var mainViewModel: MainViewModel = MainViewModel()
    private lateinit var locationManager: LocationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainViewModel.activity = this@MainActivity
        getLocation()
        loadSavedData(savedInstanceState)
        setContentView(R.layout.activity_main)
        navView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)

        // set up of navigation bar
        val appBarConfiguration = AppBarConfiguration(setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        clearAllFragments()

        // sets up all fragments
        fm.beginTransaction().add(R.id.nav_host_fragment, conditionsFragment, "conditions")
                .hide(conditionsFragment).commit()
        fm.beginTransaction().add(R.id.nav_host_fragment, databaseFragment, "database")
                .hide(databaseFragment).commit()
        fm.beginTransaction().add(R.id.nav_host_fragment, mapFragment, "map").commit()
        active = mapFragment
    }

    // Clears all loaded fragments
    private fun clearAllFragments()
    {
        for (fragment in supportFragmentManager.fragments) {
            supportFragmentManager.beginTransaction().remove(fragment!!).commit()
        }
    }

    private val mOnNavigationItemSelectedListener: BottomNavigationView.OnNavigationItemSelectedListener
        = BottomNavigationView.OnNavigationItemSelectedListener { item -> switchNavigation(item) }

    // Switches navigation to a different fragment
    private fun switchNavigation(item: MenuItem): Boolean
    {
        when (item.itemId) {
            R.id.navigation_home -> {
                fm.beginTransaction().hide(active).show(mapFragment).commit()
                active = mapFragment
                return true
            }
            R.id.navigation_dashboard -> {
                fm.beginTransaction().hide(active).show(databaseFragment).commit()
                if (active == databaseFragment) {
                    showListInDatabaseFragment()
                }
                active = databaseFragment
                return true
            }
            R.id.navigation_notifications -> {
                fm.beginTransaction().hide(active).show(conditionsFragment).commit()
                active = conditionsFragment
                return true
            }
        }
        return false
    }

    fun switchNavigation(itemId: Int)
    {
        navView.selectedItemId = itemId
    }

    fun showDetailsInDatabaseFragment(id: Int)
    {
        databaseFragment.showDetailsFragment(id)
    }

    fun showListInDatabaseFragment()
    {
        databaseFragment.showListFragment()
    }

    // Gets current location of user
    @SuppressLint("MissingPermission")
    fun getLocation()
    {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                100000, 100f, this)
    }

    override fun onBackPressed() {
        val fragment =  this.supportFragmentManager.findFragmentById(R.id.database_constraint)
        if (fragment as? IOnBackPressed != null)
        {
            (fragment as IOnBackPressed).onBackPressed()
        }
        else
        {
            super.onBackPressed()

            if (active == mapFragment)
            {
                super.onBackPressed()
            }
            else
            {
                fm.beginTransaction().hide(active).show(mapFragment).commit()
                active = mapFragment
            }
        }
    }

    override fun onLocationChanged(location: Location) {
        this.mainViewModel.setLocation(location)
    }

    // Loads saved observed satellites
    private fun loadSavedData(savedInstanceState: Bundle?)
    {
        val filename = "satellites"
        try {
            openFileInput(filename).bufferedReader().useLines { lines ->
                lines.forEach {
                    if (it != "id" && it.isNotEmpty()) {
                        mainViewModel.addObservedSat(it.toInt())
                    }
                }
            }
        } catch (e: Exception) {
            print("Error loading data.")
        }

    }

    // Saves observed satellites on app close
    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        val sats = mutableListOf<Int>()
        val observedSats = mainViewModel.getObservedSatIds()
        if (observedSats != null)
        {
            sats.addAll(observedSats)
        }
        saveData(sats)
    }

    // Saves data to a file
    private fun saveData(sats : List<Int>)
    {
        val filename = "satellites"
        var fileWriter: OutputStreamWriter? = null
        openFileOutput(filename, Context.MODE_PRIVATE).use {
            try {
                fileWriter = it.writer()
                //fileWriter = FileWriter("satellites.csv")
                if (fileWriter != null)
                {
                    fileWriter!!.write("id")
                    fileWriter!!.append('\n')
                    for (sat in sats) {
                        fileWriter?.append(sat.toString())
                        fileWriter?.append('\n')
                    }
                }
                println("Write CSV successfully!")
            } catch (e: Exception) {
                println("Writing CSV error!")
                e.printStackTrace()
            } finally {
                try {
                    fileWriter!!.flush()
                    fileWriter!!.close()
                } catch (e: IOException) {
                    println("Flushing/closing error!")
                    e.printStackTrace()
                }
            }
        }
    }
}