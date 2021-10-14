package com.example.abyan.model

import com.google.android.gms.maps.model.Marker

data class MarkerOnRoute(
    var marker: Marker? = null,
    var coordinate: Coordinate? = null,
    var isActive: Boolean? = false
){
    fun toMap(): Map<String,Any? >{
        return mapOf(
            "marker" to marker,
            "coordinate" to coordinate,
            "isActive" to isActive
        )
    }
}
