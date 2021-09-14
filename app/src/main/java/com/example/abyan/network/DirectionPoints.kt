package com.example.abyan.network

import com.squareup.moshi.Json

data class GeocoderStatus(
    @Json(name = "geocoded_waypoints='[0]'")val geocoderStatus: Any
)
