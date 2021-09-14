package com.example.abyan.ui

import android.Manifest
import android.content.pm.PackageManager
import android.location.LocationManager
import android.nfc.Tag
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.util.Log

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.example.abyan.R
import com.example.abyan.databinding.FragmentLoginBinding
import com.example.abyan.databinding.FragmentSendLocationBinding
import com.example.abyan.viewmodel.LoginSignUpViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.PolylineOptions
import com.google.gson.GsonBuilder
import com.google.maps.DirectionsApi
import com.google.maps.DirectionsApiRequest
import com.google.maps.GeoApiContext
import com.google.maps.GeocodingApi
import com.google.maps.internal.PolylineEncoding
import com.google.maps.model.DirectionsResult
import com.google.maps.model.DirectionsRoute
import com.google.maps.model.EncodedPolyline
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okio.utf8Size
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.ArrayList
import java.util.Arrays.toString
import kotlin.math.log
import kotlin.math.roundToLong


class SendLocationFragment : Fragment() {
    private lateinit var binding: FragmentSendLocationBinding
    private val sharedViewModel : LoginSignUpViewModel by activityViewModels()
    private val REQUEST_LOCATION_PERMISSION = 1
    private var mMap: GoogleMap? = null
//    private var context: GeoApiContext? = null









    private fun isPermissionGranted() : Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }


    fun getmMap(mMap:GoogleMap):GoogleMap{
        return mMap
    }

    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {


                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            mMap?.isMyLocationEnabled = true
        }
        else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            }
        }
    }


    private val callback = OnMapReadyCallback { googleMap ->



        enableMyLocation()
        getmMap(googleMap)

        val sydney = LatLng(-34.0, 151.0)
        googleMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))



        val kyoto = LatLng(sharedViewModel.lat()!!, sharedViewModel.lng()!!)
        // Set the map type to Hybrid.
        // Set the map type to Hybrid.
        googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        // Add a marker on the map coordinates.
        // Add a marker on the map coordinates.
        googleMap.addMarker(
            MarkerOptions()
                .position(kyoto)
                .title("Ivisan")
        )



        // Move the camera to the map coordinates and zoom in closer.
        // Move the camera to the map coordinates and zoom in closer.
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(kyoto))
        googleMap.moveCamera(CameraUpdateFactory.zoomTo(15f))
        // Display traffic.
        // Display traffic.
        googleMap.isTrafficEnabled = true


        try {
            //google direction api caller and converter
            var context = GeoApiContext.Builder()
                .apiKey("AIzaSyAgOzM44kdRUKudckM8o_zerMri0kx-sQc")
                .build()
            var gson = GsonBuilder().setPrettyPrinting().create()
            //sending coordinates
            var result = DirectionsApi.newRequest(context).origin(com.google.maps.model.LatLng(11.580189, 122.755482)).destination(com.google.maps.model.LatLng(11.575932, 122.757358)).await()

            val directionApi = DirectionsApi.newRequest(context)
            var stepCount = 0
//            var direction = gson.toJson(result.routes[0].legs[0].steps[stepCount].polyline.encodedPath)
            var direction = (result.routes[0].overviewPolyline.encodedPath.toString())
            var directionCount = gson.toJson(result.routes[0].legs[0].steps.size).toInt()
            stepCount++
            Log.d("maps", "Direction polyline:  $direction")
//
            var directToPoly: MutableList<com.google.maps.model.LatLng>? =PolylineEncoding.decode(direction)
            Log.d("maps", "DirectToPoly polyline to latlng:  ${directToPoly} ",)
            Log.d("maps", "DirectToPoly contain sample 1:  ${directToPoly?.get(0)} ",)
            var newDirectToPoly: MutableList<LatLng> = ArrayList()
            var polyListCount = 0
            for (polylist in directToPoly!!) {
                Log.d("maps", "Polylist lang: ${polylist.lng} DirectToPoly on loop: $newDirectToPoly")

                newDirectToPoly.add(
                    LatLng(
                         polylist.lat,
                        polylist.lng
                    )
                )
                polyListCount++
            }

            Log.d("maps", "DirectToPoly result: $newDirectToPoly")
            val polyline1 = googleMap.addPolyline(
                PolylineOptions()
                .clickable(true)
                .addAll(newDirectToPoly
                   ))

            // Position the map's camera near Alice Springs in the center of Australia,
            // and set the zoom factor so most of Australia shows on the screen.
            googleMap.addMarker(
                MarkerOptions()
                    .position(newDirectToPoly[0])
                    .title("start")
            )
            googleMap.addMarker(
                MarkerOptions()
                    .position(newDirectToPoly.last())
                    .title("finish")
            )
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(directToPoly[0].lat, directToPoly[0].lng), 20f))




//            var poly = directToPoly.isEmpty()








        } catch (e: Exception){
            android.util.Log.d("maps", "Error: ${e.message}")


        }
    }
//    fun getDirections(
//        context: GeoApiContext?,
//        origin: String?,
//        destination: String?
//    ): DirectionsApiRequest? {
//        return this
//    }


//    fun tryy (){
//
//
//
//
//
//        try {
//            var context = GeoApiContext.Builder()
//                .apiKey("AIzaSyAgOzM44kdRUKudckM8o_zerMri0kx-sQc")
//                .build()
//
//            var gson = GsonBuilder().setPrettyPrinting().create()
//            // var s = gson.toJson(results[0].addressComponents).toString()
//
//            var result =
//                DirectionsApi.newRequest(context).origin(com.google.maps.model.LatLng(11.5303, 122.6842)).destination(com.google.maps.model.LatLng(11.5529, 122.7407)).await()
//            val directionApi = DirectionsApi.newRequest(context)
//        var stepCount = 0
//            var direction = gson.toJson(result.routes[0].legs[0].steps[stepCount].polyline.encodedPath)
//                direction = gson.toJson((result.routes[0].overviewPolyline.encodedPath))
//                var directionCount = gson.toJson(result.routes[0].legs[0].steps.size).toInt()
//                stepCount++
//            Log.d("maps", "Direction:  $direction")
////
//   var directToPoly: MutableList<com.google.maps.model.LatLng>? =PolylineEncoding.decode(direction)
//            Log.d("maps", "Direction:  ${directToPoly?.get(0)}")
//
//                for (polylist in directToPoly!!) {
//                    // ...
//
//
//                    val newDirectToPoly: MutableList<LatLng> = ArrayList()
//
//                    newDirectToPoly.add(
//                        LatLng(
//                            String.format("%.5f", polylist.lng).toDouble(),
//                            String.format("%.5f", polylist.lat).toDouble()
//                    )
//                    )
//                    Log.d("maps", "DirectToPoly: $newDirectToPoly")
//                }
//
//
//
////            var poly = directToPoly.isEmpty()
//
//
//
//
//
//
//
//
//      } catch (e: Exception){
//           android.util.Log.d("maps", "Error: ${e.message}")
//
//
//        }
//    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragment = FragmentSendLocationBinding.inflate(inflater, container, false)
        binding = fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)


    }


}