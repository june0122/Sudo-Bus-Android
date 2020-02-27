package com.june0122.bis_sample.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.june0122.bis_sample.R
import com.june0122.bis_sample.model.Data
import com.june0122.bis_sample.model.StationPreviewData
import com.june0122.bis_sample.ui.viewholder.PreviewStationViewHolder
import com.june0122.bis_sample.utils.createParser
import kotlinx.android.synthetic.main.item_preview_station_list.view.*
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.net.URL

class PreviewStationAdapter : RecyclerView.Adapter<PreviewStationViewHolder>() {
    var items = arrayListOf<StationPreviewData>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PreviewStationViewHolder =
            PreviewStationViewHolder(parent)

    override fun getItemCount(): Int = items.count()

    override fun onBindViewHolder(holder: PreviewStationViewHolder, position: Int) {
        val model = items[position]

        with(holder.itemView) {
            stationNamePreviewTextView.text = model.stationName
            stationArsIdPreviewTextView.text = model.stationArsId
            directionPreviewTextView.text = resources.getString(R.string.direction_station, searchDirection(model.stationArsId))
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun searchDirection(stationId: String): String {
        val directionList = arrayListOf<String>()
        val url = URL("http://ws.bus.go.kr/api/rest/stationinfo/getStationByUid?ServiceKey=${Data.SERVICE_KEY}&arsId=$stationId")

        val parser = createParser(url).parser
        var parserEvent = createParser(url).parserEvent

        var nxtStnTag = false
        var nxtStn = ""

        while (parserEvent != XmlPullParser.END_DOCUMENT) {
            when (parserEvent) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "nxtStn" -> {
                            nxtStnTag = true
                        }
                    }
                }

                XmlPullParser.TEXT -> {
                    when {
                        nxtStnTag -> {
                            nxtStn = parser.text
                            directionList.add(nxtStn)
                        }
                    }
                    nxtStnTag = false
                }
            }
            parserEvent = parser.next()
        }

        return when (directionList.size) {
            0 -> ""
            else -> directionList[0]
        }
    }
}
