package com.june0122.bis_sample.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import com.google.android.material.appbar.AppBarLayout
import com.june0122.bis_sample.R
import com.june0122.bis_sample.model.BusLocationData
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
import kotlinx.android.synthetic.main.fragment_bus_route.refreshFAB
import kotlinx.android.synthetic.main.fragment_bus_route.view.*
import kotlinx.android.synthetic.main.fragment_station_bus_list.*
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
    private val busLocationData = arrayListOf<BusLocationData>()
    private val stationList = arrayListOf<StationList>()
    private val busRouteAdapter = BusRouteAdapter()

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

        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(view.busRouteRecyclerView)

        val busRouteLayoutManager = LinearLayoutManager(context)
        busRouteRecyclerView.layoutManager = busRouteLayoutManager
        busRouteLayoutManager.orientation = LinearLayoutManager.VERTICAL
        busRouteRecyclerView.adapter = busRouteAdapter

        busRouteLayoutManager.findLastCompletelyVisibleItemPosition()

        busRouteRecyclerView.addItemDecoration(
                BusRouteItemDecoration(
                        context,
                        busRouteAdapter.itemCount,
                        snapHelper,
                        busLocationData
                )
        )

        activity?.runOnUiThread {
            stationList.clear()
            when (inputData) {
                "" -> stationList.clear()
                else -> {
                    searchBusRouteInfo(inputData)
                    searchBusRoute(searchBusRouteInfo(inputData))
                    searchBusLocationInfo(searchBusRouteInfo(inputData))
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

        toolbarBusInfoButton.setOnClickListener {
            val busRouteInfoFragment = BusRouteInfoFragment()

            busRouteInfoFragment.inputRouteInfo(routeData[0])

            activity?.supportFragmentManager
                    ?.beginTransaction()
                    ?.replace(R.id.fragmentContainer, busRouteInfoFragment)
                    ?.addToBackStack(null)?.commit()
        }

        appbarBusInfoButton.setOnClickListener {
            val busRouteInfoFragment = BusRouteInfoFragment()

            busRouteInfoFragment.inputRouteInfo(routeData[0])

            activity?.supportFragmentManager
                    ?.beginTransaction()
                    ?.replace(R.id.fragmentContainer, busRouteInfoFragment)
                    ?.addToBackStack(null)?.commit()
        }

        toolbarBusRouteMapButton.setOnClickListener {
            val busRouteMapFragment = BusRouteMapFragment()

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

        appbarMapButton.setOnClickListener {
            val busRouteMapFragment = BusRouteMapFragment()

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
                ratio =
                        (verticalOffset.toFloat() + visibleTriggerHeight) / (appBarLayout.totalScrollRange.toFloat() - visibleTriggerHeight)

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

            busLocationData.clear()
            searchBusLocationInfo(searchBusRouteInfo(inputData))

        }
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
                                    sectSpd,
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
//            Log.d("XXX", "${it.stationName} ${it.stationArsId} | ${it.firstTime} ~ ${it.lastTime}")
//            Log.d(
//                    "XXX",
//                    "$direction $gpsX $gpsY $lastTm $posX $posY $routeType $sectSpd $section $seq $station $stationNo $transYn $fullSectDist $trnstnid"
//            )

            Log.d("SectSpd", "[${it.stationName} 구간 속도] ${it.sectionSpeed}")
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

        busTypeTextView.text = resources.getString(R.string.bus_type, checkBusType(routeType))
        busNumberTextView.text = busRouteNm
        toolbarBusNumberTextView.text = busRouteNm
        firstStationNameTextView.text = stStationNm
        endStationNameTextView.text = edStationNm

        return busRouteId
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun searchBusLocationInfo(busRouteId: String) {
        val url =
                URL("http://ws.bus.go.kr/api/rest/buspos/getBusPosByRtid?ServiceKey=$SERVICE_KEY&busRouteId=$busRouteId")

        val parser = createParser(url).parser
        var parserEvent = createParser(url).parserEvent

        var busTypeTag = false
        var congetionTag = false
        var dataTmTag = false
        var fullSectDistTag = false
        var gpsXTag = false
        var gpsYTag = false
        var isFullFlagTag = false
        var islastynTag = false
        var isrunynTag = false
        var lastStTmTag = false
        var lastStnIdTag = false
        var nextStIdTag = false
        var nextStTmTag = false
        var plainNoTag = false
        var posXTag = false
        var posYTag = false
        var rtDistTag = false
        var sectDistTag = false
        var sectOrdTag = false
        var sectionIdTag = false
        var stopFlagTag = false
        var trnstnidTag = false
        var vehIdTag = false

        var busType = ""
        var congetion = ""
        var dataTm = ""
        var fullSectDist = ""
        var gpsX = ""
        var gpsY = ""
        var isFullFlag = ""
        var islastyn = ""
        var isrunyn = ""
        var lastStTm = ""
        var lastStnId = ""
        var nextStId = ""
        var nextStTm = ""
        var plainNo = ""
        var posX = ""
        var posY = ""
        var rtDist = ""
        var sectDist = ""
        var sectOrd = ""
        var sectionId = ""
        var stopFlag = ""
        var trnstnid = ""
        var vehId = ""

        while (parserEvent != XmlPullParser.END_DOCUMENT) {
            when (parserEvent) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "busType" -> {
                            busTypeTag = true
                        }
                        "congetion" -> {
                            congetionTag = true
                        }
                        "dataTm" -> {
                            dataTmTag = true
                        }
                        "fullSectDist" -> {
                            fullSectDistTag = true
                        }
                        "gpsX" -> {
                            gpsXTag = true
                        }
                        "gpsY" -> {
                            gpsYTag = true
                        }
                        "isFullFlag" -> {
                            isFullFlagTag = true
                        }
                        "islastyn" -> {
                            islastynTag = true
                        }
                        "isrunyn" -> {
                            isrunynTag = true
                        }
                        "lastStTm" -> {
                            lastStTmTag = true
                        }
                        "lastStnId" -> {
                            lastStnIdTag = true
                        }
                        "nextStId" -> {
                            nextStIdTag = true
                        }
                        "nextStTm" -> {
                            nextStTmTag = true
                        }
                        "plainNo" -> {
                            plainNoTag = true
                        }
                        "posX" -> {
                            posXTag = true
                        }
                        "posY" -> {
                            posYTag = true
                        }
                        "rtDist" -> {
                            rtDistTag = true
                        }
                        "sectDist" -> {
                            sectDistTag = true
                        }
                        "sectOrd" -> {
                            sectOrdTag = true
                        }
                        "sectionId" -> {
                            sectionIdTag = true
                        }
                        "stopFlag" -> {
                            stopFlagTag = true
                        }
                        "trnstnid" -> {
                            trnstnidTag = true
                        }
                        "vehId" -> {
                            vehIdTag = true
                        }
                    }
                }

                XmlPullParser.TEXT -> {
                    when {
                        busTypeTag -> {
                            busType = parser.text
                        }
                        congetionTag -> {
                            congetion = parser.text
                        }
                        dataTmTag -> {
                            dataTm = parser.text
                        }
                        fullSectDistTag -> {
                            fullSectDist = parser.text
                        }
                        gpsXTag -> {
                            gpsX = parser.text
                        }
                        gpsYTag -> {
                            gpsY = parser.text
                        }
                        isFullFlagTag -> {
                            isFullFlag = parser.text
                        }
                        islastynTag -> {
                            islastyn = parser.text
                        }
                        isrunynTag -> {
                            isrunyn = parser.text
                        }
                        lastStTmTag -> {
                            lastStTm = parser.text
                        }
                        lastStnIdTag -> {
                            lastStnId = parser.text
                        }
                        nextStIdTag -> {
                            nextStId = parser.text
                        }
                        nextStTmTag -> {
                            nextStTm = parser.text
                        }
                        plainNoTag -> {
                            plainNo = parser.text
                        }
                        posXTag -> {
                            posX = parser.text
                        }
                        posYTag -> {
                            posY = parser.text
                        }
                        rtDistTag -> {
                            rtDist = parser.text
                        }
                        sectDistTag -> {
                            sectDist = parser.text
                        }
                        sectOrdTag -> {
                            sectOrd = parser.text
                        }
                        sectionIdTag -> {
                            sectionId = parser.text
                        }
                        stopFlagTag -> {
                            stopFlag = parser.text
                        }
                        trnstnidTag -> {
                            trnstnid = parser.text
                        }
                        vehIdTag -> {
                            vehId = parser.text

                            val data = BusLocationData(
                                    sectOrd,
                                    sectDist,
                                    fullSectDist,
                                    stopFlag,
                                    sectionId,
                                    dataTm,
                                    gpsX,
                                    gpsY,
                                    vehId,
                                    plainNo,
                                    busType,
                                    isrunyn,
                                    trnstnid,
                                    islastyn,
                                    isFullFlag,
                                    lastStnId,
                                    congetion
                            )
                            busLocationData.add(data)
                        }
                    }

                    busTypeTag = false
                    congetionTag = false
                    dataTmTag = false
                    fullSectDistTag = false
                    gpsXTag = false
                    gpsYTag = false
                    isFullFlagTag = false
                    islastynTag = false
                    isrunynTag = false
                    lastStTmTag = false
                    lastStnIdTag = false
                    nextStIdTag = false
                    nextStTmTag = false
                    plainNoTag = false
                    posXTag = false
                    posYTag = false
                    rtDistTag = false
                    sectDistTag = false
                    sectOrdTag = false
                    sectionIdTag = false
                    stopFlagTag = false
                    trnstnidTag = false
                    vehIdTag = false
                }
            }
            parserEvent = parser.next()
        }

        busRouteAdapter.items.clear()
        busRouteAdapter.items.addAll(stationList)
        busRouteAdapter.notifyDataSetChanged()

        busLocationData.forEach {
            Log.d("BUSLOCATIONDATA", "[구간 순번] ${it.sectionOrder}, [버스 번호] ${it.plainBusNumber} , [혼잡도] ${it.busCongestion}," +
                    "[타입] ${it.type}, [정류소 간 거리] ${it.fullSectionDistance}, [정류장 구간 이동거리] ${it.sectionDistance} " +
                    "[정류소 도착 여부] ${it.stationArrivalFlag}, [회차지 정류소 ID] ${it.turningStationId}")
        }
    }
}
