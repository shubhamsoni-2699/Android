package com.example.map

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.map.databinding.ActivityMapsBinding
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.Marker
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.coroutines.delay
import java.util.*
import kotlin.collections.ArrayList

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationProvider :FusedLocationProviderClient
    private  var latitude :Double = 0.0
    private  var longitude:Double = 0.0

    private var marker :Marker?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationProvider =
            LocationServices.getFusedLocationProviderClient(this)

        getLocation()
        Handler().postDelayed({getLocation()},5000)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.


    }


    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        getLocation()

        Log.i("Lattitude" , "$latitude")
        Log.i("Longitude" , "$longitude")

        // Add a marker in Sydney and move the camera
        val myLoc = LatLng(latitude, longitude)

        if (marker!=null){
            mMap.clear()
        }

        marker = mMap.addMarker(MarkerOptions().position(myLoc).title("Marker in Sydney"))

        mMap.moveCamera(CameraUpdateFactory.newLatLng(myLoc))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLoc, 16f))

        mMap.addCircle(CircleOptions().center(myLoc)
            .radius(500.0).fillColor(R.color.teal_700).strokeColor(Color.LTGRAY))
    }

    private fun isLocationEnabled():Boolean{
        val locationManager:LocationManager
        = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun checkPermissions():Boolean{
        if( (ContextCompat.checkSelfPermission(this@MapsActivity
                , android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED)
            && (ContextCompat.checkSelfPermission(this@MapsActivity
                , android.Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) ){
            return true
        }
        return false
    }

    @SuppressLint("MissingPermission")
    private fun getLocation(){
        if(checkPermissions()){
            if(isLocationEnabled()){
                var locationRequest = LocationRequest.create().apply {
                    priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                    interval = 10000
                }

                if(locationRequest!=null){
                    fusedLocationProvider.requestLocationUpdates(locationRequest
                        , mLocationCallBack, Looper.myLooper())
                }

            }else{
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        }else{
            requestPermission()
        }
    }

    private fun requestPermission(){
        Dexter.withContext(this).withPermissions(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(permisionReport: MultiplePermissionsReport?) {
               if (permisionReport!!.areAllPermissionsGranted()){
                   Toast.makeText(this@MapsActivity,
                       "permission granted" , Toast.LENGTH_SHORT ).show()
               }
            }

            override fun onPermissionRationaleShouldBeShown(
                permissionRe: MutableList<PermissionRequest>?,
                token: PermissionToken?
            ) {
                showRationalDialog()
            }

        }).onSameThread().check()
    }

    private fun showRationalDialog(){
        AlertDialog.Builder(this).setMessage("You Denied permissions ")
    }

    private var mLocationCallBack = object :LocationCallback(){
        override fun onLocationResult(location: LocationResult) {
            super.onLocationResult(location)
            val mLastLocation = location.lastLocation
            latitude = mLastLocation!!.latitude
            longitude = mLastLocation!!.longitude

            val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync(this@MapsActivity)
        }
    }
}
