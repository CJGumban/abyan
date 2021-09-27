package com.example.abyan.model

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import java.security.Timestamp

@IgnoreExtraProperties
class Coordinate(
    val key: String? = null,
    val email: String? = null,
    val lat: Double? = null,
    val lng: Double? = null,
    val status: String? = null,
    val type: String? = null,
    val dateTime: String? = null
                  ) {
    @Exclude
    fun toMap(): Map<String,Any? >{
        return mapOf(
            "key" to key,
            "email" to email,
            "lat" to lat,
            "lng" to lng,
            "status" to status,
            "type" to type,
            "dateTime" to dateTime



        )
    }
}