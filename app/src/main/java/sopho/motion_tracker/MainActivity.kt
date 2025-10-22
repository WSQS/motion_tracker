package sopho.motion_tracker

import android.Manifest
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    private val TAG = MainActivity::class.java.simpleName
    private lateinit var locationManager: LocationManager

    private var preLocation : Location? = null
    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            var distance = preLocation?.distanceTo(location)?:0f
            val text = "Lat:${location.latitude} Lng:${location.longitude} Accuracy:${location.accuracy} m Speed:${location.speed} m/s Provider:${location.provider} Distance:$distance m"
            Log.d(TAG, "onLocationChanged: $text")
            preLocation = location
        }

        override fun onProviderEnabled(provider: String) {
            Log.i(TAG, "onProviderEnabled provider:$provider")
        }
        override fun onProviderDisabled(provider: String) {
            Log.i(TAG, "onProviderDisabled provider:$provider")
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG,"onCreate")
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

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

        Log.i(TAG, "provider:$provider")

        for(provid in locationManager.allProviders){
            Log.i(TAG, "allProviders: $provid")
        }

        locationManager.requestLocationUpdates(
            provider,
            1000L,    // 每 1 秒请求一次
            1f,       // 位移超过 1 米才触发
            locationListener,
            Looper.getMainLooper()
        )
    }
}