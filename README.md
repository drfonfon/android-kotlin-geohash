# Kotlin Geohash Multiplatform

[![Kotlin](https://img.shields.io/badge/kotlin-2.4.10-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![Platform](https://img.shields.io/badge/platform-Android%20%7C%20iOS%20%7C%20JVM-orange.svg)](#)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)

A high-performance Geohash implementation for Kotlin Multiplatform, supporting **Android**, **JVM**, and **iOS**. 
The library leverages bitwise operations for maximum speed and minimal memory footprint.

## Features

- **Multiplatform support**: Works on Android, JVM, and iOS.
- **Highly Optimized**: Bit-manipulation based implementation.
- **Precision control**: Support for both character-based (multiples of 5 bits) and arbitrary bit-based precision.
- **Navigation**: Easily find neighbors (N, S, E, W) and adjacent cells.
- **Hierarchy**: Support for finding parent and children hashes.
- **iOS Integration**: Specialized extensions for `CoreLocation` framework.

## Requirements

- **Kotlin**: 2.4.10+
- **Java**: 21
- **Android SDK**: minSdk 23, targetSdk 37
- **AGP**: 9.3.0+

## Usage

### Creating a GeoHash

```kotlin
import com.fonfon.kgeohash.GeoHash

// Create from coordinates
val hash = GeoHash(53.2030476, 45.0324948) // "v12n8trdjnvu"

// Create with specific precision (8 characters)
val hash8 = GeoHash(53.2030476, 45.0324948, 8) // "v12n8trd"

// Create from string
val hashFromString = GeoHash("v12n8trd")
```

### Core Methods

```kotlin
val hash = GeoHash("v12n8trd")

// Get base32 string value
hash.toString() // "v12n8trd"

// Navigation
val north = hash.northernNeighbour
val south = hash.southernNeighbour
val east = hash.easternNeighbour
val west = hash.westernNeighbour

// Neighbors list
val adjacent = hash.adjacent // List of 8 neighbors
val box = hash.adjacentBox // List of 8 neighbors + self (center)

// Hierarchy
val parent = hash.parent() // "v12n8tr"
val children = hash.childs() // List of 32 sub-hashes
```

### iOS Extensions

The library provides seamless integration with the iOS `CoreLocation` framework:

```kotlin
import com.fonfon.kgeohash.toGeoHash
import com.fonfon.kgeohash.toCLLocationCoordinate2D
import platform.CoreLocation.CLLocationCoordinate2D

// From CoreLocation to GeoHash
val iosCoord: CLLocationCoordinate2D = ... 
val hash = iosCoord.toGeoHash()

// From GeoHash to CoreLocation
val backToIos = hash.toCLLocationCoordinate2D()
```

## License

This project is licensed under the Apache License 2.0.
