package com.june0122.bis_sample.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.AppBarLayout
import com.june0122.bis_sample.R
import com.june0122.bis_sample.model.Data.Companion.SERVICE_KEY
import com.june0122.bis_sample.model.RouteData
import com.june0122.bis_sample.model.StationList
import com.june0122.bis_sample.ui.adapter.BusRouteAdapter
import com.june0122.bis_sample.utils.RecyclerItemClickListener
import com.june0122.bis_sample.utils.checkBusType
import com.june0122.bis_sample.utils.createParser
import com.june0122.bis_sample.utils.decoration.BusRouteItemDecoration
import com.june0122.bis_sample.utils.formatTime
import kotlinx.android.synthetic.main.fragment_bus_route.*
import kotlinx.android.synthetic.main.layout_appbar_bus_route.*
import kotlinx.android.synthetic.main.layout_appbar_bus_route.appbarMapButton
import kotlinx.android.synthetic.main.layout_appbar_bus_route.backButtonImageView
import kotlinx.android.synthetic.main.layout_appbar_bus_route.toolbarBusRouteMapButton
import kotlinx.android.synthetic.main.layout_appbar_bus_route.toolbarHomeButton
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.net.URL
import kotlin.math.abs

class BusRouteFragment : Fragment() {
    private var inputData: String = ""
    private val routeData = arrayListOf<RouteData>()
    private val stationList = arrayListOf<StationList>()
    private val busRouteAdapter = BusRouteAdapter()
    private val busRouteMapFragment = BusRouteMapFragment()


    fun inputBusNumber(busNumber: String) {
        inputData = busNumber
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bus_route, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val busRouteLayoutManager = LinearLayoutManager(context)
        busRouteRecyclerView.layoutManager = busRouteLayoutManager
        busRouteLayoutManager.orientation = LinearLayoutManager.VERTICAL
        busRouteRecyclerView.adapter = busRouteAdapter

        busRouteLayoutManager.findLastCompletelyVisibleItemPosition()

        busRouteRecyclerView.addItemDecoration(
                BusRouteItemDecoration(
                        context,
                        busRouteAdapter.itemCount
                )
        )

        activity?.runOnUiThread {
            stationList.clear()
            when (inputData) {
                "" -> stationList.clear()
                else -> {
                    searchBusRouteInfo(inputData)
                    searchBusRoute(searchBusRouteInfo(inputData))
                }
            }
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

        toolbarBusNumberTextView.isSelected = true


        appbarMapButton.setOnClickListener {
            busRouteMapFragment.inputBusRouteId(searchBusRouteInfo(inputData))

            busRouteMapFragment.inputBusRouteInfo(
                    routeData[0].busRouteName,
                    routeData[0].startStationName,
                    routeData[0].endStationName,
                    "${routeData[0].firstTime} ~ ${routeData[0].lastTime}",
                    routeData[0].term
            )


            activity?.supportFragmentManager
                    ?.beginTransaction()
                    ?.replace(R.id.fragmentContainer, busRouteMapFragment)
                    ?.addToBackStack(null)?.commit()
        }

        val busRouteAppBarLayout: AppBarLayout? = view.findViewById(R.id.busRouteAppbar)

        busRouteAppBarLayout?.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            val ratio: Float
            val totalScrollRange = busRouteAppBarLayout.totalScrollRange
            val visibleTriggerHeight = 250

            Log.d("APPBAR", "verticalOffset : $verticalOffset")
            Log.d("APPBAR", "totalScrollRange : ${busRouteAppBarLayout.totalScrollRange}")
            Log.d("APPBAR", "height : ${busRouteCollapsingToolbarLayout.height}")

            if (verticalOffset in -totalScrollRange..-visibleTriggerHeight) {
                ratio = (verticalOffset.toFloat() + visibleTriggerHeight) / (appBarLayout.totalScrollRange.toFloat() - visibleTriggerHeight)

                toolbarBusNumberTextView.alpha = abs(ratio)
                toolbarBusInfoButton.alpha = abs(ratio)
                toolbarBusRouteMapButton.alpha = abs(ratio)

            } else {
                ratio = 0f

                toolbarBusNumberTextView.alpha = abs(ratio)
                toolbarBusInfoButton.alpha = abs(ratio)
                toolbarBusRouteMapButton.alpha = abs(ratio)
            }
        })


        busRouteRecyclerView.addOnItemTouchListener(
                RecyclerItemClickListener(
                        view.context,
                        busRouteRecyclerView,
                        object : RecyclerItemClickListener.OnItemClickListener {
                            override fun onItemClick(view: View, position: Int) {

                                val stationBusListFragment = StationBusListFragment()

                                stationBusListFragment.inputArsId(busRouteAdapter.items[position].stationArsId)
                                stationBusListFragment.inputLatLng(
                                        busRouteAdapter.items[position].wgs84Y.toDouble(),
                                        busRouteAdapter.items[position].wgs84X.toDouble()
                                )

                                activity?.supportFragmentManager
                                        ?.beginTransaction()
                                        ?.replace(R.id.fragmentContainer, stationBusListFragment)
                                        ?.addToBackStack(null)?.commit()
                            }
                        })
        )
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun searchBusRoute(routeId: String) {
        val url =
                URL("http://ws.bus.go.kr/api/rest/busRouteInfo/getStaionByRoute?ServiceKey=$SERVICE_KEY&busRouteId=$routeId")

        val parser = createParser(url).parser
        var parserEvent = createParser(url).parserEvent

        var arsIdTag = false
        var beginTmTag = false
        var busRouteIdTag = false
        var busRouteNmTag = false
        var directionTag = false
        var gpsXTag = false
        var gpsYTag = false
        var lastTmTag = false
        var posXTag = false
        var posYTag = false
        var routeTypeTag = false
        var sectSpdTag = false
        var sectionTag = false
        var seqTag = false
        var stationTag = false
        var stationNmTag = false
        var stationNoTag = false
        var transYnTag = false
        var fullSectDistTag = false
        var trnstnidTag = false

        var arsId = ""
        var beginTm = ""
        var busRouteId = ""
        var busRouteNm = ""
        var direction = ""
        var gpsX = ""
        var gpsY = ""
        var lastTm = ""
        var posX = ""
        var posY = ""
        var routeType = ""
        var sectSpd = ""
        var section = ""
        var seq = ""
        var station = ""
        var stationNm = ""
        var stationNo = ""
        var transYn = ""
        var fullSectDist = ""
        var trnstnid = ""

        while (parserEvent != XmlPullParser.END_DOCUMENT) {
            when (parserEvent) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "arsId" -> {
                            arsIdTag = true
                        }
                        "beginTm" -> {
                            beginTmTag = true
                        }
                        "busRouteId" -> {
                            busRouteIdTag = true
                        }
                        "busRouteNm" -> {
                            busRouteNmTag = true
                        }
                        "direction" -> {
                            directionTag = true
                        }
                        "gpsX" -> {
                            gpsXTag = true
                        }
                        "gpsY" -> {
                            gpsYTag = true
                        }
                        "lastTm" -> {
                            lastTmTag = true
                        }
                        "posX" -> {
                            posXTag = true
                        }
                        "posY" -> {
                            posYTag = true
                        }
                        "routeType" -> {
                            routeTypeTag = true
                        }
                        "sectSpd" -> {
                            sectSpdTag = true
                        }
                        "section" -> {
                            sectionTag = true
                        }
                        "seq" -> {
                            seqTag = true
                        }
                        "station" -> {
                            stationTag = true
                        }
                        "stationNm" -> {
                            stationNmTag = true
                        }
                        "stationNo" -> {
                            stationNoTag = true
                        }
                        "transYn" -> {
                            transYnTag = true
                        }
                        "fullSectDist" -> {
                            fullSectDistTag = true
                        }
                        "trnstnid" -> {
                            trnstnidTag = true
                        }


                    }
                }

                XmlPullParser.TEXT -> {
                    when {
                        arsIdTag -> {
                            arsId = parser.text
                        }
                        beginTmTag -> {
                            beginTm = parser.text
                        }
                        busRouteIdTag -> {
                            busRouteId = parser.text
                        }
                        busRouteNmTag -> {
                            busRouteNm = parser.text
                        }
                        directionTag -> {
                            direction = parser.text
                        }
                        gpsXTag -> {
                            gpsX = parser.text
                        }
                        gpsYTag -> {
                            gpsY = parser.text
                        }
                        lastTmTag -> {
                            lastTm = parser.text
                        }
                        posXTag -> {
                            posX = parser.text
                        }
                        posYTag -> {
                            posY = parser.text
                        }
                        routeTypeTag -> {
                            routeType = parser.text
                        }
                        sectSpdTag -> {
                            sectSpd = parser.text
                        }
                        sectionTag -> {
                            section = parser.text
                        }
                        seqTag -> {
                            seq = parser.text
                        }
                        stationTag -> {
                            station = parser.text
                        }
                        stationNmTag -> {
                            stationNm = parser.text
                        }
                        stationNoTag -> {
                            stationNo = parser.text
                        }
                        transYnTag -> {
                            transYn = parser.text
                        }
                        fullSectDistTag -> {
                            fullSectDist = parser.text
                        }

                        trnstnidTag -> {
                            trnstnid = parser.text

                            val data = StationList(
                                    busRouteId,
                                    busRouteNm,
                                    stationNm,
                                    arsId,
                                    beginTm,
                                    lastTm,
                                    routeType,
                                    gpsX,
                                    gpsY
                            )
                            stationList.add(data)
                        }
                    }

                    arsIdTag = false
                    beginTmTag = false
                    busRouteIdTag = false
                    busRouteNmTag = false
                    directionTag = false
                    gpsXTag = false
                    gpsYTag = false
                    lastTmTag = false
                    posXTag = false
                    posYTag = false
                    routeTypeTag = false
                    sectSpdTag = false
                    sectionTag = false
                    seqTag = false
                    stationTag = false
                    stationNmTag = false
                    stationNoTag = false
                    transYnTag = false
                    fullSectDistTag = false
                    trnstnidTag = false
                }
            }
            parserEvent = parser.next()
        }

        busRouteAdapter.items.clear()
        busRouteAdapter.items.addAll(stationList)
        busRouteAdapter.notifyDataSetChanged()

        stationList.forEach {
            Log.d("XXX", "${it.stationName} ${it.stationArsId} | ${it.firstTime} ~ ${it.lastTime}")
            Log.d(
                    "XXX",
                    "$direction $gpsX $gpsY $lastTm $posX $posY $routeType $sectSpd $section $seq $station $stationNo $transYn $fullSectDist $trnstnid"
            )
        }

    }


    @Throws(XmlPullParserException::class, IOException::class)
    fun searchBusRouteInfo(busNumber: String): String {
        val url =
                URL("http://ws.bus.go.kr/api/rest/busRouteInfo/getBusRouteList?ServiceKey=$SERVICE_KEY&strSrch=$busNumber")

        val parser = createParser(url).parser
        var parserEvent = createParser(url).parserEvent

        var busRouteIdTag = false
        var busRouteNmTag = false
        var corpNmTag = false
        var edStationNmTag = false
        var firstBusTmTag = false
        var lastBusTmTag = false
        var lastBusYnTag = false
        var routeTypeTag = false
        var stStationNmTag = false
        var termTag = false

        var busRouteId = ""
        var busRouteNm = ""
        var corpNm = ""
        var edStationNm = ""
        var firstBusTm = ""
        var lastBusTm = ""
        var lastBusYn = ""
        var routeType = ""
        var stStationNm = ""
        var term: String


        while (parserEvent != XmlPullParser.END_DOCUMENT) {
            when (parserEvent) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "busRouteId" -> {
                            busRouteIdTag = true
                        }
                        "busRouteNm" -> {
                            busRouteNmTag = true
                        }
                        "corpNm" -> {
                            corpNmTag = true
                        }
                        "edStationNm" -> {
                            edStationNmTag = true
                        }
                        "firstBusTm" -> {
                            firstBusTmTag = true
                        }
                        "lastBusTm" -> {
                            lastBusTmTag = true
                        }
                        "lastBusYn" -> {
                            lastBusYnTag = true
                        }
                        "routeType" -> {
                            routeTypeTag = true
                        }
                        "stStationNm" -> {
                            stStationNmTag = true
                        }
                        "term" -> {
                            termTag = true
                        }

                    }
                }

                XmlPullParser.TEXT -> {
                    when {
                        busRouteIdTag -> {
                            busRouteId = parser.text
                        }
                        busRouteNmTag -> {
                            busRouteNm = parser.text
                        }
                        corpNmTag -> {
                            corpNm = parser.text
                        }
                        edStationNmTag -> {
                            edStationNm = parser.text
                        }
                        firstBusTmTag -> {
                            firstBusTm = formatTime(parser.text)
                        }
                        lastBusTmTag -> {
                            lastBusTm = formatTime(parser.text)
                        }
                        lastBusYnTag -> {
                            lastBusYn = parser.text
                        }
                        routeTypeTag -> {
                            routeType = parser.text
                        }
                        stStationNmTag -> {
                            stStationNm = parser.text
                        }
                        termTag -> {
                            term = parser.text

                            val data = RouteData(
                                    busRouteId,
                                    busRouteNm,
                                    routeType,
                                    stStationNm,
                                    edStationNm,
                                    term,
                                    firstBusTm,
                                    lastBusTm,
                                    corpNm
                            )
                            routeData.add(data)
                        }
                    }

                    busRouteIdTag = false
                    busRouteNmTag = false
                    corpNmTag = false
                    edStationNmTag = false
                    firstBusTmTag = false
                    lastBusTmTag = false
                    lastBusYnTag = false
                    routeTypeTag = false
                    stStationNmTag = false
                    termTag = false
                }
            }
            parserEvent = parser.next()
        }

        Log.d("XXX", "$stStationNm ~ $edStationNm")

        busTypeTextView.text = checkBusType(routeType)
        busNumberTextView.text = busRouteNm
        toolbarBusNumberTextView.text = busRouteNm
        firstStationNameTextView.text = stStationNm
        endStationNameTextView.text = edStationNm

        return busRouteId
    }
}
