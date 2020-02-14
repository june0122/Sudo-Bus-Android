package com.june0122.bis_sample.ui.fragment

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
import com.google.android.gms.maps.model.MarkerOptions
import com.june0122.bis_sample.R

class StationLocationMapFragment : Fragment(), OnMapReadyCallback {
    private var googleMap: GoogleMap? = null

    override fun onMapReady(p0: GoogleMap?) {

        googleMap = p0

        val latLng = LatLng(37.5625221708, 126.9275410215)
        val markerOptions = MarkerOptions().position(latLng).title("연남")

        val zoomLevel = 17f

        googleMap?.apply {
            addMarker(markerOptions)
            moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel))
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_station_location_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment: SupportMapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        mapFragment.getMapAsync(this)

    }
}