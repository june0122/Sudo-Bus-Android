package com.june0122.bis_sample.ui.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.june0122.bis_sample.R
import com.june0122.bis_sample.model.StationList
import com.june0122.bis_sample.ui.viewholder.BusRouteViewHolder
import com.june0122.bis_sample.utils.backgroundColor
import kotlinx.android.synthetic.main.item_bus_route_station.view.*

class BusRouteAdapter : RecyclerView.Adapter<BusRouteViewHolder>() {
    var items = arrayListOf<StationList>()

    private fun sectionSpeedIndicator(context: Context, model: StationList, divider: View) {
        val sectionSpeed = model.sectionSpeed.toInt()

        when {
            sectionSpeed in 0..9 -> {
                divider.backgroundColor(context, R.color.red)
            }
            sectionSpeed in 10..29 -> {
                divider.backgroundColor(context, R.color.yellow)
            }
            sectionSpeed > 30 -> {
                divider.backgroundColor(context, R.color.green)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BusRouteViewHolder = BusRouteViewHolder(parent)

    override fun getItemCount(): Int = items.count()

    override fun onBindViewHolder(holder: BusRouteViewHolder, position: Int) {
        val model = items[position]

        with(holder.itemView) {
            stationNameTextView.text = model.stationName
            stationArsIdTextView.text = model.stationArsId
            stationScheduleTextView.text = resources.getString(R.string.station_schedule, model.firstTime, model.lastTime)

            sectionSpeedIndicator(context, model, bottomDivider)

            if (position > 0) {
                val previousModel = items[position - 1]
                sectionSpeedIndicator(context, previousModel, topDivider)
            }

            // 처음과 마지막 정류장의 구간속도 표시색을 회색으로 고정
            when (position) {
                0 -> {
                    bottomDivider.backgroundColor(context, R.color.dark_gray)
                }
                1 -> {
                    topDivider.backgroundColor(context, R.color.dark_gray)
                }
                items.size - 2 -> {
                    bottomDivider.backgroundColor(context, R.color.dark_gray)
                }
                items.size - 1 -> {
                    topDivider.backgroundColor(context, R.color.dark_gray)
                }
            }

            // 회차지 item의 backgroundColor 변경
            if (model.stationId == model.turningStationId) {
                turingStationLayout.visibility = View.VISIBLE
                busRouteItemLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.white_gray))
            } else {
                turingStationLayout.visibility = View.GONE
                busRouteItemLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
            }
        }
    }
}