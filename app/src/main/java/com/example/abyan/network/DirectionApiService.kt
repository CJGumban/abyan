package com.example.abyan.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET

private const val BASE_URL = "https://maps.googleapis.com"
private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()


interface DirectionApiService {

    @GET("maps/api/directions/json?origin=11.568841,%20122.751464&destination=11.583220,%20122.752441&key=AIzaSyAgOzM44kdRUKudckM8o_zerMri0kx-sQc")
    suspend fun getDirection(): GeocoderStatus
}
object DirectionApi{
    val retrofitService : DirectionApiService by lazy {
        retrofit.create(DirectionApiService::class.java)
    }
}