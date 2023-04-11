package com.htc.whether.utils

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.google.firebase.firestore.GeoPoint
import java.io.IOException

object Utils {

    fun hasInternetConnection(context: Context?): Boolean {
        if (context == null) {
            return false
        }
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
            as ConnectivityManager

        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val networkCapabilities =
            connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false

        return when {
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }

    fun getLocationFromAddress(strAddress: String?, context: Context?): GeoPoint? {
        val coder = Geocoder(context)
        val address: List<Address>?
        var p1: GeoPoint? = null
        try {
            address = coder.getFromLocationName(strAddress, 5)
            if (address == null) {
                return null
            }
            val location: Address = address[0]
            location.getLatitude()
            location.getLongitude()
            p1 = GeoPoint(
                (location.getLatitude() * 1E6) as Double,
                (location.getLongitude() * 1E6) as Double
            )
            return p1
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }
}
