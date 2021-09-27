package com.example.abyan.viewmodel

import android.text.format.DateFormat
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.abyan.model.Coordinate
import com.example.abyan.model.User
import com.google.android.gms.maps.model.LatLng
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
import java.util.concurrent.TimeUnit
import java.util.Locale
import kotlin.time.milliseconds


class ApplicationViewModel : ViewModel() {
    var auth : FirebaseAuth = Firebase.auth
    var database: DatabaseReference = Firebase.database.reference
    private var userMutableLiveData = MutableLiveData<FirebaseUser>()
    private var currentUser   = userMutableLiveData.value
    lateinit var coordinatesRef: DatabaseReference
    lateinit var coordinatesListener: ValueEventListener

    var lastName: String? = null
    var firstName: String? = null
    var emailNumber:String? = null
    var password:String? = null
    var gender: String? = null
    var birthDate: String? = null
    var address: String? = null
    var longitude: Double? = null
    var latitude: Double? = null
    var latlngList: MutableList<LatLng> = ArrayList()
    var coordinatelist: MutableList<Coordinate> = ArrayList()
    companion object{
        const val TAG = "AppTesting"
    }

    fun signOut() {
        //method to sign out account
        Firebase.auth.signOut()

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
                    // Extract Message object from the DataSnapshot
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


        val coordinate = Coordinate(key, email, latitude, longitude,"need help", null, date)
        val coordinateValues = coordinate.toMap()

        val childUpdates = hashMapOf<String, Any>(
            "coordinates/$key" to coordinateValues,
            "active-coordinates/$key" to coordinateValues,

            //"/user-coordinates/$email/$key" to coordinateValues
        )

        database.updateChildren(childUpdates)
            .addOnSuccessListener { Log.w(TAG,"it worked")}
            .addOnFailureListener {
                Log.w(TAG, "failed")
            }

    }

    fun register(email: String, password: String) {
            auth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener()
                {
                        task ->
                    if (task.isSuccessful){
                        userMutableLiveData.postValue(auth.currentUser)
                        Log.d(TAG, "Create Account: Success $userMutableLiveData")

                    }else{
                        Log.d(TAG, "Create Account: ${email + password} Failed" + task.exception)
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

    fun createAccount(){
        register(emailNumber.toString() ,password.toString())
        writeNewPost()
    }

    private fun writeNewPost() {
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

        val user = User(email, firstName, lastName, birthDate, gender, address)
        val userValues = user.toMap()

        val childUpdates = hashMapOf<String, Any>(
            "/user/$key" to userValues,

        )

        database.updateChildren(childUpdates)
            .addOnSuccessListener { Log.w(TAG,"it worked")}
            .addOnFailureListener {
                Log.w(TAG, Exception())
            }

    }

    fun loggedIn():Boolean{
        return currentUser != null
    }






}
