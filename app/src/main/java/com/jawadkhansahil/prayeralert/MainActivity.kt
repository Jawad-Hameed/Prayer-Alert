package com.jawadkhansahil.prayeralert

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import com.jawadkhansahil.prayeralert.api.PrayerAPI
import com.jawadkhansahil.prayeralert.api.RetrofitHelper
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.jawadkhansahil.prayeralert.models.Data
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private lateinit var fusedLocationClient: FusedLocationProviderClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        setContentView(R.layout.activity_main)


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            // Permission has already been granted
            getLocation()
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation()
            } else {
                finish()
            }
        }
    }

    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude

                    Toast.makeText(this, latitude.toString()+longitude.toString(), Toast.LENGTH_SHORT).show()
                    val getPrayerAPI = RetrofitHelper.getInstance().create(PrayerAPI::class.java)
                    val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                    val date = dateFormat.format(Date())
                    GlobalScope.launch {
                    val result = getPrayerAPI.getData(date, latitude, longitude)

                        withContext(Dispatchers.Main){
                            findViewById<TextView>(R.id.fajarTime).text = convertTo12HourFormat(result.body()?.data?.timings?.Fajr.toString())
                            findViewById<TextView>(R.id.zoharTime).text = convertTo12HourFormat(result.body()?.data?.timings?.Dhuhr.toString())
                            findViewById<TextView>(R.id.asrTime).text = convertTo12HourFormat(result.body()?.data?.timings?.Asr.toString())
                            findViewById<TextView>(R.id.magribTime).text = convertTo12HourFormat(result.body()?.data?.timings?.Maghrib.toString())
                            findViewById<TextView>(R.id.eshaTime).text = convertTo12HourFormat(result.body()?.data?.timings?.Isha.toString())
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Cannot Find Location", Toast.LENGTH_SHORT).show()
            }
    }

    fun convertTo12HourFormat(time24: String): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val time = sdf.parse(time24)
        val sdfOut = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdfOut.format(time!!)
    }
}
