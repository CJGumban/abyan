package com.example.abyan.ui

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.example.abyan.R
import com.example.abyan.databinding.FragmentCreateAccount1Binding
import com.example.abyan.viewmodel.ApplicationViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class CreateAccount1Fragment : Fragment() {
    private lateinit var binding: FragmentCreateAccount1Binding
    private val applicationViewModel: ApplicationViewModel by activityViewModels()
    var auth: FirebaseAuth = Firebase.auth
    private var progressBar: ProgressBar? = null
    var database: DatabaseReference = Firebase.database.reference
    private val appPreference: SharedPreferences? = activity?.getPreferences(
        Context.MODE_PRIVATE)
    private val editor = appPreference?.edit()



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val fragmentBinding = FragmentCreateAccount1Binding.inflate(inflater, container, false)
        binding = fragmentBinding
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        updateUI()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            viewModel = applicationViewModel
            lifecycleOwner = viewLifecycleOwner
            textfieldEmail.editText?.doOnTextChanged { text, start, before, count ->
                applicationViewModel.emailNumber = text.toString()
            }
            textfieldPassword.editText?.doOnTextChanged { text, start, before, count ->
                applicationViewModel.password = text.toString()
            }
            textfieldConfirmpassword.editText?.doOnTextChanged { text, start, before, count ->
                applicationViewModel.confirmPassword = text.toString()
            }
        }
        binding.ConfirmButton.setOnClickListener {
            showProgressBar()
            if (validateForm()){
                register(applicationViewModel.emailNumber.toString(),applicationViewModel.password.toString())
            }

        }
        setProgressBar(R.id.progressBar)


    }


    fun register(email: String, password: String) {
        try {
            auth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener()
                { task ->
                    if (task.isSuccessful){
                        val action = CreateAccount1FragmentDirections.actionCreateAccount1FragmentToHomeFragment()
                        view?.findNavController()?.navigate(action)
                        hideProgressBar()
                        writeNewUser()
                    }
                }

                .addOnFailureListener {
                    Toast.makeText(requireContext(), "${it.localizedMessage}", Toast.LENGTH_SHORT).show()
                    hideProgressBar()
                    Log.d(
                        ApplicationViewModel.TAG, "Create Account: ${email + password}" +
                                "\n Failed  task exception.localizedmsg " + it.localizedMessage
                                + "\n it.cause ${it.cause}")
                }
        }catch (e: Exception){}
    }

    private fun writeNewUser() {


        // Create new post at /user-posts/$userid/$postid and at
        // /posts/$postid simultaneously
        val key = database.push().key
        if (key == null) {
            Log.w(ApplicationViewModel.TAG, "Couldn't get push key for posts")
            return
        }
        Log.w(ApplicationViewModel.TAG, "$key")
        applicationViewModel.registerUser()
        val user = applicationViewModel.currentUserData
        val userValues = user.toMap()

        val childUpdates = hashMapOf<String, Any>(
            "/user/$key" to userValues,

            )

        database.updateChildren(childUpdates)
            .addOnSuccessListener {
                Log.w(ApplicationViewModel.TAG,"it worked")
                editor?.apply() {
                    this.putString("email", user.email)
                    this.putString("firstname",user.firstName)
                    this.putString("lastname",user.lastName)
                    this.putString("birthDate",user.birthDate)
                    this.putString("gender",user.gender)
                    this.putString("address",user.address)
                    this.putString("address",user.role)
                }?.commit()
                updateUI()

            }
            .addOnFailureListener {
                Log.w(ApplicationViewModel.TAG, Exception())
            }

    }

    private fun updateUI() {
        if (auth.currentUser!=null){
            val action =
                CreateAccount1FragmentDirections.actionCreateAccount1FragmentToHomeFragment()
            view?.findNavController()?.navigate(action)

        }

    }

    private fun validateForm(): Boolean {
        var result = true
        if (TextUtils.isEmpty(binding.textfieldEmail.editText?.text.toString())) {
            binding.textfieldEmail.error = "Required"
            result = false
            hideProgressBar()

        } else {
            binding.textfieldEmail.error = null
        }

        if (TextUtils.isEmpty(binding.textfieldPassword.editText?.text.toString())) {
            binding.textfieldPassword.error = "Required"
            result = false
            hideProgressBar()
        } else {
            binding.textfieldPassword.error = null
            binding.textfieldConfirmpassword.error = null
            if (binding.textfieldPassword.editText?.text.toString() != binding.textfieldConfirmpassword.editText?.text?.toString()) {
                binding.textfieldConfirmpassword.error = "Password Mismatch"
                result = false
                hideProgressBar()
            } else {
                result = true
            }
        }
        return result

    }
    fun setProgressBar(resId: Int) {
        progressBar = view?.findViewById(resId)
    }

    fun showProgressBar() {
        progressBar?.visibility = View.VISIBLE
    }

    fun hideProgressBar() {
        progressBar?.visibility = View.INVISIBLE
    }

}


