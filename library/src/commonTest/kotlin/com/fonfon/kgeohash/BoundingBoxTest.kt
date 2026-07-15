package com.fonfon.kgeohash

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class BoundingBoxTest {

    companion object {
        private const val DELTA = 0.00001
    }

    private fun createLocation(lat: Double, lon: Double) = Coordinate(lat, lon)

    private val locationA = createLocation(10.0, 11.0)
    private val locationB = createLocation(20.0, 21.0)
    private val locationC = createLocation(15.0, 16.0)

    private val boxA = BoundingBox(locationA, locationB)
    private val boxB = BoundingBox(locationB, locationC)

    @Test
    fun testCornersCalculation() {
        assertEquals(20.0, boxA.maxLat, DELTA)
        assertEquals(10.0, boxA.minLat, DELTA)
        assertEquals(21.0, boxA.maxLon, DELTA)
        assertEquals(11.0, boxA.minLon, DELTA)

        assertEquals(20.0, boxA.topLeft.latitude, DELTA)
        assertEquals(11.0, boxA.topLeft.longitude, DELTA)

        assertEquals(20.0, boxA.topRight.latitude, DELTA)
        assertEquals(21.0, boxA.topRight.longitude, DELTA)

        assertEquals(10.0, boxA.bottomLeft.latitude, DELTA)
        assertEquals(11.0, boxA.bottomLeft.longitude, DELTA)

        assertEquals(10.0, boxA.bottomRight.latitude, DELTA)
        assertEquals(21.0, boxA.bottomRight.longitude, DELTA)
    }

    @Test
    fun testEqualityAndHashcode() {
        val boundingBox = BoundingBox(locationA, locationB)
        assertEquals(boxA, boxA)
        assertEquals(boxA, boundingBox)
        assertEquals(boxA.hashCode(), boundingBox.hashCode())
        assertNotEquals(boxA, boxB)
    }

    @Test
    fun testPointContainment() {
        assertTrue(boxA.contains(locationC))
        assertTrue(boxA.contains(locationA))
        assertTrue(boxA.contains(locationB))
        assertFalse(boxA.contains(createLocation(25.0, 25.0)))
    }

    @Test
    fun testBoxIntersection() {
        val boxOutside = BoundingBox.fromCoordinates(22.0, 33.0, 33.0, 44.0)

        assertTrue(boxA.intersects(boxB))
        assertTrue(boxB.intersects(boxA))
        assertFalse(boxA.intersects(boxOutside))
    }

    @Test
    fun testCenterCalculation() {
        assertEquals(15.0, boxA.center.latitude, DELTA)
        assertEquals(16.0, boxA.center.longitude, DELTA)
    }

    @Test
    fun testGeoHashConversion() {
        assertEquals(GeoHash("s6emk4dv748t"), boxA.geoHash)
    }

    @Test
    fun testFromCoordinatesStaticFactory() {
        val box = BoundingBox.fromCoordinates(10.0, 20.0, 11.0, 21.0)
        assertEquals(boxA, box)
        
        // Test mixed order
        val boxMixed = BoundingBox.fromCoordinates(20.0, 10.0, 21.0, 11.0)
        assertEquals(boxA, boxMixed)
    }

    @Test
    fun testAntimeridian() {
        val box = BoundingBox(10.0, 20.0, 170.0, -170.0)
        assertTrue(box.isCrossAntimeridian)
        assertEquals(180.0, box.centerLon, DELTA)

        assertTrue(box.contains(15.0, 175.0))
        assertTrue(box.contains(15.0, -175.0))
        assertFalse(box.contains(15.0, 0.0))
        
        assertEquals(20.0, box.lonSpan, DELTA)
    }

    @Test
    fun testExpandToInclude() {
        var box = BoundingBox(10.0, 20.0, 10.0, 20.0)
        box = box.expandToInclude(25.0, 25.0)
        assertEquals(25.0, box.maxLat, DELTA)
        assertEquals(25.0, box.maxLon, DELTA)

        // Antimeridian expansion
        var antiBox = BoundingBox(10.0, 20.0, 170.0, 175.0)
        antiBox = antiBox.expandToInclude(15.0, -175.0)
        assertTrue(antiBox.isCrossAntimeridian)
        assertEquals(170.0, antiBox.minLon, DELTA)
        assertEquals(-175.0, antiBox.maxLon, DELTA)
    }

    @Test
    fun testUnion() {
        val box1 = BoundingBox(10.0, 20.0, 10.0, 20.0)
        val box2 = BoundingBox(15.0, 25.0, 15.0, 25.0)
        val union = box1.union(box2)
        assertEquals(10.0, union.minLat, DELTA)
        assertEquals(25.0, union.maxLat, DELTA)
        assertEquals(10.0, union.minLon, DELTA)
        assertEquals(25.0, union.maxLon, DELTA)
    }

    @Test
    fun testBestPrecision() {
        val box = BoundingBox(53.2030, 53.2031, 45.0324, 45.0325)
        val precision = box.bestPrecision()
        assertTrue(precision >= 8)
    }
}
