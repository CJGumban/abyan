package com.example.abyan.ui.home


import android.Manifest
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.example.abyan.R
import com.example.abyan.databinding.FragmentHomeBinding
import com.example.abyan.viewmodel.ApplicationViewModel
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DatabaseReference


class HomeFragment : Fragment() {
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var mCurrentLocation: Location? = null
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var locationPermissionGranted = true
    private lateinit var binding: FragmentHomeBinding
    private val sharedViewModel : ApplicationViewModel by activityViewModels()
    lateinit var database : DatabaseReference
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations){
                    // Update UI with location data
                    // ...
                    mCurrentLocation = location

                }
            }
        }

        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onPause() {
        super.onPause()
        stopLocationUpdates()

    }

    private fun stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    override fun onResume() {
        super.onResume()
         startLocationUpdates()
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
            locationPermissionGranted = true
                return
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,
            locationCallback,
            Looper.getMainLooper())
    }
    override fun onStart() {
        super.onStart()
        sharedViewModel.getLocationsListener()
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity)
        database = sharedViewModel.database
        sharedViewModel.addPostEventListener(database.child("coordinates"))
        //code for making bottomnav item visible
//      binding.bottomNavigation.menu[0].isVisible = false
        binding.bottomNavigation.selectedItemId = R.id.home
        binding.bottomNavigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.news -> setCurrentFragment("news")
                R.id.home -> setCurrentFragment("home")
                R.id.map -> setCurrentFragment("map")
            }
            true
        }
        binding.sendLocationButton.setOnClickListener {
            try {
                    getDeviceLocation()
            } catch (e: Exception) {
                Log.d(TAG, "${e.message}")
            }
        }
        binding.signoutButton.setOnClickListener{
            sharedViewModel.signOut()
            setCurrentFragment("logOut")

        }
        createLocationRequest()
        startLocationUpdates()



    }

    //create location request
    fun createLocationRequest() {
        locationRequest = LocationRequest.create().apply {
            interval = 1000
            fastestInterval = 1000
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


    //change fragments
    private fun setCurrentFragment(itemId: String) {
        when (itemId) {
            "news" -> {
                val action =
                    HomeFragmentDirections.actionHomeFragmentToNewsUpdateFragment()
                view?.findNavController()?.navigate(action)
            }
            "map" -> {
                val action =
                    HomeFragmentDirections.actionHomeFragmentToSendLocationFragment()
                view?.findNavController()?.navigate(action)
            }

            "logOut" -> {
                val action =
                HomeFragmentDirections.actionHomeFragmentToLoginFragment()
                view?.findNavController()?.navigate(action)
            }

        }
    }

    companion object {
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
        private const val TAG = "AppTesting"
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        locationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    locationPermissionGranted = true

                }
            }
        }
    }



    private fun getDeviceLocation() {
        try {
            if (locationPermissionGranted) {
                        if (mCurrentLocation!=null) {
                            sharedViewModel.latitude = mCurrentLocation!!.latitude
                            sharedViewModel.longitude = mCurrentLocation!!.longitude
                            sharedViewModel.sendLocation()
                        }
                    } else {
                        Log.d(TAG, "Current location is null. Using defaults.")
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

}



