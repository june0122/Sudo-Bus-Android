package com.june0122.bis_sample.model

import org.xmlpull.v1.XmlPullParser

data class ParserElement(val parser: XmlPullParser, val parserEvent: Int)

data class StationList(
    val busRouteId: String,
    val busRouteName: String,
    val stationName: String,
    val stationArsId: String,
    val sectionSpeed: String,
    val firstTime: String,
    val lastTime: String,
    val routeType: String,
    val wgs84X: String,
    val wgs84Y: String
)

data class BusList(
        val arsId: String,
        val stationName: String,
        val busNumber: String,
        val nextStation: String,
        val firstArrivalBusInfo: String,
        val secondArrivalBusInfo: String
)

data class RoutePathData(
        val pathNumber: String,
        val wgs84X: String,
        val wgs84Y: String,
        val grs80X: String,
        val grs80Y: String
)

data class RouteData(
        val busRouteId: String,
        val busRouteName: String,
        val routeType: String,
        val startStationName: String,
        val endStationName: String,
        val term: String,
        val firstTime: String,
        val lastTime: String,
        val corpName: String
)

data class BusData(
        val busNumber: String,
        val busId: String,
        val busType: String,
        val term: String,
        val startStationName: String,
        val endStationName: String,
        val firstTime: String,
        val lastTime: String,
        val lastBusPresence: String
)

data class BusLocationData(
        val sectionOrder: String,
        val sectionDistance: String,
        val fullSectionDistance: String,
        val stationArrivalFlag: String,
        val sectionID: String,
        val dataUpdateTime: String,
        val wgs84X: String,
        val wgs84Y: String,
        val vehicleId: String,
        val plainBusNumber: String,
        val type: String,
        val busRunningFlag: String,
        val turningStationId: String,
        val lastBusFlag: String,
        val fullBusFlag: String,
        val lastStationId: String,
        val busCongestion: String
)

data class StationPreviewData(
        val stationName: String,
        val stationArsId: String,
        val stationId: String,
        val wgs84X: String,
        val wgs84Y: String,
        val grs80X: String,
        val grs80Y: String
)

class Data {
    companion object {
        const val SERVICE_KEY = "6Gi1UHlRZK0oxUZHrb5I5L%2Fb466WpwHkOp%2BBfVMdZFJAq6O7B5E1uQuxNlgAbfxrjjDSTJOuyGjrU25iiZS6hA%3D%3D"
    }
}