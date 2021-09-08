package com.example.abyan.ui

import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.example.abyan.R
import com.example.abyan.databinding.FragmentCreateAccount2Binding
import com.example.abyan.viewmodel.LoginSignUpViewModel


class CreateAccount2Fragment : Fragment() {
    private lateinit var binding: FragmentCreateAccount2Binding
    private val loginSignUpViewModel: LoginSignUpViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragmentBinding = FragmentCreateAccount2Binding.inflate(inflater, container, false)
        binding = fragmentBinding
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.ConfirmButton.setOnClickListener{
            loginSignUpViewModel.address = binding.textfieldAddress.editText?.text.toString()
            loginSignUpViewModel.birthDate = binding.textfieldBirthDate.editText?.text.toString()
            loginSignUpViewModel.gender = binding.textfieldGender.editText?.text.toString()
            Log.d("try","firstName ${loginSignUpViewModel.firstName.toString()}")
            val action =
                CreateAccount2FragmentDirections.actionCreateAccount2FragmentToConfirmUserDataFragment()
            view?.findNavController()?.navigate(action)

        }
    }

}