package com.fonfon.kgeohash

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class KTXTest {

    @Test
    fun testStringToGeoHashExtension() {
        val raw = "v12n8trd"
        assertEquals(GeoHash(raw), raw.toGeoHash())
    }

    @Test
    fun testLocationToGeoHashExtension() {
        val coordinate = Coordinate(10.0, 11.0)
        assertEquals(GeoHash(coordinate), coordinate.toGeoHash())
        assertEquals(GeoHash(coordinate, 8), coordinate.toGeoHash(8))
    }

    @Test
    fun testPairToCoordinate() {
        val pair = 10.0 to 11.0
        val coordinate = pair.toCoordinate()
        assertEquals(10.0, coordinate.latitude)
        assertEquals(11.0, coordinate.longitude)
    }

    @Test
    fun testGeoHashContains() {
        val hash = GeoHash("v12n8")
        assertTrue(hash.contains(53.2030, 45.0324))
        assertFalse(hash.contains(0.0, 0.0))
    }

    @Test
    fun testIsAdjacentTo() {
        val hash = GeoHash("v12n8trdj")
        val neighbor = hash.northernNeighbour
        assertTrue(hash.isAdjacentTo(neighbor))
        assertFalse(hash.isAdjacentTo(GeoHash("s6emk")))
    }

    @Test
    fun testIterableBoundingBox() {
        val points = listOf(
            Coordinate(10.0, 10.0),
            Coordinate(20.0, 20.0),
            Coordinate(15.0, 15.0)
        )
        val box = points.boundingBox()
        assertNotNull(box)
        assertEquals(10.0, box.minLat)
        assertEquals(20.0, box.maxLat)
        assertEquals(10.0, box.minLon)
        assertEquals(20.0, box.maxLon)
    }

    @Test
    fun testDistanceTo() {
        val p1 = Coordinate(53.2030, 45.0324)
        val p2 = Coordinate(53.2031, 45.0325)
        val distance = p1.distanceTo(p2)
        assertTrue(distance > 0)
        assertTrue(distance < 100)
    }
}
