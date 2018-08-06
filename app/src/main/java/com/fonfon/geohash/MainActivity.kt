package com.fonfon.geohash

import android.annotation.SuppressLint
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import com.fonfon.geohash.R.id.*
import com.fonfon.kgeohash.GeoHash
import com.fonfon.kgeohash.toGeoHash
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    textEdit.addTextChangedListener(object : TextWatcher {
      override fun afterTextChanged(p0: Editable?) {

      }

      override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
      }

      @SuppressLint("SetTextI18n")
      override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        p0?.let {
          val geoHash = it.toString().toGeoHash()
          textInc.text = "Inc: ${geoHash.inc()}"
          textDec.text = "Dec: ${geoHash.dec()}"
          textN.text = "Northern Neighbour: ${geoHash.northernNeighbour}"
          textS.text = "Southern Neighbour: ${geoHash.southernNeighbour}"
          textW.text = "Western Neighbour: ${geoHash.westernNeighbour}"
          textE.text = "Eastern Neighbour: ${geoHash.easternNeighbour}"
          val childs = geoHash.childs()
          if (childs != null) {
            textChilds.text = "Childs: {"
            childs.forEach {
              textChilds.text = textChilds.text.toString() + " " + it.toString()
            }
            textChilds.text = textChilds.text.toString() + "}"
          } else {
            textChilds.text = "Childs: []"
          }
          val parent = geoHash.parent()
          if(parent != null) {
            textParent.text = "Parent: $parent"
          } else {
            textParent.text = "Parent: -"
          }
          textLocation.text = "Location: ${geoHash.toLocation()}"
        }
      }
    })

    textEdit.setText("v12n8trdh")
  }
}
