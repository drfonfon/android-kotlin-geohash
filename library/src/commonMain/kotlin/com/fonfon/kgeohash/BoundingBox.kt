package com.fonfon.kgeohash

/**
 * Represents a geographic bounding box defined by its latitude and longitude boundaries.
 * Supports areas crossing the antimeridian (180th meridian).
 *
 * @property minLat Minimum latitude (South)
 * @property maxLat Maximum latitude (North)
 * @property minLon Minimum longitude (West or East depending on antimeridian crossing)
 * @property maxLon Maximum longitude (East or West depending on antimeridian crossing)
 */
data class BoundingBox(
    val minLat: Double,
    val maxLat: Double,
    val minLon: Double,
    val maxLon: Double
) {

    constructor(p1: Coordinate, p2: Coordinate) : this(
        if (p1.latitude < p2.latitude) p1.latitude else p2.latitude,
        if (p1.latitude < p2.latitude) p2.latitude else p1.latitude,
        if (p1.longitude < p2.longitude) p1.longitude else p2.longitude,
        if (p1.longitude < p2.longitude) p2.longitude else p1.longitude
    )

    /**
     * Calculates the latitude of the center of the bounding box.
     */
    val centerLat: Double get() = (minLat + maxLat) * 0.5

    /**
     * Calculates the longitude of the center of the bounding box, correctly handling antimeridian crossing.
     */
    val centerLon: Double
        get() = if (minLon <= maxLon) {
            (minLon + maxLon) * 0.5
        } else {
            var center = (minLon + maxLon + 360.0) * 0.5
            if (center > 180.0) center -= 360.0
            center
        }

    /**
     * Latitude span of the bounding box in degrees.
     */
    val latSpan: Double get() = maxLat - minLat

    /**
     * Longitude span of the bounding box in degrees, correctly handling antimeridian crossing.
     */
    val lonSpan: Double get() = if (minLon <= maxLon) maxLon - minLon else 360.0 - (minLon - maxLon)

    /**
     * Top-left coordinate of the bounding box.
     */
    val topLeft: Coordinate get() = Coordinate(maxLat, minLon)

    /**
     * Top-right coordinate of the bounding box.
     */
    val topRight: Coordinate get() = Coordinate(maxLat, maxLon)

    /**
     * Bottom-left coordinate of the bounding box.
     */
    val bottomLeft: Coordinate get() = Coordinate(minLat, minLon)

    /**
     * Bottom-right coordinate of the bounding box.
     */
    val bottomRight: Coordinate get() = Coordinate(minLat, maxLon)

    /**
     * Center coordinate of the bounding box.
     */
    val center: Coordinate get() = Coordinate(centerLat, centerLon)

    /**
     * Returns a [GeoHash] representing the center of this bounding box.
     */
    val geoHash: GeoHash get() = GeoHash(centerLat, centerLon)

    /**
     * Returns true if the bounding box crosses the antimeridian.
     */
    val isCrossAntimeridian: Boolean get() = minLon > maxLon

    /**
     * Checks if the specified coordinates are contained within this bounding box.
     */
    fun contains(lat: Double, lon: Double): Boolean {
        if (lat !in minLat..maxLat) return false
        return if (minLon <= maxLon) {
            lon in minLon..maxLon
        } else {
            lon >= minLon || lon <= maxLon
        }
    }

    /**
     * Checks if the specified [point] is contained within this bounding box.
     */
    fun contains(point: Coordinate): Boolean = contains(point.latitude, point.longitude)

    /**
     * Checks if this bounding box intersects with [other] bounding box.
     */
    fun intersects(other: BoundingBox): Boolean {
        if (minLat > other.maxLat || maxLat < other.minLat) return false

        val thisCross = minLon > maxLon
        val otherCross = other.minLon > other.maxLon

        return when {
            !thisCross && !otherCross -> !(other.minLon > maxLon || other.maxLon < minLon)
            thisCross && !otherCross -> other.maxLon >= minLon || other.minLon <= maxLon
            !thisCross && otherCross -> maxLon >= other.minLon || minLon <= other.maxLon
            else -> true
        }
    }

    /**
     * Returns a new [BoundingBox] that includes the specified coordinates.
     * Handles antimeridian crossing by choosing the shortest expansion path.
     */
    fun expandToInclude(lat: Double, lon: Double): BoundingBox {
        if (contains(lat, lon)) return this

        val newMinLat = if (lat < minLat) lat else minLat
        val newMaxLat = if (lat > maxLat) lat else maxLat

        if (containsLong(lon)) {
            return BoundingBox(newMinLat, newMaxLat, minLon, maxLon)
        }

        val distToMin = distanceSqr(lon, minLon)
        val distToMax = distanceSqr(lon, maxLon)

        return if (distToMin < distToMax) {
            BoundingBox(newMinLat, newMaxLat, lon, maxLon)
        } else {
            BoundingBox(newMinLat, newMaxLat, minLon, lon)
        }
    }

    private fun containsLong(lon: Double): Boolean {
        return if (minLon <= maxLon) {
            lon in minLon..maxLon
        } else {
            lon >= minLon || lon <= maxLon
        }
    }

    private fun distanceSqr(lon1: Double, lon2: Double): Double {
        var diff = lon1 - lon2
        if (diff > 180.0) diff -= 360.0
        if (diff < -180.0) diff += 360.0
        return diff * diff
    }

    /**
     * Returns a new [BoundingBox] that includes the specified [point].
     */
    fun expandToInclude(point: Coordinate): BoundingBox =
        expandToInclude(point.latitude, point.longitude)

    /**
     * Returns a new [BoundingBox] that is the union of this and [other].
     */
    fun union(other: BoundingBox): BoundingBox {
        val newMinLat = if (other.minLat < minLat) other.minLat else minLat
        val newMaxLat = if (other.maxLat > maxLat) other.maxLat else maxLat
        
        var result = BoundingBox(newMinLat, newMaxLat, minLon, maxLon)
        if (!result.containsLong(other.minLon)) {
            result = result.expandToInclude(newMinLat, other.minLon)
        }
        if (!result.containsLong(other.maxLon)) {
            result = result.expandToInclude(newMinLat, other.maxLon)
        }
        return result
    }

    /**
     * Returns the best [GeoHash] precision (number of characters) to represent this bounding box.
     */
    fun bestPrecision(): Int {
        val latS = latSpan
        val lonS = lonSpan
        for (i in 1 until GeoHash.MAX_CHARACTER_PRECISION) {
            val box = GeoHash(centerLat, centerLon, i).boundingBox
            if (box.latSpan < latS || box.lonSpan < lonS) return i - 1
        }
        return GeoHash.MAX_CHARACTER_PRECISION
    }

    companion object {
        /**
         * Creates a [BoundingBox] from the specified latitude and longitude boundaries.
         */
        fun fromCoordinates(lat1: Double, lat2: Double, lon1: Double, lon2: Double): BoundingBox {
            val minLat = if (lat1 < lat2) lat1 else lat2
            val maxLat = if (lat1 < lat2) lat2 else lat1
            val minLon = if (lon1 < lon2) lon1 else lon2
            val maxLon = if (lon1 < lon2) lon2 else lon1
            return BoundingBox(minLat, maxLat, minLon, maxLon)
        }
    }
}
