package ir.srp.rasad.core

object Constants {

    const val HTTP_BASE_URL = "http://srp-rasad.ir:30001/api/user/"
    const val WEBSOCKET_URL = "ws://srp-rasad.ir:30002"

    const val MOBILE_KEY = "mobile"
    const val USER_STATE_KEY = "userAccountState"
    const val USER_ACCOUNT_INFO_KEY = "userAccountInfo"
    const val HEADER_TOKEN_KEY = "Authorization"
    const val EDIT_PROFILE_KEY = "fieldType"
    const val USERNAME_ARG_VALUE = "username"
    const val EMAIL_VALUE = "email"
    const val COARSE_RESULT_KEY = "android.permission.ACCESS_COARSE_LOCATION"
    const val FINE_RESULT_KEY = "android.permission.ACCESS_FINE_LOCATION"
    const val SERVICE_BUNDLE_KEY = "bundle"
    const val SERVICE_TYPE_KEY = "type"
    const val SERVICE_DATA_KEY = "data"
    const val START_SERVICE_OBSERVABLE = "startService_observable"
    const val START_SERVICE_OBSERVER = "startService_observer"
    const val STOP_SERVICE_OBSERVABLE = "stopService_observable"
    const val STOP_SERVICE_OBSERVER = "stopService_observer"
    const val GRANT_PERMISSION_ACTION = "grant_permission"
    const val DENY_PERMISSION_ACTION = "deny_permission"
    const val CLOSE_WEBSOCKET_STATUS_CODE = 1000
    const val CLOSE_WEBSOCKET_STATUS_REASON = "disconnect"
    const val TARGETS_KEY = "targets"

    const val LOCATION_PERMISSION_TYPE_CHANGES = 5
    const val LOCATION_PERMISSION_TYPE_EVERY_5_M = 300
    const val LOCATION_PERMISSION_TYPE_EVERY_30_M = 1800
    const val LOCATION_PERMISSION_TYPE_EVERY_1_H = 3600
    const val LOCATION_PERMISSION_TYPE_EVERY_3_H = 10800
    const val LOCATION_PERMISSION_TYPE_EVERY_1_D = 79200

    const val MESSENGER_TRANSFORMATION = 0
    const val SERVICE_STATE = 1
    const val APP_STATE = 2

    const val DISCONNECT = 3

    const val OBSERVABLE_CONNECTING = 4
    const val OBSERVABLE_CONNECT_SUCCESS = 5
    const val OBSERVABLE_CONNECT_FAIL = 6
    const val OBSERVABLE_LOGIN_SUCCESS = 7
    const val OBSERVABLE_LOGIN_FAIL = 8
    const val OBSERVABLE_LOGOUT_SUCCESS = 9
    const val OBSERVABLE_LOGOUT_FAIL = 10
    const val OBSERVABLE_LOGIN_STATE = 11
    const val OBSERVABLE_RECEIVE_REQUEST_PERMISSION = 12
    const val OBSERVABLE_SENDING_PERMISSION_RESPONSE = 13
    const val OBSERVABLE_GRANT_PERMISSION_FAIL = 14
    const val OBSERVABLE_GRANT_PERMISSION_SUCCESS = 15
    const val OBSERVABLE_DENY_PERMISSION_FAIL = 16
    const val OBSERVABLE_DENY_PERMISSION_SUCCESS = 17
    const val OBSERVABLE_SEND_DATA_SUCCESS = 18
    const val OBSERVABLE_GET_PERMISSION_DATA = 19
    const val OBSERVABLE_ADDED_NEW_OBSERVER = 20
    const val OBSERVABLE_DISCONNECT_ALL_TARGETS = 21
    const val OBSERVABLE_REQUEST_TARGETS = 22

    const val OBSERVER_CONNECTING = 23
    const val OBSERVER_CONNECT_SUCCESS = 24
    const val OBSERVER_CONNECT_FAIL = 25
    const val OBSERVER_LOGIN_SUCCESS = 26
    const val OBSERVER_LOGIN_FAIL = 27
    const val OBSERVER_SENDING_REQUEST_DATA = 28
    const val OBSERVER_SEND_REQUEST_DATA_SUCCESS = 29
    const val OBSERVER_SEND_REQUEST_DATA_FAIL = 30
    const val CANCEL_OBSERVE = 31
    const val OBSERVER_DISCONNECT_ALL_TARGETS = 32
    const val OBSERVER_DISCONNECT_TARGET = 33
    const val OBSERVER_RECEIVE_DATA = 34
    const val OBSERVER_REQUEST_LAST_RECEIVED_DATA = 35
    const val OBSERVER_LAST_RECEIVED_DATA = 36
    const val OBSERVER_FAILURE = 37
    const val OBSERVER_LOGIN_STATE = 38

    const val STATE_DISABLE = 39
    const val OBSERVER_STATE_LOADING = 40
    const val OBSERVER_STATE_WAITING_RESPONSE = 41
    const val OBSERVER_STATE_RECEIVING_DATA = 42
    const val OBSERVABLE_STATE_LOADING = 43
    const val OBSERVABLE_STATE_READY = 44
    const val OBSERVABLE_STATE_PERMISSION_REQUEST = 45
    const val OBSERVABLE_STATE_SENDING_DATA = 46
}