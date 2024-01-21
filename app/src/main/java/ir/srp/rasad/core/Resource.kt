package ir.srp.rasad.core

import ir.srp.rasad.core.errors.network_errors.UnknownError

sealed class Resource<T>(
    val data: T? = null,
    val error: ir.srp.rasad.core.errors.Error = UnknownError(),
) {

    class Initial<T> : Resource<T>()
    class Loading<T> : Resource<T>()
    class Success<T>(data: T?) : Resource<T>(data)
    class Error<T>(error: ir.srp.rasad.core.errors.Error) : Resource<T>(null, error)
}