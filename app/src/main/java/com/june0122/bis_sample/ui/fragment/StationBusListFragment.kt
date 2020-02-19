package com.june0122.bis_sample.ui.fragment

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.june0122.bis_sample.R
import com.june0122.bis_sample.model.BusList
import com.june0122.bis_sample.model.Data.Companion.SERVICE_KEY
import com.june0122.bis_sample.ui.adapter.StationBusListAdapter
import com.june0122.bis_sample.utils.createParser
import kotlinx.android.synthetic.main.fragment_station_bus_list.*
import kotlinx.android.synthetic.main.fragment_station_location_map.*
import kotlinx.android.synthetic.main.layout_appbar_station_bus_list.*
import kotlinx.android.synthetic.main.layout_appbar_station_bus_list.backButtonImageView
import kotlinx.android.synthetic.main.layout_appbar_station_bus_list.stationNameTextView
import kotlinx.android.synthetic.main.layout_appbar_station_bus_list.toolbarBusRouteMapButton
import kotlinx.android.synthetic.main.layout_appbar_station_bus_list.toolbarHomeButton
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.net.URL
import kotlin.math.abs

class StationBusListFragment : Fragment() {
    private var inputData: String = ""
    private val busList = arrayListOf<BusList>()
    private val stationBusListAdapter = StationBusListAdapter()

    private val stationLocationMapFragment = StationLocationMapFragment()
    private var lat : Double = 0.0
    private var lng : Double = 0.0

    fun inputArsId(arsId: String) {
        inputData = arsId
    }

    fun inputLatLng(latitude: Double, longitude: Double) {
        lat = latitude
        lng = longitude
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_station_bus_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val stationBusListLayoutManager = LinearLayoutManager(context)
        busListRecyclerView.layoutManager = stationBusListLayoutManager
        stationBusListLayoutManager.orientation = LinearLayoutManager.VERTICAL
        busListRecyclerView.adapter = stationBusListAdapter

        activity?.runOnUiThread {
            busList.clear()
            searchBusListAtStation(inputData)
        }

        backButtonImageView.setOnClickListener {
            activity?.supportFragmentManager
                    ?.beginTransaction()
                    ?.replace(R.id.fragmentContainer, SearchInfoFragment())
                    ?.addToBackStack(null)?.commit()
        }

        toolbarHomeButton.setOnClickListener {
            activity?.supportFragmentManager
                    ?.beginTransaction()
                    ?.replace(R.id.fragmentContainer, SearchInfoFragment())
                    ?.addToBackStack(null)?.commit()
        }

        stationLocationMapFragment.setLatLng(lat, lng)
        stationLocationMapFragment.inputUiText(busList[0].stationName, busList[0].arsId, busList[0].nextStation)

        appbarMapButton.setOnClickListener {
            activity?.supportFragmentManager
                ?.beginTransaction()
                ?.replace(R.id.fragmentContainer, stationLocationMapFragment)
                ?.addToBackStack(null)?.commit()
        }


        refreshFAB.setOnClickListener {
            refreshFAB.animate()
                    .rotationBy(180f)
                    .setDuration(300)
                    .scaleX(1.2f)
                    .scaleY(1.2f)
                    .setInterpolator(AccelerateInterpolator())
                    .withEndAction {
                        refreshFAB.animate()
                                .rotationBy(180f)
                                .setDuration(300)
                                .scaleX(1f)
                                .scaleY(1f)
                                .setInterpolator(DecelerateInterpolator())
                                .start()
                    }
                    .start()

            busList.clear()
            searchBusListAtStation(inputData)
        }

        toolbarStationNameTextView.isSelected = true

        val stationBusListAppBarLayout: AppBarLayout? = view.findViewById(R.id.stationBusListAppbar)

        stationBusListAppBarLayout?.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            val ratio: Float
            val totalScrollRange = stationBusListAppBarLayout.totalScrollRange
            val visibleTriggerHeight = 250

            Log.d("APPBAR", "verticalOffset : $verticalOffset")
            Log.d("APPBAR", "totalScrollRange : ${stationBusListAppBarLayout.totalScrollRange}")
            Log.d("APPBAR", "height : ${stationBusListCollapsingToolbarLayout.height}")

            if (verticalOffset in -totalScrollRange..-visibleTriggerHeight) {
                ratio = (verticalOffset.toFloat() + visibleTriggerHeight) / (appBarLayout.totalScrollRange.toFloat() - visibleTriggerHeight)

                toolbarStationNameTextView.alpha = abs(ratio)
                toolbarBusRouteMapButton.alpha = abs(ratio)
                toolbarFavoriteButton.alpha = abs(ratio)

            } else {
                ratio = 0f

                toolbarStationNameTextView.alpha = abs(ratio)
                toolbarBusRouteMapButton.alpha = abs(ratio)
                toolbarFavoriteButton.alpha = abs(ratio)
            }
        })


    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun searchBusListAtStation(stationId: String) {
        val url = URL("http://ws.bus.go.kr/api/rest/stationinfo/getStationByUid?ServiceKey=${SERVICE_KEY}&arsId=$stationId")

        val parser = createParser(url).parser
        var parserEvent = createParser(url).parserEvent

        var arrmsg1Tag = false
        var arrmsg2Tag = false
        var arsIdTag = false
        var nxtStnTag = false
        var rtNmTag = false
        var stNmTag = false

        var arrmsg1 = ""
        var arrmsg2 = ""
        var arsId = ""
        var nxtStn = ""
        var rtNm = ""
        var stNm: String

        while (parserEvent != XmlPullParser.END_DOCUMENT) {
            when (parserEvent) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "arrmsg1" -> {
                            arrmsg1Tag = true
                        }
                        "arrmsg2" -> {
                            arrmsg2Tag = true
                        }
                        "arsId" -> {
                            arsIdTag = true
                        }
                        "nxtStn" -> {
                            nxtStnTag = true
                        }
                        "rtNm" -> {
                            rtNmTag = true
                        }
                        "stNm" -> {
                            stNmTag = true
                        }
                    }
                }

                XmlPullParser.TEXT -> {
                    when {
                        arrmsg1Tag -> {
                            arrmsg1 = parser.text
                        }
                        arrmsg2Tag -> {
                            arrmsg2 = parser.text
                        }
                        arsIdTag -> {
                            arsId = parser.text
                        }
                        nxtStnTag -> {
                            nxtStn = parser.text
                        }
                        rtNmTag -> {
                            rtNm = parser.text
                        }
                        stNmTag -> {
                            stNm = parser.text

                            val data = BusList(arsId, stNm, rtNm, nxtStn, arrmsg1, arrmsg2)
                            busList.add(data)
                        }
                    }

                    arrmsg1Tag = false
                    arrmsg2Tag = false
                    arsIdTag = false
                    nxtStnTag = false
                    rtNmTag = false
                    stNmTag = false
                }
            }
            parserEvent = parser.next()
        }

        if (busList.isEmpty()) {
            Log.d("EMPTY", "Empty Info")
            Toast.makeText(context, "해당 정류소의 정보가 존재하지 않습니다.", Toast.LENGTH_SHORT).show()
        }

        stationBusListAdapter.apply {
            items.clear()
            items.addAll(busList)
            notifyDataSetChanged()
        }

        stationArsIdTextView?.text = busList[0].arsId
        stationNameTextView?.text = busList[0].stationName
        toolbarStationNameTextView?.text = busList[0].stationName
        stationDirectionTextView?.text = resources.getString(R.string.direction_station, busList[0].nextStation)
    }
}