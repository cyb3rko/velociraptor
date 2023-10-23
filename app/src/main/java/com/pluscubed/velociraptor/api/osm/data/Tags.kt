package com.pluscubed.velociraptor.api.osm.data

import kotlinx.serialization.Serializable

@Serializable
class Tags(
    val maxspeed: String? = null,
    val name: String? = null,
    val ref: String? = null
)
