package com.example.abyan.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.abyan.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.lang.Exception

class LoginSignUpViewModel : ViewModel() {
    private var auth : FirebaseAuth = Firebase.auth
    private val database = Firebase.database.reference
    private val userMutableLiveData = MutableLiveData<FirebaseUser>()
    private val currentUser = userMutableLiveData.value

    var lastName: String? = null
    var firstName: String? = null
    var emailNumber:String? = null
    var password:String? = null
    var gender: String? = null
    var birthDate: String? = null
    var address: String? = null



    companion object{
        private const val TAG_AUTH = "Auth"
    }



    fun register(email: String, password: String) {
            auth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener()
                {
                        task ->
                    if (task.isSuccessful){
                        userMutableLiveData.postValue(auth.currentUser)
                        Log.d(TAG_AUTH, "Create Account: Success ${userMutableLiveData.toString()}")

                    }else{
                        Log.d(TAG_AUTH, "Create Account: ${email + password} Failed" + task.exception)
                    }

        }
    }

    fun login(email: String,password: String){
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener{
                task ->
                if(task.isSuccessful) {
                    Log.d(TAG_AUTH, "Sign In: Success")
                } else {
                    Log.d(TAG_AUTH, "Sign In: Failed" , task.exception)
                }
            }
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
            Log.w(TAG_AUTH, "Couldn't get push key for posts")
            return
        }
        Log.w(TAG_AUTH, "$key")

        val user = User(email, firstName, lastName, birthDate, gender, address)
        val userValues = user.toMap()

        val childUpdates = hashMapOf<String, Any>(
            "/user/$key" to userValues,

        )

        database.updateChildren(childUpdates)
            .addOnSuccessListener { Log.w(TAG_AUTH,"it worked")}
            .addOnFailureListener {
                Log.w(TAG_AUTH, Exception())
            }

    }

    fun loggedIn():Boolean{
        return currentUser != null
    }


}