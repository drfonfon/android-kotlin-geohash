package com.fonfon.kgeohash

import android.location.Location

fun String.toGeoHash(): GeoHash = GeoHash(this)

fun Location.toGeoHash(charsCount: Int = GeoHash.MAX_CHARACTER_PRECISION): GeoHash = GeoHash(this, charsCount)

