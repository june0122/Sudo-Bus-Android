package com.june0122.bis_sample.utils.decoration

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.june0122.bis_sample.utils.dp
import kotlinx.android.synthetic.main.item_bus_route_station.view.*

class BusRouteItemDecoration(
        val context: Context?,
        private val count: Int
) : RecyclerView.ItemDecoration() {

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
}