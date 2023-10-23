package com.pluscubed.velociraptor.api.osm.data

import com.pluscubed.velociraptor.api.Coord
import kotlinx.serialization.Serializable

@Serializable
data class Element(
    var id: Int = -1,
    var geometry: List<Coord> = listOf(),
    var nodes: List<Long> = listOf(),
    var tags: Tags
)
