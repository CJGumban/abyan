package com.example.abyan

import android.os.Bundle
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class FirebaseConfig : MainActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Firebase.database.setPersistenceEnabled(true)

    }
}