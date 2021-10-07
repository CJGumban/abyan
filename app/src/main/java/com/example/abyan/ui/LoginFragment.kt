package com.example.abyan.ui
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.example.abyan.databinding.FragmentLoginBinding
import com.example.abyan.viewmodel.ApplicationViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginFragment : Fragment() {
    var auth: FirebaseAuth = Firebase.auth
    private lateinit var binding: FragmentLoginBinding
    private val shareViewModel : ApplicationViewModel by activityViewModels()
    var email: String? = null
    var password: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onStart() {
        super.onStart()
        if (shareViewModel.loggedIn()){
            val action =
                LoginFragmentDirections.actionLoginFragmentToHomeFragment()
            view?.findNavController()?.navigate(action)
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (shareViewModel.auth.currentUser!=null){
            val action =
                LoginFragmentDirections.actionLoginFragmentToHomeFragment()
            view.findNavController().navigate(action)


        }
        binding.signupButton.setOnClickListener {
        val action =    LoginFragmentDirections.actionLoginFragmentToCreateAccount1Fragment()
            view.findNavController().navigate(action)
        }


        binding.loginButton.setOnClickListener {
            email = binding.textfieldUsername.editText?.text.toString()
            password = binding.textfieldPassword.editText?.text.toString()
            login(email!!,password!!)




        }
    }



    fun login(email: String,password: String){

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener{
                    task ->

                if(task.isSuccessful) {
                    Log.d(TAG, "Login Fragment Sign In: Success")
                    updateUI()
                } else {
                    Log.d(TAG, "Login Fragment Sign In: Failed" , task.exception)
                    updateUI()
                }
            }
    }

    private fun updateUI() {
        if (auth.currentUser!=null){
            shareViewModel.auth = auth
            val action =
                LoginFragmentDirections.actionLoginFragmentToHomeFragment()
            view?.findNavController()?.navigate(action)
        }
        else{
            Toast.makeText(requireContext(), "Account doesn't exist", Toast.LENGTH_SHORT).show()
        }
    }

    companion object{
        const val TAG = "AppTesting"
    }

}


