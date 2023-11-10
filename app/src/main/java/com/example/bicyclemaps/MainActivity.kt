package com.example.bicyclemaps

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.bicyclemaps.databinding.ActivityMainBinding
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback

class MainActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener {
    private lateinit var binding: ActivityMainBinding

    private var mMap: GoogleMap?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "[life-cycle] onCreate()")
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //지도 생성
        binding.mapView.onCreate(null)
        binding.mapView.getMapAsync(this)
    }

    override fun onStart() {
        super.onStart()
        Log.i(TAG, "[life-cycle] onStart()")
    }

    override fun onMapReady(p0: GoogleMap) {
        Log.i(TAG, "onMapReady: $p0")
        mMap = p0

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap?.isMyLocationEnabled = true //현재 위치로 가는 아이콘 생성
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_CODE
            )
        }
    }

    override fun onLocationChanged(location: Location) {
        Log.i(TAG, "onLocationChanged")
    }


    companion object {
        val TAG: String = MainActivity::class.java.simpleName
        private const val PERMISSIONS_REQUEST_CODE = 1001
    }
}