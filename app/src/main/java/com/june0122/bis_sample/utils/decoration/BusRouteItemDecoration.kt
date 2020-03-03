package com.june0122.bis_sample.utils.decoration

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.june0122.bis_sample.R
import com.june0122.bis_sample.model.BusLocationData
import com.june0122.bis_sample.utils.dp
import com.june0122.bis_sample.utils.px
import kotlinx.android.synthetic.main.item_bus_route_station.view.*

class BusRouteItemDecoration(
        val context: Context?,
        private val count: Int,
        private val snapHelper: LinearSnapHelper,
        private val busLocationData: ArrayList<BusLocationData>
) : RecyclerView.ItemDecoration() {

    private val busToken = context?.let { ContextCompat.getDrawable(it, R.drawable.ic_bus_token) }
    private val busTag = context?.let { ContextCompat.getDrawable(it, R.drawable.ic_bus_info) }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
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

//                val sectionLineLength = child.bottomDivider.height + child.topDivider.height
                val sectionLineLength = 60.dp()
                val sectionProcess =  data.sectionDistance.toFloat() / data.fullSectionDistance.toFloat()
                val sectionProcessedLength = (sectionLineLength * sectionProcess).toInt()

                Log.d("SECT", "${sectionProcess}")

                val busTokenSize = 16.dp()
                val horizontalCenter = (child.busDirectionImageView.left + child.busDirectionImageView.right) / 2
                val verticalCenter = child.top + (child.busDirectionImageView.top + child.busDirectionImageView.bottom) / 2

                busToken?.apply {
                    setBounds(horizontalCenter - (busTokenSize / 2), verticalCenter - (busTokenSize / 2) + (sectionProcessedLength / 2), horizontalCenter + (busTokenSize / 2), verticalCenter + (busTokenSize / 2) + (sectionProcessedLength / 2))
                    draw(c)
                }

                val tagWidth = 60.dp()
                val tagHeight = 50.dp()
                val tagOffset = 40.dp()

                busTag?.apply {
                    setBounds(horizontalCenter - tagOffset - (tagWidth / 2), verticalCenter - (tagHeight / 2) + (sectionProcessedLength / 2), horizontalCenter - tagOffset + (tagWidth / 2), verticalCenter + (tagHeight / 2) + (sectionProcessedLength / 2))
                    draw(c)
                }
            }
        }


//        busLocationData.forEach {
//            dataSampleList.add(layoutManager.findViewByPosition(it.sectionOrder.toInt()))
//        }
//
//        dataSampleList.forEach { child ->
//
//            child?.let {
//
//                val sectionLineLength = child.bottomDivider.height + child.topDivider.height
////                val movedDistance = sectionLineLength * ()
//
//                val busTokenSize = 16.dp()
//                val horizontalCenter = (child.busDirectionImageView.left + child.busDirectionImageView.right) / 2
//                val verticalCenter = child.top + (child.busDirectionImageView.top + child.busDirectionImageView.bottom) / 2
//
//                busToken?.apply {
//                    setBounds(horizontalCenter - (busTokenSize / 2), verticalCenter - (busTokenSize / 2), horizontalCenter + (busTokenSize / 2), verticalCenter + (busTokenSize / 2))
//                    draw(c)
//                }
//
//                val tagWidth = 60.dp()
//                val tagHeight = 50.dp()
//                val tagOffset = 40.dp()
//
//                busTag?.apply {
//                    setBounds(horizontalCenter - tagOffset - (tagWidth / 2), verticalCenter - (tagHeight / 2), horizontalCenter - tagOffset + (tagWidth / 2), verticalCenter + (tagHeight / 2))
//                    draw(c)
//                }
//            }
//        }
    }
}