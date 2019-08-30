package com.nrmk.smartwatch.Service

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.ActivityCompat
import com.nrmk.smartwatch.Data.Dto.*

class LocationService {
    private var context: Context? = null
    private var locationManager: LocationManager? = null
    private var interval: Long = 1000
    private var minDistance: Float = 1f

    constructor(context: Context, interval: Long, minDistance: Float ) {
        this.context = context
        this.interval = interval
        this.minDistance = minDistance
        this.locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
    }

    val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            if (location != null) {
                var deviceLocationDto = LocationDto()
                deviceLocationDto.latitude = location.latitude
                deviceLocationDto.longitude = location.longitude
                deviceLocationDto.altitude = location.altitude
                deviceLocationDto.speed = location?.speed
                deviceLocationDto.isFromMockProvider = location.isFromMockProvider
                deviceLocationDto.accuracy = location.accuracy
                deviceLocationDto.timestamp = location.time
                deviceLocationDto.provider = location.provider


                var deviceDto = DeviceDto()
                deviceDto.Idiom = ""
                deviceDto.Manufacturer = Build.MANUFACTURER
                deviceDto.Model = Build.MODEL
                deviceDto.Version = Build.VERSION.RELEASE
                deviceDto.Imei = ""// getImeiCode()


                var request = RequestDto(deviceDto, deviceLocationDto)
//                postToServer(request)
//
//                txtLat.text = "" + location.latitude
//                txtLong.text = "" + location.longitude
//                txtTime.text = ""+ location.time
            }
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {

        }

        override fun onProviderEnabled(provider: String) {
            //locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, INTERVAL, MIN_DISTANCE, locationListener)
            //var location = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        }

        override fun onProviderDisabled(provider: String) {}
    }

    fun start() {


        //locationManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, interval, minDistance, locationListener)
    }
}