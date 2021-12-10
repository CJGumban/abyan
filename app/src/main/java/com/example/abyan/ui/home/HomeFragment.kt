package com.example.abyan.ui.home


import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.CountDownTimer
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.view.MotionEvent
import android.view.View.OnTouchListener
import androidx.core.content.ContextCompat
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.lang.Exception
import android.net.Uri

import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.text.TextUtils.replace
import android.text.format.DateFormat
import androidx.activity.addCallback
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.abyan.model.Coordinate
import com.example.abyan.model.User
import com.google.firebase.database.*
import java.util.*


class HomeFragment : Fragment() {
    lateinit var sharedPreferences: SharedPreferences
    lateinit var editor: SharedPreferences.Editor
    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var mCurrentLocation: Location? = null
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var locationPermissionGranted = true
    private lateinit var binding: FragmentHomeBinding
    private val sharedViewModel: ApplicationViewModel by activityViewModels()
    lateinit var database: DatabaseReference
    var isOnline: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            // Handle the back button event
            requireActivity().finish()

        }

        // The callback can be enabled or disabled here or in the lambda
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "location callback $mCurrentLocation")

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                mCurrentLocation = locationResult.lastLocation
                for (location in locationResult.locations) {
                    // Update UI with location data
                    // ...
//                    mCurrentLocation = location
//                    Log.d(TAG, "location callback $mCurrentLocation")
                }
            }
        }

        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        private const val TAG = "AppTesting"
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
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    override fun onStart() {
        super.onStart()
        startLocationUpdates()
        sharedViewModel.getLocationsListener()
        sharedViewModel.getPostListener()
        binding.bottomNavigation.selectedItemId = R.id.home

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        loadpref()

        connectionListener()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity)
        createLocationRequest()
        startLocationUpdates()
        enableMyLocation()
        database = sharedViewModel.database
        sharedViewModel.addPostEventListener(database.child("coordinates"))
        binding.topAppBar.setNavigationOnClickListener { setCurrentFragment("profile") }
        binding.bottomNavigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.news -> setCurrentFragment("news")
                R.id.home -> setCurrentFragment("home")
                R.id.map -> setCurrentFragment("map")
            }
            true
        }
        var sendButtonCancelled = false
        binding.sendLocationButton.setOnTouchListener(OnTouchListener
        { v, event ->
            sendButtonCancelled = false
            if (event.action == MotionEvent.ACTION_DOWN) {
                sendButtonCancelled = false
                object : CountDownTimer(4000, 1000) {

                    override fun onTick(millisUntilFinished: Long) {
                        var timeLeft: Long = millisUntilFinished
                        timeLeft /= 1000
                        if (sendButtonCancelled) {
                            cancel()
                        } else {
                            binding.sendLocationText.setText("$timeLeft").toString()
                        }
                    }

                    override fun onFinish() {
                        if (!net()) {
                            emergencyCall()
                        } else {
                            sendDeviceDialog()
                            Log.d(TAG, "Confirm")
                        }

                    }
                }.start()
            } else if (event.action == MotionEvent.ACTION_UP) {
                // stop your timer.

                sendButtonCancelled = true
                binding.sendLocationText.setText("Send Location").toString()

                Log.d(TAG, "cancelled")
            }
            false
        })

        checkRoles()
    }

    fun net(): Boolean {
        val cm = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        val isConnected: Boolean = activeNetwork?.isConnectedOrConnecting == true
        return isConnected
    }

    private fun emergencyCall() {


        var emergencyCallDialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Location Unknown")
            .setMessage("Check your internet connection and GPS or Call 911 Instead")
            .setNeutralButton("Retry") { dialog, which -> }
            .setPositiveButton("Call") { dialog, which ->
                val number: String = "911"
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$number")
                }

                startActivity(intent)
            }.show()


    }

    //create location request
    fun createLocationRequest() {
        locationRequest = LocationRequest.create().apply {
            interval = 5000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        val client: SettingsClient = LocationServices.getSettingsClient(requireActivity())
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
        task.addOnSuccessListener { locationSettingsResponse ->

            Log.d(
                TAG,
                "createlocationrequest success: locationsettingresponse ${locationSettingsResponse.locationSettingsStates.isLocationPresent}"
            )
            startLocationUpdates()
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


    //change fragments
    private fun setCurrentFragment(itemId: String) {
        try {


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

                "logout" -> {
                    val action =
                        HomeFragmentDirections.actionHomeFragmentToLoginFragment()
                    view?.findNavController()?.navigate(action)

                }
                "profile" -> {
                    val action =
                        HomeFragmentDirections.actionHomeFragmentToProfileFragment()
                    view?.findNavController()?.navigate(action)
                }

            }
        } catch (e: Exception) {
        }
    }


    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
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
                return
            }

        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {

        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.contains(PackageManager.PERMISSION_GRANTED)
            ) {
                enableMyLocation()

            }

        }
    }
    private fun sendLocation() {
        Log.d(TAG, "Send location")
        Log.d(TAG, "locationpermissiongranted $locationPermissionGranted")
        Log.d(TAG, "currtentlocation not null ${mCurrentLocation != null}")
        sharedViewModel.getFullname()
        sharedViewModel.getAge2()
        if (locationPermissionGranted) {

            if (mCurrentLocation != null) {

                val items = arrayOf("ambulance", "car accident", "fire", "crime", "other")

                var typepicker = MaterialAlertDialogBuilder(requireContext())

                    .setTitle("Specify your situation")
                    .setItems(items) { dialog, which ->
                        if (items[which].equals("other")) {
                            sharedViewModel.type = "unspecified"
                        } else {
                            sharedViewModel.type = items[which]
                        }
                    }
                    .setOnDismissListener {
                        var c = MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Location Sent")
                            .setMessage("Keep calm. Help is on its way")
                            .setPositiveButton("Got it!") { dialog, which ->
                            }.show()
                        sharedViewModel.latitude = mCurrentLocation!!.latitude
                        sharedViewModel.longitude = mCurrentLocation!!.longitude
                        sendLocationNow()

                    }
                    .show()
                typepicker.setCanceledOnTouchOutside(false)
            }
        } else {
            Log.d(TAG, "Current location is null. Using defaults.")
        }
    }

    fun sendLocationNow() {
        var userExist: Boolean = false
        var currentCoordinateKey: String? = null
        sharedViewModel.coordinatelist.forEach { coordinate ->
            if (sharedViewModel.auth.currentUser?.email == coordinate.email) {
                userExist = true
                currentCoordinateKey = coordinate.key
                Log.d(ApplicationViewModel.TAG, "sendLocationMethod: Current user exist")
            }
        }
        if (userExist) {
            var tsLong = System.currentTimeMillis() / 1000
            Log.d(ApplicationViewModel.TAG, " timestamp  $tsLong")
            val cal = Calendar.getInstance(Locale.ENGLISH)
            cal.timeInMillis = tsLong * 1000L
            val date: String = DateFormat.format("MM-dd hh:mm", cal).toString()
            Log.d(ApplicationViewModel.TAG, "time stamp to date: $date")
            val email = sharedViewModel.auth.currentUser?.email
            // Create new post at /user-posts/$userid/$postid and at
            // /posts/$postid simultaneously
            val key = database.push().key
            if (key == null) {
                Log.w(ApplicationViewModel.TAG, "Couldn't get push key for posts")
                return
            }
            Log.w(ApplicationViewModel.TAG, "$key")
            val coordinate = Coordinate(
                key = currentCoordinateKey,
                email = email,
                fullname = sharedViewModel.getFullname,
                lat = sharedViewModel.latitude,
                lng = sharedViewModel.longitude,
                gender = sharedViewModel.currentUserData.gender,
                age = sharedViewModel.getAge,
                status = "need help",
                type = sharedViewModel.type,
                dateTime = date
            )
            val coordinateValues = coordinate.toMap()

            val childUpdates = hashMapOf<String, Any>(
                "coordinates/$currentCoordinateKey" to coordinateValues,
                "active-coordinates/$currentCoordinateKey" to coordinateValues,
            )

            database.updateChildren(childUpdates)
                .addOnSuccessListener { Log.w(ApplicationViewModel.TAG, "it worked") }
                .addOnFailureListener {
                    Log.w(ApplicationViewModel.TAG, "failed")
                    emergencyCall()
                }
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "finish task results ${task.result}")
                        Log.d(TAG, "coordinatevalues $coordinateValues")
                    } else {
                        emergencyCall()
                        Log.d(TAG, "task failed ${task.exception?.localizedMessage.toString()}")
                    }
                }
        } else if (!userExist) {
            var tsLong = System.currentTimeMillis() / 1000
            Log.d(ApplicationViewModel.TAG, " timestamp  $tsLong")
            val cal = Calendar.getInstance(Locale.ENGLISH)
            cal.timeInMillis = tsLong * 1000L
            val date: String = DateFormat.format("MM-dd hh:mm", cal).toString()
            Log.d(ApplicationViewModel.TAG, "time stamp to date: $date")
            val email = sharedViewModel.auth.currentUser?.email
            val key = database.push().key
            if (key == null) {
                Log.w(ApplicationViewModel.TAG, "Couldn't get push key for posts")
                return
            }
            Log.w(ApplicationViewModel.TAG, "$key")


            val coordinate = Coordinate(
                key = key,
                email = email,
                fullname = sharedViewModel.getFullname,
                lat = sharedViewModel.latitude,
                lng = sharedViewModel.longitude,
                gender = sharedViewModel.currentUserData.gender,
                age = sharedViewModel.getAge,
                status = "need help",
                type = sharedViewModel.type,
                dateTime = date
            )

            val coordinateValues = coordinate.toMap()

            val childUpdates = hashMapOf<String, Any>(
                "coordinates/$key" to coordinateValues,
                "active-coordinates/$key" to coordinateValues,
            )
            database.updateChildren(childUpdates)
                .addOnSuccessListener { Log.w(ApplicationViewModel.TAG, "it worked") }
                .addOnFailureListener {
                    Log.w(ApplicationViewModel.TAG, "failed")
                    emergencyCall()
                }
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "finish task results ${task.result}")
                        Log.d(TAG, "coordinatevalues $coordinateValues")

                    } else {
                        emergencyCall()
                        Log.d(TAG, "task failed ${task.exception?.localizedMessage.toString()}")
                    }
                }
        }
    }

    private fun sendDeviceDialog() {
        var timeLeft = 5
        var cancelled = false
        var c = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Sending location")
            .setMessage("You're location will be send after $timeLeft seconds\n")
            .setNeutralButton("Cancel") { dialog, which ->
                cancelled = true
            }.show()
        c.setCanceledOnTouchOutside(false)


        object : CountDownTimer(5000, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                var timeLeft: Long = millisUntilFinished
                timeLeft /= 1000
                if (cancelled) {
                    cancel()

                } else {
                    c.setMessage("You're location will be send after $timeLeft seconds")

                }
            }

            override fun onFinish() {
                Log.d(SendLocationFragment.TAG, "Timer Done")
                c.cancel()
                sendLocation()
            }

        }.start()

        try {

        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    fun connectionListener() {
        val connectedRef = Firebase.database.getReference(".info/connected")
        connectedRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var connected = snapshot.getValue(Boolean::class.java) ?: false
                if (connected) {
                    isOnline = connected
                    Log.d(TAG, "connectionListener: connected")
                } else {
                    isOnline = connected
                    Log.d(TAG, "connectionListener: not connected")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "ConnectionListener was cancelled")
            }
        })
    }

    fun checkRoles() {

        Log.d(TAG, "checkRoles ${sharedViewModel.currentUserData.role.toString()}")
        if (!sharedViewModel.currentUserData.role.equals("user")) {
            binding.bottomNavigation.menu[2].isVisible
            if (sharedViewModel.currentUserData.role.equals("admin")) {
                binding.sendLocationButton.isEnabled = false
                binding.sendLocationText.text = "ADMIN"
                binding.info.isVisible = false
            }
        } else if (sharedViewModel.currentUserData.role.equals("user")) {
            binding.bottomNavigation.menu[2].isVisible = false
        }
    }

    fun loadpref() {
        sharedPreferences = activity?.getPreferences(Context.MODE_PRIVATE)!!
        editor = sharedPreferences.edit()!!
        Log.d(TAG, "sharedpref ${sharedPreferences.all}")
        sharedViewModel.currentUserData = User(
            sharedPreferences.getString("email", ""),
            sharedPreferences.getString("firstname", ""),
            sharedPreferences.getString("lastname", ""),
            sharedPreferences.getString("birthDate", ""),
            sharedPreferences.getString("gender", ""),
            sharedPreferences.getString("address", ""),
            sharedPreferences.getString("role", "null"),
        )
        Log.d(TAG, "sharedvm ${sharedViewModel.currentUserData}")


    }


}



