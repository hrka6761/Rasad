package ir.srp.rasad.core

enum class WebSocketDataType {
    LogInObserver,
    LogInObservable,
    LogOutObserver,
    LogOutObservable,
    RequestPermission,
    RequestData,
    Grant,
    Deny,
    Data,
    Failed,
    Confirmation
}