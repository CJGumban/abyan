package com.example.abyan.viewmodel

import android.text.format.DateFormat
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.abyan.model.Coordinate
import com.example.abyan.model.MarkerOnRoute
import com.example.abyan.model.Post
import com.example.abyan.model.User
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Period
import java.util.*
import java.util.Locale
import kotlin.collections.ArrayList


class ApplicationViewModel : ViewModel() {

    var auth : FirebaseAuth = Firebase.auth
    var database: DatabaseReference = Firebase.database.reference
    private var currentUser   = auth.currentUser

    lateinit var coordinatesRef: DatabaseReference
    lateinit var coordinatesListener: ValueEventListener
    var currentUserData = User()
    var userToEdit = User()

    var birthdateToString:String = ""
    var emailNumber:String? = ""
    var confirmPassword:String? = null
    var password:String? = null
    var firstName: String? = null
    var lastName: String? = null
    var birthDate: String? = ""
    var gender: String? = null
    var address: String? = null
    var longitude: Double? = null
    var latitude: Double? = null
    var type: String? = null
    var latlngList: MutableList<LatLng> = ArrayList()
    var coordinatelist: MutableList<Coordinate> = ArrayList()
    var postList: MutableList<Post> = ArrayList()
    var markerOnRoute = MarkerOnRoute(null, null,false)
    companion object{
        const val TAG = "AppTesting"
    }



    // TODO: 16/11/2021 {wrong date}
    fun birthdateToString(){
        if (birthDate.equals("")){

        }else if (!birthDate.equals("")){
            var timestamp: Long? = birthDate?.toLong()
            var date: Date = Date(timestamp?.toLong()!!)
            var dateFormat: SimpleDateFormat = SimpleDateFormat("YYYY/MM/dd")
            birthdateToString  = dateFormat.format(date)
            Log.d("testingthis","${timestamp}")

            Log.d(TAG,"birth $birthdateToString")}
    }
    fun editBirthdateToString(){

            var timestamp: Long? = userToEdit.birthDate?.toLong()
            var date: Date = Date(timestamp?.toLong()!!)
            var dateFormat: SimpleDateFormat = SimpleDateFormat("YYYY/MM/dd")
            birthdateToString  = dateFormat.format(date)
            Log.d(TAG,"birth $birthdateToString")}



    fun userToEdit(){
        userToEdit = currentUserData
    }
    var getFullname: String = ""
    fun getFullname() {
        getFullname = "${currentUserData.firstName.toString()} ${currentUserData.lastName}"
    }
    fun getLocationsListener() {
        // [START basic_listen]
        // Get a reference to Messages and attach a listener
        try {


        coordinatesRef = database.child("active-coordinates")
        coordinatesListener = object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // New data at this path. This method will be called after every change in the
                // data at this path or a subpath.
                coordinatelist.clear()
                Log.d(TAG, "Number of coordinates: ${dataSnapshot.childrenCount}")

                dataSnapshot.children.forEach { child ->
                    // Extract Post object from the DataSnapshot
                    val coordinate: Coordinate? = child.getValue<Coordinate>()


                    coordinatelist.add(Coordinate(
                        coordinate?.key,
                        coordinate?.email,
                        fullname = coordinate?.fullname,
                        gender = coordinate?.gender,
                        age = coordinate?.age,
                        lat = coordinate?.lat,
                        lng = coordinate?.lng,
                        status = coordinate?.status,
                        type = coordinate?.type,
                        dateTime = coordinate?.dateTime,
                    ))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Could not successfully listen for data, log the error
                Log.e(TAG, "coordinates:onCancelled: ${error.message}")
            }
        }
        coordinatesRef.addValueEventListener(coordinatesListener)
        // [END basic_listen]
    }catch (e: java.lang.Exception){
        Log.e(TAG, "error ${e.message}")
    }
    }

//even listener for a single item
    fun addPostEventListener(postReference: DatabaseReference) {
        // [START post_value_event_listener]
        val postListener = object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI

                val post = dataSnapshot.getValue<Coordinate>()

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }
        postReference.addValueEventListener(postListener)
        //read once using a listener
        postReference.addValueEventListener(postListener)
        // [END post_value_event_listener]

    }

    fun sendLocation() {
        var userExist:Boolean = false
        var currentCoordinateKey: String? = null
       coordinatelist.forEach { coordinate ->
           if (auth.currentUser?.email == coordinate.email ){
               userExist = true
               currentCoordinateKey = coordinate.key
               Log.d(TAG, "sendLocationMethod: Current user exist")
           }
       }
        if (userExist){
            var tsLong = System.currentTimeMillis() / 1000
            Log.d(TAG, " timestamp  $tsLong")
            val cal = Calendar.getInstance(Locale.ENGLISH)
            cal.timeInMillis = tsLong * 1000L
            val date: String = DateFormat.format("MM-dd hh:mm", cal).toString()
            Log.d(TAG,"time stamp to date: $date")
            val userId = auth.currentUser?.uid.toString()
            val email = auth.currentUser?.email
            // Create new post at /user-posts/$userid/$postid and at
            // /posts/$postid simultaneously
            val key = database.push().key
            if (key == null) {
                Log.w(TAG, "Couldn't get push key for posts")
                return
            }
            Log.w(TAG, "$key")


            val coordinate = Coordinate(
                currentCoordinateKey,
                email,
                lat = latitude,
                lng = longitude,
                status = "need help",
                type = type,
                dateTime = date
            )
            val coordinateValues = coordinate.toMap()

            val childUpdates = hashMapOf<String, Any>(
                "coordinates/$currentCoordinateKey" to coordinateValues,
                "active-coordinates/$currentCoordinateKey" to coordinateValues,

            )

            database.updateChildren(childUpdates)
                .addOnSuccessListener { Log.w(TAG,"it worked")}
                .addOnFailureListener {
                    Log.w(TAG, "failed")
                }

        } else if(!userExist){
            var tsLong = System.currentTimeMillis() / 1000
            Log.d(TAG, " timestamp  $tsLong")
            val cal = Calendar.getInstance(Locale.ENGLISH)
            cal.timeInMillis = tsLong * 1000L
            val date: String = DateFormat.format("MM-dd hh:mm", cal).toString()
            Log.d(TAG,"time stamp to date: $date")

            val userId = auth.currentUser?.uid.toString()
            val email = auth.currentUser?.email
            // Create new post at /user-posts/$userid/$postid and at
            // /posts/$postid simultaneously
            val key = database.push().key
            if (key == null) {
                Log.w(TAG, "Couldn't get push key for posts")
                return
            }
            Log.w(TAG, "$key")


            val coordinate = Coordinate(
                key,
                email,
                lat = latitude,
                lng = longitude,
                status = "need help",
                type = type,
                dateTime = date
            )
            val coordinateValues = coordinate.toMap()

            val childUpdates = hashMapOf<String, Any>(
                "coordinates/$key" to coordinateValues,
                "active-coordinates/$key" to coordinateValues,
         //"active-coordinates/@email" to coordinateValues,
         )
            database.updateChildren(childUpdates)
                .addOnSuccessListener { Log.w(TAG,"it worked")}
                .addOnFailureListener {
                    Log.w(TAG, "failed")
                }
        }


    }

    fun activateRoute(marker: Marker){
        markerOnRoute.marker = marker
        markerOnRoute.coordinate = marker.tag as Coordinate
        markerOnRoute.isActive = true
    }
    fun deactivateRoute() {
        markerOnRoute.marker = null
        markerOnRoute.coordinate = null
        markerOnRoute.isActive = false
    }

    fun changeStatus(coordinate: Coordinate, function:String){
        var key:String = coordinate.key.toString()
        when(function){
            "need help"->{
                coordinate.status = "need help"
                val coordinateValues = coordinate.toMap()
                val childUpdates = hashMapOf<String, Any>(
                    "coordinates/$key" to coordinateValues,
                    "active-coordinates/$key" to coordinateValues,
                )
                database.updateChildren(childUpdates)
                    .addOnSuccessListener { Log.w(TAG,"change status to need help: Success")}
                    .addOnFailureListener {
                        Log.w(TAG, "change status to need help : Failed")
                    }
            }
            "ongoing"->{
                coordinate.status = "ongoing"
                val coordinateValues = coordinate.toMap()
                val childUpdates = hashMapOf<String, Any>(
                    "coordinates/$key" to coordinateValues,
                    "active-coordinates/$key" to coordinateValues,
                )
                database.updateChildren(childUpdates)
                    .addOnSuccessListener { Log.w(TAG,"change status to ongoing: Success")}
                    .addOnFailureListener {
                        Log.w(TAG, "change status to ongoing: Failed")
                    }
            }
            "done"->{
                 coordinate.status = "done"
                val coordinateValues = coordinate.toMap()
                val childUpdates = hashMapOf<String, Any>(
                    "coordinates/$key" to coordinateValues,
                    "active-coordinates/$key" to coordinateValues,
                )
                database.updateChildren(childUpdates)
                    .addOnSuccessListener { Log.w(TAG,"change status to done: Success")}
                    .addOnFailureListener {
                        Log.w(TAG, "change status to done: Failed")
                    }}
            "delete"->{

                database.child("active-coordinates").child("$key").removeValue()
                    .addOnSuccessListener {
                        Log.d(TAG,"Change status: Delete success")
                    }
                    .addOnFailureListener {
                        Log.d(TAG,"Change status: Delete failure")
                    }
            }
        }
    }

    fun registerUser(){
        currentUserData = User(emailNumber,firstName, lastName, birthDate, gender, address,"user")
    }
    fun eraseUserData(){
        currentUserData = User("","", "", "", "", "","")
        password = ""
    }
    fun getPostListener() {
        // [START basic_listen]
        // Get a reference to Messages and attach a listener
        try {


            coordinatesRef = database.child("post")
            coordinatesListener = object : ValueEventListener {

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // New data at this path. This method will be called after every change in the
                    // data at this path or a subpath.
                    postList.clear()
                    dataSnapshot.children.forEach { child ->
                        val post: Post? = child.getValue<Post>()
                        postList.add(post!!)
                    }
                    postList.reverse()
                }

                override fun onCancelled(error: DatabaseError) {
                    // Could not successfully listen for data, log the error
                    Log.e(TAG, "coordinates:onCancelled: ${error.message}")
                }
            }
            coordinatesRef.addValueEventListener(coordinatesListener)
            // [END basic_listen]
        }catch (e: java.lang.Exception){
            Log.e(TAG, "error ${e.message}")
        }
    }

    fun getAge(): String? {
        var timestamp: Long? = userToEdit.birthDate?.toLong()
        var date: Date = Date(timestamp?.toLong()!!)
        var year: Int = SimpleDateFormat("YYYY").format(date).toInt()
        var month: Int = SimpleDateFormat("MM").format(date).toInt()
        var dayOfMonth: Int = SimpleDateFormat("dd").format(date).toInt()
        return Period.between(
            LocalDate.of(year, month, dayOfMonth),
            LocalDate.now()
        ).years.toString()
    }
    var getAge:String = ""
    fun getAge2() {
        var timestamp: Long? = currentUserData.birthDate?.toLong()
        var date: Date = Date(timestamp?.toLong()!!)
        var year: Int = SimpleDateFormat("YYYY").format(date).toInt()
        var month: Int = SimpleDateFormat("MM").format(date).toInt()
        var dayOfMonth: Int = SimpleDateFormat("dd").format(date).toInt()
        getAge = Period.between(
            LocalDate.of(year, month, dayOfMonth),
            LocalDate.now()
        ).years.toString()
    }

    fun resetData() {
        emailNumber = ""
        firstName = ""
        lastName = ""
        birthDate = ""
        birthdateToString = ""
        gender = ""
        address = ""
    }


}
