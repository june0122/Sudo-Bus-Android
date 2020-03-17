package com.june0122.bis_sample.utils

import com.june0122.bis_sample.model.RouteService

interface Section {
    fun type(): Int
    fun data(): Any
    fun sectionPosition(): Int

    companion object {
        const val HEADER = 0
        const val ITEM = 1
        const val CUSTOM_HEADER = 2
    }
}