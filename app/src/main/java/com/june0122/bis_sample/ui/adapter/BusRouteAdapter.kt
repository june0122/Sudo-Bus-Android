package com.june0122.bis_sample.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.june0122.bis_sample.R
import com.june0122.bis_sample.model.StationList
import com.june0122.bis_sample.ui.viewholder.BusRouteViewHolder
import kotlinx.android.synthetic.main.item_bus_route_station.view.*

class BusRouteAdapter : RecyclerView.Adapter<BusRouteViewHolder>() {
    var items = arrayListOf<StationList>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BusRouteViewHolder = BusRouteViewHolder(parent)

    override fun getItemCount(): Int = items.count()

    override fun onBindViewHolder(holder: BusRouteViewHolder, position: Int) {
        val model = items[position]

        with(holder.itemView) {
            stationNameTextView.text = model.stationName
            stationArsIdTextView.text = model.stationArsId
            stationScheduleTextView.text = resources.getString(R.string.station_schedule, model.firstTime, model.lastTime)
        }
    }
}