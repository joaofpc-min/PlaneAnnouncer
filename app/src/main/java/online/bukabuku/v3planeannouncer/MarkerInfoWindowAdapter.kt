package online.bukabuku.v3planeannouncer

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class MarkerInfoWindowAdapter(
    private val context: Context
) : GoogleMap.InfoWindowAdapter {
    override fun getInfoContents(marker: Marker?): View? {
        // 1. Get tag
        val airplane = marker?.title as? String ?: return null

        // 2. Inflate view and set title, address, and rating
        val view = LayoutInflater.from(context).inflate(
            R.layout.custom_info_contents, null
        )
        view.findViewById<TextView>(
            R.id.title
        ).text = airplane

        return view
    }

    override fun getInfoWindow(marker: Marker?): View? {
        // Return null to indicate that the
        // default window (white bubble) should be used
        return null
    }
}
