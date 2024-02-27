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
    const val SERVICE_BUNDLE = "bundle"
    const val SERVICE_TYPE = "type"
    const val SERVICE_DATA = "data"
    const val START_SERVICE_OBSERVABLE = "startService_observable"
    const val START_SERVICE_OBSERVER = "startService_observer"
    const val STOP_SERVICE_OBSERVABLE = "stopService_observable"
    const val STOP_SERVICE_OBSERVER = "stopService_observer"
    const val CLOSE_WEBSOCKET_STATUS_CODE = 1000
    const val CLOSE_WEBSOCKET_STATUS_REASON = "disconnect"
    const val TARGETS_PREFERENCE_KEY = "targets"
    const val LOCATION_PERMISSION_TYPE_CHANGES = 0
    const val LOCATION_PERMISSION_TYPE_EVERY_5_M = 5
    const val LOCATION_PERMISSION_TYPE_EVERY_30_M = 30
    const val LOCATION_PERMISSION_TYPE_EVERY_1_H = 60
    const val LOCATION_PERMISSION_TYPE_EVERY_3_H = 180
    const val LOCATION_PERMISSION_TYPE_EVERY_1_D = 1320
    const val MESSENGER_TRANSFORMATION = 0
    const val OBSERVABLE_CONNECTION_SUCCESS = 1
    const val OBSERVABLE_CONNECTION_FAIL = 2
    const val OBSERVABLE_SEND_MESSAGE_FAIL = 3
    const val OBSERVABLE_CLOSING_CONNECTION = 4
    const val OBSERVABLE_CLOSED_CONNECTION = 5
}