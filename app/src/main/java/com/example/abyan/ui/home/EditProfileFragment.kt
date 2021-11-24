package com.example.abyan.ui.home

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import com.example.abyan.R
import com.example.abyan.databinding.FragmentEditProfileBinding
import com.example.abyan.databinding.FragmentLoginBinding
import com.example.abyan.model.User
import com.example.abyan.ui.LoginFragment
import com.example.abyan.viewmodel.ApplicationViewModel
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import java.util.*


class EditProfileFragment : Fragment() {
    var auth: FirebaseAuth = Firebase.auth
    lateinit var sharedPreferences : SharedPreferences
    lateinit var editor: SharedPreferences.Editor
    private val sharedViewModel: ApplicationViewModel by activityViewModels()
    private lateinit var binding: FragmentEditProfileBinding
    private var progressBar: ProgressBar? = null
    var database: DatabaseReference = Firebase.database.reference



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }companion object{
        const val TAG = "AppTesting"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentEditProfileBinding.inflate(inflater, container, false)

        return binding.root    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadpref()
        binding.apply {
            viewModel = sharedViewModel
            lifecycleOwner = viewLifecycleOwner
            buttonConfirm.setOnClickListener {
                updateUserData()
            }
            textfieldAddress.editText?.doOnTextChanged { text, start, before, count ->
                sharedViewModel.userToEdit.address = text.toString()
            }
            textfieldFirstname.editText?.doOnTextChanged { text, start, before, count ->
                sharedViewModel.userToEdit.firstName = text.toString()

            }
            textfieldLastname.editText?.doOnTextChanged { text, start, before, count ->
                sharedViewModel.userToEdit.lastName = text.toString()

            }
            textfieldGender.editText?.doOnTextChanged { text, start, before, count ->
                sharedViewModel.userToEdit.gender = text.toString()

            }

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
                .setTitleText("Birth date")
                .setSelection(sharedViewModel.userToEdit.birthDate?.toLong())
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
            sharedViewModel.userToEdit.birthDate = it.toString()
            sharedViewModel.editBirthdateToString()
            binding.textfieldBirthDate.editText?.setText(sharedViewModel.birthdateToString)
        }
    }

    fun updateUserData() {
        Log.d(TAG, "Load user info for edit")
        var key: String? = null
        val userRef = Firebase.database.getReference("user").limitToFirst(1).orderByChild("email")
            .equalTo(auth.currentUser?.email)
        userRef.keepSynced(false)
        userRef.get().addOnSuccessListener {
            Log.d(LoginFragment.TAG, "user get: task success ${it.exists()}")
            var currentUser: User? = it.getValue<User>()
            it.children.forEach { child->
                key = child.key
            }
            val user = sharedViewModel.userToEdit
            val userValues = user.toMap()
            val childUpdates = hashMapOf<String, Any>(
                "/user/$key" to userValues,
                )
            database.updateChildren(childUpdates)
                .addOnSuccessListener {
                    sharedViewModel.currentUserData = user
                    editor?.apply {
                        this.putString("email", sharedViewModel.currentUserData.email)
                        this.putString("firstname",sharedViewModel.currentUserData.firstName)
                        this.putString("lastname",sharedViewModel.currentUserData.lastName)
                        this.putString("birthDate",sharedViewModel.currentUserData.birthDate)
                        this.putString("gender",sharedViewModel.currentUserData.gender)
                        this.putString("address",sharedViewModel.currentUserData.address)
                        this.putString("role",sharedViewModel.currentUserData.role)
                    }?.apply()
                    Toast.makeText(context, "Profile Saved", Toast.LENGTH_SHORT).show()

                }
                .addOnFailureListener {
                    Toast.makeText(context, it.localizedMessage.toString(), Toast.LENGTH_SHORT).show()
                }
        }.addOnFailureListener {
            Toast.makeText(context, it.localizedMessage.toString(), Toast.LENGTH_SHORT).show()
        }

    }
    fun loadpref() {
        sharedPreferences = activity?.getPreferences(Context.MODE_PRIVATE)!!
        editor = sharedPreferences.edit()!!
    }
}