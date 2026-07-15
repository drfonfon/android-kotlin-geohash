package com.fonfon.geohash

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fonfon.kgeohash.toGeoHash

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GeoHashScreen()
                }
            }
        }
    }
}

@Composable
fun GeoHashScreen() {
    var text by remember { mutableStateOf("v12n8trdh") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { newValue ->
                if (newValue.all { it in "0123456789bcdefghjkmnpqrstuvwxyz" } && newValue.length <= 12) {
                    text = newValue
                }
            },
            label = { Text("GeoHash") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        if (text.isNotEmpty()) {
            val geoHash = remember(text) { text.toGeoHash() }

            Text("Inc: ${geoHash.inc()}")
            Text("Dec: ${geoHash.dec()}")
            Text("Northern Neighbour: ${geoHash.northernNeighbour}")
            Text("Southern Neighbour: ${geoHash.southernNeighbour}")
            Text("Western Neighbour: ${geoHash.westernNeighbour}")
            Text("Eastern Neighbour: ${geoHash.easternNeighbour}")

            val childs = geoHash.childs()
            if (childs != null) {
                Text("Childs: { ${childs.joinToString(" ")} }")
            } else {
                Text("Childs: []")
            }

            val parent = geoHash.parent()
            Text("Parent: ${parent ?: "-"}")

            Text("Location: ${geoHash.location}")
        }
    }
}
