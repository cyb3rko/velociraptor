package com.pluscubed.velociraptor.api

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import kotlinx.serialization.Serializable
import java.util.Locale

@Serializable
class Coord(
    var lat: Double,
    var lon: Double
) {
    constructor(location: Location): this(location.latitude, location.longitude)

    fun toLatLng(): LatLng {
        return LatLng(lat, lon)
    }

    override fun toString(): String {
        return "(" + String.format(Locale.getDefault(), "%.5f", lat) +
                "," + String.format(Locale.getDefault(), "%.5f", lon) +
                ")"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val coord = other as Coord
        return if (coord.lat.compareTo(lat) != 0) {
            false
        } else {
            coord.lon.compareTo(lon) == 0
        }
    }

    override fun hashCode(): Int {
        var temp = java.lang.Double.doubleToLongBits(lat)
        val result = (temp xor (temp ushr 32)).toInt()
        temp = java.lang.Double.doubleToLongBits(lon)
        return 31 * result + (temp xor (temp ushr 32)).toInt()
    }
}
