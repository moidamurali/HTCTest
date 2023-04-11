package com.htc.whether.utils

sealed class ResponseStates<T>(
    val data: T? = null,
    val message: String? = null
) {

    class Success<T>(data: T) : ResponseStates<T>(data)

    class Error<T>(message: String, data: T? = null) : ResponseStates<T>(data, message)

    class Loading<T> : ResponseStates<T>()

}