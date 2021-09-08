package com.example.abyan.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.abyan.databinding.FragmentConfirmUserDataBinding
import com.example.abyan.viewmodel.LoginSignUpViewModel


class ConfirmUserDataFragment : Fragment() {
    private var binding: FragmentConfirmUserDataBinding? = null
    private val loginSignUpViewModel: LoginSignUpViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val fragmentBinding = FragmentConfirmUserDataBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = loginSignUpViewModel
            confirmUserDataFragment = this@ConfirmUserDataFragment
        }

    }

}