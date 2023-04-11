package com.htc.whether.apidata.remote

import javax.inject.Inject

class RemoteDataSource @Inject constructor(private val whetherService: WhetherService) {
    suspend fun getWhether(lat: String, longitude: String, apiKey: String) = whetherService.getWhetherDetails(lat, longitude, apiKey)
    suspend fun getWhetherFromAddress(address: String, apiKey: String) = whetherService.getWhetherDetailsFromAddress(address, apiKey)
}
