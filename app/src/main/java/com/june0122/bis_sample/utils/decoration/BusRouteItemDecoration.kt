package com.june0122.bis_sample.utils.decoration

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.june0122.bis_sample.R
import com.june0122.bis_sample.model.BusLocationData
import com.june0122.bis_sample.utils.*
import kotlinx.android.synthetic.main.item_bus_location.view.*
import kotlinx.android.synthetic.main.item_bus_route_station.view.bottomDivider
import kotlinx.android.synthetic.main.item_bus_route_station.view.topDivider

class BusRouteItemDecoration(
        val context: Context?,
        private val count: Int,
        private val busLocationData: ArrayList<BusLocationData>
) : RecyclerView.ItemDecoration() {

    private var busLocationItem: Bitmap? = null

    override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)

        val itemPosition = parent.getChildAdapterPosition(view)

        val itemCount = state.itemCount

        with(outRect) {
            when (itemPosition) {
                0 -> {
                    top = 16.dp()
                    view.topDivider.visibility = View.INVISIBLE
                    view.bottomDivider.visibility = View.VISIBLE
                }

                itemCount - 1 -> {
                    bottom = 32.dp()
                    view.topDivider.visibility = View.VISIBLE
                    view.bottomDivider.visibility = View.INVISIBLE
                }

                else -> {
                    view.topDivider.visibility = View.VISIBLE
                    view.bottomDivider.visibility = View.VISIBLE

                }
            }
        }
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)

        val layoutManager = parent.layoutManager as LinearLayoutManager

        busLocationData.forEach { data ->
            val child = layoutManager.findViewByPosition(data.sectionOrder.toInt() - 1)

            child?.let {
                val sectionLineLength = 60.dp()
                val sectionProcess = data.sectionDistance.toFloat() / data.fullSectionDistance.toFloat()
                val sectionProcessedLength = (sectionLineLength * sectionProcess).toInt()

                initBusLocationItem(parent, data)

                busLocationItem?.let { item ->
                    c.drawBitmap(item, 0.toFloat(), (child.top + sectionProcessedLength).toFloat(), Paint())
                }
            }
        }
    }

    private fun initBusLocationItem(parent: RecyclerView, data: BusLocationData) {
        val view = LayoutInflater.from(context).inflate(R.layout.item_bus_location, parent, false)
        val bitmap = Bitmap.createBitmap(parent.width, parent.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val widthSpec = View.MeasureSpec.makeMeasureSpec(parent.width, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(60.dp(), View.MeasureSpec.EXACTLY)

        view.plainBusNumberTextView?.text = data.plainBusNumber.fourDigitsNumber()
        view.congestionTextView?.text = data.busCongestion.checkCongestion()
        view.busTypeTextView?.text = data.type.checkBusType()

//        when (data.type.checkBusType()) {
//            "일반" -> {
//                view.busTagImageView.layoutParams.height = 18.dp()
//            }
//            else -> {
//                view.busTagImageView.layoutParams.height = 32.dp()
//            }
//        }

        view?.measure(widthSpec, heightSpec)
        view?.layout(parent.left, parent.top, parent.width, parent.height)
        view?.draw(canvas)
        busLocationItem = bitmap
    }

    private fun getTextWidth(text: String, paint: Paint): Int {
        val bounds = Rect()
        paint.getTextBounds(text, 0, text.length, bounds)

        return bounds.left + bounds.width()
    }

}