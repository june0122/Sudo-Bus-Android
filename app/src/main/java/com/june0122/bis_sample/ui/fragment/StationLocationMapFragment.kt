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
import kotlinx.android.synthetic.main.fragment_station_location_map.*
import java.util.*

class StationLocationMapFragment : Fragment(), OnMapReadyCallback {
    private var googleMap: GoogleMap? = null

    private var lat: Double = 0.0
    private var lng: Double = 0.0
    private var stationName = ""
    private var stationArsId = ""
    private var stationDirection = ""

    fun setLatLng(latitude: Double, longitude: Double) {
        lat = latitude
        lng = longitude
    }

    fun inputStationInfo(stNm: String, arsId: String, nxtStn: String) {
        stationName = stNm
        stationArsId = arsId
        stationDirection = nxtStn
    }

    override fun onMapReady(p0: GoogleMap?) {
        val geocoder = Geocoder(context, Locale.KOREA)
        val latLng = LatLng(lat, lng)
        val markerOptions = MarkerOptions().position(latLng).title(stationName)
        val zoomLevel = 17f

        googleMap = p0
        googleMap?.apply {
            addMarker(markerOptions)
            moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel))
            animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel))

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

                Log.d("LATLNG", "${geocoder.getFromLocation(cameraPosition.target.latitude, cameraPosition.target.longitude, 1)}")
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_station_location_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment: SupportMapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        mapFragment.getMapAsync(this)

        stationMapBackButton.setOnClickListener {
            activity?.onBackPressed()
        }

        stationNameTextView.text = stationName
        bottomStationNameTextView.text = stationName
        bottomBusArsIdTextView.text = stationArsId
        bottomDirectionTextView.text = resources.getString(R.string.direction_station, stationDirection)
    }
}