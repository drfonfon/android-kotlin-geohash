package com.fonfon.kgeohash

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class GeoHashTest {

    companion object {
        private const val EPS = 0.00000001
        private const val LAT = 53.2030476
        private const val LON = 45.0324948
    }

    private val coordinate = Coordinate(LAT, LON)
    private val testHash = GeoHash(coordinate, 9)

    @Test
    fun testCreationFromCoordinates() {
        assertEquals("v12n8trdjnvu", GeoHash(LAT, LON).toString())
        assertEquals("v12n8trdj", GeoHash(LAT, LON, 9).toString())
    }

    @Test
    fun testCreationFromLocation() {
        assertEquals("v12n8trdj", GeoHash(coordinate, 9).toString())
        assertEquals("v12n8trd", GeoHash(coordinate, 8).toString())
        assertEquals("v12n8trdjnvu", GeoHash(coordinate).toString())
    }

    @Test
    fun testCreationFromBoundingBox() {
        val boundingBox = BoundingBox.fromCoordinates(53.203011, 53.203182, 45.032616, 45.032272)
        assertEquals("v12n8trde", GeoHash(boundingBox, 9).toString())
        assertEquals("v12n8trd", GeoHash(boundingBox, 8).toString())
        assertEquals("v12n8trdebpb", GeoHash(boundingBox).toString())
    }

    @Test
    fun testCreationFromString() {
        val hash = GeoHash("v12n8")
        assertEquals(53.19580078, hash.location.latitude, EPS)
        assertEquals(45.02197266, hash.location.longitude, EPS)
    }

    @Test
    fun testIncrementAndDecrement() {
        assertEquals("v12n8trdk", testHash.inc().toString())
        assertEquals("v12n8trdh", testHash.dec().toString())
        assertEquals("v12n8trdm", testHash.next(2).toString())
    }

    @Test
    fun testAdjacentHashes() {
        val expected = listOf(
            "v12n8trdm", "v12n8trdq", "v12n8trdn", "v12n8tr9y", 
            "v12n8tr9v", "v12n8tr9u", "v12n8trdh", "v12n8trdk"
        )
        val actual = testHash.adjacent.map { it.toString() }
        assertEquals(expected, actual)
    }

    @Test
    fun testAdjacentBox() {
        val expected = listOf(
            "v12n8trdk", "v12n8trdm", "v12n8trdq", "v12n8trdh", 
            "v12n8trdj", "v12n8trdn", "v12n8tr9u", "v12n8tr9v", "v12n8tr9y"
        )
        val actual = testHash.adjacentBox.map { it.toString() }
        assertEquals(expected, actual)
    }

    @Test
    fun testChildHashes() {
        val children = testHash.childs()
        assertNotNull(children)
        assertEquals(32, children.size)
        assertEquals("v12n8trdj0", children.first().toString())
        assertEquals("v12n8trdjz", children.last().toString())

        val maxPrecisionHash = GeoHash(coordinate, 12)
        assertNull(maxPrecisionHash.childs())
    }

    @Test
    fun testParentHash() {
        assertEquals("v12n8trd", testHash.parent()?.toString())
        assertNull(GeoHash(coordinate, 1).parent())
    }

    @Test
    fun testNeighborsIndividually() {
        assertEquals("v12n8trdm", testHash.northernNeighbour.toString())
        assertEquals("v12n8tr9v", testHash.southernNeighbour.toString())
        assertEquals("v12n8trdn", testHash.easternNeighbour.toString())
        assertEquals("v12n8trdh", testHash.westernNeighbour.toString())
        
        // Test new neighbor(Direction) API
        assertEquals("v12n8trdm", testHash.neighbor(Direction.NORTH).toString())
        assertEquals("v12n8tr9v", testHash.neighbor(Direction.SOUTH).toString())
        assertEquals("v12n8trdn", testHash.neighbor(Direction.EAST).toString())
        assertEquals("v12n8trdh", testHash.neighbor(Direction.WEST).toString())
        
        // Test diagonal neighbors
        assertEquals("v12n8trdq", testHash.neighbor(Direction.NORTH_EAST).toString())
        assertEquals("v12n8trdk", testHash.neighbor(Direction.NORTH_WEST).toString())
        assertEquals("v12n8tr9y", testHash.neighbor(Direction.SOUTH_EAST).toString())
        assertEquals("v12n8tr9u", testHash.neighbor(Direction.SOUTH_WEST).toString())
    }

    @Test
    fun testEquality() {
        val hash1 = GeoHash(coordinate, 9)
        val hash2 = GeoHash("v12n8trdj")
        assertEquals(testHash, testHash)
        assertEquals(testHash, hash1)
        assertEquals(testHash, hash2)
        assertEquals(testHash.hashCode(), hash2.hashCode())
    }

    @Test
    fun testInvalidCharacter() {
        assertFailsWith<IllegalArgumentException> {
            GeoHash("v12n8trda") // 'a' is not in base32
        }
    }

    @Test
    fun testToStringWithInvalidBits() {
        // Create a hash with bits not multiple of 5
        val hash = GeoHash(12345L, 7)
        assertEquals("", hash.toString())
    }
}
