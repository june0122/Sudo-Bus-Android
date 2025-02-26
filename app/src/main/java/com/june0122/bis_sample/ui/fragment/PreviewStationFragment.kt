package com.june0122.bis_sample.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.june0122.bis_sample.R
import com.june0122.bis_sample.model.Data.Companion.SERVICE_KEY
import com.june0122.bis_sample.model.StationPreviewData
import com.june0122.bis_sample.ui.adapter.PreviewStationAdapter
import com.june0122.bis_sample.utils.RecyclerItemClickListener
import com.june0122.bis_sample.utils.createParser
import kotlinx.android.synthetic.main.fragment_preview_station.*
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.net.URL

class PreviewStationFragment : Fragment() {
    private var inputData: String = ""
    private val seoulStationPreviewDataList = arrayListOf<StationPreviewData>()
    private val gyeonggiStationPreviewDataList = arrayListOf<StationPreviewData>()
    private val previewStationAdapter = PreviewStationAdapter()

    fun inputStationArsId(stationArsId: String) {
        inputData = stationArsId
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_preview_station, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val previewStationListLayoutManager = LinearLayoutManager(context)
        previewStationRecyclerView.layoutManager = previewStationListLayoutManager
        previewStationListLayoutManager.orientation = LinearLayoutManager.VERTICAL
        previewStationRecyclerView.adapter = previewStationAdapter

        activity?.runOnUiThread {
            seoulStationPreviewDataList.clear()
            gyeonggiStationPreviewDataList.clear()
            when (inputData) {
                "" -> previewStationAdapter.items.clear()
                else -> {
                    searchStationId(inputData)
                    searchGyeonggiStationId(inputData)
                }
            }
        }

        previewStationRecyclerView.addOnItemTouchListener(
                RecyclerItemClickListener(
                        view.context,
                        previewStationRecyclerView,
                        object : RecyclerItemClickListener.OnItemClickListener {
                            override fun onItemClick(view: View, position: Int) {

                                if (seoulStationPreviewDataList[position].stationArsId == "0") {
                                    Toast.makeText(context, "해당 정류소의 정보가 존재하지 않습니다.", Toast.LENGTH_SHORT).show()
                                    return
                                }

                                val stationBusListFragment = StationBusListFragment()

                                stationBusListFragment.inputArsId(previewStationAdapter.items[position].stationArsId)
                                stationBusListFragment.inputLatLng(
                                        previewStationAdapter.items[position].wgs84Y.toDouble(),
                                        previewStationAdapter.items[position].wgs84X.toDouble()
                                )

//                                if (stationBusListFragment.busList[position].arsId == "") {
//                                    Log.d("CHECK", "NULL")
//                                    Toast.makeText(context, "해당 정류소의 정보가 존재하지 않습니다.", Toast.LENGTH_SHORT).show()
//                                    return
//                                }

                                activity?.supportFragmentManager
                                        ?.beginTransaction()
                                        ?.replace(R.id.fragmentContainer, stationBusListFragment)
                                        ?.addToBackStack(null)?.commit()
                            }
                        })
        )
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun searchStationId(busStationName: String) {
        val url = URL(
                "http://ws.bus.go.kr/api/rest/stationinfo/getStationByName?ServiceKey=${SERVICE_KEY}&stSrch=$busStationName"
        )

        val parser = createParser(url).parser
        var parserEvent = createParser(url).parserEvent

        var arsIdTag = false
        var posXTag = false
        var posYTag = false
        var stIdTag = false
        var stNmTag = false
        var tmXTag = false
        var tmYTag = false

        var arsId = ""
        var posX = ""
        var posY = ""
        var stId = ""
        var stNm = ""
        var tmX = ""
        var tmY: String

        while (parserEvent != XmlPullParser.END_DOCUMENT) {
            when (parserEvent) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "arsId" -> {
                            arsIdTag = true
                        }

                        "posX" -> {
                            posXTag = true
                        }

                        "posY" -> {
                            posYTag = true
                        }

                        "stId" -> {
                            stIdTag = true
                        }

                        "stNm" -> {
                            stNmTag = true
                        }

                        "tmX" -> {
                            tmXTag = true
                        }

                        "tmY" -> {
                            tmYTag = true
                        }
                    }
                }

                XmlPullParser.TEXT -> {
                    when {
                        arsIdTag -> {
                            arsId = parser.text
                        }

                        posXTag -> {
                            posX = parser.text
                        }

                        posYTag -> {
                            posY = parser.text
                        }

                        stIdTag -> {
                            stId = parser.text
                        }

                        stNmTag -> {
                            stNm = parser.text
                        }

                        tmXTag -> {
                            tmX = parser.text
                        }

                        tmYTag -> {
                            tmY = parser.text

                            val data = StationPreviewData(stNm, arsId, stId, tmX, tmY)

                            when (data.stationArsId) {
                                "0" -> seoulStationPreviewDataList.remove(data)
                                else -> seoulStationPreviewDataList.add(data)
                            }

                        }
                    }

                    arsIdTag = false
                    posXTag = false
                    posYTag = false
                    stIdTag = false
                    stNmTag = false
                    tmXTag = false
                    tmYTag = false
                }
            }
            parserEvent = parser.next()
        }

//        previewStationAdapter.items.clear()
        previewStationAdapter.items.addAll(seoulStationPreviewDataList)
        previewStationAdapter.notifyDataSetChanged()

        seoulStationPreviewDataList.forEach {
            Log.d(
                    "XXX",
                    "[서울] - [정류소 이름] ${it.stationName}, [정류소 고유번호] ${it.stationArsId}, [정류소 ID] ${it.stationId}"
            )
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun searchGyeonggiStationId(busStationName: String) {
        val url = URL(
                "http://openapi.gbis.go.kr/ws/rest/busstationservice?serviceKey=$SERVICE_KEY&keyword=$busStationName"
        )

        val parser = createParser(url).parser
        var parserEvent = createParser(url).parserEvent

        var centerYnTag = false
        var districtCdTag = false
        var mobileNoTag = false
        var regionNameTag = false
        var stationIdTag = false
        var stationNameTag = false
        var xTag = false
        var yTag = false

        var centerYn = ""
        var districtCd = ""
        var mobileNo = ""
        var regionName = ""
        var stationId = ""
        var stationName = ""
        var x = ""
        var y = ""

        while (parserEvent != XmlPullParser.END_DOCUMENT) {
            when (parserEvent) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "centerYn" -> {
                            centerYnTag = true
                        }

                        "districtCd" -> {
                            districtCdTag = true
                        }

                        "mobileNo" -> {
                            mobileNoTag = true
                        }

                        "regionName" -> {
                            regionNameTag = true
                        }

                        "stationId" -> {
                            stationIdTag = true
                        }

                        "stationName" -> {
                            stationNameTag = true
                        }

                        "x" -> {
                            xTag = true
                        }

                        "y" -> {
                            yTag = true
                        }
                    }
                }

                XmlPullParser.TEXT -> {
                    when {
                        centerYnTag -> {
                            centerYn = parser.text
                        }

                        districtCdTag -> {
                            districtCd = parser.text
                        }

                        mobileNoTag -> {
                            mobileNo = parser.text
                        }

                        regionNameTag -> {
                            regionName = parser.text
                        }

                        stationIdTag -> {
                            stationId = parser.text
                        }

                        stationNameTag -> {
                            stationName = parser.text
                        }

                        xTag -> {
                            x = parser.text
                        }

                        yTag -> {
                            y = parser.text

                            val data = StationPreviewData(stationName, mobileNo, stationId, x, y)
                            gyeonggiStationPreviewDataList.add(data)
                        }
                    }

                    centerYnTag = false
                    districtCdTag = false
                    mobileNoTag = false
                    regionNameTag = false
                    stationIdTag = false
                    stationNameTag = false
                    xTag = false
                    yTag = false
                }
            }
            parserEvent = parser.next()
        }

//        previewStationAdapter.items.clear()
        previewStationAdapter.items.addAll(gyeonggiStationPreviewDataList)
        previewStationAdapter.notifyDataSetChanged()

        gyeonggiStationPreviewDataList.forEach {
            Log.d(
                    "XXX",
                    "[경기] - [정류소 이름] ${it.stationName}, [정류소 고유번호] ${it.stationArsId}, [정류소 ID] ${it.stationId}"
            )
        }
    }

}