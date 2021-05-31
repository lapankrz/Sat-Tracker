package com.example.sattracker.listadapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sattracker.R
import com.example.sattracker.model.Satellite
import com.example.sattracker.ui.database.DetailsFragment
import com.example.sattracker.ui.database.ListFragment
import java.util.*

// Class used to display satellite list in ListFragment
class ItemAdapter(
    private val context: ListFragment,
    private val dataset: List<Satellite>,
    private val fragmentManager: FragmentManager

) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>(), Filterable {

    var satelliteFilterList = listOf<Satellite>()

    init {
        satelliteFilterList = dataset
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just an Affirmation object.
    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val linearLayout : LinearLayout = view.findViewById(R.id.list_item_layout)
        val satIdTextView: TextView = view.findViewById(R.id.sat_id_text)
        val satNameTextView: TextView = view.findViewById(R.id.sat_name_text)
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item, parent, false)
        return ItemViewHolder(adapterLayout)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        if (position >= satelliteFilterList.count())
        {
            return
        }
        val item = satelliteFilterList[position]
        holder.satIdTextView.text = """${"%6d".format(item.id)}:"""
        holder.satNameTextView.text = item.name
        holder.linearLayout.setOnClickListener {
            val detailsFragment = DetailsFragment()
            val arguments = Bundle()
            arguments.putInt("id", item.id)
            detailsFragment.arguments = arguments
            fragmentManager.beginTransaction()
                .replace(R.id.database_constraint, detailsFragment)
                .commit()
        }
    }

    override fun getItemCount() = satelliteFilterList.size

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                if (charSearch.isEmpty()) {
                    satelliteFilterList = dataset
                } else {
                    val resultList = mutableListOf<Satellite>()
                    for (row in dataset) {
                        if (row.name.toLowerCase(Locale.ROOT).contains(charSearch.toLowerCase(Locale.ROOT)) ||
                            row.id.toString().contains(charSearch.toLowerCase(Locale.ROOT))) {
                            resultList.add(row)
                        }
                    }
                    satelliteFilterList = resultList
                }
                val filterResults = FilterResults()
                filterResults.values = satelliteFilterList
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                satelliteFilterList = results?.values as List<Satellite>
                notifyDataSetChanged()
            }

        }
    }
}