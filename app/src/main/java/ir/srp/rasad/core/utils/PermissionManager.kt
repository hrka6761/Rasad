package ir.srp.rasad.core.utils

import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.POST_NOTIFICATIONS
import android.os.Build
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import ir.srp.rasad.core.BaseFragment

class PermissionManager(
    private val fragment: BaseFragment,
    private val callback: ActivityResultCallback<Map<String, Boolean>>,
) {

    private val permissionsLauncher: ActivityResultLauncher<Array<String>> =
        fragment.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(), callback)


    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            isPermissionGranted(POST_NOTIFICATIONS)
        else
            true
    }

    fun hasBasicLocationPermission() =
        isPermissionGranted(ACCESS_COARSE_LOCATION) && isPermissionGranted(ACCESS_FINE_LOCATION)

    @RequiresApi(Build.VERSION_CODES.Q)
    fun hasBackgroundLocationPermission() = isPermissionGranted(ACCESS_BACKGROUND_LOCATION)

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun getNotificationPermission() =
        permissionsLauncher.launch(arrayOf(POST_NOTIFICATIONS))

    fun getBasicLocationPermission() =
        permissionsLauncher.launch(arrayOf(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION))

    @RequiresApi(Build.VERSION_CODES.Q)
    fun getBackgroundLocationPermission() =
        permissionsLauncher.launch(arrayOf(ACCESS_BACKGROUND_LOCATION))


    private fun isPermissionGranted(permission: String): Boolean =
        ContextCompat.checkSelfPermission(
            fragment.requireContext(),
            permission
        ) == PackageManager.PERMISSION_GRANTED
}