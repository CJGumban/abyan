package com.example.abyan.model

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    val email: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val birthDate: String? = null,
    val gender: String? = null,
    val address: String? = null){
    @Exclude
    fun toMap(): Map<String,Any? >{
        return mapOf(
            "email" to email,
            "firstName" to firstName,
            "lastName" to lastName,
            "birthDate" to birthDate,
            "gender" to gender,
            "address" to address
        )
    }

}
