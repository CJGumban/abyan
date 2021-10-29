package com.example.abyan.ui

import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
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
        if (validateForm()){
            val action = CreateAccount1FragmentDirections.actionCreateAccount1FragmentToCreateAccount2Fragment()
            view.findNavController().navigate(action)
        }

        }

        }

    private fun validateForm(): Boolean {
        var result = true
        if (TextUtils.isEmpty(binding.textfieldEmail.editText?.text.toString())) {
            binding.textfieldEmail.error = "Required"
            result = false
        } else {
            binding.textfieldEmail.error = null
        }

        if (TextUtils.isEmpty(binding.textfieldPassword.editText?.text.toString())) {
            binding.textfieldPassword.error = "Required"
            result = false
        } else {
            binding.textfieldPassword.error = null
            binding.textfieldConfirmpassword.error = null
            if (binding.textfieldPassword.editText?.text.toString()!=binding.textfieldConfirmpassword.editText?.text?.toString()){
                binding.textfieldConfirmpassword.error = "Password Mismatch"
                result = false
            }
            else {
                result = true
            }
        }

        return result
    }


}


