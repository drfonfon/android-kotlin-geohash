package com.fonfon.kgeohash

import android.location.Location
import android.os.Bundle
import android.support.test.runner.AndroidJUnit4
import junit.framework.TestCase.*

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GeoHashTest {

  private var location = Location(GeoHashTest::javaClass.name).apply {
    latitude = 53.2030476
    longitude = 45.0324948
  }
  private var testhash = GeoHash(location, 9)

  private val bundle = Bundle().apply {
    putParcelable("key", testhash)
  }

  @Before
  @Throws(Exception::class)
  fun setUp() {
  }

  @Test
  @Throws(Exception::class)
  fun fromCoordinatesTest() {
    assertEquals(GeoHash(53.2030476, 45.0324948).toString(), "v12n8trdjnvu")
    assertEquals(GeoHash(53.2030476, 45.0324948, 9).toString(), "v12n8trdj")
  }

  @Test
  @Throws(Exception::class)
  fun fromLocationTest() {
    assertEquals(GeoHash(location, 9).toString(), "v12n8trdj")
    assertEquals(GeoHash(location, 8).toString(), "v12n8trd")
    assertEquals(GeoHash(location).toString(), "v12n8trdjnvu")
  }

  @Test
  @Throws(Exception::class)
  fun fromBoundingBoxTest() {
    val boundingBox = BoundingBox(53.203011, 53.203182, 45.032616, 45.032272)
    assertEquals(GeoHash(boundingBox, 9).toString(), "v12n8trde")
    assertEquals(GeoHash(boundingBox, 8).toString(), "v12n8trd")
    assertEquals(GeoHash(boundingBox).toString(), "v12n8trdebpb")
  }

  @Test
  @Throws(Exception::class)
  fun fromStringTest() {
    val hash = GeoHash("v12n8")
    assertEquals(hash.toLocation().latitude, 53.19580078, EPS)
    assertEquals(hash.toLocation().longitude, 45.02197266, EPS)
  }

  @Test
  @Throws(Exception::class)
  fun centerTest() {
    assertEquals(testhash.toLocation().latitude, 53.20303202, EPS)
    assertEquals(testhash.toLocation().longitude, 45.03250837, EPS)
  }

  @Test
  @Throws(Exception::class)
  fun incTest() {
    assertEquals(testhash.inc().toString(), "v12n8trdk")
  }

  @Test
  @Throws(Exception::class)
  fun nextTest() {
    assertEquals(testhash.next(2).toString(), "v12n8trdm")
  }

  @Test
  @Throws(Exception::class)
  fun prevTest() {
    assertEquals(testhash.dec().toString(), "v12n8trdh")
  }

  @Test
  @Throws(Exception::class)
  fun ajacentTest() {
    val hashs = arrayOf("v12n8trdm", "v12n8trdq", "v12n8trdn", "v12n8tr9y", "v12n8tr9v", "v12n8tr9u", "v12n8trdh", "v12n8trdk")
    val geoHashs = testhash.adjacent
    for (i in geoHashs.indices) {
      assertEquals(geoHashs[i].toString(), hashs[i])
    }
  }

  @Test
  @Throws(Exception::class)
  fun ajacentBoxTest() {
    val hashs = arrayOf("v12n8trdk", "v12n8trdm", "v12n8trdq", "v12n8trdh", "v12n8trdj", "v12n8trdn", "v12n8tr9u", "v12n8tr9v", "v12n8tr9y")
    val geoHashs = testhash.adjacentBox
    for (i in geoHashs.indices) {
      assertEquals(geoHashs[i].toString(), hashs[i])
    }
  }

  @Test
  @Throws(Exception::class)
  fun childHashTests() {
    val hashs = arrayOf("v12n8trdj0", "v12n8trdj1", "v12n8trdj2", "v12n8trdj3", "v12n8trdj4", "v12n8trdj5", "v12n8trdj6", "v12n8trdj7", "v12n8trdj8", "v12n8trdj9", "v12n8trdjb", "v12n8trdjc", "v12n8trdjd", "v12n8trdje", "v12n8trdjf", "v12n8trdjg", "v12n8trdjh", "v12n8trdjj", "v12n8trdjk", "v12n8trdjm", "v12n8trdjn", "v12n8trdjp", "v12n8trdjq", "v12n8trdjr", "v12n8trdjs", "v12n8trdjt", "v12n8trdju", "v12n8trdjv", "v12n8trdjw", "v12n8trdjx", "v12n8trdjy", "v12n8trdjz")
    val geoHashs = testhash.childs()
    assertNotNull(geoHashs)
    for (i in geoHashs!!.indices) {
      assertEquals(geoHashs[i].toString(), hashs[i])
    }

    val hash = GeoHash(location)
    assertNull(hash.childs())
  }

  @Test
  @Throws(Exception::class)
  fun parentHashTests() {
    assertNotNull(testhash.parent())
    assertEquals(testhash.parent()!!.toString(), "v12n8trd")

    val hash = GeoHash(location, 1)
    assertNull(hash.parent())
  }

  @Test
  @Throws(Exception::class)
  fun northernNeighbourTest() {
    assertEquals(testhash.northernNeighbour.toString(), "v12n8trdm")
  }

  @Test
  @Throws(Exception::class)
  fun southernNeighbourTest() {
    assertEquals(testhash.southernNeighbour.toString(), "v12n8tr9v")
  }

  @Test
  @Throws(Exception::class)
  fun easternNeighbourTest() {
    assertEquals(testhash.easternNeighbour.toString(), "v12n8trdn")
  }

  @Test
  @Throws(Exception::class)
  fun westernNeighbourTest() {
    assertEquals(testhash.westernNeighbour.toString(), "v12n8trdh")
  }

  @Test
  @Throws(Exception::class)
  fun toStringTest() {
    assertEquals(testhash.toString(), "v12n8trdj")
  }

  @Test
  @Throws(Exception::class)
  fun equalsTest() {
    val hash = GeoHash(location, 9)
    val hash1 = GeoHash("v12n8trdj")
    assertEquals(testhash == testhash, true)
    assertEquals(testhash == hash, true)
    assertEquals(testhash == hash1, true)
  }

  @Test
  @Throws(Exception::class)
  fun parcelableTest() {
    assertEquals(bundle.getParcelable<GeoHash>("key"), GeoHash("v12n8trdj"))
  }

  companion object {
    private val EPS = 0.00000001
  }

}
