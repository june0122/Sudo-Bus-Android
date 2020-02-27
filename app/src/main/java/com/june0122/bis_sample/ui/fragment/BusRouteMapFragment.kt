package com.june0122.bis_sample.ui.fragment

import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.june0122.bis_sample.R
import com.june0122.bis_sample.model.Data.Companion.SERVICE_KEY
import com.june0122.bis_sample.model.RoutePathData
import com.june0122.bis_sample.utils.createParser
import kotlinx.android.synthetic.main.fragment_bus_route_map.*
import kotlinx.android.synthetic.main.fragment_bus_route_map.mapLocationLayout
import kotlinx.android.synthetic.main.fragment_bus_route_map.mapLocationTextView
import kotlinx.android.synthetic.main.fragment_station_location_map.*
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.net.URL
import java.util.*

class BusRouteMapFragment : Fragment(), OnMapReadyCallback {
    var routePathData = arrayListOf<RoutePathData>()
    private var inputData: String = ""
    private var googleMap: GoogleMap? = null

    private var lat: Double = 0.0
    private var lng: Double = 0.0

//    private var lat: Double = 37.607963 // 임시 값
//    private var lng: Double = 127.001598

    fun inputBusRouteId(busRouteId: String) {
        inputData = busRouteId
    }

    override fun onMapReady(p0: GoogleMap?) {
        val geocoder = Geocoder(context, Locale.KOREA)
        var latLng = LatLng(lat, lng)
        val markerOptions = MarkerOptions().position(latLng)
        val zoomLevel = 17f

        routePathData.forEach {
            latLng = LatLng(it.wgs84X.toDouble(), it.wgs84Y.toDouble())
            googleMap?.addMarker(markerOptions)
        }

        googleMap = p0
        googleMap?.apply {
//            addMarker(markerOptions)
            moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel))
            animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel))

            setOnCameraIdleListener {
                val cameraPositionAddress = geocoder.getFromLocation(37.607963, 127.001598, 3)
                val formattedAddress = cameraPositionAddress[0].getAddressLine(0).toString()  // IndexOutOfBoundsException 예외 처리 필요
                var exceptCountryNameAddress = ""

                if (formattedAddress.contains("대한민국")) {
                    exceptCountryNameAddress = formattedAddress.replace("대한민국", "")
                }

                if (cameraPosition.zoom > 13f) {
                    mapLocationLayout.visibility = View.VISIBLE
                    mapLocationTextView.text = exceptCountryNameAddress
                } else {
                    mapLocationLayout.visibility = View.INVISIBLE
                }

                Log.d("LATLNG", "${geocoder.getFromLocation(cameraPosition.target.latitude, cameraPosition.target.longitude, 1)}")
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

        searchBusRoutePath(inputData)

//        Log.d("XXXX", "${routePathData[0]}")
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

//        stationBusListAdapter.apply {
//            items.clear()
//            items.addAll(busList)
//            notifyDataSetChanged()
//        }
    }
}