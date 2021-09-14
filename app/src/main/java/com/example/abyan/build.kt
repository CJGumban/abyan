package com.example.abyan

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.maps.GeoApiContext

import com.google.maps.GeocodingApi

import com.google.maps.model.GeocodingResult


class build {



    var context = GeoApiContext.Builder()
        .apiKey("AIza...")
        .build()
    var results = GeocodingApi.geocode(
        context,
        "1600 Amphitheatre Parkway Mountain View, CA 94043"
    ).await()
    var gson = GsonBuilder().setPrettyPrinting().create()
    var s = gson.toJson(results[0].addressComponents)


// Invoke .shutdown() after your application is done making requests

}