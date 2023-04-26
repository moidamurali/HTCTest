package com.htc.whether.utils

import android.content.Context
import androidx.datastore.DataStore
import androidx.datastore.preferences.Preferences
import androidx.datastore.preferences.createDataStore
import androidx.datastore.preferences.edit
import androidx.datastore.preferences.preferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MyDataManager(context: Context) {

    // Create the dataStore and give it a name same as shared preferences
    private val dataStore = context.createDataStore(name = "location_preferences")
    //private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("location_preferences")


    // Create some keys we will use them to store and retrieve the data
    companion object {
        val LATITUDE_AGE_KEY = preferencesKey<String>("LATITUDE")
        val LONGITUDE_NAME_KEY = preferencesKey<String>("LONGITUDE")
    }

    // Store location data
    // refer to the data store and using edit
    // we can store values using the keys
    suspend fun storeLatLong(latitude: String, longitude: String) {
        dataStore.edit {
            it[LATITUDE_AGE_KEY] = latitude
            it[LONGITUDE_NAME_KEY] = longitude
            // here it refers to the preferences we are editing
        }
    }

    // Create an latitude flow to retrieve latitude from the preferences
    // flow comes from the kotlin coroutine
    val latitudeFlow: Flow<String> = dataStore.data.map {
        it[LATITUDE_AGE_KEY] ?: ""
    }

    // Create a longitude flow to retrieve longitude from the preferences
    val longitudeFlow: Flow<String> = dataStore.data.map {
        it[LONGITUDE_NAME_KEY] ?: ""
    }
}
