# Android-Kotlin-Geohash

An implementation of Geohashes in Android.
The produced hashes, when using character precision (multiples of 5 bits) are compatible to the reference implementation geohash.org.

## Creation examples
```kotlin
    val location = Location("geohash");
    location.latitude = 53.2030476;
    location.longitude = 45.0324948;

    val hashFromLaLo = GeoHash(53.2030476, 45.0324948)//"v12n8trdjnvu"

    val hashFromLaLoAndCharSize = GeoHash(53.2030476, 45.0324948, 8)//"v12n8trd"

    val hashFromLocation = GeoHash(location)//"v12n8trdjnvu"

    val hashFromLocationAndCharSize = GeoHash(location, 8)//"v12n8trd"

    val hashFromString = GeoHash("v12n8trd")

    "v12n8trd".toGeoHash()

    location.toGeoHash()//"v12n8trdjnvu"

    location.toGeoHash(8)//"v12n8trd"
```

## metods
```kotlin
    val location = Location("geohash");
    location.latitude = 53.2030476;
    location.longitude = 45.0324948;

    val hash = GeoHash(location, 8)//"v12n8trd"

    //Get base32 string value
    hash.toString()//"v12n8trd"

    //get next incremented geoHash
    hash.inc()//"v12n8trdk"

    //get previous decremented geoHash
    hash.dec()//"v12n8trdh"

    //get next geoHash from step
    hash.next(2) //"v12n8trdm"

    //get adjacent -  N, NE, E, SE, S, SW, W, NW geoHashes
    hash.adjacent //"v12n8trdm", "v12n8trdq", "v12n8trdn", "v12n8tr9y", "v12n8tr9v", "v12n8tr9u", "v12n8trdh", "v12n8trdk"

    //get adjacent boxed - NW, N, NE, W, CENTER, E , SW, S, SE geoHashes
    hash.adjacentBox //"v12n8trdk", "v12n8trdm", "v12n8trdq", "v12n8trdh", "v12n8trdj", "v12n8trdn", "v12n8tr9u", "v12n8tr9v", "v12n8tr9y"

    //get geoHash childs
    hash.childs() //"v12n8trdj0", "v12n8trdj1", "v12n8trdj2", "v12n8trdj3", "v12n8trdj4", "v12n8trdj5", "v12n8trdj6", "v12n8trdj7", "v12n8trdj8", "v12n8trdj9", "v12n8trdjb", "v12n8trdjc", "v12n8trdjd", "v12n8trdje", "v12n8trdjf", "v12n8trdjg", "v12n8trdjh", "v12n8trdjj", "v12n8trdjk", "v12n8trdjm", "v12n8trdjn", "v12n8trdjp", "v12n8trdjq", "v12n8trdjr", "v12n8trdjs", "v12n8trdjt", "v12n8trdju", "v12n8trdjv", "v12n8trdjw", "v12n8trdjx", "v12n8trdjy", "v12n8trdjz"

    //get geoHash parents
    hash.parent() // "v12n8trd"

    hash.northernNeighbour //"v12n8trdm"

    hash.southernNeighbour //"v12n8tr9v"

    hash.easternNeighbour //"v12n8trdn"

    hash.westernNeighbour //"v12n8trdh"
```

