package com.example.abyan.ui

import android.os.Bundle
import android.text.Editable
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StyleRes
import androidx.core.view.get
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.example.abyan.databinding.FragmentCreateAccount2Binding
import com.example.abyan.viewmodel.ApplicationViewModel
import com.google.android.material.datepicker.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.time.milliseconds


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
            applicationViewModel.currentUserData.firstName = binding.textfieldFirstname.editText?.text.toString()
            applicationViewModel.currentUserData.lastName = binding.textfieldLastname.editText?.text.toString()
            applicationViewModel.currentUserData.birthDate = binding.textfieldBirthDate.editText?.text.toString()
            applicationViewModel.currentUserData.gender = binding.textfieldGender.editText?.text.toString()
            val action =
                CreateAccount2FragmentDirections.actionCreateAccount2FragmentToConfirmUserDataFragment()
            view.findNavController().navigate(action)

        }

        val today = MaterialDatePicker.todayInUtcMilliseconds()
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC+8"))
        calendar.timeInMillis = today

        val constraintsBuilder =
            CalendarConstraints.Builder()
                .setEnd(calendar.timeInMillis)
                .setValidator(DateValidatorPointBackward.now())
        val datePicker =
            MaterialDatePicker.Builder.datePicker()
                .setTitleText("Birthday")
                .setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR)
                .setCalendarConstraints(constraintsBuilder.build())

                .build()
        binding.textfieldBirthDate.editText?.showSoftInputOnFocus = false
        binding.textfieldBirthDate.editText?.setOnFocusChangeListener { view, b ->
            if (view.isFocused){
                Log.d("testingthis", " ${view.isFocused}")
                datePicker.show(requireFragmentManager(),"TAG")
            }
            else {
                Log.d("testingthis", " ${view.isFocused}")
            }
                true
        }
        datePicker.addOnDismissListener {
            binding.textfieldBirthDate.clearFocus()
        }

        datePicker.addOnPositiveButtonClickListener {

            Log.d("testingthis", "selection  ${datePicker.selection!!.toLong()/1000}")
            var birthDate: Date = Date(datePicker.selection!!.toLong())
//            var dateFormat: SimpleDateFormat = SimpleDateFormat("YYYY-MM-dd HH:mm:ss")
            var dateFormat: SimpleDateFormat = SimpleDateFormat("YYYY/MM/dd")
            var date: String = dateFormat.format(birthDate);
            Log.d("testingthis", "date  ${date}")
            binding.textfieldBirthDate.editText?.setText(date)

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        applicationViewModel.currentUserData.firstName = binding.textfieldFirstname.editText?.text.toString()
        applicationViewModel.currentUserData.lastName = binding.textfieldLastname.editText?.text.toString()
        applicationViewModel.currentUserData.birthDate = binding.textfieldBirthDate.editText?.text.toString()
        applicationViewModel.currentUserData.gender = binding.textfieldGender.editText?.text.toString()
    }
}