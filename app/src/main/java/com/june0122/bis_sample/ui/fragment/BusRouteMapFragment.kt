package com.june0122.bis_sample.ui.fragment

import android.graphics.Color
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.june0122.bis_sample.R
import com.june0122.bis_sample.model.Data.Companion.SERVICE_KEY
import com.june0122.bis_sample.model.RoutePathData
import com.june0122.bis_sample.utils.createParser
import kotlinx.android.synthetic.main.fragment_bus_route_map.*
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.net.URL
import java.util.*

class BusRouteMapFragment : Fragment(), OnMapReadyCallback {
    private var routePathData = arrayListOf<RoutePathData>()
    private var googleMap: GoogleMap? = null
    private var inputData: String = ""

    private var routeName = ""
    private var firstLocation = ""
    private var lastLocation = ""
    private var routeSchedule = ""
    private var busTerm = ""

    private var polylineOptions : PolylineOptions? = null

    fun inputBusRouteId(busRouteId: String) {
        inputData = busRouteId
    }

    fun inputBusRouteInfo(rtNm: String, firstPos: String, lastPos: String, schedule: String, term: String) {
        routeName = rtNm
        firstLocation = firstPos
        lastLocation = lastPos
        routeSchedule = schedule
        busTerm = term
    }

    override fun onMapReady(p0: GoogleMap?) {
        val geocoder = Geocoder(context, Locale.KOREA)

        searchBusRoutePath(inputData)

        googleMap = p0

        val builder = LatLngBounds.Builder()

        routePathData.forEach {
            val markerOptions = MarkerOptions().position(LatLng(it.wgs84Y.toDouble(), it.wgs84X.toDouble())).visible(false)

            val polyline = googleMap?.addPolyline(PolylineOptions().color(Color.GREEN).width(10f).clickable(true).add(LatLng(it.wgs84Y.toDouble(), it.wgs84X.toDouble())))

            polyline?.tag = "A"


            googleMap?.addMarker(markerOptions)
            builder.include(markerOptions.position)
        }

        val bounds = builder.build()
        val displayWidth = resources.displayMetrics.widthPixels
        val displayHeight = resources.displayMetrics.heightPixels
        val displayPadding: Int = (displayWidth * 0.2).toInt()
        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, displayWidth, displayHeight, displayPadding)

        googleMap?.apply {


            moveCamera(cameraUpdate)
            setOnCameraIdleListener {
                val cameraPositionAddress = geocoder.getFromLocation(cameraPosition.target.latitude, cameraPosition.target.longitude, 3)
                val formattedAddress = cameraPositionAddress[0].getAddressLine(0).toString()  // IndexOutOfBoundsException 예외 처리 필요
                var shortAddress = ""

                if (formattedAddress.contains("대한민국")) {
                    shortAddress = formattedAddress.replace("대한민국", "")
                }

                if (cameraPosition.zoom > 13f) {
                    mapLocationLayout.visibility = View.VISIBLE
                    mapLocationTextView.text = shortAddress
                } else {
                    mapLocationLayout.visibility = View.INVISIBLE
                }
            }

        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_bus_route_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment: SupportMapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        busRouteMapBackButton.setOnClickListener {
            activity?.onBackPressed()
        }

        busRouteNameTextView.text = routeName
        firstStationNameTextView.text = firstLocation
        lastStationNameTextView.text = lastLocation
        busRouteScheduleTextView.text = routeSchedule
        busTermTextView.text = resources.getString(R.string.bus_term, busTerm)

    }


    @Throws(XmlPullParserException::class, IOException::class)
    private fun searchBusRoutePath(busRouteId: String) {
        val url = URL("http://ws.bus.go.kr/api/rest/busRouteInfo/getRoutePath?ServiceKey=${SERVICE_KEY}&busRouteId=$busRouteId")

        val parser = createParser(url).parser
        var parserEvent = createParser(url).parserEvent

        var gpsXTag = false
        var gpsYTag = false
        var noTag = false
        var posXTag = false
        var posYTag = false

        var gpsX = ""
        var gpsY = ""
        var no = ""
        var posX = ""
        var posY: String

        while (parserEvent != XmlPullParser.END_DOCUMENT) {
            when (parserEvent) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "gpsX" -> {
                            gpsXTag = true
                        }
                        "gpsY" -> {
                            gpsYTag = true
                        }
                        "no" -> {
                            noTag = true
                        }
                        "posX" -> {
                            posXTag = true
                        }
                        "posY" -> {
                            posYTag = true
                        }
                    }
                }

                XmlPullParser.TEXT -> {
                    when {
                        gpsXTag -> {
                            gpsX = parser.text
                        }
                        gpsYTag -> {
                            gpsY = parser.text
                        }
                        noTag -> {
                            no = parser.text
                        }
                        posXTag -> {
                            posX = parser.text
                        }
                        posYTag -> {
                            posY = parser.text

                            val data = RoutePathData(no, gpsX, gpsY, posX, posY)
                            routePathData.add(data)
                        }
                    }

                    gpsXTag = false
                    gpsYTag = false
                    noTag = false
                    posXTag = false
                    posYTag = false
                }
            }
            parserEvent = parser.next()
        }
    }
}