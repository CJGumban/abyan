package com.example.abyan.ui.home

import android.Manifest
import android.content.Context
import android.content.IntentSender
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
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
import com.example.abyan.R.drawable.*
import com.example.abyan.databinding.FragmentSendLocationBinding
import com.example.abyan.model.Coordinate
import com.example.abyan.viewmodel.ApplicationViewModel
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.Task
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.internal.PolylineEncoding
import java.util.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory

import android.graphics.Canvas

import android.graphics.drawable.VectorDrawable
import android.net.ConnectivityManager
import android.net.NetworkInfo

import android.os.Build
import androidx.activity.addCallback
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController

import com.google.android.gms.maps.model.BitmapDescriptor


class SendLocationFragment : Fragment() {
    private lateinit var binding: FragmentSendLocationBinding
    private lateinit var mMap: GoogleMap
    val sharedViewModel: ApplicationViewModel by activityViewModels()
    private val REQUEST_LOCATION_PERMISSION = 1
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var mCurrentLocation: Location? = null
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var locationPermissionGranted = true
    private var polyline: Polyline? = null
    private lateinit var coordinateKey: String
    lateinit var sharedPreferences: SharedPreferences
    lateinit var editor: SharedPreferences.Editor


//    private var context: GeoApiContext? = null


    companion object {

        const val TAG = "AppTesting"
        const val COORDINATEKEY = "coordinateKey"

    }

    override fun onStart() {
        super.onStart()
        binding.bottomNavigation.selectedItemId = R.id.map

    }
    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun getmMap(mMap: GoogleMap): GoogleMap {
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
        } else {
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
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            }
        }
    }



    private var callback = OnMapReadyCallback{ googleMap ->

        getmMap(googleMap)
        mMap = googleMap
        var roxas = LatLng(11.5529, 122.7407)


        mMap.setMinZoomPreference(10f)
        mMap.setMaxZoomPreference(30f)
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        mMap.isTrafficEnabled = true
        sharedViewModel.getLocationsListener()

        enableMyLocation()
        refreshMapPin()

        if(coordinateKey.equals("")){
            roxas = LatLng(11.5529, 122.7407)

            Log.d("testingthis","on map ready coodinatekey is null = ${coordinateKey}"+
                    "\n mMap cameraposition ${mMap.cameraPosition}")

        } else {
            sharedViewModel.coordinatelist.forEach {coordinate->
                if (coordinate.key == coordinateKey){
                    roxas = LatLng(coordinate.lat!!,coordinate.lng!!)

                    Log.d("testingthis","on map ready coodinatekey is not null = ${coordinateKey}" +
                            "\n map latlang = $roxas" +
                            "\n mMap cameraposition ${mMap.cameraPosition}")


                }
                Log.d("testingthis","on map ready coodinatekey is null = ${coordinateKey}"+
                        "\n mMap cameraposition ${mMap.cameraPosition}")

            }
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(roxas, 12f))





        //on Marker click
        //put logic or menus depending on the status of the marker
        mMap.setOnInfoWindowClickListener { marker ->
            var markerCoordinate: Coordinate = marker.tag as Coordinate
            Log.d(TAG, "Coordinate key: ${markerCoordinate.toMap()}")


            try {
                when {

                    markerCoordinate.status?.equals("need help")!! -> {
                        Log.d(
                            TAG,
                            "marker on route ${sharedViewModel.markerOnRoute.toMap()}"
                        )
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Get Direction?")
                            .setMessage("Get Directions to ${markerCoordinate.fullname}")
                            .setNeutralButton("Close") { dialog, which ->
                                // Respond to neutral button press
                            }
                            .setPositiveButton("Accept") { dialog, which ->
                                //code if create route accepted

                                    if (!sharedViewModel.markerOnRoute.isActive!!||sharedViewModel.markerOnRoute.marker==null) {
                                        //to this if there's no active route
                                            //this code activate route and get marker's coordinate
                                        sharedViewModel.activateRoute(marker)
//                                      update the coordinate status to ongoing and send data back to database
                                        sharedViewModel.changeStatus(marker.tag as Coordinate, "ongoing")
                                        (marker.tag as Coordinate).status = "ongoing"
                                        editMarker(marker)
                                        object : CountDownTimer(500000, 5000) {
                                            override fun onTick(millisUntilFinished: Long) {
                                                if (sharedViewModel.markerOnRoute.isActive!!) {
                                                    showDirection(
                                                        mCurrentLocation!!,
                                                        LatLng(
                                                            sharedViewModel.markerOnRoute.coordinate?.lat!!,
                                                            sharedViewModel.markerOnRoute.coordinate?.lng!!
                                                        )
                                                    )
                                                } else if (!sharedViewModel.markerOnRoute.isActive!!) {
                                                    cancel()
                                                }
                                            }

                                            override fun onFinish() {
                                            }

                                        }.start()
                                    } else if (sharedViewModel.markerOnRoute.isActive!!) {
                                       if (sharedViewModel.markerOnRoute.coordinate!=null){
                                            //coordinate exist
                                            if (sharedViewModel.markerOnRoute.coordinate?.key != markerCoordinate.key) {
                                                //coordinate.key not equal on active markerkey
                                                MaterialAlertDialogBuilder(
                                                    requireContext()
                                                )
                                                    .setTitle("Ongoing route")
                                                    .setMessage("You already have an active route to another user. Proceeding will cancel your ongoing active Route.")
                                                    .setNeutralButton("Cancel") { dialog, which ->
                                                        // Respond to neutral button press
                                                    }
                                                    .setPositiveButton("Proceed") { dialog, which ->
                                                        sharedViewModel.changeStatus(sharedViewModel.markerOnRoute.coordinate!!, "need help")
                                                        (sharedViewModel.markerOnRoute.coordinate as Coordinate).status = "need help"
                                                        editMarker(
                                                            sharedViewModel.markerOnRoute.coordinate!!,
                                                            sharedViewModel.markerOnRoute.marker!!)
                                                        sharedViewModel.activateRoute(marker)
                                                        (marker.tag as Coordinate).status = "ongoing"
                                                        editMarker(marker)
                                                        showDirection(
                                                            mCurrentLocation!!,
                                                            LatLng(
                                                                sharedViewModel.markerOnRoute.coordinate?.lat!!,
                                                                sharedViewModel.markerOnRoute.coordinate?.lng!!
                                                            )
                                                        )
//                                                   TODO("cancel previous marker route" +
//                                                           "change marker route" +
//                                                           "mMap.clear and refreshPin" +
//                                                           "confirm if user wanted to change marker route")

                                                    }.show()
                                            }
                                            else if (sharedViewModel.markerOnRoute.coordinate?.key == markerCoordinate.key) {
                                                MaterialAlertDialogBuilder(
                                                    requireContext()
                                                )
                                                    .setTitle("Ongoing route")
                                                    .setMessage("Already on Route with this marker. Do you want to cancel route?")
                                                    .setPositiveButton("Yes") { dialog, which ->
                                                        sharedViewModel.changeStatus(
                                                            markerCoordinate,
                                                            "need help"
                                                        )

                                                        sharedViewModel.deactivateRoute()
                                                    }
                                                    .setNegativeButton("No"){ dialog, which ->
                                                    }.show()
                                            }
                                       }
                                    }
                                
                            }.show()
                    }
                    markerCoordinate.status?.equals("ongoing")!! -> {
                        val items = arrayOf("cancel route", "continue", "done")
                        //markerRoute is null
                        if (sharedViewModel.markerOnRoute.coordinate == null|| !sharedViewModel.markerOnRoute.isActive!!) {
                            MaterialAlertDialogBuilder(requireContext())
                                .setTitle("Get Direction?")
                                .setMessage("Get Directions to ${markerCoordinate.fullname}")
                                .setNeutralButton("Close") { dialog, which ->
                                    // Respond to neutral button press
                                }
                                .setPositiveButton("Accept") { dialog, which ->
                                    sharedViewModel.activateRoute(marker)
                                    sharedViewModel.changeStatus(marker.tag as Coordinate, "ongoing")
                                    (marker.tag as Coordinate).status = "ongoing"
                                    editMarker(marker)
                                    object : CountDownTimer(500000, 5000) {
                                        override fun onTick(millisUntilFinished: Long) {
                                            if (sharedViewModel.markerOnRoute.isActive == true) {
                                                showDirection(
                                                    mCurrentLocation!!,
                                                    LatLng(
                                                        sharedViewModel.markerOnRoute.coordinate?.lat!!,
                                                        sharedViewModel.markerOnRoute.coordinate?.lng!!
                                                    )
                                                )
                                            } else if (sharedViewModel.markerOnRoute.isActive == false) {
                                                cancel()
                                            }
                                        }
                                        override fun onFinish() {
                                        }
                                    }.start()
                                }.show()

                        }
                        else if(sharedViewModel.markerOnRoute.coordinate != null){
                            if (sharedViewModel.markerOnRoute.coordinate!!.key == markerCoordinate.key){
                                var typepicker = MaterialAlertDialogBuilder(requireContext())
                                    .setTitle("ongoing")
                                    .setItems(items) { dialog, which ->
                                        when (which) {
                                            0 -> {
                                                sharedViewModel.changeStatus(markerCoordinate, "need help")
                                                sharedViewModel.deactivateRoute()
                                                polyline?.points = emptyList()
                                                (marker.tag as Coordinate).status = "need help"
                                                editMarker(marker.tag as Coordinate, marker)

                                             }
                                            1 -> {

                                            }
                                            2 -> {
                                                sharedViewModel.changeStatus(markerCoordinate, "done")
                                                sharedViewModel.deactivateRoute()
                                                polyline?.points = emptyList()
                                                (marker.tag as Coordinate).status = "done"
                                                editMarker(marker)
                                            }
                                        }
                                    }.show()
                            }
                            else if (sharedViewModel.markerOnRoute.coordinate!!.key != markerCoordinate.key){
                                MaterialAlertDialogBuilder(
                                    requireContext()
                                )
                                    .setTitle("Ongoing route")
                                    .setMessage("You already have an active route to another user. Proceeding will cancel your ongoing active Route.")
                                    .setNeutralButton("Cancel") { dialog, which ->
                                        // Respond to neutral button press
                                    }
                                    .setPositiveButton("Proceed") { dialog, which ->

                                        sharedViewModel.changeStatus(sharedViewModel.markerOnRoute.coordinate!!, "need help")
                                        (sharedViewModel.markerOnRoute.coordinate as Coordinate).status = "need help"
                                        editMarker(
                                            sharedViewModel.markerOnRoute.coordinate!!,
                                            sharedViewModel.markerOnRoute.marker!!)
                                        sharedViewModel.activateRoute(marker)
                                        (marker.tag as Coordinate).status = "ongoing"
                                        editMarker(marker)

                                        showDirection(
                                            mCurrentLocation!!,
                                            LatLng(
                                                sharedViewModel.markerOnRoute.coordinate?.lat!!,
                                                sharedViewModel.markerOnRoute.coordinate?.lng!!
                                            )
                                        )
                                    }.show()
                            }
                        }
                    }
                    markerCoordinate.status?.equals("done")!! -> {
                        if(sharedViewModel.currentUserData.role.equals("admin")){
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Delete Marker?")
                            .setMessage("The marker was done. Do you want to delete it?")
                            .setNegativeButton("Cancel") { dialog, which ->
                                // Respond to neutral button press
                            }
                            .setPositiveButton("Delete") { dialog, which ->
                                sharedViewModel.changeStatus(marker.tag as Coordinate, "delete")
                                marker.remove()
                            }.show()
                        }
                    }
                }
            } catch (e: java.lang.Exception) {
                Log.e(TAG, "send location error: ${e.message}")
            }

        }
        mMap.setOnMarkerClickListener { marker ->
            marker.showInfoWindow()
            mMap.moveCamera(CameraUpdateFactory.newLatLng(marker.position))
            true
        }

    }

    override fun onStop() {
        super.onStop()
        if (sharedViewModel.markerOnRoute.isActive == true){
            sharedViewModel.markerOnRoute.isActive = false
        }
    }

    fun refreshMapPin() {
        try {
            polyline = mMap.addPolyline(
                PolylineOptions()
            )

            for (coordinate in sharedViewModel.coordinatelist) {
                if (coordinate.lat != null && coordinate.lng != null) {
                    addMarker(coordinate)
                }
            }
        } catch (e: Exception) {
            android.util.Log.d(TAG, " Map Error: ${e.message}")
        }
    }


    private fun addMarker(coordinate: Coordinate) {
        var pin: Int = pin_null_red


        var type = coordinate.type
        var status = coordinate.status
        when (type) {
            "ambulance" -> {
                when (status) {
                    "need help" -> {
                        pin = pin_ambulance_red
                    }
                    "ongoing" -> {
                        pin = pin_ambulance_blue
                    }
                    "done" -> {
                        pin = pin_ambulance_green

                    }
                }
            }
            "car accident" -> {
                when (status) {
                    "need help" -> {
                        pin = pin_car_accident_red
                    }
                    "ongoing" -> {
                        pin = pin_car_accident_blue
                    }
                    "done" -> {
                        pin = pin_car_accident_green

                    }
                }

            }
            "fire" -> {
                when (status) {
                    "need help" -> {
                        pin = pin_fire_red

                    }
                    "ongoing" -> {
                        pin = pin_fire_blue

                    }
                    "done" -> {
                        pin = pin_fire_green

                    }
                }

            }
            "crime" -> {
                when (status) {
                    "need help" -> {
                        pin = pin_crime_red

                    }
                    "ongoing" -> {
                        pin = pin_crime_blue

                    }
                    "done" -> {
                        pin = pin_crime_green

                    }
                }

            }
            null -> {
                when (status) {
                    "need help" -> {

                    }
                    "ongoing" -> {
                        pin = pin_null_blue

                    }
                    "done" -> {
                        pin = pin_null_green

                    }
                }
            }
        }

        mMap.addMarker(
            MarkerOptions()
                .position(LatLng(coordinate.lat!!, coordinate.lng!!))
                .title("${coordinate.fullname}")
                .snippet("${coordinate.dateTime}")
                .icon(getBitmapDescriptor(pin))
        ).tag = coordinate


    }

    private fun editMarker(marker: Marker) {
        var coordinate: Coordinate = marker.tag as Coordinate
        var pin: Int = pin_null_red


        var type = coordinate.type
        var status = coordinate.status
        when (type) {
            "ambulance" -> {
                when (status) {
                    "need help" -> {
                        pin = pin_ambulance_red
                    }
                    "ongoing" -> {
                        pin = pin_ambulance_blue
                    }
                    "done" -> {
                        pin = pin_ambulance_green

                    }
                }
            }
            "car accident" -> {
                when (status) {
                    "need help" -> {
                        pin = pin_car_accident_red
                    }
                    "ongoing" -> {
                        pin = pin_car_accident_blue
                    }
                    "done" -> {
                        pin = pin_car_accident_green

                    }
                }

            }
            "fire" -> {
                when (status) {
                    "need help" -> {
                        pin = pin_fire_red

                    }
                    "ongoing" -> {
                        pin = pin_fire_blue

                    }
                    "done" -> {
                        pin = pin_fire_green

                    }
                }

            }
            "crime" -> {
                when (status) {
                    "need help" -> {
                        pin = pin_crime_red

                    }
                    "ongoing" -> {
                        pin = pin_crime_blue

                    }
                    "done" -> {
                        pin = pin_crime_green

                    }
                }

            }
            null -> {
                when (status) {
                    "need help" -> {

                    }
                    "ongoing" -> {
                        pin = pin_null_blue

                    }
                    "done" -> {
                        pin = pin_null_green

                    }
                }
            }
        }

        marker.setIcon(getBitmapDescriptor(pin))
    }
    private fun editMarker(coordinate: Coordinate, marker: Marker) {

        var pin: Int = pin_null_red


        var type = coordinate.type
        var status = coordinate.status
        when (type) {
            "ambulance" -> {
                when (status) {
                    "need help" -> {
                        pin = pin_ambulance_red
                    }
                    "ongoing" -> {
                        pin = pin_ambulance_blue
                    }
                    "done" -> {
                        pin = pin_ambulance_green

                    }
                }
            }
            "car accident" -> {
                when (status) {
                    "need help" -> {
                        pin = pin_car_accident_red
                    }
                    "ongoing" -> {
                        pin = pin_car_accident_blue
                    }
                    "done" -> {
                        pin = pin_car_accident_green

                    }
                }

            }
            "fire" -> {
                when (status) {
                    "need help" -> {
                        pin = pin_fire_red

                    }
                    "ongoing" -> {
                        pin = pin_fire_blue

                    }
                    "done" -> {
                        pin = pin_fire_green

                    }
                }

            }
            "crime" -> {
                when (status) {
                    "need help" -> {
                        pin = pin_crime_red


                    }
                    "ongoing" -> {
                        pin = pin_crime_blue

                    }
                    "done" -> {
                        pin = pin_crime_green

                    }
                }

            }
            null -> {
                when (status) {
                    "need help" -> {

                    }
                    "ongoing" -> {
                        pin = pin_null_blue

                    }
                    "done" -> {
                        pin = pin_null_green

                    }
                }
            }
        }

        marker.setIcon(getBitmapDescriptor(pin))
    }

    private fun getBitmapDescriptor(id: Int): BitmapDescriptor? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val vectorDrawable = getDrawable(requireContext(), id) as VectorDrawable
            val h = vectorDrawable.intrinsicHeight
            val w = vectorDrawable.intrinsicWidth
            vectorDrawable.setBounds(0, 0, w, h)
            val bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bm)
            vectorDrawable.draw(canvas)
            BitmapDescriptorFactory.fromBitmap(bm)
        } else {
            BitmapDescriptorFactory.fromResource(id)
        }
    }

    fun showDirection(currentLocation: Location, position: LatLng) {
        try {


            val context = GeoApiContext.Builder()
                .apiKey("AIzaSyAgOzM44kdRUKudckM8o_zerMri0kx-sQc")
                .build()
            var result = DirectionsApi.newRequest(context).origin(
                com.google.maps.model.LatLng(
                    currentLocation.latitude.toDouble(),
                    currentLocation.longitude.toDouble()
                )
            ).destination(com.google.maps.model.LatLng(position.latitude, position.longitude))
                .await()

//            var direction = gson.toJson(result.routes[0].legs[0].steps[stepCount].polyline.encodedPath)
            var direction = (result.routes[0].overviewPolyline.encodedPath.toString())
            var directToPoly: MutableList<com.google.maps.model.LatLng>? = null
            Log.d(TAG, " newdirecttopoly ${direction}")

            var newDirectToPoly: MutableList<LatLng> = ArrayList()
            directToPoly = PolylineEncoding.decode(direction)
            Log.d(TAG, " newdirecttopoly ${directToPoly}")
            for (polylist in directToPoly!!) {
                newDirectToPoly.add(
                    LatLng(
                        polylist.lat,
                        polylist.lng
                    )
                )
            }
            Log.d(TAG, " newdirecttopoly ${newDirectToPoly}")
            polyline?.points = newDirectToPoly
            polyline?.zIndex = 1f


            Log.d(TAG, " polyline points ${polyline?.points}")


        } catch (e: Exception) {
            Log.d(TAG, " ${e.message}")

        }
    }


    fun testingShowDirection(position: LatLng) {
        var context = GeoApiContext.Builder()
            .apiKey("AIzaSyAgOzM44kdRUKudckM8o_zerMri0kx-sQc")
            .build()
        //sending coordinates
        var result = DirectionsApi.newRequest(context)
            .origin(com.google.maps.model.LatLng(11.580189, 122.755482))
            .destination(com.google.maps.model.LatLng(position.latitude, position.longitude))
            .await()
        val directionApi = DirectionsApi.newRequest(context)
//            var direction = gson.toJson(result.routes[0].legs[0].steps[stepCount].polyline.encodedPath)
        var direction = (result.routes[0].overviewPolyline.encodedPath.toString())

        Log.d(TAG, "Direction polyline:  $direction")
        var directToPoly: MutableList<com.google.maps.model.LatLng>? =
            PolylineEncoding.decode(direction)
        Log.d(TAG, "DirectToPoly polyline to latlngList:  ${directToPoly} ")
        Log.d(TAG, "DirectToPoly contain sample 1:  ${directToPoly?.get(0)} ")
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
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
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
            if (exception is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    val REQUEST_CHECK_SETTINGS = 0
                    exception.startResolutionForResult(
                        activity,
                        REQUEST_CHECK_SETTINGS
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            coordinateKey = it.getString(COORDINATEKEY).toString()
        }
        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            // Handle the back button event


            view?.findNavController()?.navigateUp()


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

        loadpref()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity)
        createLocationRequest()
        startLocationUpdates()
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
        binding.buttonList.setOnClickListener{
            setCurrentFragment("list")
        }
        Log.d(
            TAG,"map fragment bottomnav selectedid ${binding.bottomNavigation.selectedItemId }" +
                "\nhome id ${R.id.home}" +
                "\nmap id ${R.id.map}" +
                "\nnews id ${R.id.news}")
        binding.bottomNavigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.news -> setCurrentFragment("news")
                R.id.home -> setCurrentFragment("home")
                R.id.map -> setCurrentFragment("map")

            }
            true
        }
    }

    private fun setCurrentFragment(itemId: String) {
        try {


            when (itemId) {
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
                "list" -> {


                    val action =
                        SendLocationFragmentDirections.actionSendLocationFragmentToMapListViewFragment()
                    view?.findNavController()?.navigate(action)
                }
            }
        }catch (e:Exception){}
    }

    fun loadpref() {
        sharedPreferences = activity?.getPreferences(Context.MODE_PRIVATE)!!
        editor = sharedPreferences.edit()!!
    }
    fun net(): Boolean {
        val cm = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        val isConnected: Boolean = activeNetwork?.isConnectedOrConnecting == true
        return isConnected
    }

}


