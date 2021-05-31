package com.example.sattracker.ui.database

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.example.sattracker.R
import com.example.sattracker.listadapter.ItemAdapter
import com.example.sattracker.model.Satellite

class DatabaseFragment : Fragment() {

    private lateinit var itemAdapter: ItemAdapter
    var satellites = mutableListOf<Satellite>()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_database, container, false)
        showListFragment()
        return root
    }

    // Opens fragment with the list of satellites
    fun showListFragment()
    {
        val manager = (context as FragmentActivity).supportFragmentManager
        val listFragment = ListFragment()
        manager.beginTransaction()
                .replace(R.id.database_constraint, listFragment)
                .addToBackStack(null)
                .commit()
    }

    // Opens fragment with details of satellite with specified ID
    fun showDetailsFragment(id: Int)
    {
        val manager = (context as FragmentActivity).supportFragmentManager
        val detailsFragment = DetailsFragment()
        val arguments = Bundle()
        arguments.putInt("id", id)
        detailsFragment.arguments = arguments
        manager.beginTransaction()
                .replace(R.id.database_constraint, detailsFragment)
                .commit()
    }
}