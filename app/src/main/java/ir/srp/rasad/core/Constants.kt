package ir.srp.rasad.core

object Constants {

    const val HTTP_BASE_URL = "http://srp-rasad.ir:30001/api/user/"
    const val WEBSOCKET_URL = "ws://srp-rasad.ir:30002"

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
    const val GRANT_PERMISSION_ACTION = "grant_permission"
    const val DENY_PERMISSION_ACTION = "deny_permission"
    const val CLOSE_WEBSOCKET_STATUS_CODE = 1000
    const val CLOSE_WEBSOCKET_STATUS_REASON = "disconnect"
    const val TARGETS_PREFERENCE_KEY = "targets"

    const val LOCATION_PERMISSION_TYPE_CHANGES = 5
    const val LOCATION_PERMISSION_TYPE_EVERY_5_M = 300
    const val LOCATION_PERMISSION_TYPE_EVERY_30_M = 1800
    const val LOCATION_PERMISSION_TYPE_EVERY_1_H = 3600
    const val LOCATION_PERMISSION_TYPE_EVERY_3_H = 10800
    const val LOCATION_PERMISSION_TYPE_EVERY_1_D = 79200

    const val MESSENGER_TRANSFORMATION = 0
    const val OBSERVABLE_CONNECT_SUCCESS = 1
    const val OBSERVABLE_CONNECT_FAIL = 2
    const val OBSERVER_CONNECT_SUCCESS = 3
    const val OBSERVER_CONNECT_FAIL = 4
    const val OBSERVABLE_LOGIN_SUCCESS = 5
    const val OBSERVABLE_LOGIN_FAIL = 6
    const val OBSERVABLE_LOGOUT_SUCCESS = 7
    const val OBSERVABLE_LOGOUT_FAIL = 8
    const val OBSERVABLE_LOGIN_STATE = 9
    const val OBSERVER_LOGIN_STATE = 10
    const val SERVICE_STATE = 11
    const val OBSERVER_LOGIN_SUCCESS = 12
    const val OBSERVER_LOGIN_FAIL = 13
    const val OBSERVABLE_CONNECTING = 14
    const val OBSERVER_CONNECTING = 15
    const val OBSERVER_SENDING_REQUEST_DATA = 16
    const val OBSERVER_SEND_REQUEST_DATA_SUCCESS = 17
    const val OBSERVER_SEND_REQUEST_DATA_FAIL = 18
    const val DISCONNECT = 19
    const val CANCEL_OBSERVE = 20
    const val OBSERVABLE_RECEIVE_REQUEST_PERMISSION = 30
    const val OBSERVABLE_SENDING_PERMISSION_RESPONSE = 31
    const val OBSERVABLE_GRANT_PERMISSION_FAIL = 32
    const val OBSERVABLE_DENY_PERMISSION_FAIL = 33
    const val OBSERVABLE_REQUEST_PERMISSION_DATA = 34
    const val OBSERVABLE_DENY_PERMISSION_SUCCESS = 35
    const val OBSERVABLE_GRANT_PERMISSION_SUCCESS = 36
    const val OBSERVER_DISCONNECT_ALL_TARGETS = 37
    const val OBSERVER_DISCONNECT_TARGET = 38
    const val OBSERVER_RECEIVE_DATA = 39
    const val OBSERVER_REQUEST_LAST_RECEIVED_DATA = 40
    const val OBSERVER_LAST_RECEIVED_DATA = 41
    const val OBSERVABLE_SEND_DATA_SUCCESS = 42
    const val OBSERVABLE_ADDED_NEW_OBSERVER = 43
    const val OBSERVABLE_DISCONNECT_ALL_TARGETS = 44
    const val OBSERVABLE_REQUEST_TARGETS = 45
    const val OBSERVER_FAILURE = 46

    const val APP_STATE = 21
    const val STATE_DISABLE = 22

    const val OBSERVER_STATE_LOADING = 23
    const val OBSERVER_STATE_WAITING_RESPONSE = 24
    const val OBSERVER_STATE_RECEIVING_DATA = 25

    const val OBSERVABLE_STATE_LOADING = 26
    const val OBSERVABLE_STATE_READY = 27
    const val OBSERVABLE_STATE_PERMISSION_REQUEST = 28
    const val OBSERVABLE_STATE_SENDING_DATA = 29
}