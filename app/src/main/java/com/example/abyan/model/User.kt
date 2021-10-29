package com.example.abyan.model

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    var email: String? = null,
    var firstName: String? = null,
    var lastName: String? = null,
    var birthDate: String? = null,
    var gender: String? = null,
    var address: String? = null,
    var role: String? = null){
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
