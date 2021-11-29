package com.example.abyan.model

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
class Coordinate(
    val key: String? = null,
    val email: String? = null,
    val fullname: String? = null,
    val age: String? = null,
    val gender: String? = null,
    val lat: Double? = null,
    val lng: Double? = null,
    var status: String? = null,
    val type: String? = null,
    val dateTime: String? = null
                  ) {
    @Exclude
    fun toMap(): Map<String,Any? >{
        return mapOf(
            "key" to key,
            "email" to email,
            "fullname" to fullname,
            "gender" to gender,
            "age" to age,
            "lat" to lat,
            "lng" to lng,
            "status" to status,
            "type" to type,
            "dateTime" to dateTime



        )
    }
}