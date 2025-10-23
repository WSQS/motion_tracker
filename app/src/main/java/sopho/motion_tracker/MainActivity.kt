package sopho.motion_tracker

import android.Manifest
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import sopho.motion_tracker.util.FileLogger
import sopho.motion_tracker.util.SLog

class MainActivity : AppCompatActivity() {
    private val TAG = MainActivity::class.java.simpleName
    private lateinit var locationManager: LocationManager

    private var preLocation: Location? = null
    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            var distance = preLocation?.distanceTo(location) ?: 0f
            val text =
                "Lat:${location.latitude} Lng:${location.longitude} Accuracy:${location.accuracy} m Speed:${location.speed} m/s Provider:${location.provider} Distance:$distance m"
            SLog.d("onLocationChanged: $text")
            preLocation = location
        }

        override fun onProviderEnabled(provider: String) {
            SLog.i("onProviderEnabled provider:$provider")
        }

        override fun onProviderDisabled(provider: String) {
            SLog.i("onProviderDisabled provider:$provider")
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        SLog.addLogger(FileLogger(this))
        SLog.d("onCreate")
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        val provider = when {
            locationManager.isProviderEnabled(LocationManager.FUSED_PROVIDER) ->
                LocationManager.FUSED_PROVIDER

            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ->
                LocationManager.GPS_PROVIDER

            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ->
                LocationManager.NETWORK_PROVIDER

            else -> null
        }

        if (provider == null) {
            return
        }

        SLog.i(TAG, "provider:$provider")

        for (provid in locationManager.allProviders) {
            SLog.i(TAG, "allProviders: $provid")
        }

        locationManager.requestLocationUpdates(
            provider,
            1000L,    // 每 1 秒请求一次
            0f,       // 位移超过 1 米才触发
            locationListener,
            Looper.getMainLooper()
        )
    }

    override fun onResume() {
        SLog.i("onResume")
        super.onResume()
    }

    override fun onStop() {
        SLog.i("onStop")
        super.onStop()
    }

    override fun onStart() {
        SLog.i("onStart")
        super.onStart()
    }

    override fun onDestroy() {
        SLog.i("onDestroy")
        super.onDestroy()
    }

}