package online.bukabuku.v3planeannouncer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val REQUEST_CODE = 12345
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
        val i = Intent(context, MainService::class.java)
        context.startForegroundService(i)
    }

}