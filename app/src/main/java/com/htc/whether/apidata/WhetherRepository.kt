package com.htc.whether.apidata

import com.htc.whether.apidata.remote.RemoteDataSource
import com.htc.whether.models.BaseApiResponse
import com.htc.whether.models.WhetherResponse
import com.htc.whether.utils.ResponseStates
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

@ActivityRetainedScoped
class WhetherRepository @Inject constructor(
    private val remoteDataSource: RemoteDataSource
) : BaseApiResponse() {

    suspend fun getWhether(lat: String, longitude: String, apiKey: String): Flow<ResponseStates<WhetherResponse>> {
        return flow<ResponseStates<WhetherResponse>> {
            emit(safeApiCall { remoteDataSource.getWhether(lat, longitude, apiKey) })
        }.flowOn(Dispatchers.IO)
    }

    suspend fun getWhetherFromAdd(address: String, apiKey: String): Flow<ResponseStates<WhetherResponse>> {
        return flow<ResponseStates<WhetherResponse>> {
            emit(safeApiCall { remoteDataSource.getWhetherFromAddress(address, apiKey) })
        }.flowOn(Dispatchers.IO)
    }
}
