package com.fonfon.kgeohash

import android.location.Location
import android.os.Bundle
import android.support.test.runner.AndroidJUnit4
import org.junit.Assert
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KTXTest {

  private var location = Location(BoundingBoxTest::class.java.name).apply {
    latitude = 10.0
    longitude = 11.0
  }

  private val string = "v12n8trd"

  @Before
  @Throws(Exception::class)
  fun setUp() {
  }

  @Test
  @Throws(Exception::class)
  fun stringToGeoHashTest() {
    assertEquals("v12n8trd".toGeoHash(), GeoHash("v12n8trd"))
  }

  @Test
  @Throws(Exception::class)
  fun locationToGeoHashTest() {
    assertEquals(location.toGeoHash(), GeoHash(location))
    assertEquals(location.toGeoHash(8), GeoHash(location, 8))
  }

}