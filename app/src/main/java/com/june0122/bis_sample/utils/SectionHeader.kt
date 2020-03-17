package com.june0122.bis_sample.utils

import com.june0122.bis_sample.model.RouteService
import com.june0122.bis_sample.utils.Section.Companion.HEADER

class SectionHeader(private val headerSection: Int, private val data: Any) : Section {
    override fun type(): Int {
        return HEADER
    }

    override fun data(): Any {
        return data
    }

    override fun sectionPosition(): Int {
        return headerSection
    }
}