package com.example.abyan.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.example.abyan.databinding.FragmentCreateAccount2Binding
import com.example.abyan.viewmodel.ApplicationViewModel


class CreateAccount2Fragment : Fragment() {
    private lateinit var binding: FragmentCreateAccount2Binding
    private val applicationViewModel: ApplicationViewModel by activityViewModels()

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
            applicationViewModel.address = binding.textfieldAddress.editText?.text.toString()
            applicationViewModel.birthDate = binding.textfieldBirthDate.editText?.text.toString()
            applicationViewModel.gender = binding.textfieldGender.editText?.text.toString()
            Log.d("try","firstName ${applicationViewModel.firstName.toString()}")
            val action =
                CreateAccount2FragmentDirections.actionCreateAccount2FragmentToConfirmUserDataFragment()
            view.findNavController().navigate(action)

        }
    }

}