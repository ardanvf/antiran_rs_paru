package com.mazenrashed.example.Model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class TicketResponse {
    @SerializedName("no_antrian")
    @Expose
    var no_antrian: String? = null
}