package ir.srp.rasad.data.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.location.LocationManager.EXTRA_PROVIDER_NAME
import android.os.Build.VERSION_CODES.P
import androidx.annotation.RequiresApi

@RequiresApi(P)
class LocationStateReceiver(
    private val locationCallback: (isEnable: Boolean) -> Unit
) : BroadcastReceiver() {

    private lateinit var locationManager: LocationManager


    override fun onReceive(context: Context?, intent: Intent?) {

        if (!this::locationManager.isInitialized)
            locationManager =
                context!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (intent!!.getStringExtra(EXTRA_PROVIDER_NAME) == LocationManager.GPS_PROVIDER &&
            intent.action == LocationManager.PROVIDERS_CHANGED_ACTION)
            locationCallback(locationManager.isLocationEnabled)
    }
}