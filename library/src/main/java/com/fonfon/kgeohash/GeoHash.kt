package com.fonfon.kgeohash

import android.location.Location
import android.os.Parcel
import android.os.Parcelable
import java.util.HashMap

open class GeoHash : Parcelable {

  private val decodeMap = HashMap<Char, Int>().also {
    base32.toCharArray().forEachIndexed { index, c -> it[c] = index }
  }

  private var bits: Long = 0
  private var significantBits: Byte = 0
  var boundingBox: BoundingBox
  private set

  val northernNeighbour
    get() = GeoHash(
        rightAlignedLatitudeBits.also {
          it[0] = it[0].inc()
          it[0] = maskLastNBits(it[0], it[1])
        },
        rightAlignedLongitudeBits
    )

  val southernNeighbour
    get() = GeoHash(
        rightAlignedLatitudeBits.also {
          it[0] = it[0].dec()
          it[0] = maskLastNBits(it[0], it[1])
        },
        rightAlignedLongitudeBits
    )

  val easternNeighbour
    get() = GeoHash(
        rightAlignedLatitudeBits,
        rightAlignedLongitudeBits.also {
          it[0] = it[0].inc()
          it[0] = maskLastNBits(it[0], it[1])
        }
    )

  val westernNeighbour
    get() = GeoHash(
        rightAlignedLatitudeBits,
        rightAlignedLongitudeBits.also {
          it[0] = it[0].dec()
          it[0] = maskLastNBits(it[0], it[1])
        }
    )

  val adjacent: Array<GeoHash>
    /**
     * @return 8 adjacent {@link GeoHash} for this one. They are in the following order:
     * N, NE, E, SE, S, SW, W, NW
     */
    get() = arrayOf(northernNeighbour,
        northernNeighbour.easternNeighbour,
        easternNeighbour,
        southernNeighbour.easternNeighbour,
        southernNeighbour,
        southernNeighbour.westernNeighbour,
        westernNeighbour,
        northernNeighbour.westernNeighbour
    )

  val adjacentBox: Array<GeoHash>
    /**
     * @return 9 adjacent {@link GeoHash} for this one. They are in the following order:
     * NW, N, NE, W, CENTER, E , SW, S, SE
     */
    get() = arrayOf(
        northernNeighbour.westernNeighbour,
        northernNeighbour,
        northernNeighbour.easternNeighbour,
        westernNeighbour,
        this,
        easternNeighbour,
        southernNeighbour.westernNeighbour,
        southernNeighbour,
        southernNeighbour.easternNeighbour
    )

  private val ord: Long
    get() = bits ushr (MAX_BIT_PRECISION - significantBits)

  private val numberOfLatLonBits
    get() = intArrayOf(significantBits / 2, significantBits / 2 + if (significantBits % 2 == 0) 0 else 1)

  private val rightAlignedLatitudeBits: LongArray
    get() {
      val bitsCopy = bits shl 1
      return longArrayOf(extractEverySecondBit(bitsCopy, numberOfLatLonBits[0]), numberOfLatLonBits[0].toLong())
    }

  private val rightAlignedLongitudeBits: LongArray
    get() {
      val bitsCopy = bits
      return longArrayOf(extractEverySecondBit(bitsCopy, numberOfLatLonBits[1]), numberOfLatLonBits[1].toLong())
    }

  constructor(parcel: Parcel) {
    bits = parcel.readLong()
    significantBits = parcel.readByte()
    boundingBox = parcel.readParcelable(BoundingBox::class.java.classLoader)
  }

  @JvmOverloads
  constructor(location: Location, charsCount: Int = MAX_CHARACTER_PRECISION) :
      this(location.latitude, location.longitude, charsCount)

  @JvmOverloads
  constructor(boundingBox: BoundingBox, charsCount: Int = MAX_CHARACTER_PRECISION) :
      this(boundingBox.center.latitude, boundingBox.center.longitude, charsCount)

  constructor(hash: String) {
    var isEvenBit = true
    val latRange = doubleArrayOf(-LATITUDE_MAX_ABS, LATITUDE_MAX_ABS)
    val lonRange = doubleArrayOf(-LONGITUDE_MAX_ABS, LONGITUDE_MAX_ABS)

    for (i in 0 until hash.length) {
      val cd = decodeMap[hash[i]]
      for (j in 0 until BASE32_BITS) {
        val mask = BITS[j]
        if (isEvenBit) {
          divideRangeDecode(lonRange, (cd!!.and(mask)) != 0)
        } else {
          divideRangeDecode(latRange, cd!!.and(mask) != 0)
        }
        isEvenBit = !isEvenBit
      }
    }
    boundingBox = BoundingBox(
        generateLocation(latRange[0], lonRange[0]),
        generateLocation(latRange[1], lonRange[1])
    )
    bits = bits shl (MAX_BIT_PRECISION - significantBits)
  }

  @JvmOverloads
  constructor(lat: Double, lon: Double, charsCount: Int = MAX_CHARACTER_PRECISION) {
    val desiredPrecision = precisionFromCharCount(charsCount)
    val precision = Math.min(desiredPrecision, MAX_BIT_PRECISION)

    var isEvenBit = true
    val latRange = doubleArrayOf(-LATITUDE_MAX_ABS, LATITUDE_MAX_ABS)
    val lonRange = doubleArrayOf(-LONGITUDE_MAX_ABS, LONGITUDE_MAX_ABS)

    while (significantBits < precision) {
      if (isEvenBit) {
        divideRangeEncode(lon, lonRange)
      } else {
        divideRangeEncode(lat, latRange)
      }
      isEvenBit = !isEvenBit
    }

    boundingBox = BoundingBox(
        generateLocation(latRange[0], lonRange[0]),
        generateLocation(latRange[1], lonRange[1])
    )
    bits = bits shl (MAX_BIT_PRECISION - precision)
  }

  constructor(hashVal: Long, significantBits: Byte) {
    var isEvenBit = true
    val latRange = doubleArrayOf(-LATITUDE_MAX_ABS, LATITUDE_MAX_ABS)
    val lonRange = doubleArrayOf(-LONGITUDE_MAX_ABS, LONGITUDE_MAX_ABS)

    var binStr = java.lang.Long.toBinaryString(hashVal)

    while (binStr.length < MAX_BIT_PRECISION) {
      binStr = "0$binStr"
    }

    for (i in 0 until significantBits) {
      if (isEvenBit) {
        divideRangeDecode(lonRange, binStr[i] != '0')
      } else {
        divideRangeDecode(latRange, binStr[i] != '0')
      }
      isEvenBit = !isEvenBit
    }

    boundingBox = BoundingBox(
        generateLocation(latRange[0], lonRange[0]),
        generateLocation(latRange[1], lonRange[1])
    )
    bits = bits shl (MAX_BIT_PRECISION - this.significantBits)
  }

  private constructor(latBits: LongArray, lonBits: LongArray) {
    latBits[0] = latBits[0] shl (MAX_BIT_PRECISION - latBits[1]).toInt()
    lonBits[0] = lonBits[0] shl (MAX_BIT_PRECISION - lonBits[1]).toInt()

    var isEvenBit = false
    val latRange = doubleArrayOf(-LATITUDE_MAX_ABS, LATITUDE_MAX_ABS)
    val lonRange = doubleArrayOf(-LONGITUDE_MAX_ABS, LONGITUDE_MAX_ABS)

    for (i in 0 until (latBits[1] + lonBits[1])) {
      if (isEvenBit) {
        divideRangeDecode(latRange, (latBits[0] and FIRST_BIT_FLAGGED) == FIRST_BIT_FLAGGED)
        latBits[0] = latBits[0] shl 1
      } else {
        divideRangeDecode(lonRange, (lonBits[0] and FIRST_BIT_FLAGGED) == FIRST_BIT_FLAGGED)
        lonBits[0] = lonBits[0] shl 1
      }
      isEvenBit = !isEvenBit
    }
    bits = bits shl (MAX_BIT_PRECISION - significantBits)
    boundingBox = BoundingBox(
        generateLocation(latRange[0], lonRange[0]),
        generateLocation(latRange[1], lonRange[1])
    )
  }

  fun next(step: Int) = GeoHash((ord + step) shl MAX_BIT_PRECISION - significantBits, significantBits)

  operator fun inc() = next(1)

  operator fun dec() = next(-1)

  /**
   * @return internal GeoHashes
   */
  fun childs(): Array<GeoHash>? {
    checkConvert()
    if (significantBits / BASE32_BITS < MAX_CHARACTER_PRECISION) {
      return Array(base32.length) { i -> GeoHash(toString() + base32[i]) }
    }
    return null
  }

  /**
   * @return external GeoHash
   */
  fun parent(): GeoHash? {
    if (significantBits > 5) {
      val hash = toString()
      return GeoHash(hash.substring(0, hash.length - 1))
    }
    return null
  }

  fun toLocation(): Location = boundingBox.center

  override fun toString(): String {
    checkConvert()
    val buf = StringBuilder()
    val firstBitFlag = -0x800000000000000L
    var bitsCopy = bits
    val partialChunks = Math.ceil((significantBits / BASE32_BITS).toDouble()).toInt()
    for (i in 0 until partialChunks) {
      buf.append(base32[(bitsCopy.and(firstBitFlag)).ushr(59).toInt()])
      bitsCopy = bitsCopy shl BASE32_BITS
    }
    return buf.toString()
  }

  private fun precisionFromCharCount(charsCount: Int): Int {
    if (charsCount > MAX_CHARACTER_PRECISION) {
      throw IllegalArgumentException("A geohash can only be $MAX_CHARACTER_PRECISION character long.")
    }
    return if (charsCount * BASE32_BITS <= MAX_GEO_HASH_BITS_COUNT)
      charsCount * BASE32_BITS
    else
      MAX_GEO_HASH_BITS_COUNT
  }

  private fun checkConvert() {
    if (significantBits % BASE32_BITS != 0) {
      throw IllegalStateException("Cannot convert a geoHash to base32")
    }
  }

  //encode

  private fun divideRangeEncode(value: Double, range: DoubleArray) {
    val mid = (range[0] + range[1]) / 2
    if (value >= mid) {
      addOnBitToEnd()
      range[0] = mid
    } else {
      addOffBitToEnd()
      range[1] = mid
    }
  }

  private fun addOnBitToEnd() {
    significantBits++
    bits = bits shl 1
    bits = bits or 0x1
  }

  private fun addOffBitToEnd() {
    significantBits++
    bits = bits shl 1
  }

  private fun generateLocation(lat: Double, lon: Double) = Location(GeoHash::javaClass.name).also {
    it.latitude = lat
    it.longitude = lon
  }

  //decode

  private fun divideRangeDecode(range: DoubleArray, b: Boolean) {
    val mid = (range[0] + range[1]) / 2
    if (b) {
      addOnBitToEnd()
      range[0] = mid
    } else {
      addOffBitToEnd()
      range[1] = mid
    }
  }

  //near

  private fun extractEverySecondBit(copyOfBits: Long, numberOfBits: Int): Long {
    var bitsCopy = copyOfBits
    var value = 0L
    for (i in 0 until numberOfBits) {
      if ((bitsCopy and FIRST_BIT_FLAGGED) == FIRST_BIT_FLAGGED) {
        value = value or 0x1
      }
      value = value shl 1
      bitsCopy = bitsCopy shl 2
    }
    value = value ushr 1
    return value
  }

  private fun maskLastNBits(value: Long, n: Long): Long {
    var mask = -1L
    mask = mask ushr (MAX_BIT_PRECISION - n).toInt()
    return value and mask
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as GeoHash

    if (bits != other.bits) return false
    if (significantBits != other.significantBits) return false
    if (boundingBox != other.boundingBox) return false

    return true
  }

  override fun hashCode(): Int {
    var result = bits.hashCode()
    result = 31 * result + significantBits
    result = 31 * result + boundingBox.hashCode()
    return result
  }

  override fun writeToParcel(parcel: Parcel, flags: Int) {
    parcel.writeLong(bits)
    parcel.writeByte(significantBits)
    parcel.writeParcelable(boundingBox, flags)
  }

  override fun describeContents() = 0

  companion object CREATOR : Parcelable.Creator<GeoHash> {

    const val base32 = "0123456789bcdefghjkmnpqrstuvwxyz"
    const val BASE32_BITS = 5
    const val MAX_CHARACTER_PRECISION = 12
    const val MAX_GEO_HASH_BITS_COUNT = BASE32_BITS * MAX_CHARACTER_PRECISION

    const val FIRST_BIT_FLAGGED: Long = Long.MIN_VALUE
    const val LATITUDE_MAX_ABS = 90.0
    const val LONGITUDE_MAX_ABS = 180.0

    val MAX_BIT_PRECISION = java.lang.Long.bitCount(Long.MAX_VALUE) + 1// max - 64;
    val BITS = intArrayOf(16, 8, 4, 2, 1)

    override fun createFromParcel(parcel: Parcel) = GeoHash(parcel)

    override fun newArray(size: Int): Array<GeoHash?> = arrayOfNulls(size)
  }


}