package online.bukabuku.v3planeannouncer

/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.icu.util.TimeUnit
import android.location.Location
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import online.bukabuku.v3planeannouncer.database.Planes
import online.bukabuku.v3planeannouncer.network.dataclasses.DataClass
import online.bukabuku.v3planeannouncer.network.dataclasses.PlaneApi
import online.bukabuku.v3planeannouncer.network.dataclasses.PlanesRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import javax.inject.Inject

import androidx.lifecycle.lifecycleScope
import java.lang.Thread.sleep
import kotlin.properties.Delegates
import kotlin.time.DurationUnit

/**
 * Service tracks location when requested and updates Activity via binding. If Activity is
 * stopped/unbinds and tracking is enabled, the service promotes itself to a foreground service to
 * insure location updates aren't interrupted.
 *
 * For apps running in the background on O+ devices, location is computed much less than previous
 * versions. Please reference documentation for details.
 */

@AndroidEntryPoint
class MainService : LifecycleService(), TextToSpeech.OnInitListener {

    /*
     * Checks whether the bound activity has really gone away (foreground service with notification
     * created) or simply orientation change (no-op).
     */
    private var configurationChange = false

    private var serviceRunningInForeground = false

    private val localBinder = LocalBinder()

    private lateinit var notificationManager: NotificationManager

    // Review variables (no changes).
    // FusedLocationProviderClient - Main class for receiving location updates.
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    // Used only for local storage of the last known location. Usually, this would be saved to your
    // database, but because this is a simplified sample without a full database, we only need the
    // last location to create a Notification if the user navigates away from the app.
    private var currentLocation: Location? = null

    var MyLat : Double = -18.90
    var MyLong : Double = -48.30

    private var bbox : String = " "
    private lateinit var apiresult : Response<DataClass>
    private var apisuccess : Boolean = false

    private lateinit var textToSpeech : TextToSpeech

    private lateinit var mCoroutineScope : CoroutineScope

    @Inject
    lateinit var repository: PlanesRepository

    override fun onCreate() {

        super.onCreate()

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Review the FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        //Initialize Text to Speech
        textToSpeech = TextToSpeech(applicationContext, this)

        //Coroutine scope
        mCoroutineScope = CoroutineScope(Dispatchers.Unconfined)

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        Toast.makeText(this, "Service Running ", Toast.LENGTH_SHORT).show()

        val notification = generateNotification()
        // Notification ID cannot be 0.
        startForeground(1, notification)

        lifecycleScope.launch {
            fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Get current coordinates
                    currentLocation = task.result
                    if (currentLocation != null) {
                        MyLat = currentLocation!!.latitude
                        MyLong = currentLocation!!.longitude
                    }
                    //Calculate bounding box and fetch Plane API
                    bbox = (MyLat - 0.06).toString() + "," + (MyLong - 0.06).toString() + "," +
                            (MyLat + 0.06).toString() + "," + (MyLong + 0.06).toString()

                    Toast.makeText(applicationContext, bbox, Toast.LENGTH_SHORT).show()

                    lifecycleScope.launch {
                        //Fetch Plane API
                        Log.d("Launch 1", "Launched")
                        Toast.makeText(applicationContext, "Launch 1", Toast.LENGTH_SHORT)
                            .show()
                        getPlanes(bbox)
                    }

                }
            }
        }

        // Tells the system not to recreate the service after it's been killed.
        return super.onStartCommand(intent, flags, START_NOT_STICKY)

    }

   override fun onBind(intent: Intent): IBinder {

       super.onBind(intent)

        // MainActivity (client) comes into foreground and binds to service, so the service can
        // become a background services.
        stopForeground(true)
        serviceRunningInForeground = false
        configurationChange = false
        return localBinder
    }

   override fun onRebind(intent: Intent) {

        // MainActivity (client) returns to the foreground and rebinds to service, so the service
        // can become a background services.
        stopForeground(true)
        serviceRunningInForeground = false
        configurationChange = false
        super<LifecycleService>.onRebind(intent)
    }

    override fun onUnbind(intent: Intent): Boolean {

        // MainActivity (client) leaves foreground, so service needs to become a foreground service
        // to maintain the 'while-in-use' label.
        // NOTE: If this method is called due to a configuration change in MainActivity,
        // we do nothing.
        //if (!configurationChange && SharedPreferenceUtil.getLocationTrackingPref(this)) {
        if (!configurationChange) {
            val notification = generateNotification()
            startForeground(NOTIFICATION_ID, notification)
            serviceRunningInForeground = true
        }

        // Ensures onRebind() is called if MainActivity (client) rebinds.
        return true
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super<LifecycleService>.onConfigurationChanged(newConfig)
        configurationChange = true
    }

    override fun onDestroy() {
        super.onDestroy()

    }

    /*
     * Generates a BIG_TEXT_STYLE Notification that represent latest location.
     */
    private fun generateNotification(): Notification {
        // Main steps for building a BIG_TEXT_STYLE notification:
        //      0. Get data
        //      1. Create Notification Channel for O+
        //      2. Build the BIG_TEXT_STYLE
        //      3. Set up Intent / Pending Intent for notification
        //      4. Build and issue the notification

        // 0. Get data
        val mainNotificationText = getString(R.string.notify)
        val titleText = getString(R.string.app_name)

        // 1. Create Notification Channel for O+ and beyond devices (26+).
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID, titleText, NotificationManager.IMPORTANCE_DEFAULT)

            // Adds NotificationChannel to system. Attempting to create an
            // existing notification channel with its original values performs
            // no operation, so it's safe to perform the below sequence.
            notificationManager.createNotificationChannel(notificationChannel)
        }

        // 2. Build the BIG_TEXT_STYLE.
        val bigTextStyle = NotificationCompat.BigTextStyle()
            .bigText(mainNotificationText)
            .setBigContentTitle(titleText)

        // 3. Set up main Intent/Pending Intents for notification.
        val launchActivityIntent = Intent(this, MainActivity::class.java)

        val cancelIntent = Intent(this, MainService::class.java)
        cancelIntent.putExtra(EXTRA_CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION, true)

        val servicePendingIntent = PendingIntent.getService(
            this, 0, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val activityPendingIntent = PendingIntent.getActivity(
            this, 0, launchActivityIntent, 0)

        // 4. Build and issue the notification.
        // Notification Channel Id is ignored for Android pre O (26).
        val notificationCompatBuilder =
            NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)

        return notificationCompatBuilder
            .setStyle(bigTextStyle)
            .setContentTitle(titleText)
            .setContentText(mainNotificationText)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(
                R.drawable.ic_launcher_foreground, " ",
                activityPendingIntent
            )
            .addAction(
                R.drawable.ic_launcher_foreground,
                " ",
                servicePendingIntent
            )
            .build()
    }

    private suspend fun getPlanes(bbox:String) {
        try {
            PlaneApi.retrofitService.getPlane(bbox).enqueue(object :
                Callback<DataClass> {
                override fun onResponse(call: Call<DataClass>, response: Response<DataClass>) {
                    if (response.body()!=null){
                        apiresult = response
                        apisuccess = true
                        lifecycleScope.launch { handleResponse(apiresult) }
                        Toast.makeText(applicationContext, "Success", Toast.LENGTH_SHORT).show()
                    }
                    else{
                        Toast.makeText(applicationContext, "Failed", Toast.LENGTH_SHORT).show()
                        return
                    }
                }
                override fun onFailure(call: Call<DataClass>, t: Throwable) {
                    Toast.makeText(applicationContext, t.message.toString(), Toast.LENGTH_SHORT).show()
                }
            })

        } catch (e: Exception) {
            val result = null
        }
    }

    private suspend fun handleResponse(resp: Response<DataClass>) {
        val planes = resp.body()?.response
        planes?.forEach { plane ->
            val s = getNewItemEntry(plane)
            s.aircraft_icao?.let { PlaneSpeak(it) }
            SpeakSilence()
            s.dep_iata?.forEach { c -> PlaneSpeak(c.toString())}
            SpeakSilence()
            s.arr_iata?.forEach { c -> PlaneSpeak(c.toString())}
            Toast.makeText(applicationContext, s.dep_iata?.toCharArray().contentToString(),
                Toast.LENGTH_SHORT).show()
            repository.insertPlanes(s)
            //Toast.makeText(applicationContext, "Saved to DB", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getNewItemEntry(plane: online.bukabuku.v3planeannouncer.network.dataclasses.Response): Planes {
        return Planes(
            hex = plane.hex,
            aircraft_icao = plane.aircraft_icao,
            //airline_iata = plane.airline_iata,
            //airline_icao = plane.airline_icao,
            alt = plane.alt,
            arr_iata = plane.arr_iata,
            //arr_icao = plane.arr_icao,
            dep_iata = plane.dep_iata,
            //dep_icao = plane.dep_icao,
            //dir = plane.dir,
            //flag = plane.flag,
            //flight_iata = plane.flight_iata,
            //flight_icao = plane.flight_icao,
            //flight_number = plane.flight_number,
            lat = plane.lat,
            lng = plane.lng,
            //reg_number = plane.reg_number,
            speed = plane.speed,
           //squawk = plane.squawk,
            status = plane.status,
            updated = plane.updated,
            //v_speed = plane.v_speed
        )
    }

    /**
     * Class used for the client Binder.  Since this service runs in the same process as its
     * clients, we don't need to deal with IPC.
     */
    inner class LocalBinder : Binder() {
        internal val service: MainService
            get() = this@MainService
    }

    companion object {

        private const val TAG = "MainService"

        private const val PACKAGE_NAME = "com.example.android.whileinuselocation"

        internal const val ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST =
            "$PACKAGE_NAME.action.FOREGROUND_ONLY_LOCATION_BROADCAST"

        //internal const val EXTRA_LOCATION = "$PACKAGE_NAME.extra.LOCATION"

        private const val EXTRA_CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION =
            "$PACKAGE_NAME.extra.CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION"

        private const val NOTIFICATION_ID = 12345678

        private const val NOTIFICATION_CHANNEL_ID = "while_in_use_channel_01"
    }

    //Converting Text to Speech
    override fun onInit(status: Int) {
        // check the results in status variable.
        if (status == TextToSpeech.SUCCESS) {
            // setting the language to the default phone language.
            val ttsLang = textToSpeech.setLanguage(Locale.getDefault())
            // check if the language is supported
            if (ttsLang == TextToSpeech.LANG_MISSING_DATA || ttsLang == TextToSpeech.LANG_NOT_SUPPORTED) {
                textToSpeech.speak("Language not found", TextToSpeech.QUEUE_ADD, null, "ID")
            }
        } else {
            return
        }
    }

    private fun PlaneSpeak(menssagem: String, queueMode: Int = TextToSpeech.QUEUE_ADD) {
        val speechStatus = textToSpeech.speak(menssagem, queueMode, null, "ID")
        if (speechStatus == TextToSpeech.ERROR) {
            return
        }
    }

    private fun SpeakSilence(){
        textToSpeech.playSilentUtterance(1000,TextToSpeech.QUEUE_ADD, "ID")
    }

}
