package com.example.abyan.ui
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.example.abyan.R
import com.example.abyan.databinding.FragmentLoginBinding
import com.example.abyan.viewmodel.ApplicationViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginFragment : Fragment() {
    var auth: FirebaseAuth = Firebase.auth
    private val sharedViewModel : ApplicationViewModel by activityViewModels()
    private lateinit var binding: FragmentLoginBinding
    private var progressBar: ProgressBar? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)

        return binding.root

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
        if (sharedViewModel.auth.currentUser!=null){
            val action =
                LoginFragmentDirections.actionLoginFragmentToHomeFragment()
            view.findNavController().navigate(action)


        }
        binding.signupButton.setOnClickListener {
            val action =    LoginFragmentDirections.actionLoginFragmentToCreateAccount1Fragment()
            view.findNavController().navigate(action)
        }


        binding.loginButton.setOnClickListener {
            showProgressBar()

            login()




        }
    }

    override fun onStart() {
        super.onStart()
        if (sharedViewModel.loggedIn()){
            val action =
                LoginFragmentDirections.actionLoginFragmentToHomeFragment()
            view?.findNavController()?.navigate(action)
        }
    }



    fun login(){
        if (!validateForm()) {
            return
        }

        var email = binding.textfieldUsername.editText?.text.toString()
        var password = binding.textfieldPassword.editText?.text.toString()
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener{
                    task ->

                if(task.isSuccessful) {

                    Log.d(TAG, "Login Fragment Sign In: Success")
                    updateUI()
                } else {
                    Log.d(TAG, "Login Fragment Sign In: Failed" , task.exception)
                    Toast.makeText(requireContext(), "Account doesn't exist: ${task.exception}", Toast.LENGTH_SHORT).show()

                    updateUI()
                }
            }
    }

    private fun updateUI() {
        hideProgressBar()
        if (auth.currentUser!=null){
            sharedViewModel.auth = auth
            val action = LoginFragmentDirections.actionLoginFragmentToHomeFragment()
            view?.findNavController()?.navigate(action)
        }
        else{
           Toast.makeText(requireContext(), "Login Error", Toast.LENGTH_SHORT).show()
        }
    }



    private fun validateForm(): Boolean {
        var result = true
        if (TextUtils.isEmpty(binding.textfieldUsername.editText?.text.toString())) {
            binding.textfieldUsername.error = "Required"
            result = false
        } else {
            binding.textfieldUsername.error = null
        }

        if (TextUtils.isEmpty(binding.textfieldPassword.editText?.text.toString())) {
            binding.textfieldPassword.error = "Required"
            result = false
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
    companion object{
        const val TAG = "AppTesting"
    }

}


