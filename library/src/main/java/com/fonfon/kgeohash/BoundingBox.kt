package com.fonfon.kgeohash

import android.location.Location
import android.os.Parcel
import android.os.Parcelable

open class BoundingBox(y1: Double, y2: Double, x1: Double, x2: Double) : Parcelable {

  var minLat: Double = 0.0
  var maxLat: Double = 0.0
  var minLon: Double = 0.0
  var maxLon: Double = 0.0

  val topLeft: Location
    get() = Location(BoundingBox::javaClass.name).apply {
      latitude = maxLat
      longitude = minLon
    }

  val topRight: Location
    get() = Location(BoundingBox::javaClass.name).apply {
      latitude = maxLat
      longitude = maxLon
    }

  val bottomLeft: Location
    get() = Location(BoundingBox::javaClass.name).apply {
      latitude = minLat
      longitude = minLon
    }

  val bottomRight: Location
    get() = Location(BoundingBox::javaClass.name).apply {
      latitude = minLat
      longitude = maxLon
    }

  val center: Location
    get() = Location(BoundingBox::javaClass.name).apply {
      latitude = (minLat + maxLat) / 2
      longitude = (minLon + maxLon) / 2
    }

  val geoHash
    get() = GeoHash(center)

  constructor(p1: Location, p2: Location) : this(p1.latitude, p2.latitude, p1.longitude, p2.longitude)

  constructor(p: Parcel) : this(p.readDouble(), p.readDouble(), p.readDouble(), p.readDouble())

  init {
    minLon = Math.min(x1, x2)
    maxLon = Math.max(x1, x2)
    minLat = Math.min(y1, y2)
    maxLat = Math.max(y1, y2)
  }

  operator fun contains(point: Location) = point.latitude >= minLat && point.longitude >= minLon
      && point.latitude <= maxLat && point.longitude <= maxLon

  fun intersects(other: BoundingBox) = !(other.minLon > maxLon || other.maxLon < minLon
      || other.minLat > maxLat || other.maxLat < minLat)

  override fun toString() = "{topLeft: $topLeft, topRight: $topRight, bottomLeft: $bottomLeft, bottomRight: $bottomRight}"

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as BoundingBox

    if (minLat != other.minLat) return false
    if (maxLat != other.maxLat) return false
    if (minLon != other.minLon) return false
    if (maxLon != other.maxLon) return false

    return true
  }

  override fun hashCode(): Int {
    var result = minLat.hashCode()
    result = 31 * result + maxLat.hashCode()
    result = 31 * result + minLon.hashCode()
    result = 31 * result + maxLon.hashCode()
    return result
  }

  override fun writeToParcel(parcel: Parcel, flags: Int) {
    parcel.writeDouble(minLon)
    parcel.writeDouble(maxLon)
    parcel.writeDouble(minLat)
    parcel.writeDouble(maxLat)
  }

  override fun describeContents() = 0

  companion object CREATOR : Parcelable.Creator<BoundingBox> {
    override fun createFromParcel(parcel: Parcel) = BoundingBox(parcel)
    override fun newArray(size: Int): Array<BoundingBox?> = arrayOfNulls(size)
  }


}