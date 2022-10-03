package com.mazenrashed.example.Rest_Api

import com.mazenrashed.example.Model.TicketRequest
import com.mazenrashed.example.Model.TicketResponse
import com.mazenrashed.example.Model.UserRequest
import com.mazenrashed.example.Model.UserResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface Api {
    @POST("next")
    fun getNumberQueue(@Body req: UserRequest): Call<UserResponse>

    @POST("store")
    fun getTicketQueue(@Body req: TicketRequest): Call<TicketResponse>
}