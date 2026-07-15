package com.fonfon.kgeohash

import android.graphics.RectF
import android.os.Bundle
import android.location.Location as AndroidLocation

/**
 * Converts [AndroidLocation] to a [GeoHash] object.
 */
fun AndroidLocation.toGeoHash(charsCount: Int = GeoHash.MAX_CHARACTER_PRECISION): GeoHash {
    return GeoHash(latitude, longitude, charsCount)
}

/**
 * Converts [AndroidLocation] to a [Coordinate] object.
 */
fun AndroidLocation.toCoordinate(): Coordinate {
    return Coordinate(latitude, longitude)
}

/**
 * Creates an [AndroidLocation] from a [GeoHash].
 */
fun GeoHash.toAndroidLocation(): AndroidLocation {
    return AndroidLocation("GeoHash").apply {
        latitude = this@toAndroidLocation.location.latitude
        longitude = this@toAndroidLocation.location.longitude
    }
}

/**
 * Creates an [AndroidLocation] from a [Coordinate].
 */
fun Coordinate.toAndroidLocation(): AndroidLocation {
    return AndroidLocation("GeoHash").apply {
        latitude = this@toAndroidLocation.latitude
        longitude = this@toAndroidLocation.longitude
    }
}

/**
 * Converts [BoundingBox] to a standard [RectF].
 * Note: RectF does not support antimeridian crossing (values will be incorrect in such cases).
 */
fun BoundingBox.toRectF(): RectF {
    return RectF(minLon.toFloat(), maxLat.toFloat(), maxLon.toFloat(), minLat.toFloat())
}

/**
 * Checks if the current [AndroidLocation] is inside the specified [GeoHash].
 */
fun AndroidLocation.isInside(geoHash: GeoHash): Boolean {
    return geoHash.contains(latitude, longitude)
}

/**
 * Checks if the current [AndroidLocation] is inside the specified [BoundingBox].
 */
fun AndroidLocation.isInside(boundingBox: BoundingBox): Boolean {
    return boundingBox.contains(latitude, longitude)
}

/**
 * Stores a [GeoHash] in a [Bundle] as a string.
 */
fun Bundle.putGeoHash(key: String, geoHash: GeoHash?) {
    putString(key, geoHash?.toString())
}

/**
 * Retrieves a [GeoHash] from a [Bundle].
 */
fun Bundle.getGeoHash(key: String): GeoHash? {
    return getString(key)?.toGeoHash()
}

/**
 * Stores a [BoundingBox] in a [Bundle] as a DoubleArray.
 */
fun Bundle.putBoundingBox(key: String, box: BoundingBox?) {
    if (box != null) {
        putDoubleArray(key, doubleArrayOf(box.minLat, box.maxLat, box.minLon, box.maxLon))
    } else {
        putDoubleArray(key, null)
    }
}

/**
 * Retrieves a [BoundingBox] from a [Bundle].
 */
fun Bundle.getBoundingBox(key: String): BoundingBox? {
    val arr = getDoubleArray(key) ?: return null
    if (arr.size != 4) return null
    return BoundingBox(arr[0], arr[1], arr[2], arr[3])
}
