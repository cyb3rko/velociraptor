package com.pluscubed.velociraptor.api.osm

import com.pluscubed.velociraptor.api.osm.data.OsmResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class OsmApiEndpoint(
    private val client: HttpClient,
    val baseUrl: String
) : Comparable<OsmApiEndpoint> {
    var timeTaken: Int = 0

    suspend fun getOsm(data: String): OsmResponse {
        return client.post("interpreter") {
            setBody(data)
        }.body()
    }

    override fun toString(): String {
        val time = when (timeTaken) {
            Int.MAX_VALUE -> "error"
            0 -> "pending"
            else -> timeTaken.toString() + "ms"
        }
        return this.baseUrl + " - " + time
    }

    override fun compareTo(other: OsmApiEndpoint): Int {
        return timeTaken.compareTo(other.timeTaken)
    }
}
