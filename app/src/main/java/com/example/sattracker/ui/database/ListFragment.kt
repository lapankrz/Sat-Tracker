package com.example.sattracker.ui.database

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.sattracker.R
import com.example.sattracker.data.SatelliteDatasource
import com.example.sattracker.listadapter.ItemAdapter
import com.example.sattracker.model.Satellite
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class ListFragment : Fragment() {

    private lateinit var itemAdapter: ItemAdapter
    var satellites = mutableListOf<Satellite>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_list, container, false)
        val manager = (context as FragmentActivity).supportFragmentManager

        itemAdapter = ItemAdapter(this, satellites, manager)
        root.findViewById<RecyclerView>(R.id.recycler_view).apply {
            setHasFixedSize(true)
            adapter = itemAdapter
        }
        loadData()
        try {
            val search = root.findViewById<SearchView>(R.id.satellite_search)
            search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }
                override fun onQueryTextChange(newText: String?): Boolean {
                    itemAdapter.filter.filter(newText)
                    return false
                }
            })
        }
        catch (ex: Exception)
        {
            ex.printStackTrace()
        }

        return root
    }

    private fun loadData() = CoroutineScope(Dispatchers.Main).launch {
        val task = async(Dispatchers.IO) {
            SatelliteDatasource().getSatelliteList(requireContext())
        }
        val res = task.await()
        satellites.clear()
        satellites.addAll(res)
        itemAdapter.notifyDataSetChanged()
    }
}