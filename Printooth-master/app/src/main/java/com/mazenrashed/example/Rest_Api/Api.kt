package com.mazenrashed.example.Rest_Api

import retrofit2.Call
import retrofit2.http.*

interface Api {
    @GET("antrian-by-group")
    fun getAntrian(): Call<ArrayList<noAntrianResponse>>

    @FormUrlEncoded
    @POST("next")
    fun createPost(
        @Body kode: String
    ): Call<CreatePostResponse>
}