package com.example.abyan.model

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    var email: String? = "",
    var firstName: String? = "",
    var lastName: String? = "",
    var birthDate: String? = "",
    var gender: String? = "",
    var address: String? = "",
    var role: String? = ""){
    @Exclude
    fun toMap(): Map<String,Any? >{
        return mapOf(
            "email" to email,
            "firstName" to firstName,
            "lastName" to lastName,
            "birthDate" to birthDate,
            "gender" to gender,
            "address" to address,
            "role" to role
        )
    }

}
