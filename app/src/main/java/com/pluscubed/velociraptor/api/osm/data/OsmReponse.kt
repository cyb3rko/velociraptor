package com.pluscubed.velociraptor.api.osm.data

import kotlinx.serialization.Serializable

@Serializable
data class OsmResponse(
    val elements: List<Element> = ArrayList()
)
