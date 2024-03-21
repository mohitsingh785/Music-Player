package com.example.myapplication.API

import com.example.myapplication.Model.ApiResponse
import com.example.myapplication.Model.Track
import retrofit2.Response
import retrofit2.http.GET

interface ApiService {
    @GET("items/songs")
    suspend fun fetchTracks(): Response<ApiResponse>
}
