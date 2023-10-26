package com.pluscubed.velociraptor.api

import android.content.Context
import android.location.Location
import com.pluscubed.velociraptor.api.cache.CacheLimitProvider
import com.pluscubed.velociraptor.api.osm.OsmLimitProvider
import com.pluscubed.velociraptor.utils.PrefUtils
import com.pluscubed.velociraptor.utils.Utils
import kotlinx.coroutines.delay
import timber.log.Timber

class LimitFetcher(private val context: Context) {
    private val cacheLimitProvider: CacheLimitProvider = CacheLimitProvider(context)
    private val osmLimitProvider: OsmLimitProvider = OsmLimitProvider(context, cacheLimitProvider)

    private var lastResponse: LimitResponse? = null
    private var lastNetworkResponse: LimitResponse? = null

    suspend fun getSpeedLimit(location: Location): LimitResponse {
        val limitResponses = ArrayList<LimitResponse>()

        val cacheResponses = cacheLimitProvider.getSpeedLimit(location, lastResponse)
        Timber.v(cacheResponses.toString())
        limitResponses.add(cacheResponses[0])

        val networkConnected = Utils.isNetworkConnected(context)

        if (limitResponses.last().speedLimit == -1 && networkConnected) {
            // Delay network query if the last response was received less than 5 seconds ago
            if (lastNetworkResponse != null) {
                val delayMs = 5000 - (System.currentTimeMillis() - lastNetworkResponse!!.timestamp)
                delay(delayMs)
            }
        }

        if (limitResponses.last().speedLimit == -1 && networkConnected) {
            // Try OSM if the cache hits didn't contain a limit BUT were not from OSM
            // i.e. query OSM as it might have the limit
            var cachedOsmWithNoLimit = false
            for (cacheRes in cacheResponses) {
                if (cacheRes.origin == LimitResponse.ORIGIN_OSM) {
                    cachedOsmWithNoLimit = true
                    break
                }
            }
            if (!cachedOsmWithNoLimit) {
                val osmResponse = osmLimitProvider.getSpeedLimit(location, lastResponse)
                limitResponses.add(osmResponse[0])
            }
        }

        //Accumulate debug infos, based on the last response (the one with the speed limit or the last option)
        var finalResponse = if (PrefUtils.isDebuggingEnabled(context)) {
            limitResponses.reduce { acc, next ->
                next.copy(debugInfo = acc.debugInfo + "\n" + next.debugInfo)
            }
        } else {
            limitResponses.last()
        }

        //Record the last response's timestamp and network response
        if (finalResponse.timestamp == 0L) {
            finalResponse = finalResponse.copy(timestamp = System.currentTimeMillis())
        }
        lastResponse = finalResponse
        if (!finalResponse.fromCache) {
            lastNetworkResponse = finalResponse
        }

        return finalResponse
    }
}
