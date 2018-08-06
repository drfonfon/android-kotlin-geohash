package com.fonfon.kgeohash

import android.location.Location
import android.os.Bundle
import android.support.test.runner.AndroidJUnit4

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.assertEquals

@RunWith(AndroidJUnit4::class)
class BoundingBoxTest {

  companion object {
    private const val DELTA = 0.00001
  }

  private var locationA = Location(BoundingBoxTest::class.java.name).apply {
    latitude = 10.0
    longitude = 11.0
  }
  private var locationB = Location(BoundingBoxTest::class.java.name).apply {
    latitude = 20.0
    longitude = 21.0
  }
  private var locationC = Location(BoundingBoxTest::class.java.name).apply {
    latitude = 15.0
    longitude = 16.0
  }
  //(20, 11) -- (20, 21)
  private var boxA = BoundingBox(locationA, locationB)
  //(10, 11) -- (10, 21)
  private var boxB = BoundingBox(locationB, locationC)

  private val bundle = Bundle().apply {
    putParcelable("key", boxA)
  }

  @Before
  @Throws(Exception::class)
  fun setUp() {
  }

  @Test
  @Throws(Exception::class)
  fun upperLeftTest() {
    assertEquals(boxA.topLeft.latitude, locationB.latitude, DELTA)
    assertEquals(boxA.topLeft.longitude, locationA.longitude, DELTA)
  }

  @Test
  @Throws(Exception::class)
  fun upperRightTest() {
    assertEquals(boxA.topRight.latitude, locationB.latitude, DELTA)
    assertEquals(boxA.topRight.longitude, locationB.longitude, DELTA)
  }

  @Test
  @Throws(Exception::class)
  fun lowerLeftTest() {
    assertEquals(boxA.bottomLeft.latitude, locationA.latitude, DELTA)
    assertEquals(boxA.bottomLeft.longitude, locationA.longitude, DELTA)
  }

  @Test
  @Throws(Exception::class)
  fun lowerRightTest() {
    assertEquals(boxA.bottomRight.latitude, locationA.latitude, DELTA)
    assertEquals(boxA.bottomRight.longitude, locationB.longitude, DELTA)
  }

  @Test
  @Throws(Exception::class)
  fun equalsTest() {
    val boundingBox = BoundingBox(locationA, locationB)
    assertEquals(boxA == boxA, true)
    assertEquals(boxA == boundingBox, true)
  }

  @Test
  @Throws(Exception::class)
  fun containsTest() {
    assertEquals(boxA.contains(locationC), true)
  }

  @Test
  @Throws(Exception::class)
  fun intersectsTest() {

    val location1 = Location(BoundingBoxTest::class.java.name)
    location1.latitude = 22.0
    location1.longitude = 33.0

    val location2 = Location(BoundingBoxTest::class.java.name)
    location2.latitude = 33.0
    location2.longitude = 44.0

    val box = BoundingBox(location1, location2)

    assertEquals(boxA.intersects(boxB), true)
    assertEquals(boxA.intersects(box), false)
  }

  @Test
  @Throws(Exception::class)
  fun centerTest() {
    assertEquals(boxA.center.latitude, locationC.latitude, DELTA)
    assertEquals(boxA.center.longitude, locationC.longitude, DELTA)
  }

  @Test
  @Throws(Exception::class)
  fun maxLatitudeTest() {
    assertEquals(boxA.maxLat, 20.0, DELTA)
  }

  @Test
  @Throws(Exception::class)
  fun maxLongitudeTest() {
    assertEquals(boxA.maxLon, 21.0, DELTA)
  }

  @Test
  @Throws(Exception::class)
  fun mnLatitudeTest() {
    assertEquals(boxA.minLat, 10.0, DELTA)
  }

  @Test
  @Throws(Exception::class)
  fun minLongitudeTest() {
    assertEquals(boxA.minLon, 11.0, DELTA)
  }

  @Test
  @Throws(Exception::class)
  fun parcelableTest() {
    assertEquals(bundle.getParcelable<BoundingBox>("key"), boxA)
  }

  @Test
  @Throws(Exception::class)
  fun toGeoHashTest() {
    assertEquals(boxA.geoHash, GeoHash("s6emk4dv748t"))
  }

}
