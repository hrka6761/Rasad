package ir.srp.rasad.core.errors

import ir.srp.rasad.core.Resource
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Named

class ErrorDetector @Inject constructor() {

    private var errorsList: HashMap<Int, Error>? = null

    @Named("E400")
    @Inject
    lateinit var e400: Error

    @Named("E404")
    @Inject
    lateinit var e404: Error

    @Named("E409")
    @Inject
    lateinit var e409: Error

    @Named("unknown")
    @Inject
    lateinit var unknownError: Error

    @Named("Retrofit")
    @Inject
    lateinit var retrofitError: Error


    operator fun <T> get(response: Response<T>): Resource<T?> {

        if (errorsList == null) {
            errorsList = HashMap()
            errorsList!![400] = e400
            errorsList!![404] = e404
            errorsList!![409] = e409
        }

        val detectedError = errorsList!![response.code()]
        detectedError?.errorMessage = response.errorBody()?.string().toString()

        return Resource.Error(detectedError ?: unknownError)
    }
}