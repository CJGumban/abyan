package com.example.abyan.ui.home

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.example.abyan.databinding.FragmentProfileBinding
import com.example.abyan.model.User
import com.example.abyan.viewmodel.ApplicationViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import android.net.NetworkInfo

import android.net.ConnectivityManager
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavArgs
import androidx.navigation.fragment.navArgs
import com.example.abyan.ui.LoginFragment
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.getValue


class ProfileFragment : Fragment(){
    private var isOnline: Boolean = false
    var auth: FirebaseAuth = Firebase.auth
    lateinit var sharedPreferences : SharedPreferences
    lateinit var editor: SharedPreferences.Editor
    private val sharedViewModel: ApplicationViewModel by activityViewModels()
    private lateinit var binding: FragmentProfileBinding
    var changeToResponder = false
    val args:ProfileFragmentArgs by navArgs()
    var database: DatabaseReference = Firebase.database.reference

    companion object {
        private const val TAG = "AppTesting"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        return binding.root    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG,"onstart profile" + "${args.isResponder}")
        changeToResponder = args.isResponder
        if (changeToResponder){
            sharedViewModel.currentUserData.role = "responder"
            updateUserData()
        }

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG,"onviewcreated profile")
        sharedViewModel.getFullname()
        binding.apply {
            viewModel = sharedViewModel
            lifecycleOwner = viewLifecycleOwner
            profileFragment = this@ProfileFragment
            buttonSignOut.setOnClickListener {
                    sharedViewModel.auth.signOut()
                    sharedViewModel.auth.addAuthStateListener {
                        try {
                            logout()
                            Log.d(TAG, "${sharedViewModel.auth.currentUser}")
                            Log.d(TAG, "${sharedViewModel.auth.currentUser}")
                            if (sharedViewModel.auth.currentUser == null) {
                                val action =
                                    ProfileFragmentDirections.actionProfileFragmentToLoginFragment()
                                view?.findNavController()?.navigate(action)


                            }
                        } catch (e: java.lang.Exception) {
                            Log.d(TAG, "Error ${e.message}")
                        }
                    }
                }
            buttonResponderAccess.setOnClickListener {
                changeRole()
            }
        }
        binding.buttonEditProfile.setOnClickListener {

            sharedViewModel.userToEdit()
            sharedViewModel.birthDate = sharedViewModel.userToEdit.birthDate
            sharedViewModel.editBirthdateToString()
            val action =
                ProfileFragmentDirections.actionProfileFragmentToEditProfileFragment()
            view?.findNavController()?.navigate(action)
        }
        connectionListener()
        loadpref()
        checkRoles()
        binding.topAppBar.setNavigationOnClickListener {
            val action =
                ProfileFragmentDirections.actionProfileFragmentToHomeFragment()
            view?.findNavController()?.navigate(action)
        }
        Log.d(TAG,"is online ${isOnline()}")

    }
    private fun logout() {
        sharedViewModel.eraseUserData()
        editor.clear().commit()
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


    fun isOnline(): Boolean {
        var connected = false
        try {
            val connectivityManager = activity!!.applicationContext
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connectivityManager.activeNetworkInfo
            connected = networkInfo != null && networkInfo.isAvailable &&
                    networkInfo.isConnected
            return connected
        } catch (e: Exception) {
            println("CheckConnectivity Exception: " + e.message)
            Log.v("connectivity", e.toString())
        }
        return connected
    }

    fun checkRoles(){
        Log.d(TAG,"checkRoles ${sharedViewModel.currentUserData.role.toString()}")
        if (sharedViewModel.currentUserData.role.equals("user")){
            binding.buttonResponderAccess.isChecked = false
        }
        else if (sharedViewModel.currentUserData.role.equals("admin")){
            binding.buttonEditProfile.isEnabled = false
            binding.buttonResponderAccess.isEnabled = false
        }
        else if (sharedViewModel.currentUserData.role.equals("responder")){
            binding.buttonResponderAccess.isEnabled = false
            binding.buttonResponderAccess.isChecked = true
        }
    }

    fun changeRole(){

        val action = ProfileFragmentDirections.actionProfileFragmentToChangeRolesDialog()
        view?.findNavController()?.navigate(action)
    }
    fun loadpref() {
        sharedPreferences = activity?.getPreferences(Context.MODE_PRIVATE)!!
        editor = sharedPreferences.edit()!!
        Log.d(TAG,"sharedpref ${sharedPreferences.all.toString()}")
        sharedViewModel.currentUserData = User(
            sharedPreferences.getString("email",""),
            sharedPreferences.getString("firstname",""),
            sharedPreferences.getString("lastname",""),
            sharedPreferences.getString("birthDate",""),
            sharedPreferences.getString("gender",""),
            sharedPreferences.getString("address",""),
            sharedPreferences.getString("role",""),
        )
        Log.d(TAG,"sharedvm ${sharedViewModel.currentUserData.toString()}")
    }

    fun updateUserData() {
        binding.buttonResponderAccess.isChecked = true
        binding.buttonResponderAccess.isEnabled = false
        Log.d(TAG, "Load user info for edit")
        var key: String? = null
        val userRef = Firebase.database.getReference("user").limitToFirst(1).orderByChild("email")
            .equalTo(auth.currentUser?.email)
        userRef.keepSynced(false)
        userRef.get().addOnSuccessListener {
            Log.d(LoginFragment.TAG, "user get: task success ${it.exists()}")
            var currentUser: User? = it.getValue<User>()
            it.children.forEach { child->
                key = child.key
            }
            val user = sharedViewModel.currentUserData
            val userValues = user.toMap()
            val childUpdates = hashMapOf<String, Any>(
                "/user/$key" to userValues,
            )
            database.updateChildren(childUpdates)
                .addOnSuccessListener {
                    Log.d(TAG,"change data success ${sharedViewModel.currentUserData.toMap()}")
                    sharedViewModel.currentUserData = user
                    editor?.apply {
                        this.putString("email", sharedViewModel.currentUserData.email)
                        this.putString("firstname",sharedViewModel.currentUserData.firstName)
                        this.putString("lastname",sharedViewModel.currentUserData.lastName)
                        this.putString("birthDate",sharedViewModel.currentUserData.birthDate)
                        this.putString("gender",sharedViewModel.currentUserData.gender)
                        this.putString("address",sharedViewModel.currentUserData.address)
                        this.putString("role",sharedViewModel.currentUserData.role)
                    }?.apply()
                    try {
                        Toast.makeText(context, "Profile Saved", Toast.LENGTH_SHORT).show()
                    }catch (e: Exception){}
                    }
                .addOnFailureListener {
                    Toast.makeText(context, it.localizedMessage.toString(), Toast.LENGTH_SHORT).show()
                    sharedViewModel.currentUserData.role = "user"
                    binding.buttonResponderAccess.isChecked = false
                    binding.buttonResponderAccess.isEnabled = true
                }
        }.addOnFailureListener {
            Toast.makeText(context, it.localizedMessage.toString(), Toast.LENGTH_SHORT).show()
            sharedViewModel.currentUserData.role = "user"
            binding.buttonResponderAccess.isChecked = false
            binding.buttonResponderAccess.isEnabled = true
        }

    }


}