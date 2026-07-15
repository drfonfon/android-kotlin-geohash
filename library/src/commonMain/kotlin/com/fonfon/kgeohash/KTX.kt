package com.fonfon.kgeohash

import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Converts a string to a [GeoHash] object.
 */
fun String.toGeoHash(): GeoHash = GeoHash(this)

/**
 * Converts coordinates to a [GeoHash] object with the specified precision.
 */
fun Coordinate.toGeoHash(charsCount: Int = GeoHash.MAX_CHARACTER_PRECISION): GeoHash =
    GeoHash(this, charsCount)

/**
 * Creates a [Coordinate] from a [Pair] of Doubles.
 */
fun Pair<Double, Double>.toCoordinate(): Coordinate = Coordinate(first, second)

/**
 * Checks if the specified coordinate is inside this [GeoHash].
 */
fun GeoHash.contains(coordinate: Coordinate): Boolean = boundingBox.contains(coordinate)

/**
 * Checks if the specified latitude and longitude are inside this [GeoHash].
 */
fun GeoHash.contains(lat: Double, lon: Double): Boolean = boundingBox.contains(lat, lon)

/**
 * Checks if the specified GeoHash is adjacent to this one.
 */
fun GeoHash.isAdjacentTo(other: GeoHash): Boolean {
    if (this.significantBits != other.significantBits) return false
    return adjacent.contains(other)
}

/**
 * Calculates a [BoundingBox] that encompasses all coordinates in the collection.
 * Returns null if the collection is empty.
 */
fun Iterable<Coordinate>.boundingBox(): BoundingBox? {
    val iterator = iterator()
    if (!iterator.hasNext()) return null

    val first = iterator.next()
    var minLat = first.latitude
    var maxLat = first.latitude
    var minLon = first.longitude
    var maxLon = first.longitude

    while (iterator.hasNext()) {
        val point = iterator.next()
        if (point.latitude < minLat) minLat = point.latitude
        if (point.latitude > maxLat) maxLat = point.latitude
        
        // Simplified approach: does not handle antimeridian crossing for simplicity.
        if (point.longitude < minLon) minLon = point.longitude
        if (point.longitude > maxLon) maxLon = point.longitude
    }

    return BoundingBox(minLat, maxLat, minLon, maxLon)
}

/**
 * Calculates the approximate distance in meters between two coordinates
 * using the Haversine formula.
 */
fun Coordinate.distanceTo(other: Coordinate): Double {
    val r = 6371000.0 // Earth radius in meters
    val dLat = (other.latitude - latitude) * PI / 180.0
    val dLon = (other.longitude - longitude) * PI / 180.0
    val a = sin(dLat / 2).pow(2) +
            cos(latitude * PI / 180.0) * cos(other.latitude * PI / 180.0) *
            sin(dLon / 2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return r * c
}
