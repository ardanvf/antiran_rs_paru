package com.mazenrashed.example.Rest_Api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST

interface Api {
    @GET("antrian-by-group")
    fun getAntrian(): Call<ArrayList<noAntrianResponse>>
}