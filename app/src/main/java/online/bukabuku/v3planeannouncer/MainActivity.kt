package online.bukabuku.v3planeannouncer

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import online.bukabuku.v3planeannouncer.database.Planes
import online.bukabuku.v3planeannouncer.helpers.BitmapHelper
import online.bukabuku.v3planeannouncer.network.dataclasses.PlanesRepository
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var btnClear: Button
    private lateinit var btnPlanes: Button

    private lateinit var mapFragment: SupportMapFragment

    // The entry point to the Fused Location Provider.
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private var map: GoogleMap? = null
    private var cameraPosition: CameraPosition? = null

    private var locationPermissionGranted = false

    private var currentLocation: Location? = null
    private var markers: MutableList<Marker> = emptyList<Marker>().toMutableList()

    private val planesIcon: BitmapDescriptor by lazy {
        val color = ContextCompat.getColor(this, R.color.purple_200)
        BitmapHelper.vectorToBitmap(this, R.drawable.airplane, color)
    }

    private var alarmManager: AlarmManager? = null

    @Inject
    lateinit var repository: PlanesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        // Build the map.
        mapFragment = supportFragmentManager
            .findFragmentById(R.id.maps_fragment) as SupportMapFragment

        btnClear = findViewById(R.id.btn_clear)
        btnClear.setOnClickListener {  CoroutineScope(Dispatchers.IO).launch {repository.clearPlanes()} }

        btnPlanes = findViewById(R.id.btn_planes)
        btnPlanes.setOnClickListener {

            //Build Alarm manager to call periodic updates
            alarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            // Construct an intent that will execute the AlarmReceiver
            Toast.makeText(applicationContext, "Start Alarm", Toast.LENGTH_LONG).show()
            val intent = Intent(this, AlarmReceiver::class.java)
            val pIntent = PendingIntent.getBroadcast(this, 0,
                intent, 0)
            alarmManager?.setInexactRepeating(
                AlarmManager.RTC,
                System.currentTimeMillis(),
                120*1000,
                pIntent
            )
            Toast.makeText(this, "Start Manager", Toast.LENGTH_LONG).show()

        }

        getLocationPermission()
        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        setUpMaps()

    }

    /**
     * Sets up the options menu.
     * @param menu The options menu.
     * @return Boolean.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.planes_menu, menu)
        return true
    }

    /**
     * Handles a click on the menu option to get a place.
     * @param item The menu item to handle.
     * @return Boolean.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.option_get_plane) {
            //Go to List of cached planes
            displayPlanesActivity();
        }
        if (item.itemId == R.id.option_display_planes) {
            //Go to List of cached planes
            logResultsToScreen();
        }
        if (item.itemId == R.id.option_stop_searching) {
            //Stop service
            val intent = Intent(this, MainService::class.java)
            stopService(intent)
        }
        return true
    }

    /**
     * Prompts the user for permission to use the device location.
     */
    private fun getLocationPermission() {
        /*
         * online.bukabuku.v3planeannouncer.network.dataclasses.Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        locationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }

    }

    private fun logResultsToScreen() {

        //Fetch database and Update Map
        MainScope().launch {
            //Get planes from DB
            repository.allPlanes().collect{ resp -> resp.forEach{planes ->
                /*addMarkers(map!!, planes.aircraft_icao, planes.lat, planes.lng)} */
                addMarkers(map!!, planes)}
            }
        }

        map?.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(currentLocation!!.latitude,
                    currentLocation!!.longitude), 10F/*DEFAULT_ZOOM.toFloat()*/))
    }

    private fun addMarkers(map: GoogleMap, planes: Planes /*planeName: String, lat: Double, lng: Double*/) {
        val marker = map.addMarker(
            MarkerOptions()
                .title(planes.aircraft_icao)
                .position(LatLng(planes.lat, planes.lng))
                //.icon(planesIcon)
        )
        // Set planes as the tag on the marker object so it can be referenced within
        // MarkerInfoWindowAdapter
        marker.tag = planes
    }

    private fun setUpMaps() {
        mapFragment.getMapAsync { googleMap ->
            if(locationPermissionGranted) {
                googleMap.isMyLocationEnabled = true
                getCurrentLocation {
                    val pos = CameraPosition.fromLatLngZoom(it.latLng, 13f)
                    googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(pos))
                }
                /*googleMap.setOnMarkerClickListener { marker ->
                    val tag = marker.tag
                    //showInfoWindow(tag)
                    return@setOnMarkerClickListener true
                }*/
                map = googleMap

                // Set custom info window adapter
                if(map != null){map!!.setInfoWindowAdapter(MarkerInfoWindowAdapter(this))}
            }
            else{getLocationPermission()}
        }
    }

    private fun getCurrentLocation(onSuccess: (Location) -> Unit) {
        if(locationPermissionGranted) {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                currentLocation = location
                onSuccess(location)
            }.addOnFailureListener {
                Log.e(TAG, "Could not get location")
            }
        }
        else{getLocationPermission()}
    }

    private fun displayPlanesActivity() {
        val intent = Intent(this, PlanesActivity::class.java)
        startActivity(intent)
    }

    val Location.latLng: LatLng
        get() = LatLng(this.latitude, this.longitude)

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private const val DEFAULT_ZOOM = 15
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1

        // Keys for storing activity state.
        private const val KEY_CAMERA_POSITION = "camera_position"
        private const val KEY_LOCATION = "location"

        private const val requestId = 100

        // Used for selecting the current place.
        private const val M_MAX_ENTRIES = 5
    }

}

