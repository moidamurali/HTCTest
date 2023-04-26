package com.htc.whether

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
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
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.htc.whether.databinding.ActivityMainBinding
import com.htc.whether.models.WhetherResponse
import com.htc.whether.utils.MyDataManager
import com.htc.whether.utils.ResponseStates
import com.htc.whether.utils.Utils
import com.htc.whether.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Locale

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val mainViewModel by viewModels<MainViewModel>()
    private lateinit var _binding: ActivityMainBinding
    lateinit var myDataManager: MyDataManager
    var locationLatitude: Double = 0.0
    var locationLongitude: Double = 0.0
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private val permissionId = 2
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
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
                /*latitude = myDataManager.latitudeFlow.collectAsState(initial = "")
                longitude = myDataManager.longitudeFlow.toString()*/
                myDataManager.latitudeFlow.asLiveData().observe(this) {
                    latitude = it.toString()
                }
                myDataManager.longitudeFlow.asLiveData().observe(this) {
                    longitude = it.toString()
                }
                locationLatitude = latitude.toDouble()
                locationLongitude = longitude.toDouble()
                if(!latitude.equals("0.0")){
                    fetchData(locationLatitude, locationLongitude)
                }else{
                    getLocation()
                }

            } else {
                Toast.makeText(this, "Please check your connectivity", Toast.LENGTH_LONG).show()
            }
        }

        _binding.searchAddress.setOnEditorActionListener(
            OnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                    _binding.loadWhetherDialog.visibility = View.VISIBLE
                    fetchDataFromAddress(_binding.searchAddress.text.toString())

                    true
                } else false
            }
        )
    }


    private fun fetchData(latitude: Double, longitude: Double) {
        mainViewModel.fetchWhetherResponse(latitude.toBigDecimal().toPlainString(),	longitude.toBigDecimal().toPlainString())
        _binding.loadWhetherDialog.visibility = View.VISIBLE
        mainViewModel.whetherResponse.observe(this) { response ->
            when (response) {
                is ResponseStates.Success -> {
                    response.data?.let {

                        updatedUI(it)
                        GlobalScope.launch {
                            it.coord?.lat?.toBigDecimal()?.toPlainString()
                                ?.let { it1 -> it.coord?.lon?.toBigDecimal()?.toPlainString()
                                    ?.let { it2 -> myDataManager.storeLatLong(it1, it2) } }
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
        mainViewModel.whetherAddressResponse.observe(this) { response ->
            when (response) {
                is ResponseStates.Success -> {
                    response.data?.let {
                        updatedUI(it)
                        GlobalScope.launch {
                            it.coord?.lat?.toBigDecimal()?.toPlainString()
                                ?.let { it1 -> it.coord?.lon?.toBigDecimal()?.toPlainString()
                                    ?.let { it2 -> myDataManager.storeLatLong(it1, it2) } }
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
                                getLocation()
                    }
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }


    @SuppressLint("MissingPermission", "SetTextI18n")
    private fun getLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    val location: Location? = task.result
                    if (location != null) {
                        val geocoder = Geocoder(this, Locale.getDefault())
                        val list: List<Address> = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        fetchData(list[0].latitude, list[0].longitude)

                    }
                }
            } else {
                Toast.makeText(this, "Please turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }
    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled( LocationManager.NETWORK_PROVIDER )
    }
    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(this,  Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true
        }
        return false
    }
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf( Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), permissionId)
    }

    private fun updatedUI(whetherResponse: WhetherResponse) {

        if(whetherResponse.weather.get(0).icon.equals("01d")){
            _binding.logo.setImageResource(R.drawable.ic_clear_sky)
        }else if(whetherResponse.weather.get(0).icon.equals("02d")){
            _binding.logo.setImageResource(R.drawable.ic_few_clouds)
        }else if(whetherResponse.weather.get(0).icon.equals("03d")){
            _binding.logo.setImageResource(R.drawable.ic_scattered_clouds)
        }else if(whetherResponse.weather.get(0).icon.equals("04d")){
            _binding.logo.setImageResource(R.drawable.ic_broken_clouds)
        }else if(whetherResponse.weather.get(0).icon.equals("09d")){
            _binding.logo.setImageResource(R.drawable.ic_shower_rain)
        }else if(whetherResponse.weather.get(0).icon.equals("10d")){
            _binding.logo.setImageResource(R.drawable.ic_rain)
        }else if(whetherResponse.weather.get(0).icon.equals("11d")){
            _binding.logo.setImageResource(R.drawable.ic_thunderstorm)
        }else {
            _binding.logo.setImageResource(R.drawable.ic_thunderstorm)
        }

        _binding.cityNameTv.setText("City Name : " +whetherResponse.name)
        _binding.temperature.setText("Temperature : " +whetherResponse.sys?.country)
        _binding.temperature.setText("Country Name : " +whetherResponse.sys?.country)
        _binding.whetherDescription.setText("Whether Description : " + whetherResponse.weather.get(0).description)
        _binding.windSpeed.setText("Wind speed: " + whetherResponse.wind?.speed)

    }
}
