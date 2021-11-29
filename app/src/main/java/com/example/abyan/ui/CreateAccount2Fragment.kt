package com.example.abyan.ui

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.abyan.databinding.FragmentCreateAccount2Binding
import com.example.abyan.viewmodel.ApplicationViewModel
import com.google.android.material.datepicker.*
import java.text.SimpleDateFormat
import java.util.*


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
        binding.apply {
            viewModel = applicationViewModel
            lifecycleOwner = viewLifecycleOwner
        }
        binding.apply {
            textfieldFirstname.editText?.doOnTextChanged { text, start, before, count ->
                applicationViewModel.firstName = binding.textfieldFirstname.editText?.text.toString()

            }
            textfieldLastname.editText?.doOnTextChanged { text, start, before, count ->
                applicationViewModel.lastName = binding.textfieldLastname.editText?.text.toString()

            }
            textfieldGender.editText?.doOnTextChanged { text, start, before, count ->
                applicationViewModel.gender = binding.textfieldGender.editText?.text.toString()

            }
            textfieldAddress.editText?.doOnTextChanged { text, start, before, count ->
                applicationViewModel.address = binding.textfieldAddress.editText?.text.toString()

            }
        }
        binding.ConfirmButton.setOnClickListener{
            if (validateForm()){
                val action =
                    CreateAccount2FragmentDirections.actionCreateAccount2FragmentToCreateAccount1Fragment()
                view.findNavController().navigate(action)
            }



        }
        binding.topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        val today = MaterialDatePicker.todayInUtcMilliseconds()
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        Log.d("timezone", "${calendar.timeZone.toString()}")
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
/*            Log.d("testingthis", "selection  ${datePicker.selection!!.toLong()/1000}")
            var birthDate: Date = Date(datePicker.selection!!.toLong())
//            var dateFormat: SimpleDateFormat = SimpleDateFormat("YYYY-MM-dd HH:mm:ss")
            var dateFormat: SimpleDateFormat = SimpleDateFormat("YYYY/MM/dd")
            var date: String = dateFormat.format(birthDate)
            Log.d("testingthis", "date  ${date}")
//            binding.textfieldBirthDate.editText?.setText(date)*/


            try {
                val sdf = SimpleDateFormat("MM/dd/yyyy")
                val netDate = Date(datePicker.selection!!.toLong())
                Log.d("testingthis","sdf ${sdf.format(netDate)}")
            } catch (e: Exception) {
                Log.d("testingthis","${e.toString()}")

            }

            applicationViewModel.birthDate = it.toString()
            applicationViewModel.birthdateToString()
            binding.textfieldBirthDate.editText?.setText(applicationViewModel.birthdateToString)


        }
    }




        private fun validateForm(): Boolean {
            var result = true
            if (TextUtils.isEmpty(binding.textfieldFirstname.editText?.text.toString())) {
                binding.textfieldFirstname.error = "Required"
                result = false
            } else {
                binding.textfieldFirstname.error = null
            }
            if (TextUtils.isEmpty(binding.textfieldLastname.editText?.text.toString())) {
                binding.textfieldLastname.error = "Required"
                result = false
            } else {
                binding.textfieldFirstname.error = null
            }
            if (TextUtils.isEmpty(binding.textfieldAddress.editText?.text.toString())) {
                binding.textfieldAddress.error = "Required"
                result = false
            } else {
                binding.textfieldAddress.error = null
            }
            if (TextUtils.isEmpty(binding.textfieldBirthDate.editText?.text.toString())) {
                binding.textfieldBirthDate.error = "Required"
                result = false
            } else {
                binding.textfieldBirthDate.error = null
            }
            if (TextUtils.isEmpty(binding.textfieldGender.editText?.text.toString())) {
                binding.textfieldGender.error = "Required"
                result = false
            } else {
                binding.textfieldGender.error = null
            }


        return result
    }

}