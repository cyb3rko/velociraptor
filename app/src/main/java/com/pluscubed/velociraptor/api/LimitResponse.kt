package com.pluscubed.velociraptor.api

import java.util.*

data class LimitResponse(
    val fromCache: Boolean = false,
    val debugInfo: String = "",
    val origin: Int = ORIGIN_INVALID,
    val error: Throwable? = null,
    /**
     * In km/h, -1 if limit does not exist
     */
    val speedLimit: Int = -1,
    val roadName: String = "",
    val coords: List<Coord> = ArrayList(),
    val timestamp: Long = 0
) {
    val isEmpty: Boolean
        get() = coords.isEmpty()

    fun initDebugInfo(debuggingEnabled: Boolean): LimitResponse {
        return if (debuggingEnabled) {
            val origin = getLimitProviderString(origin)

            var text = "\nOrigin: $origin\n--From cache: $fromCache"
            text += if (error == null) {
                "\n--Road name: " + roadName +
                    "\n--Coords: " + coords +
                    "\n--Limit: " + speedLimit
            } else {
                "\n--Error: $error"
            }
            copy(debugInfo = debugInfo + text)
        } else {
            this
        }
    }

    companion object {
        const val ORIGIN_INVALID = -1
        const val ORIGIN_OSM = 0

        internal fun getLimitProviderString(origin: Int): String {
            val provider = when (origin) {
                ORIGIN_OSM -> "OSM"
                -1 -> "?"
                else -> origin.toString()
            }
            return provider
        }
    }
}
