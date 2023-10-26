package com.pluscubed.velociraptor.api.osm

import android.content.Context
import android.location.Location
import android.net.Uri
import com.pluscubed.velociraptor.BuildConfig
import com.pluscubed.velociraptor.api.LimitProvider
import com.pluscubed.velociraptor.api.LimitResponse
import com.pluscubed.velociraptor.api.cache.CacheLimitProvider
import com.pluscubed.velociraptor.api.osm.data.Element
import com.pluscubed.velociraptor.api.osm.data.OsmResponse
import com.pluscubed.velociraptor.api.osm.data.Tags
import com.pluscubed.velociraptor.utils.PrefUtils
import com.pluscubed.velociraptor.utils.Utils
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.io.IOException
import java.util.Locale

class OsmLimitProvider(
    private val context: Context,
    private val cacheLimitProvider: CacheLimitProvider
) : LimitProvider {
    private lateinit var endpoint: OsmApiEndpoint

    private fun initializeOsmService() {
        val client = buildHttpClient()
        endpoint = OsmApiEndpoint(client, ENDPOINT_URL)
    }

    private fun buildHttpClient(): HttpClient {
        val client = HttpClient(CIO) {
            defaultRequest {
                contentType(ContentType.Application.Json)
                url(ENDPOINT_URL)
                header("User-Agent", "VelociraptorV2/${BuildConfig.VERSION_NAME}")
            }
            HttpResponseValidator {
                validateResponse {
                    if (!it.status.isSuccess()) return@validateResponse
                    updateEndpointTimeTaken(
                        (it.responseTime.timestamp - it.requestTime.timestamp).toInt(),
                        endpoint
                    )
                }
            }
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 15000
            }
            if (BuildConfig.DEBUG) {
                install(Logging) {
                    logger = object: Logger {
                        override fun log(message: String) {
                            Timber.d(message)
                        }
                    }
                    level = LogLevel.HEADERS
                }
            }
        }
        return client
    }

    private fun updateEndpointTimeTaken(timeTaken: Int, endpoint: OsmApiEndpoint) {
        endpoint.timeTaken = timeTaken
    }

    private fun buildQueryBody(location: Location): String {
        val latitude = String.format(Locale.ROOT, "%.5f", location.latitude)
        val longitude = String.format(Locale.ROOT, "%.5f", location.longitude)
        return ("[out:json];" +
                "way(around:" + OSM_RADIUS + ","
                + latitude + ","
                + longitude +
                ")" +
                "[\"highway\"];out body geom;")
    }

    private fun getOsmResponse(location: Location): OsmResponse {
        try {
            val osmNetWorkResponse: OsmResponse
            runBlocking {
                osmNetWorkResponse = endpoint.getOsm(buildQueryBody(location))
            }
            logOsmRequest(endpoint)
            return osmNetWorkResponse
        } catch (e: Exception) {
            //catch any errors, rethrow
            updateEndpointTimeTaken(Integer.MAX_VALUE, endpoint)
            logOsmError(endpoint, e)
            throw e
        }
    }

    override fun getSpeedLimit(
            location: Location,
            lastResponse: LimitResponse?,
            origin: Int
    ): List<LimitResponse> {
        val debuggingEnabled = PrefUtils.isDebuggingEnabled(context)
        var limitResponse = LimitResponse(
            timestamp = System.currentTimeMillis(),
            origin = LimitResponse.ORIGIN_OSM,
            debugInfo = if (debuggingEnabled) "\nOSM Info:${endpoint.baseUrl}" else ""
        )
        try {
            val osmResponse = getOsmResponse(location)

            val emptyObservableResponse = {
                listOf(limitResponse.initDebugInfo(debuggingEnabled))
            }

            val elements = osmResponse.elements

            if (elements.isEmpty()) {
                return emptyObservableResponse()
            }

            val bestMatch = getBestElement(elements, lastResponse)
            var bestResponse: LimitResponse? = null

            for (element in elements) {
                //Get coords
                if (element.geometry.isNotEmpty()) {
                    limitResponse = limitResponse.copy(coords = element.geometry)
                } else if (element !== bestMatch) {
                    // If coords are empty and element is not the best one,
                    // no need to continue parsing info for cacheLimitProvider.
                    // Skip to next element.
                    continue
                }

                //Get road names
                val tags = element.tags
                limitResponse = limitResponse.copy(roadName = parseOsmRoadName(tags))

                //Get speed limit
                val maxspeed = tags.maxspeed
                if (maxspeed != null) {
                    limitResponse = limitResponse.copy(speedLimit = parseOsmSpeedLimit(maxspeed))
                }

                val response = limitResponse.initDebugInfo(debuggingEnabled)

                //Cache
                cacheLimitProvider.put(response)

                if (element === bestMatch) {
                    bestResponse = response
                }
            }

            if (bestResponse != null) {
                return listOf(bestResponse)
            }

            return emptyObservableResponse()

        } catch (e: Exception) {
            return listOf(
                limitResponse.copy(error = e).initDebugInfo(debuggingEnabled)
            )
        }
    }

    private fun parseOsmRoadName(tags: Tags): String {
        val ref = tags.ref
        val name = tags.name
        return if (ref == null && name == null) {
            "null"
        } else if (name != null && ref != null) {
            "$ref$ROADNAME_DELIM$name"
        } else name ?: ref.orEmpty()
    }

    private fun parseOsmSpeedLimit(maxspeed: String): Int {
        var speedLimit = -1
        if (maxspeed.matches("^-?\\d+$".toRegex())) {
            //If it is an integer, it is in km/h
            speedLimit = Integer.valueOf(maxspeed)
        } else if (maxspeed.contains("mph")) {
            val split = maxspeed.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            speedLimit = Integer.valueOf(split[0])
            speedLimit = Utils.convertMphToKmh(speedLimit)
        }
        return speedLimit
    }

    private fun getBestElement(elements: List<Element>, lastResponse: LimitResponse?): Element {
        var bestElement: Element? = null
        var fallback: Element? = null

        if (lastResponse != null) {
            for (newElement in elements) {
                val newTags = newElement.tags
                if (fallback == null && newTags.maxspeed != null) {
                    fallback = newElement
                }
                if (lastResponse.roadName == parseOsmRoadName(newTags)) {
                    bestElement = newElement
                    break
                }
            }
        }

        if (bestElement == null) {
            bestElement = fallback ?: elements[0]
        }
        return bestElement
    }

    private fun logOsmRequest(endpoint: OsmApiEndpoint) {
        val endpointString = Uri.parse(endpoint.baseUrl).authority!!
            .replace(".", "_")
            .replace("-", "_")
        Timber.d("Request to %s", endpointString)
    }

    private fun logOsmError(endpoint: OsmApiEndpoint, throwable: Throwable) {
        if (throwable is IOException) {
            val endpointString = Uri.parse(endpoint.baseUrl).authority!!
                .replace(".", "_")
                .replace("-", "_")
            Timber.e("OSM Error at %s", endpointString)
        }
        throwable.printStackTrace()
    }

    companion object {
        const val ENDPOINT_URL = "https://overpass.kumi.systems/api/"
        const val OSM_RADIUS = 15
        const val ROADNAME_DELIM = "`"
    }

    init {
        initializeOsmService()
    }
}
