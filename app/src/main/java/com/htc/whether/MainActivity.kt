package com.htc.whether

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.asLiveData
import com.htc.whether.databinding.ActivityMainBinding
import com.htc.whether.utils.MyDataManager
import com.htc.whether.utils.ResponseStates
import com.htc.whether.utils.Utils
import com.htc.whether.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val mainViewModel by viewModels<MainViewModel>()
    private lateinit var _binding: ActivityMainBinding
    lateinit var myDataManager: MyDataManager
    var locationLatitude: Double = 0.0
    var locationLongitude: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(_binding.root)

        // Get reference to our userManager class
        myDataManager = MyDataManager(this)

        if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACCESS_FINE_LOCATION) !== PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this@MainActivity, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            } else {
                ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            }
        } else {
            var latitude: String = "0.0"
            var longitude: String = "0.0"

            if (Utils.hasInternetConnection(this)) {
                myDataManager.latitudeFlow.asLiveData().observe(this) {
                    latitude = it.toString()
                }
                myDataManager.longitudeFlow.asLiveData().observe(this) {
                    longitude = it.toString()
                }
                locationLatitude = latitude.toDouble()
                locationLongitude = longitude.toDouble()
                fetchData()
            } else {
                Toast.makeText(this, "Please check your connectivity", Toast.LENGTH_LONG).show()
            }
        }

        _binding.searchAddress.setOnEditorActionListener(
            OnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
               /* //val geoPoint = Utils.getLocationFromAddress(_binding.searchAddress.text.toString(), this@MainActivity)
                locationLatitude = geoPoint!!.latitude
                locationLongitude = geoPoint.longitude*/

                    _binding.loadWhetherDialog.visibility = View.VISIBLE
                    fetchDataFromAddress(_binding.searchAddress.text.toString())

                    true
                } else false
            }
        )
    }

    private fun fetchResponse() {
        mainViewModel.fetchWhetherResponse("44.500000",	"‑89.500000")
        _binding.loadWhetherDialog.visibility = View.VISIBLE
    }

    private fun fetchData() {
        fetchResponse()
        mainViewModel.whetherResponse.observe(this) { response ->
            when (response) {
                is ResponseStates.Success -> {
                    response.data?.let {
                        Log.i("INFO", "Test")
                        GlobalScope.launch {
                            myDataManager.storeLatLong(locationLatitude.toBigDecimal().toPlainString(), locationLongitude.toBigDecimal().toPlainString())
                        }
                    }
                    _binding.loadWhetherDialog.visibility = View.GONE
                }

                is ResponseStates.Error -> {
                    _binding.loadWhetherDialog.visibility = View.GONE
                    Toast.makeText(this, response.message, Toast.LENGTH_SHORT).show()
                }

                is ResponseStates.Loading -> {
                    _binding.loadWhetherDialog.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun fetchDataFromAddress(address: String) {
        mainViewModel.fetchWhetherFromAddressResponse(address)
        mainViewModel.whetherResponse.observe(this) { response ->
            when (response) {
                is ResponseStates.Success -> {
                    response.data?.let {
                        response.data.name?.let { it1 -> Log.i("INFO::::::", it1) }
                        GlobalScope.launch {
                            myDataManager.storeLatLong(locationLatitude.toBigDecimal().toPlainString(), locationLongitude.toBigDecimal().toPlainString())
                        }
                    }
                    _binding.loadWhetherDialog.visibility = View.GONE
                }

                is ResponseStates.Error -> {
                    _binding.loadWhetherDialog.visibility = View.GONE
                    Toast.makeText(this, response.message, Toast.LENGTH_SHORT).show()
                }

                is ResponseStates.Loading -> {
                    _binding.loadWhetherDialog.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACCESS_FINE_LOCATION) === PackageManager.PERMISSION_GRANTED) {
                        fetchData()
                    }
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }
}
