package com.example.bicyclemaps

import android.Manifest
import android.content.Context.LOCATION_SERVICE
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.bicyclemaps.databinding.ActivityMainBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import org.checkerframework.checker.units.qual.s
import java.io.BufferedReader
import java.io.InputStreamReader

//목업 데이터를 저장하는 data class
data class SignalGps(
    val latitude: Double,
    val longitude: Double
) {
    var polyline: Polyline? = null
    var drawn: Boolean = false
}

data class CurrentMarker(
    val title: String, val latitude: Double, val longitude: Double, val snr: Int
) {
    var marker: Marker? = null
}

class MainActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener {
    private lateinit var binding: ActivityMainBinding

    //지도 객체
    private var mMap: GoogleMap?= null

    //실시간 지도 라인
    private var mPolylineOptions: PolylineOptions ?= null
    private var mPolyline: Polyline?= null
    private var mGpsLocationListener: LocationListener? = null
    private var mCurrSignalGpsList = ArrayList<LatLng>() //현재 라인의 gps정보들 (같은 색상이면 리스트에 추가 된다.)

    //실시간 현재 위치의 마커
    private var mCurrentMarker: CurrentMarker ?= null

    //지도데이터 초기화
    private var mLatitude: Double = 0.0
    private var mLongitude: Double = 0.0
    private var mStartLatLng = LatLng(0.0, 0.0)
    private var mEndLatLng = LatLng(0.0, 0.0)

    private var mPolylineUiHandler: Handler? = null
    private var mIsStart = true

    private var mDrawIndex = 0



    //목업 테스트 용도
    private val mSignalGps = ArrayList<SignalGps>()
    private val mTestTimerHandler = Handler(Looper.getMainLooper())
    private lateinit var mTestTimer: Runnable
    private fun testTimer() {
        mTestTimer.run()
    }

    private fun setTestData() {
        val temp = BufferedReader(InputStreamReader(assets.open("signals_jeju.txt")))
        temp.lines().forEach { line ->
            val str = line.split("\t")
            //Log.i(TAG, "${str[0].toDouble()}, ${str[1].toDouble()}")
            mSignalGps.add(SignalGps(str[0].toDouble(), str[1].toDouble()))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "[life-cycle] onCreate()")
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //지도 생성
        binding.mapView.onCreate(null)
        binding.mapView.getMapAsync(this)

        //목업 테스트 용도
        setTestData()
        mTestTimer = object : Runnable {
            override fun run() {
                mTestTimerHandler.postDelayed(this, 100)
                drawPolyLines() //임시 주석.
            }
        }

        if (mPolylineUiHandler == null) {
            mPolylineUiHandler = Handler(Looper.getMainLooper()) {
                when(it.what) {
                    DATA_DRAW -> {
                        var count = 0
                        val sTime = System.currentTimeMillis()
                        if(mSignalGps.isNotEmpty()) {
                            for (idx in mDrawIndex..<mSignalGps.size) {
                                if (!mSignalGps[idx].drawn) {
                                    if (count <= 100) {
                                        drawPolyLineData(mSignalGps[idx])

                                        mDrawIndex++  //총개수증가.
                                        count++       //현재개수 증가

                                        mSignalGps[idx].drawn = true
                                        val eTime = System.currentTimeMillis()
                                        if ((eTime - sTime) > 10) {
                                            //Log.i(TAG, "time = $eTime - $sTime, count = $count")
                                            break
                                        }
                                    } else {
                                        //Log.i(TAG, "count가 100이상 [$idx]")
                                        break
                                    }
                                } else {
                                    //Log.i(TAG, "drawn is true")
                                    mDrawIndex = 0
                                }
                            }
                            mPolylineUiHandler?.sendEmptyMessageDelayed(DATA_DRAW, 1)
                        } else {
                            mPolylineUiHandler?.sendEmptyMessageDelayed(DATA_DRAW, 1)
                        }
                    }
                }
                true
            }
        }

        mPolylineUiHandler?.sendEmptyMessageDelayed(DATA_DRAW, 100)
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

        //목업 테스트
        testTimer()
    }

    override fun onLocationChanged(location: Location) {
        Log.i(TAG, "onLocationChanged")
    }

    var idx = 0
    private fun drawPolyLines() {
        val lm = getSystemService(LOCATION_SERVICE) as LocationManager
        //mGpsLocationListener = LocationListener { location ->
            //TEST용도 (SHLEE)
            if(idx >= mSignalGps.size) return
//            mLatitude = signalGpsSample[idx].latitude
//            mLongitude = signalGpsSample[idx].longitude

              idx++


//            mLatitude = location.latitude
//            mLongitude = location.longitude

            //signalGps.add(signals) //위 주석으로 인하여 추가 하였음. 위 내용을 사용할 경우 해당 부분 제거.
            //drawCurrentPosMarker(signals)
        //}

        //gpsProcess(lm)
    }

    private fun drawPolyLineData(signals: SignalGps) {
        mStartLatLng = LatLng(signals.latitude, signals.longitude)
        if (mIsStart) {
            mEndLatLng = mStartLatLng
            mIsStart = false
            goCurrentLocation(signals.latitude, signals.longitude)
            mPolylineOptions = PolylineOptions()

            //순서가 변경되어야 한다. 이전좌표 -> 최신좌표 순서.. 역시 이름이 변경되어야 하나??
            mCurrSignalGpsList.add(mEndLatLng)
            mCurrSignalGpsList.add(mStartLatLng)

            mPolylineOptions?.apply {
                addAll(mCurrSignalGpsList)
                width(20f)
                color(Color.GREEN)
                geodesic(true)
            }

            mPolyline = mPolylineOptions?.let {
                mMap?.addPolyline(it)  // 최종 지도에 라인을 그린다.
            }
        }

        mCurrSignalGpsList.add(mStartLatLng) //시작지점이 항상 최신 좌표이므로 최신 좌표를 넣어준다. ( 이름이 변경되어야 할려나...? )
        mPolyline?.points = mCurrSignalGpsList

        mPolyline?.let {
            signals.drawn = true
            signals.polyline= it
        }

        mEndLatLng = mStartLatLng
    }

    private fun gpsProcess(lm: LocationManager) {
        val isGPSEnabled: Boolean = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        //val isNetworkEnabled: Boolean = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        //권한을 다시 한번 확인 한다.
        if (ContextCompat.checkSelfPermission(
                applicationContext, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(TAG, "Manifest 권한 확인")

            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 0
            )
        } else {
            when { //provider 제공자 활성화 여부 체크.
                isGPSEnabled -> {
                    //val location =
                    //    lm.getLastKnownLocation(LocationManager.GPS_PROVIDER) //GPS기반으로 위치를 찾음.
                    //val getLongitude = location?.longitude
                    //val getLatitude = location?.latitude
                    //Log.d(TAG, "GPS기반 현재 위치를 불러 옵니다. ")
                }

                else -> {
                    Log.d(TAG, "아무것도 아님....ELSE")
                }
            }/* 주기적으로 업데이트가 필요 할때 사용. */

            /* 콜백 등록을 한다. GPS / Network */
            mGpsLocationListener?.let {
                lm.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 1000, 1f, it
                )

            }
        }
    }

    private fun goCurrentLocation(latitude: Double, longitude: Double) {
        Log.i(TAG, "goCurrent = $latitude, $longitude")
        //fitToMarkers() //SHLEE
        mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(latitude, longitude), 13f))
    }

    companion object {
        val TAG: String = MainActivity::class.java.simpleName
        private const val PERMISSIONS_REQUEST_CODE = 1001

        private const val DATA_DRAW = 1
    }
}