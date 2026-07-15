package com.fonfon.kgeohash

/**
 * Represents a geographic coordinate with latitude and longitude.
 *
 * @property latitude The latitude in degrees.
 * @property longitude The longitude in degrees.
 */
data class Coordinate(
    var latitude: Double = 0.0,
    var longitude: Double = 0.0
)
