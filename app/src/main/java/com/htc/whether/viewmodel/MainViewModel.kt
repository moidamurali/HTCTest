package com.htc.whether.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.htc.whether.models.WhetherResponse
import com.htc.whether.apidata.WhetherRepository
import com.htc.whether.utils.Constants
import com.htc.whether.utils.ResponseStates
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: WhetherRepository,
    application: Application
) : AndroidViewModel(application) {
    val whetherResponse: MutableLiveData<ResponseStates<WhetherResponse>> = MutableLiveData()
    val whetherAddressResponse: MutableLiveData<ResponseStates<WhetherResponse>> = MutableLiveData()


    fun fetchWhetherResponse(latitude: String, longitude: String) = viewModelScope.launch {
        repository.getWhether(latitude, longitude, Constants.API_KEY).collect { values ->
            whetherResponse.postValue(values)
        }
    }

    fun fetchWhetherFromAddressResponse(address: String) = viewModelScope.launch {
        repository.getWhetherFromAdd(address, Constants.API_KEY).collect { values ->
            whetherAddressResponse.postValue(values)
        }
    }
}
