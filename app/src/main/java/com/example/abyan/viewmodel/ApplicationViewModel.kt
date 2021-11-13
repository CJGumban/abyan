package com.example.abyan.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.text.format.DateFormat
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.abyan.model.Coordinate
import com.example.abyan.model.MarkerOnRoute
import com.example.abyan.model.Post
import com.example.abyan.model.User
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import java.security.Timestamp
import java.text.SimpleDateFormat
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


    var birthdateToString:String = ""
    var emailNumber:String? = ""
    var confirmPassword:String? = null
    var password:String? = null
    var firstName: String? = null
    var lastName: String? = null
    var birthDate: String? = null
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

   /* fun allowOffline(){
        Firebase.database.setPersistenceEnabled(true)

        val userRef = Firebase.database.getReference("user")
        userRef.keepSynced(true)

    }*/
        fun getUser() {

            database.child("user").get().addOnSuccessListener {
                Log.i(TAG, "user. children ${it.children }}" +
                        "\n datasnapshot.value ${it.value}" +
                        "\n key ${it.key}" +
                        "\n priority ${it.priority}" +
                        "\n ref ${it.ref}")
            }.addOnFailureListener {
                Log.e("firebase", "Error getting data", it)
            }

        }

    fun birthdateToString(){
        if (birthDate.isNullOrEmpty()){

        }else if (!birthDate.isNullOrEmpty()){
            var timestamp: Long? = currentUserData?.birthDate?.toLong()
            var date: Date = Date(timestamp?.toLong()!!)
            var dateFormat: SimpleDateFormat = SimpleDateFormat("YYYY/MM/dd")
            birthdateToString  = dateFormat.format(date)
            Log.d(TAG,"birth ${birthdateToString.toString()}")}


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
                        coordinate?.lat,
                        coordinate?.lng,
                        coordinate?.status,
                        coordinate?.type,
                        coordinate?.dateTime,
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
                // ...
//                Log.w(TAG, "loadPost: Success ${post.toString()}")
//                Log.w(TAG, "loadPost: email ${post?.email.toString()}")
//                Log.w(TAG, "loadPost: lat ${post?.lat.toString()}")
//                Log.w(TAG, "loadPost: lng ${post?.lng.toString()}")


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


            val coordinate = Coordinate(currentCoordinateKey, email, latitude, longitude,"need help", type, date)
            val coordinateValues = coordinate.toMap()

            val childUpdates = hashMapOf<String, Any>(
                "coordinates/$currentCoordinateKey" to coordinateValues,
                "active-coordinates/$currentCoordinateKey" to coordinateValues,


                //coordinates gets record of all of the coordinates send
                //active-coordinates records coordinates for each email if email gets new coordinate the older coordinates get overwritten
                //"active-coordinates/@email" to coordinateValues,


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


            val coordinate = Coordinate(key, email, latitude, longitude,"need help", type, date)
            val coordinateValues = coordinate.toMap()

            val childUpdates = hashMapOf<String, Any>(
                "coordinates/$key" to coordinateValues,
                "active-coordinates/$key" to coordinateValues,


                //coordinates gets record of all of the coordinates send
                //active-coordinates records coordinates for each email if email gets new coordinate the older coordinates get overwritten
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
        markerOnRoute?.marker = marker
        markerOnRoute?.coordinate = marker.tag as Coordinate
        markerOnRoute?.isActive = true
    }
    fun deactivateRoute() {
        markerOnRoute?.marker = null
        markerOnRoute?.coordinate = null
        markerOnRoute?.isActive = false
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

    }

    fun register(email: String, password: String) {
            auth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener()
                { task ->
                    if (task.isSuccessful){
                    }else if(task.isComplete){
                        Log.d(TAG, "Create Account: ${email + password}" +
                                "\n Failed  task exception " + task.exception
                                + "\n task results ${task.result}")
                    }
        }
    }

    fun login(email: String,password: String): Boolean{

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener{
                task ->

                if(task.isSuccessful) {
                    Log.d(TAG, "Sign In: Success")
                    true
                } else {
                    Log.d(TAG, "Sign In: Failed" , task.exception)
                    false
                }
            }


        return true
    }







    fun deletePost(key:String) {
        database.child("post").child("$key").removeValue()
            .addOnSuccessListener {
                Log.d(TAG,"Change status: Delete success")
            }
            .addOnFailureListener {
                Log.d(TAG,"Change status: Delete failure")
            }


    }
    fun editPost(post: Post, title: String = "",body: String = "") {
        post.title = title
        post.body = body
        val postValues = post.toMap()
        val childUpdates = hashMapOf<String, Any>(
            "post/${post.key}" to postValues,
            "user-post/${post.key}" to postValues,
        )
        database.updateChildren(childUpdates)
            .addOnSuccessListener { Log.w(TAG,"edit post successful")}
            .addOnFailureListener {
                Log.w(TAG, "edit post Failed")
            }}



     fun writePost(title: String = "",body: String = "") {


        var tsLong = System.currentTimeMillis() / 1000
        Log.d(TAG, " timestamp  $tsLong")
        val cal = Calendar.getInstance(Locale.ENGLISH)
        cal.timeInMillis = tsLong * 1000L
        val date: String = DateFormat.format("MMM dd, yyyy", cal).toString()

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

        val message = Post(userId,key,email,title, body, date)


        val messageValues = message.toMap()

        val childUpdates = hashMapOf<String, Any>(
            "/post/$key" to messageValues,
            "user-post/$userId/$key" to messageValues

            )

        database.updateChildren(childUpdates)
            .addOnSuccessListener { Log.w(TAG,"it worked")}
            .addOnFailureListener {
                Log.w(TAG, Exception())
            }

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
                        // Extract Post object from the DataSnapshot
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




    fun loggedIn():Boolean{
        return currentUser != null
    }






}
