package com.example.abyan.model

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Post(
    var uid: String? = "",
    var key: String? = "",
    var author: String? = "",
    var title: String? = "",
    var body: String? = "",
    var date: String? = ""
){
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "key" to key,
            "uid" to uid,
            "author" to author,
            "title" to title,
            "body" to body,
            "date" to date
        )
    }
}
