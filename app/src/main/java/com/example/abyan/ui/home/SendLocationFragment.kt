package com.example.abyan.ui.home

import android.Manifest
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Looper
import android.util.Log

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.example.abyan.R
import com.example.abyan.databinding.FragmentSendLocationBinding
import com.example.abyan.viewmodel.ApplicationViewModel
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.tasks.Task
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.GsonBuilder
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.internal.PolylineEncoding
import java.time.Clock
import java.util.*


class SendLocationFragment : Fragment() {
    private lateinit var binding: FragmentSendLocationBinding
    private lateinit var mMap: GoogleMap
    val sharedViewModel : ApplicationViewModel by activityViewModels()
    private val REQUEST_LOCATION_PERMISSION = 1
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var mCurrentLocation: Location? = null
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var locationPermissionGranted = true




//    private var context: GeoApiContext? = null


    companion object{

        const val TAG = "AppTesting"

    }
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
            mMap.isMyLocationEnabled = true
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


    private var callback = OnMapReadyCallback { googleMap ->
        getmMap(googleMap)
        mMap = googleMap
        val roxas = LatLng(11.5529,122.7407)
        mMap.setMinZoomPreference(10f)
        mMap.setMaxZoomPreference(30f)
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        mMap.moveCamera(CameraUpdateFactory.newLatLng(roxas))
        mMap.moveCamera(CameraUpdateFactory.zoomTo(10f))
        mMap.isTrafficEnabled = true
        sharedViewModel.getLocationsListener()
        enableMyLocation()
        refreshMapPin()
        mMap.setOnInfoWindowClickListener {
            marker->
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Get Direction?")
                .setMessage("Get Directions to ${marker.title}")
                .setNeutralButton("Cancel") { dialog, which ->
                    // Respond to neutral button press
                }
                .setPositiveButton("Accept") { dialog, which ->
                    var position = marker.position



                    object : CountDownTimer(300000, 5000) {

                        override fun onTick(millisUntilFinished: Long) {
                            Log.d(TAG,"Timer ${millisUntilFinished / 5000}")
                            mMap.clear()
                            refreshMapPin()
                            showDirection(mCurrentLocation!!, position)
                        }

                        override fun onFinish() {
                            Log.d(TAG,"Timer Done")

                        }
                    }.start()

                }
                .show()
        }
        mMap.setOnMarkerClickListener {
                marker->

            marker.showInfoWindow()
//            MaterialAlertDialogBuilder(requireContext())
//                .setTitle("Get Direction?")
//                .setMessage("Get Directions to ${marker.title}")
//                .setNeutralButton("Cancel") { dialog, which ->
//                    // Respond to neutral button press
//                }
//                .setPositiveButton("Accept") { dialog, which ->
//                    var position = marker.position
//                   // showDirection(mCurrentLocation!!, position)
//
//                   // var a = googleMap.setOnMyLocationChangeListener {
//
//                  //          location ->
//
//
//                        mMap.clear()
//                        refreshMapPin()
//                        showDirection(mCurrentLocation!!, position)
//
//
//                  //  }
//                }
//                .show()





            true
        }

    }


    fun refreshMapPin(){

        try {
            for (coordinate in sharedViewModel.coordinatelist){
                if (coordinate.lat!=null&&coordinate.lng!=null){
                    Log.d(TAG, "foreach method ${coordinate.toMap()}")

                    mMap.addMarker(
                        MarkerOptions()
                            .position(LatLng(coordinate.lat!!,coordinate.lng!!))
                            .title(coordinate.email)

                    )
                }
            }
        } catch (e: Exception){
            android.util.Log.d(TAG, " Map Error: ${e.message}")
        }
    }

    fun showDirection(currentLocation: Location, position: LatLng) {
        var polyline = mMap.addPolyline(
            PolylineOptions()
                .clickable(true)

        )


            val context = GeoApiContext.Builder()
                .apiKey("AIzaSyAgOzM44kdRUKudckM8o_zerMri0kx-sQc")
                .build()
            val gson = GsonBuilder().setPrettyPrinting().create()
            var result = DirectionsApi.newRequest(context).origin(
                com.google.maps.model.LatLng(
                    currentLocation?.latitude!!.toDouble(),
                    currentLocation?.longitude!!.toDouble()
                )
            ).destination(com.google.maps.model.LatLng(position.latitude, position.longitude))
                .await()
//            var direction = gson.toJson(result.routes[0].legs[0].steps[stepCount].polyline.encodedPath)
            var direction = (result.routes[0].overviewPolyline.encodedPath.toString())
            var directToPoly: MutableList<com.google.maps.model.LatLng>? = null
            Log.d(TAG, " direct to poly is empty: ${directToPoly.isNullOrEmpty()}")
            var newDirectToPoly: MutableList<LatLng> = ArrayList()
            Log.d(TAG, "new direct to poly is empty: ${newDirectToPoly.isNullOrEmpty()}")
            directToPoly = PolylineEncoding.decode(direction)





            for (polylist in directToPoly!!) {
                newDirectToPoly.add(
                    LatLng(
                        polylist.lat,
                        polylist.lng
                    )
                )
            }



            polyline = mMap.addPolyline(
                PolylineOptions()
                    .clickable(true)
                    .addAll(
                        newDirectToPoly
                    )
            )

            Log.d(TAG, " polyline points ${polyline.points.size}")




    }


    fun testingShowDirection(position: LatLng) {
        var context = GeoApiContext.Builder()
            .apiKey("AIzaSyAgOzM44kdRUKudckM8o_zerMri0kx-sQc")
            .build()
        var gson = GsonBuilder().setPrettyPrinting().create()
        //sending coordinates
        var result = DirectionsApi.newRequest(context)
            .origin(com.google.maps.model.LatLng(11.580189, 122.755482))
            .destination(com.google.maps.model.LatLng(position.latitude, position.longitude))
            .await()
        val directionApi = DirectionsApi.newRequest(context)
//            var direction = gson.toJson(result.routes[0].legs[0].steps[stepCount].polyline.encodedPath)
        var direction = (result.routes[0].overviewPolyline.encodedPath.toString())
        var directionCount = gson.toJson(result.routes[0].legs[0].steps.size).toInt()

        Log.d(TAG, "Direction polyline:  $direction")
        var directToPoly: MutableList<com.google.maps.model.LatLng>? =
            PolylineEncoding.decode(direction)
        Log.d(TAG, "DirectToPoly polyline to latlngList:  ${directToPoly} ",)
        Log.d(TAG, "DirectToPoly contain sample 1:  ${directToPoly?.get(0)} ",)
        var newDirectToPoly: MutableList<LatLng> = ArrayList()
        for (polylist in directToPoly!!) {
            Log.d(TAG, "Polylist lang: ${polylist.lng} DirectToPoly on loop: $newDirectToPoly")
            newDirectToPoly.clear()
            newDirectToPoly.add(
                LatLng(
                    polylist.lat,
                    polylist.lng
                )
            )
        }
        val polyline2 = mMap.addPolyline(
            PolylineOptions()
                .clickable(true)
                .addAll(
                    newDirectToPoly
                )
        )
    }
    override fun onDetach() {
        super.onDetach()
        sharedViewModel.latlngList.clear()
    }

    private fun stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    private fun startLocationUpdates() {
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
            locationPermissionGranted = true
            return
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,
            locationCallback,
            Looper.getMainLooper())
    }


    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }

    fun createLocationRequest() {
        locationRequest = LocationRequest.create().apply {
            interval = 3000
            fastestInterval = 3000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        val client: SettingsClient = LocationServices.getSettingsClient(requireActivity())
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
        task.addOnSuccessListener { locationSettingsResponse ->
            // All location settings are satisfied. The client can initialize
            // location requests here.
            // ...

        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException){
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    val REQUEST_CHECK_SETTINGS = 0
                    exception.startResolutionForResult(activity,
                        REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                mCurrentLocation = locationResult.lastLocation
            }
        }

        val fragment = FragmentSendLocationBinding.inflate(inflater, container, false)
        binding = fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity)
        createLocationRequest()
        startLocationUpdates()
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
        binding.bottomNavigation.selectedItemId = R.id.map
        binding.bottomNavigation.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.news->setCurrentFragment("news")
                R.id.home->setCurrentFragment("home")
                R.id.map->setCurrentFragment("map")

            }
            true
        }
    }
    private fun setCurrentFragment(itemId: String) {
        when(itemId) {
            "home" -> {
                val action =
                    SendLocationFragmentDirections.actionSendLocationFragmentToHomeFragment()
                view?.findNavController()?.navigate(action)
            }
            "news" -> {
                val action =
                    SendLocationFragmentDirections.actionSendLocationFragmentToNewsUpdateFragment()
                view?.findNavController()?.navigate(action)
            }
        }
    }}


