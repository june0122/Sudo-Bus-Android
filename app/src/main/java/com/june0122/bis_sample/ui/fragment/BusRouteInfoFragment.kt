package com.june0122.bis_sample.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.june0122.bis_sample.R
import com.june0122.bis_sample.model.RouteData
import com.june0122.bis_sample.utils.checkRouteType
import kotlinx.android.synthetic.main.fragment_bus_route_info.*
import kotlinx.android.synthetic.main.fragment_bus_route_info.busRouteNameTextView

class BusRouteInfoFragment : Fragment() {
    private var routeData : RouteData? = null

    fun inputRouteInfo(data: RouteData) {
        routeData = data
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bus_route_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        busRouteInfoBackButton.setOnClickListener {
            activity?.onBackPressed()
        }

        busRouteNameTextView.text = routeData?.busRouteName
        routeTypeTextView.text = resources.getString(R.string.bus_type, routeData?.routeType?.checkRouteType())
        startStationTextView.text = routeData?.startStationName
        endStationTextView.text = routeData?.endStationName
        busScheduleDataTextView.text = resources.getString(R.string.bus_schedule, routeData?.firstTime, routeData?.lastTime)
        busTermDataTextView.text = resources.getString(R.string.bus_term, routeData?.term)
    }
}