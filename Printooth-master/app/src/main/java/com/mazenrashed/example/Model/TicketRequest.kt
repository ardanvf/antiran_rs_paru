package com.mazenrashed.example.Model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class TicketRequest {
    @SerializedName("kode")
    @Expose
    var kode: String? = null
}