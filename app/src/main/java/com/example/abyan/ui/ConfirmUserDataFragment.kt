package com.example.abyan.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.abyan.databinding.FragmentConfirmUserDataBinding
import com.example.abyan.model.User
import com.example.abyan.viewmodel.ApplicationViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class  ConfirmUserDataFragment : Fragment() {
    private var binding: FragmentConfirmUserDataBinding? = null
    private val applicationViewModel: ApplicationViewModel by activityViewModels()
    var auth: FirebaseAuth = Firebase.auth
    var database: DatabaseReference = Firebase.database.reference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val fragmentBinding = FragmentConfirmUserDataBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = applicationViewModel
            confirmUserDataFragment = this@ConfirmUserDataFragment


        }


    }



    fun createAccount(){
        register(applicationViewModel.emailNumber.toString() ,applicationViewModel.password.toString())

    }


    fun register(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener()
            {
                    task ->
                if (task.isSuccessful){
                    writeNewUser()
                }else{
                    Log.d(ApplicationViewModel.TAG, "Create Account: ${email + password} Failed" + task.exception)
                }
            }
    }

    private fun writeNewUser() {
        val email = auth.currentUser?.email
        // Create new post at /user-posts/$userid/$postid and at
        // /posts/$postid simultaneously
        val key = database.push().key
        if (key == null) {
            Log.w(ApplicationViewModel.TAG, "Couldn't get push key for posts")
            return
        }
        Log.w(ApplicationViewModel.TAG, "$key")
        val user = applicationViewModel.currentUserData
        val userValues = user.toMap()

        val childUpdates = hashMapOf<String, Any>(
            "/user/$key" to userValues,

            )

        database.updateChildren(childUpdates)
            .addOnSuccessListener {
                Log.w(ApplicationViewModel.TAG,"it worked")
                applicationViewModel.currentUserData = user
            }
            .addOnFailureListener {
                Log.w(ApplicationViewModel.TAG, Exception())
            }

    }


}