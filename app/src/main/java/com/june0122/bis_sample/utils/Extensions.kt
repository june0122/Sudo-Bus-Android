package com.june0122.bis_sample.utils

import android.content.Context
import android.content.res.Resources
import android.os.StrictMode
import android.util.DisplayMetrics
import android.view.View
import android.widget.Button
import androidx.core.content.ContextCompat
import com.june0122.bis_sample.model.ParserElement
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStreamReader
import java.net.URL
import kotlin.math.roundToInt

fun Int.px(): Int = (this / Resources.getSystem().displayMetrics.density).toInt()

fun Int.dp(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

fun Int.sp() = this * Resources.getSystem().displayMetrics.scaledDensity

fun convertPixelsToDp(context: Context, px: Int): Int {
    return (px / (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
}

fun convertDpToPixel(context: Context, dp: Int): Float {
    val resources = context.resources
    val metrics = resources.displayMetrics
    return dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
}

fun Button.textColor(view: View, color: Int) {
    this.setTextColor(ContextCompat.getColor(view.context, color))
}

fun View.backgroundColor(context: Context, color: Int) {
    this.setBackgroundColor(ContextCompat.getColor(context, color))
}

fun createParser(url: URL): ParserElement {
    val inputStream = url.openStream()
    val factory: XmlPullParserFactory = XmlPullParserFactory.newInstance()
    val parser = factory.newPullParser()
    parser.setInput(InputStreamReader(inputStream, "UTF-8"))

    return ParserElement(parser, parser.eventType)
}

fun formatTime(string: String): String {
    val hour = string.substring(8, 10)
    val minute = string.substring(10, 12)

    return "$hour:$minute"
}

fun formatArrivalTime(string: String): ArrayList<String> {

    return if (string.contains("분") && string.contains("초")) {

        val minuteIndex = string.indexOf("분")
        val secondIndex = string.indexOf("초")
        val leftBracketIndex = string.indexOf("[")
        val rightBracketIndex = string.indexOf("]")

        val minute = string.substring(0, minuteIndex)
        val second = string.substring(minuteIndex + 1, secondIndex)
        val arrivalCount =
            string.substring(leftBracketIndex + 1, rightBracketIndex).replace(" ", "")

        arrayListOf(minute, second, arrivalCount)

    } else if (string.contains("분")) {

        val minuteIndex = string.indexOf("분")
        val leftBracketIndex = string.indexOf("[")
        val rightBracketIndex = string.indexOf("]")

        val minute = string.substring(0, minuteIndex)
        val second = "0"
        val arrivalCount =
            string.substring(leftBracketIndex + 1, rightBracketIndex).replace(" ", "")

        arrayListOf(minute, second, arrivalCount)

    } else {
        arrayListOf(string)
    }
}

fun setStrictMode() {
    val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
    StrictMode.setThreadPolicy(policy)
}

fun String.checkRouteType(): String {
    return when (this) {
        "0" -> "공용"
        "1" -> "공항"
        "2" -> "마을"
        "3" -> "간선"
        "4" -> "지선"
        "5" -> "순환"
        "6" -> "광역"
        "7" -> "인천"
        "8" -> "경기"
        "9" -> "폐지"
        else -> "미정"
    }
}

fun String.checkBusType(): String {
    return when (this) {
        "0" -> "일반"
        "1" -> "저상"
        "2" -> "굴절"
        else -> ""
    }
}

fun String.checkCongestion(): String {
    return when (this) {
        "3" -> "여유"
        "4" -> "보통"
        "5" -> "혼잡"
        "6" -> "매우혼잡"
        else -> "여유"
    }
}

fun String.fourDigitsNumber(): String {
    return this.substring(this.length - 4, this.length)
}