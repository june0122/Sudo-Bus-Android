package com.june0122.bis_sample.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.june0122.bis_sample.R
import com.june0122.bis_sample.model.RouteService
import com.june0122.bis_sample.utils.Section

class PreviewBusAdapter : StickyAdapter<RecyclerView.ViewHolder, RecyclerView.ViewHolder>() {
    var items = arrayListOf<Section>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        if (viewType == Section.HEADER || viewType == Section.CUSTOM_HEADER) {
            return HeaderViewHolder(inflater.inflate(R.layout.item_preview_bus_list_header, parent, false))
        }
        return ItemViewHolder(inflater.inflate(R.layout.item_preview_bus_list, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val type = items[position].type()
        val section = items[position].sectionPosition()
        val data : RouteService = items[position].data() as RouteService

        when (type) {
            Section.HEADER -> {
                (holder as HeaderViewHolder).headerTextView.text =
                    when (section) {
                        0 -> "서울"
                        1 -> "경기"
                        2 -> "인천"
                        else -> ""
                    }
            }

            Section.ITEM -> {
                with(holder as ItemViewHolder) {
                    busNumberTextView.text = data.routeName
                    routeTypeTextView.text = data.routeType
                }
            }
            else -> {
                (holder as HeaderViewHolder).headerTextView.text =
                    holder.itemView.resources.getString(R.string.custom_header)
            }
        }
    }

    override fun getItemViewType(position: Int): Int = items[position].type()

    override fun getItemCount(): Int = items.size

    override fun getHeaderPositionForItem(itemPosition: Int): Int =
        items[itemPosition].sectionPosition()

    override fun onBindHeaderViewHolder(holder: RecyclerView.ViewHolder, headerPosition: Int) {
        (holder as HeaderViewHolder).headerTextView.text =
            when (headerPosition) {
                0 -> "서울"
                1 -> "경기"
                2 -> "인천"
                else -> ""
            }
    }

    override fun onCreateHeaderViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return createViewHolder(parent, Section.HEADER)
    }

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var headerTextView: TextView = itemView.findViewById(R.id.headerTextView)
    }

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var busNumberTextView: TextView = itemView.findViewById(R.id.routeNamePreviewTextView)
        var routeTypeTextView: TextView = itemView.findViewById(R.id.routeTypeTextView)
    }
}