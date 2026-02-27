package com.example.oklink.network

import retrofit2.Response
import retrofit2.http.*
import okhttp3.RequestBody
import com.example.oklink.network.SubmitResponse
import com.example.oklink.network.AnalysisResponse

interface VirusTotalApi {

    @Headers("x-apikey: 125999e8d72f7b309806e4c8c04dfe97d9c462ecd5a09ec9e0a19f71179f06a6")
    @POST("urls")
    suspend fun submitUrl(
        @Body body: RequestBody
    ): Response<SubmitResponse>

    @Headers("x-apikey: 125999e8d72f7b309806e4c8c04dfe97d9c462ecd5a09ec9e0a19f71179f06a6")
    @GET("analyses/{id}")
    suspend fun getAnalysis(
        @Path("id") id: String
    ): Response<AnalysisResponse>
}