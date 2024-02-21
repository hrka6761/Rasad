package ir.srp.rasad.core

object Constants {

    const val HTTP_BASE_URL = "http://192.168.54.187:1367/api/user/"
    const val WEBSOCKET_URL = "ws://192.168.54.187:11066"

    const val USER_STATE_PREFERENCE_KEY = "userAccountState"
    const val USER_ACCOUNT_INFO_PREFERENCE_KEY = "userAccountInfo"
    const val TOKEN_HEADER_KEY = "Authorization"
    const val EDIT_PROFILE_ARG_KEY = "field_type"
    const val USERNAME_ARG_VALUE = "username"
    const val EMAIL_ARG_VALUE = "email"
    const val COARSE_RESULT_KEY = "android.permission.ACCESS_COARSE_LOCATION"
    const val FINE_RESULT_KEY = "android.permission.ACCESS_FINE_LOCATION"
    const val SERVICE_INTENT_DATA = "data"
    const val START_SERVICE_OBSERVABLE = "startService_observable"
    const val START_SERVICE_OBSERVER = "startService_observer"
    const val STOP_SERVICE_OBSERVABLE = "stopService_observable"
    const val STOP_SERVICE_OBSERVER = "stopService_observer"
    const val CLOSE_WEBSOCKET_STATUS_CODE = 1000
    const val CLOSE_WEBSOCKET_STATUS_REASON = "disconnect"
}