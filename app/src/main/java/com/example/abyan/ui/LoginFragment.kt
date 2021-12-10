package com.example.abyan.ui

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.example.abyan.R
import com.example.abyan.databinding.FragmentLoginBinding
import com.example.abyan.model.User
import com.example.abyan.viewmodel.ApplicationViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlin.String as String

class LoginFragment : Fragment() {
    var auth: FirebaseAuth = Firebase.auth
    lateinit var sharedPreferences : SharedPreferences
    lateinit var editor: SharedPreferences.Editor
    private val sharedViewModel: ApplicationViewModel by activityViewModels()
    private lateinit var binding: FragmentLoginBinding
    private var progressBar: ProgressBar? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*  mFirebaseDatabase.setPersistenceEnabled(true)*/
        val callback = requireActivity().onBackPressedDispatcher.addCallback(this) {
            // Handle the back button event
            requireActivity().finish()

        }
        sharedViewModel.resetData()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)

        return binding.root


    }
    fun loadpref() {
        sharedPreferences = activity?.getPreferences(Context.MODE_PRIVATE)!!
        editor = sharedPreferences.edit()!!
        /*val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
            putString("Username", "sharedprefun")
            putString("Password", "sharedPrefpw")
            apply()
        }*/

 /*       editor?.putString("Username", "sharpened")
        editor?.apply()
        Log.d(TAG,"sharedpref ${activity?.getPreferences(Context.MODE_PRIVATE)?.all.toString()}")
        Log.d(TAG,"sharedpref ${sharedPreferences?.all.toString()}")

*/


    }
    fun net(): Boolean {
        val cm = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        val isConnected: Boolean = activeNetwork?.isConnectedOrConnecting == true
        return isConnected
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedViewModel.resetData()
        loadpref()

//       allowOffline()
        binding.apply {
            viewModel = sharedViewModel
            lifecycleOwner = viewLifecycleOwner
            loginFragment = this@LoginFragment

            textfieldUsername.editText?.doOnTextChanged { text, start, before, count ->
                sharedViewModel.emailNumber = text.toString()
            }
            textfieldPassword.editText?.doOnTextChanged { text, start, before, count ->
                sharedViewModel.password = text.toString()
            }
        }


        setProgressBar(R.id.progressBar)
/*        if (sharedViewModel.auth.currentUser != null) {
            val action =
                LoginFragmentDirections.actionLoginFragmentToHomeFragment()
            view.findNavController().navigate(action)
        }*/
        binding.signupButton.setOnClickListener {
            sharedViewModel.birthDate=""
            val action = LoginFragmentDirections.actionLoginFragmentToCreateAccount2Fragment()
            view.findNavController().navigate(action)
        }


        binding.loginButton.setOnClickListener {
            showProgressBar()

            login()


        }
    }

     fun loadUserInfo(){
        Log.d(TAG, "Load user info")

        val userRef = Firebase.database.getReference("user").limitToFirst(1).orderByChild("email")
            .equalTo(auth.currentUser?.email)
        userRef.keepSynced(false)

        /*userRef.get().addOnSuccessListener {
            sharedViewModel.currentUserData = it.value as User
            Log.d(TAG, "currentuserdata ${sharedViewModel.currentUserData}")
        }.addOnFailureListener {
            Toast.makeText(context, "${it.localizedMessage}", Toast.LENGTH_SHORT).show()
        }*/

        /*sharedViewModel.currentUserData = userRef.get().result.value as User
        Log.d(TAG, "currentuserdata ${sharedViewModel.currentUserData}")*/



        userRef.get().addOnSuccessListener {

            Log.d(TAG, "user get: task success ${it.exists()}")

            var currentUser: User? = it.getValue<User>()
            it.children.forEach { child->
                sharedViewModel.currentUserData = child.getValue<User>()!!

            }
            Log.d(TAG,"Current user ${currentUser?.toMap().toString()}")
            Log.d(TAG,"shareviewmodel.currentuserdata ${sharedViewModel.currentUserData.toMap()}")


            editor?.apply {
                this.putString("email", sharedViewModel.currentUserData.email)
                this.putString("firstname",sharedViewModel.currentUserData.firstName)
                this.putString("lastname",sharedViewModel.currentUserData.lastName)
                this.putString("birthDate",sharedViewModel.currentUserData.birthDate)
                this.putString("gender",sharedViewModel.currentUserData.gender)
                this.putString("address",sharedViewModel.currentUserData.address)
                this.putString("role",sharedViewModel.currentUserData.role)
            }.apply()



//            sharedViewModel.currentUserData = it.value as User

            Log.d(TAG, "loginFragment loadUserInfo sharedViewModel.currentUserData ${sharedViewModel.currentUserData}")

            Log.d(TAG, "loginFragment loadUserInfo apppreference.all ${sharedPreferences.all}")

            Log.d(TAG, "user get: task success ${it.value}")
            updateUI()
        }.addOnFailureListener {
            Log.d(TAG, "task failed ${it.localizedMessage}")
        }


        userRef.get().addOnCompleteListener { task ->
 /*           if (task.isSuccessful) {
                Log.d(TAG, "task success ${task.result.value}")
            } else {
                Log.d(TAG, "task failed ${task.exception?.localizedMessage}")
            }*/
        }

     /*   userRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChild: String?) {

//                sharedViewModel.currentUserData = snapshot.value as User
//                var ds: User? = snapshot.getValue<User>()
//                Log.d(TAG, "ds value ${ds?.toMap().toString()}")

//                sharedViewModel.currentUserData = userRef.get().result.value as User
                Log.d(TAG, "currentuserdata ${sharedViewModel.currentUserData}")
                Log.d(TAG, "snapshot count ${snapshot.childrenCount}")
                Log.d(TAG, "snapshot value ${snapshot.value}")

            }

            // [START_EXCLUDE]
            override fun onChildRemoved(dataSnapshot: DataSnapshot) = Unit

            override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) = Unit

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d(TAG, "database error ${databaseError.message}")
            }


            override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {

            }
        })*/



    }

    fun allowOffline() {

//        val userRef = Firebase.database.getReference("user").orderByKey()
//            .equalTo("-Ml3A5hwbGQ76DkO7Xnn")
//        userRef.keepSynced(true)
//
//        userRef.addChildEventListener(object : ChildEventListener {
//            override fun onChildAdded(snapshot: DataSnapshot, previousChild: String?) {
//
//                Log.d(TAG, "snapshot count ${snapshot.childrenCount}")
//                Log.d(TAG, "snapshot value ${snapshot.value}")
//
//            }
//
//            // [START_EXCLUDE]
//            override fun onChildRemoved(dataSnapshot: DataSnapshot) = Unit
//
//            override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) = Unit
//
//            override fun onCancelled(databaseError: DatabaseError) = Unit
//
//            override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
//
//            }
//        })

    }


    override fun onStart() {
        super.onStart()
        CoroutineScope(Default).launch{
            updateUI()
        }
    }


    fun login() {
        if (!validateForm()) {
            return
        }

        var email = binding.textfieldUsername.editText?.text.toString()
        var password = binding.textfieldPassword.editText?.text.toString()
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                hideProgressBar()
                if (task.isSuccessful) {

                    Log.d(TAG, "Login Fragment Sign In: Success")

                        loadUserInfo()




                } else if (task.isCanceled) {
                    Log.d(TAG, "Login Fragment Sign In: Failed", task.exception)
                    Toast.makeText(
                        requireContext(),
                        "${task.exception?.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()

                   /* updateUI()*/
                }
            }.addOnFailureListener {
                hideProgressBar()
                Log.d(TAG, "Login Fragment Sign In: Failed ${it.localizedMessage}")
                Toast.makeText(
                    requireContext(),
                    "${it.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }

    }

    private fun updateUI() {

            hideProgressBar()
            try{
                if (auth.currentUser != null) {

                    sharedViewModel.auth = auth
                    val action = LoginFragmentDirections.actionLoginFragmentToHomeFragment()
                    view?.findNavController()?.navigate(action)

                } else {

                }
            }catch (e: Exception){}


        }



    private fun validateForm(): Boolean {
        var result = true
        if (TextUtils.isEmpty(binding.textfieldUsername.editText?.text.toString())) {
            binding.textfieldUsername.error = "Required"
            result = false
            hideProgressBar()
        } else {
            binding.textfieldUsername.error = null
        }

        if (TextUtils.isEmpty(binding.textfieldPassword.editText?.text.toString())) {
            binding.textfieldPassword.error = "Required"
            result = false
            hideProgressBar()
        } else {
            binding.textfieldPassword.error = null
        }

        return result
    }

    val uid: String
        get() = Firebase.auth.currentUser!!.uid


    fun setProgressBar(resId: Int) {
        progressBar = view?.findViewById(resId)
    }

    fun showProgressBar() {
        progressBar?.visibility = View.VISIBLE
    }

    fun hideProgressBar() {
        progressBar?.visibility = View.INVISIBLE
    }

    companion object {
        const val TAG = "AppTesting"
    }

}


