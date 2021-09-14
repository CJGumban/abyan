package com.example.abyan.ui
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.viewModelScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.abyan.MainActivity
import com.example.abyan.databinding.FragmentLoginBinding
import com.example.abyan.viewmodel.LoginSignUpViewModel
import com.google.android.gms.maps.GoogleMap

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private val sharedViewModel : LoginSignUpViewModel by activityViewModels()
    var email: String? = null
    var password: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.signupButton.setOnClickListener {
        val action =    LoginFragmentDirections.actionLoginFragmentToCreateAccount1Fragment()
        view?.findNavController()?.navigate(action)
        }

        sharedViewModel.getMapsDirection()
        binding.loginButton.setOnClickListener {
//          email = binding.textfieldUsername.editText?.text.toString()
//            password = binding.textfieldPassword.editText?.text.toString()
//            if (sharedViewModel.login(email!!, password!!)){
//
//            }
            val action =
                LoginFragmentDirections.actionLoginFragmentToSendLocationFragment()
            view?.findNavController()?.navigate(action)

        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


    }


}