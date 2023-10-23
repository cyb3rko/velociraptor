package com.pluscubed.velociraptor.api.osm.data

import kotlinx.serialization.Serializable

@Serializable
class OsmResponse(
    val elements: List<Element> = ArrayList()
)
