package com.fonfon.kgeohash

import kotlin.math.min

/**
 * Represents a GeoHash string in a highly optimized way using bit manipulation.
 *
 * @property bits The bit-representation of the GeoHash, right-aligned.
 * @property significantBits The number of significant bits in the GeoHash.
 */
data class GeoHash(
    val bits: Long,
    val significantBits: Int
) {

    /**
     * Creates a [GeoHash] from coordinates with the specified precision.
     */
    constructor(lat: Double, lon: Double, charsCount: Int = MAX_CHARACTER_PRECISION) :
            this(fromCoordinates(lat, lon, charsCount))

    /**
     * Creates a [GeoHash] from a [Coordinate] with the specified precision.
     */
    constructor(coordinate: Coordinate, charsCount: Int = MAX_CHARACTER_PRECISION) :
            this(coordinate.latitude, coordinate.longitude, charsCount)

    /**
     * Creates a [GeoHash] from a [BoundingBox] with the specified precision.
     */
    constructor(boundingBox: BoundingBox, charsCount: Int = MAX_CHARACTER_PRECISION) :
            this(boundingBox.center.latitude, boundingBox.center.longitude, charsCount)

    /**
     * Creates a [GeoHash] from its string representation.
     */
    constructor(hash: String) : this(fromHashString(hash))

    private constructor(other: GeoHash) : this(other.bits, other.significantBits)

    /**
     * Returns the [BoundingBox] corresponding to this GeoHash.
     */
    val boundingBox: BoundingBox by lazy(LazyThreadSafetyMode.NONE) {
        calculateBoundingBox(bits, significantBits)
    }

    private val stringValue: String by lazy(LazyThreadSafetyMode.NONE) {
        generateString()
    }

    /**
     * Returns the northern neighbor of this GeoHash.
     */
    val northernNeighbour: GeoHash get() = neighbor(Direction.NORTH)

    /**
     * Returns the southern neighbor of this GeoHash.
     */
    val southernNeighbour: GeoHash get() = neighbor(Direction.SOUTH)

    /**
     * Returns the eastern neighbor of this GeoHash.
     */
    val easternNeighbour: GeoHash get() = neighbor(Direction.EAST)

    /**
     * Returns the western neighbor of this GeoHash.
     */
    val westernNeighbour: GeoHash get() = neighbor(Direction.WEST)

    /**
     * Returns the center coordinate of this GeoHash.
     */
    val location: Coordinate get() = boundingBox.center

    /**
     * Returns the neighbor of this GeoHash in the specified [direction].
     */
    fun neighbor(direction: Direction): GeoHash {
        val latCount = significantBits / 2
        val lonCount = significantBits - latCount
        val latBits = rightAlignedLatitudeBits
        val lonBits = rightAlignedLongitudeBits

        return when (direction) {
            Direction.NORTH -> fromBits(
                maskLastNBits(latBits + 1, latCount), latCount,
                lonBits, lonCount
            )
            Direction.SOUTH -> fromBits(
                maskLastNBits(latBits - 1, latCount), latCount,
                lonBits, lonCount
            )
            Direction.EAST -> fromBits(
                latBits, latCount,
                maskLastNBits(lonBits + 1, lonCount), lonCount
            )
            Direction.WEST -> fromBits(
                latBits, latCount,
                maskLastNBits(lonBits - 1, lonCount), lonCount
            )
            Direction.NORTH_EAST -> fromBits(
                maskLastNBits(latBits + 1, latCount), latCount,
                maskLastNBits(lonBits + 1, lonCount), lonCount
            )
            Direction.NORTH_WEST -> fromBits(
                maskLastNBits(latBits + 1, latCount), latCount,
                maskLastNBits(lonBits - 1, lonCount), lonCount
            )
            Direction.SOUTH_EAST -> fromBits(
                maskLastNBits(latBits - 1, latCount), latCount,
                maskLastNBits(lonBits + 1, lonCount), lonCount
            )
            Direction.SOUTH_WEST -> fromBits(
                maskLastNBits(latBits - 1, latCount), latCount,
                maskLastNBits(lonBits - 1, lonCount), lonCount
            )
        }
    }

    /**
     * Returns a list of all 8 adjacent GeoHashes.
     */
    val adjacent: List<GeoHash> by lazy(LazyThreadSafetyMode.NONE) {
        listOf(
            neighbor(Direction.NORTH),
            neighbor(Direction.NORTH_EAST),
            neighbor(Direction.EAST),
            neighbor(Direction.SOUTH_EAST),
            neighbor(Direction.SOUTH),
            neighbor(Direction.SOUTH_WEST),
            neighbor(Direction.WEST),
            neighbor(Direction.NORTH_WEST)
        )
    }

    /**
     * Returns a list including this GeoHash and all 8 adjacent GeoHashes.
     */
    val adjacentBox: List<GeoHash> by lazy(LazyThreadSafetyMode.NONE) {
        listOf(
            neighbor(Direction.NORTH_WEST),
            neighbor(Direction.NORTH),
            neighbor(Direction.NORTH_EAST),
            neighbor(Direction.WEST),
            this,
            neighbor(Direction.EAST),
            neighbor(Direction.SOUTH_WEST),
            neighbor(Direction.SOUTH),
            neighbor(Direction.SOUTH_EAST)
        )
    }

    private val rightAlignedLatitudeBits: Long
        get() = if (significantBits % 2 == 0) compact(bits) else compact(bits ushr 1)

    private val rightAlignedLongitudeBits: Long
        get() = if (significantBits % 2 == 0) compact(bits ushr 1) else compact(bits)

    /**
     * Increments/decrements the raw bits of the GeoHash by [step].
     */
    fun next(step: Int): GeoHash {
        return GeoHash(maskLastNBits(bits + step, significantBits), significantBits)
    }

    operator fun inc(): GeoHash = next(1)

    operator fun dec(): GeoHash = next(-1)

    /**
     * Returns all 32 child GeoHashes of this one, or null if [significantBits] is not divisible by 5.
     */
    fun childs(): List<GeoHash>? {
        if (significantBits % BASE32_BITS != 0) return null
        if (significantBits < MAX_GEO_HASH_BITS_COUNT) {
            val newSigBits = significantBits + BASE32_BITS
            val baseBits = bits shl BASE32_BITS
            return List(32) { i -> GeoHash(baseBits or i.toLong(), newSigBits) }
        }
        return null
    }

    /**
     * Returns the parent GeoHash, or null if the current hash is too short.
     */
    fun parent(): GeoHash? {
        if (significantBits > BASE32_BITS) {
            return GeoHash(bits ushr BASE32_BITS, significantBits - BASE32_BITS)
        }
        return null
    }

    /**
     * Returns the string representation of the GeoHash.
     */
    override fun toString(): String = stringValue

    private fun generateString(): String {
        if (significantBits % BASE32_BITS != 0) return ""
        val partialChunks = significantBits / BASE32_BITS
        val buf = CharArray(partialChunks)
        var bitsCopy = bits shl (MAX_BIT_PRECISION - significantBits)
        for (i in 0 until partialChunks) {
            val index = (bitsCopy ushr (MAX_BIT_PRECISION - BASE32_BITS)).toInt() and 31
            buf[i] = BASE32[index]
            bitsCopy = bitsCopy shl BASE32_BITS
        }
        return buf.concatToString()
    }

    companion object {
        const val BASE32 = "0123456789bcdefghjkmnpqrstuvwxyz"
        private val DECODE_TABLE = ByteArray(128) { -1 }.apply {
            BASE32.forEachIndexed { index, char ->
                this[char.code] = index.toByte()
                if (char.isLetter()) {
                    this[char.uppercaseChar().code] = index.toByte()
                }
            }
        }

        const val BASE32_BITS = 5
        const val MAX_CHARACTER_PRECISION = 12
        const val MAX_GEO_HASH_BITS_COUNT = BASE32_BITS * MAX_CHARACTER_PRECISION

        const val LATITUDE_MAX_ABS = 90.0
        const val LONGITUDE_MAX_ABS = 180.0
        private const val LAT_RANGE_INV = 1.0 / (LATITUDE_MAX_ABS * 2)
        private const val LON_RANGE_INV = 1.0 / (LONGITUDE_MAX_ABS * 2)
        const val MAX_BIT_PRECISION = 64

        private val MASKS = LongArray(65) { if (it == 64) -1L else (1L shl it) - 1 }
        private val LAT_STEPS = DoubleArray(33) { (LATITUDE_MAX_ABS * 2) / (1L shl it).toDouble() }
        private val LON_STEPS = DoubleArray(33) { (LONGITUDE_MAX_ABS * 2) / (1L shl it).toDouble() }
        private val POW2 = LongArray(64) { 1L shl it }

        private fun spread(x: Long): Long {
            var v = x
            v = (v or (v shl 16)) and 0x0000FFFF0000FFFFL
            v = (v or (v shl 8))  and 0x00FF00FF00FF00FFL
            v = (v or (v shl 4))  and 0x0F0F0F0F0F0F0F0FL
            v = (v or (v shl 2))  and 0x3333333333333333L
            v = (v or (v shl 1))  and 0x5555555555555555L
            return v
        }

        private fun compact(x: Long): Long {
            var v = x and 0x5555555555555555L
            v = (v or (v ushr 1)) and 0x3333333333333333L
            v = (v or (v ushr 2)) and 0x0F0F0F0F0F0F0F0FL
            v = (v or (v ushr 4)) and 0x00FF00FF00FF00FFL
            v = (v or (v ushr 8)) and 0x0000FFFF0000FFFFL
            v = (v or (v ushr 16)) and 0x00000000FFFFFFFFL
            return v
        }

        private fun maskLastNBits(value: Long, n: Int): Long {
            if (n <= 0) return 0L
            if (n >= 64) return value
            return value and MASKS[n]
        }

        private fun fromCoordinates(lat: Double, lon: Double, charsCount: Int): GeoHash {
            val precision = min(charsCount * BASE32_BITS, MAX_GEO_HASH_BITS_COUNT)
            val latCount = precision / 2
            val lonCount = precision - latCount

            val latVal = (((lat + LATITUDE_MAX_ABS) * LAT_RANGE_INV) * POW2[latCount]).toLong()
                .coerceIn(0L, POW2[latCount] - 1)
            val lonVal = (((lon + LONGITUDE_MAX_ABS) * LON_RANGE_INV) * POW2[lonCount]).toLong()
                .coerceIn(0L, POW2[lonCount] - 1)

            return fromBits(latVal, latCount, lonVal, lonCount)
        }

        private fun fromHashString(hash: String): GeoHash {
            var bits = 0L
            for (element in hash) {
                val code = element.code
                val cd = if (code < 128) DECODE_TABLE[code] else -1
                if (cd < 0) throw IllegalArgumentException("Invalid character: $element")
                bits = (bits shl BASE32_BITS) or cd.toLong()
            }
            return GeoHash(bits, hash.length * BASE32_BITS)
        }

        private fun fromBits(latVal: Long, latCount: Int, lonVal: Long, lonCount: Int): GeoHash {
            val totalBits = latCount + lonCount
            val interleaved = if (totalBits % 2 == 0) {
                spread(latVal) or (spread(lonVal) shl 1)
            } else {
                spread(lonVal) or (spread(latVal) shl 1)
            }
            return GeoHash(interleaved, totalBits)
        }

        private fun calculateBoundingBox(bits: Long, significantBits: Int): BoundingBox {
            val latCount = significantBits / 2
            val lonCount = significantBits - latCount

            val latVal = if (significantBits % 2 == 0) compact(bits) else compact(bits ushr 1)
            val lonVal = if (significantBits % 2 == 0) compact(bits ushr 1) else compact(bits)

            val latStep = LAT_STEPS[latCount]
            val lonStep = LON_STEPS[lonCount]

            val minLat = -LATITUDE_MAX_ABS + latVal * latStep
            val maxLat = -LATITUDE_MAX_ABS + (latVal + 1) * latStep
            val minLon = -LONGITUDE_MAX_ABS + lonVal * lonStep
            val maxLon = -LONGITUDE_MAX_ABS + (lonVal + 1) * lonStep

            return BoundingBox(minLat, maxLat, minLon, maxLon)
        }
    }
}
