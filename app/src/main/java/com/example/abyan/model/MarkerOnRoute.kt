package com.example.abyan.model

import com.google.android.gms.maps.model.Marker

data class MarkerOnRoute(
    var coordinate: Coordinate? = null,
    var isActive: Boolean? = false
){
    fun toMap(): Map<String,Any? >{
        return mapOf(
            "coordinate" to coordinate,
            "isActive" to isActive
        )
    }
}
