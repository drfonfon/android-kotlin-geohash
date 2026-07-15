package com.fonfon.kgeohash

import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreLocation.CLCircularRegion
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationCoordinate2D
import platform.CoreLocation.CLLocationCoordinate2DMake

/**
 * Creates a [GeoHash] from iOS [CLLocationCoordinate2D].
 * @param coordinate iOS coordinate
 * @param charsCount precision in characters
 */
@OptIn(ExperimentalForeignApi::class)
fun GeoHash(
    coordinate: CLLocationCoordinate2D,
    charsCount: Int = GeoHash.MAX_CHARACTER_PRECISION
): GeoHash = GeoHash(coordinate.latitude, coordinate.longitude, charsCount)

/**
 * Converts [GeoHash] bounding box center to iOS [CLLocationCoordinate2D].
 */
@OptIn(ExperimentalForeignApi::class)
fun GeoHash.toCLLocationCoordinate2D(): CLLocationCoordinate2D =
    location.toCLLocationCoordinate2D()

/**
 * Converts [Coordinate] to iOS [CLLocationCoordinate2D].
 */
@OptIn(ExperimentalForeignApi::class)
fun Coordinate.toCLLocationCoordinate2D(): CLLocationCoordinate2D =
    CLLocationCoordinate2DMake(latitude, longitude).useContents { this }

/**
 * Converts [Coordinate] to iOS [CValue] of [CLLocationCoordinate2D].
 */
@OptIn(ExperimentalForeignApi::class)
fun Coordinate.toCLValue(): CValue<CLLocationCoordinate2D> =
    CLLocationCoordinate2DMake(latitude, longitude)

/**
 * Extension to convert iOS [CLLocationCoordinate2D] directly to [GeoHash].
 */
@OptIn(ExperimentalForeignApi::class)
fun CLLocationCoordinate2D.toGeoHash(
    charsCount: Int = GeoHash.MAX_CHARACTER_PRECISION
): GeoHash = GeoHash(this, charsCount)

/**
 * Extension to convert iOS [CLLocation] directly to [GeoHash].
 */
@OptIn(ExperimentalForeignApi::class)
fun CLLocation.toGeoHash(
    charsCount: Int = GeoHash.MAX_CHARACTER_PRECISION
): GeoHash = GeoHash(coordinate.useContents { this }, charsCount)

/**
 * Extension to convert iOS [CLLocation] directly to [Coordinate].
 */
@OptIn(ExperimentalForeignApi::class)
fun CLLocation.toCoordinate(): Coordinate = coordinate.useContents { 
    Coordinate(latitude, longitude)
}

/**
 * Converts [Coordinate] to iOS [CLLocation].
 */
@OptIn(ExperimentalForeignApi::class)
fun Coordinate.toCLLocation(): CLLocation = CLLocation(latitude, longitude)

/**
 * Converts [GeoHash] center to iOS [CLLocation].
 */
fun GeoHash.toCLLocation(): CLLocation = location.toCLLocation()

/**
 * Checks if the [CLLocation] is inside the given [GeoHash].
 */
@OptIn(ExperimentalForeignApi::class)
fun CLLocation.isInside(geoHash: GeoHash): Boolean = coordinate.useContents {
    geoHash.contains(latitude, longitude)
}

/**
 * Checks if the [CLLocation] is inside the given [BoundingBox].
 */
@OptIn(ExperimentalForeignApi::class)
fun CLLocation.isInside(boundingBox: BoundingBox): Boolean = coordinate.useContents {
    boundingBox.contains(latitude, longitude)
}

/**
 * Converts [BoundingBox] to [CLCircularRegion]. 
 * Useful for iOS geofencing.
 * @param identifier unique identifier for the region
 */
@OptIn(ExperimentalForeignApi::class)
fun BoundingBox.toCLCircularRegion(identifier: String): CLCircularRegion {
    val radius = center.distanceTo(topLeft)
    return CLCircularRegion(
        center = center.toCLValue(),
        radius = radius,
        identifier = identifier
    )
}
