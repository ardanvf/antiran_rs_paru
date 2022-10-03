package com.mazenrashed.example.Model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class UserResponse {
    @SerializedName("antrian")
    @Expose
    var antrian: String? = null
}