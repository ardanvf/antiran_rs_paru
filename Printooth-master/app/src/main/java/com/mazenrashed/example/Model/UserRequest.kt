package com.mazenrashed.example.Model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class UserRequest {
    @SerializedName("kode")
    @Expose
    var kode: String? = null
}