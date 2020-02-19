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
    private val stationPreviewData = arrayListOf<StationPreviewData>()
    private val previewStationAdapter = PreviewStationAdapter()
    private val stationBusListFragment = StationBusListFragment()

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
            stationPreviewData.clear()
            when (inputData) {
                "" -> stationPreviewData.clear()
                else -> searchStationId(inputData)
            }
        }

        previewStationRecyclerView.addOnItemTouchListener(
                RecyclerItemClickListener(
                        view.context,
                        previewStationRecyclerView,
                        object : RecyclerItemClickListener.OnItemClickListener {
                            override fun onItemClick(view: View, position: Int) {

                                if (stationPreviewData[position].stationArsId == "0") {
                                    Toast.makeText(context, "해당 정류소의 정보가 존재하지 않습니다.", Toast.LENGTH_SHORT).show()
                                    return
                                }

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

                            val data = StationPreviewData(stNm, arsId, stId, tmX, tmY, posX, posY)
                            stationPreviewData.add(data)
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

        previewStationAdapter.items.clear()
        previewStationAdapter.items.addAll(stationPreviewData)
        previewStationAdapter.notifyDataSetChanged()

        stationPreviewData.forEach {
            Log.d(
                    "XXX",
                    "[정류소 이름] ${it.stationName}, [정류소 고유번호] ${it.stationArsId}, [정류소 ID] ${it.stationId}"
            )
        }
    }

//
//    @Throws(XmlPullParserException::class, IOException::class)
//    private fun searchNextStation(stationId: String) {
//        val url = URL("http://ws.bus.go.kr/api/rest/stationinfo/getStationByUid?ServiceKey=${SERVICE_KEY}&arsId=$stationId")
//
//        val parser = createParser(url).parser
//        var parserEvent = createParser(url).parserEvent
//
//        var nxtStnTag = false
//        var nxtStn = ""
//
//        while (parserEvent != XmlPullParser.END_DOCUMENT) {
//            when (parserEvent) {
//                XmlPullParser.START_TAG -> {
//                    when (parser.name) {
//                        "nxtStn" -> {
//                            nxtStnTag = true
//                        }
//                    }
//                }
//
//                XmlPullParser.TEXT -> {
//                    when {
//                        nxtStnTag -> {
//                            nxtStn = parser.text
//
//                        }
//                    }
//                    nxtStnTag = false
//                }
//            }
//            parserEvent = parser.next()
//        }
//
//        Log.d("TEST", nxtStn)
//
//    }

}