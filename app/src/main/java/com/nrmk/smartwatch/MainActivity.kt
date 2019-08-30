package com.nrmk.smartwatch

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyManager
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.core.app.ActivityCompat

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import com.nrmk.smartwatch.Data.Dto.*

import com.google.gson.Gson
import com.nrmk.smartwatch.Service.AccountService
import java.lang.Override as Override

class MainActivity : AppCompatActivity() {
    private val PERMISSIONS_REQUEST_FINE_LOCATION = 100
    private val PERMISSIONS_REQUEST_PHONE_STATE = 101
    private var PERMISSIONS_REQUEST_BODY_SENSORS = 102

    val INTERVAL: Long = 1000
    val MIN_DISTANCE: Float = 1f

    var locationManager: LocationManager? = null
    var telephonyManager: TelephonyManager? = null
    var accountService: AccountService = AccountService()
    var sensorManager: SensorManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show()
        }

        btn_start_upds.setOnClickListener {
            startLocationUpdates()
            startHeartRateSensor()
            btn_start_upds.isEnabled = false
            btn_stop_upds.isEnabled = true
        }

        btn_stop_upds.setOnClickListener {
            stoplocationUpdates()
            stopHeartRateSensor()

            txtTime.text = "Updates Stoped"
            btn_start_upds.isEnabled = true
            btn_stop_upds.isEnabled = false
        }


        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager?
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager?
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    @Override
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_BODY_SENSORS -> {
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    registerHeartRateSensor()
                }
            }
            PERMISSIONS_REQUEST_FINE_LOCATION -> {
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    registerLocationSensor()
                }
            }

        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private val locationListener: LocationListener = object : LocationListener {
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
                deviceDto.Imei = getImeiCode()
                deviceDto.Name = accountService.getAccountInfo(this@MainActivity)?.userName ?: ""


                var request = RequestDto(deviceDto, deviceLocationDto)
                postToServer(request)

                txtLat.text = "" + location.latitude
                txtLong.text = "" + location.longitude
                txtTime.text = "" + location.time
            }
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {
            /******** Called when User on Gps  *********/
            Toast.makeText(baseContext, "Gps turned on ", Toast.LENGTH_LONG).show()
        }

        override fun onProviderDisabled(provider: String) {
            /******** Called when User off Gps *********/
            Toast.makeText(baseContext, "Gps turned off ", Toast.LENGTH_LONG).show()
        }
    }

    @SuppressLint("MissingPermission")
    private fun registerLocationSensor() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        locationManager!!.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, INTERVAL, MIN_DISTANCE, locationListener)
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        if (permissionFineLocationNotGranted()) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), PERMISSIONS_REQUEST_FINE_LOCATION)
        } else if (permissionPhoneStateNotGranted()) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_PHONE_STATE), PERMISSIONS_REQUEST_PHONE_STATE)
        } else {
            txtTime.text = "Location and Phone State permissions granted"
            registerLocationSensor()
        }
    }

    /**
     * останавливаем обновление GPS данных
     */
    private fun stoplocationUpdates() {
        locationManager?.removeUpdates(locationListener)
    }


    private val hartrateSensorListener: SensorEventListener = object : SensorEventListener {
        override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
            txtHeartRate.text = "" + p1
           //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onSensorChanged(p0: SensorEvent?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }

    private fun registerHeartRateSensor() {
        var heartRate: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_HEART_RATE)
        if(heartRate != null) {
            var isRegistered: Boolean = sensorManager!!.registerListener(hartrateSensorListener, heartRate, SensorManager.SENSOR_DELAY_FASTEST)
            Toast.makeText(this, "Sensor is " + (if (isRegistered) "" else "NOT ") + "supported and successfully enabled", Toast.LENGTH_LONG).show()
        }
    }

    private fun startHeartRateSensor() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BODY_SENSORS), PERMISSIONS_REQUEST_BODY_SENSORS)
        } else {
            registerHeartRateSensor()
        }
    }

    private fun stopHeartRateSensor() {
        sensorManager!!.unregisterListener(hartrateSensorListener)
    }

    @SuppressLint("MissingPermission")
    private fun getImeiCode(): String {
        val imei: String = if (android.os.Build.VERSION.SDK_INT >= 26) {
            telephonyManager?.imei ?: ""
        } else {
            telephonyManager?.deviceId ?: ""
        }
        return imei
    }

    private fun permissionFineLocationNotGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
    }

    private fun permissionPhoneStateNotGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
    }

    private fun postToServer(request: RequestDto) {
        val gson = Gson()
        var req = gson.toJson(request)

        val httpAsync = "http://95.161.224.146:50000/api/devicelocation/".httpPost().header("Content-Type" to "application/json").body(req).response { request, response, result ->
                when (result) {
                    is Result.Failure -> {
                        val ex = result.getException()
                        Toast.makeText(baseContext, "Ошибка при отправке сообщения:" + ex, Toast.LENGTH_LONG).show()
                    }
                    is Result.Success -> {
                        val data = result.get()
                       // Toast.makeText(baseContext, "Responce: " + data, Toast.LENGTH_LONG)
                    }
                }
            }
        httpAsync.join()
    }
}
