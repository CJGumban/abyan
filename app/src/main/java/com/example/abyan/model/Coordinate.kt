package com.example.abyan.model

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
class Coordinate(val lat: Double? = null,
                 val lng: Double? = null) {
    @Exclude
    fun toMap(): Map<String,Any? >{
        return mapOf(
            "lat" to  lat,
            "lng" to lng

        )
    }
}