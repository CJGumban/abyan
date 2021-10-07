package com.example.abyan.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.example.abyan.databinding.FragmentCreateAccount1Binding
import com.example.abyan.viewmodel.ApplicationViewModel


class CreateAccount1Fragment : Fragment() {
    private lateinit var binding: FragmentCreateAccount1Binding
    private val applicationViewModel: ApplicationViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val fragmentBinding = FragmentCreateAccount1Binding.inflate(inflater, container, false)
        binding = fragmentBinding
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.ConfirmButton.setOnClickListener {
            applicationViewModel.firstName = binding.textfieldFirstname.editText?.text.toString()
            applicationViewModel.lastName = binding.textfieldLastname.editText?.text.toString()
            applicationViewModel.emailNumber = binding.textfieldEmail.editText?.text.toString()
            applicationViewModel.password = binding.textfieldPassword.editText?.text.toString()
            val action = CreateAccount1FragmentDirections.actionCreateAccount1FragmentToCreateAccount2Fragment()
            view.findNavController().navigate(action)
        }

        }

    }


