package com.june0122.bis_sample.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.june0122.bis_sample.R
import com.june0122.bis_sample.model.Data.Companion.SERVICE_KEY
import com.june0122.bis_sample.model.RouteService
import com.june0122.bis_sample.ui.adapter.PreviewBusAdapter
import com.june0122.bis_sample.utils.*
import kotlinx.android.synthetic.main.fragment_preview_bus.*
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.net.URL

class PreviewBusFragment : Fragment() {
    private var inputData: String = ""
    private val seoulBusData = arrayListOf<RouteService>()
    private val gyeonggiBusData = arrayListOf<RouteService>()
    private val previewBusAdapter = PreviewBusAdapter()

    fun inputBusNumber(busNumber: String) {
        inputData = busNumber
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_preview_bus, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val previewBusListLayoutManager = LinearLayoutManager(context)
        previewBusRecyclerView.layoutManager = previewBusListLayoutManager
        previewBusListLayoutManager.orientation = LinearLayoutManager.VERTICAL
        previewBusRecyclerView.adapter = previewBusAdapter

        activity?.runOnUiThread {
            seoulBusData.clear()
            gyeonggiBusData.clear()
            when (inputData) {
                "" -> previewBusAdapter.items.clear()
                else -> {
                    searchBusRouteId(inputData)
                    searchGyeonggiBusRouteId(inputData)
                }
            }
        }

        previewBusRecyclerView.addOnItemTouchListener(
                RecyclerItemClickListener(view.context, previewBusRecyclerView, object : RecyclerItemClickListener.OnItemClickListener {
                    override fun onItemClick(view: View, position: Int) {

                        val busRouteFragment = BusRouteFragment()

                        busRouteFragment.inputBusNumber(previewBusAdapter.items[position].routeName)

                        activity?.supportFragmentManager
                                ?.beginTransaction()
                                ?.replace(R.id.fragmentContainer, busRouteFragment)
                                ?.addToBackStack(null)?.commit()
                    }
                })
        )
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun searchBusRouteId(busNumber: String) {
        val url = URL("http://ws.bus.go.kr/api/rest/busRouteInfo/getBusRouteList?ServiceKey=$SERVICE_KEY&strSrch=$busNumber")

        val parser = createParser(url).parser
        var parserEvent = createParser(url).parserEvent

        var busRouteIdTag = false
        var busRouteNmTag = false
        var edStationNmTag = false
        var firstBusTmTag = false
        var lastBusTmTag = false
        var lastBusYnTag = false
        var routeTypeTag = false
        var stStationNmTag = false
        var termTag = false

        var busRouteId = ""
        var busRouteNm = ""
        var edStationNm = ""
        var firstBusTm = ""
        var lastBusTm = ""
        var lastBusYn = ""
        var routeType = ""
        var stStationNm = ""
        var term: String


        while (parserEvent != XmlPullParser.END_DOCUMENT) {
            when (parserEvent) {
                XmlPullParser.START_DOCUMENT -> {

                }

                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "busRouteId" -> {
                            busRouteIdTag = true
                        }
                        "busRouteNm" -> {
                            busRouteNmTag = true
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
                            routeType = parser.text.checkRouteType()
                        }
                        stStationNmTag -> {
                            stStationNm = parser.text
                        }
                        termTag -> {
                            term = parser.text

                            val data = RouteService(
                                    busRouteNm,
                                    busRouteId,
                                    routeType
                            )

                            when (data.routeType) {
                                "인천" -> seoulBusData.remove(data)
                                "경기" -> seoulBusData.remove(data)
                                else -> seoulBusData.add(data)
                            }
                        }
                    }

                    busRouteIdTag = false
                    busRouteNmTag = false
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

//        previewBusAdapter.items.clear()
        previewBusAdapter.items.addAll(seoulBusData)
        previewBusAdapter.notifyDataSetChanged()

        seoulBusData.forEach {
            Log.d(
                    "XXX",
                    "[서울] ${it.routeName}번 버스 | [노선 ID] : ${it.routeId}, [노선 종류] : ${it.routeType}"
            )
        }

    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun searchGyeonggiBusRouteId(busNumber: String) {
        val url = URL("http://openapi.gbis.go.kr/ws/rest/busrouteservice?serviceKey=$SERVICE_KEY&keyword=$busNumber")

        val parser = createParser(url).parser
        var parserEvent = createParser(url).parserEvent

        var districtCdTag = false
        var regionNameTag = false
        var routeIdTag = false
        var routeNameTag = false
        var routeTypeCdTag = false
        var routeTypeNameTag = false

        var districtCd = ""
        var regionName = ""
        var routeId = ""
        var routeName = ""
        var routeTypeCd = ""
        var routeTypeName = ""

        while (parserEvent != XmlPullParser.END_DOCUMENT) {
            when (parserEvent) {
                XmlPullParser.START_DOCUMENT -> {
                }

                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "districtCd" -> {
                            districtCdTag = true
                        }
                        "regionName" -> {
                            regionNameTag = true
                        }
                        "routeId" -> {
                            routeIdTag = true
                        }
                        "routeName" -> {
                            routeNameTag = true
                        }
                        "routeTypeCd" -> {
                            routeTypeCdTag = true
                        }
                        "routeTypeName" -> {
                            routeTypeNameTag = true
                        }
                    }
                }

                XmlPullParser.TEXT -> {
                    when {
//                        districtCdTag -> {
//                            districtCd = parser.text
//                        }
//                        regionNameTag -> {
//                            regionName = parser.text
//                        }
                        routeIdTag -> {
                            routeId = parser.text
                        }
                        routeNameTag -> {
                            routeName = parser.text
                        }
                        routeTypeCdTag -> {
                            routeTypeCd = parser.text.checkRouteType()
                        }
                        routeTypeNameTag -> {
                            routeTypeName = parser.text

                            val data = RouteService(
                                    routeName,
                                    routeId,
                                    routeTypeCd
                            )

                            gyeonggiBusData.add(data)
                        }
                    }
                    districtCdTag = false
                    regionNameTag = false
                    routeIdTag = false
                    routeNameTag = false
                    routeTypeCdTag = false
                    routeTypeNameTag = false
                }
            }
            parserEvent = parser.next()
        }

//        previewBusAdapter.items.clear()
        previewBusAdapter.items.addAll(gyeonggiBusData)
        previewBusAdapter.notifyDataSetChanged()

        gyeonggiBusData.forEach {
            Log.d(
                    "XXX",
                    "[경기] ${it.routeName}번 버스 | [노선 ID] : ${it.routeId}, [노선 종류] : ${it.routeType}"
            )
        }
    }
}