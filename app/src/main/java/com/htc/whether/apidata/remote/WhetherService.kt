package com.htc.whether.apidata.remote

import com.htc.whether.models.WhetherResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WhetherService {
    @GET("weather")
    suspend fun getWhetherDetails(
        @Query("lat") one: String,
        @Query("lon") two: String,
        @Query("appid") key: String
    ): Response<WhetherResponse>



    @GET("weather")
    suspend fun getWhetherDetailsFromAddress(
        @Query("q") address: String,
        @Query("appid") key: String
    ): Response<WhetherResponse>
}
