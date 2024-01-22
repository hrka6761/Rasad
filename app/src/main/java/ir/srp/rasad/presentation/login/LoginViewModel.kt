package ir.srp.rasad.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.srp.rasad.core.Resource
import ir.srp.rasad.domain.models.LoginDataModel
import ir.srp.rasad.domain.usecases.OTPUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class LoginViewModel @Inject constructor(
    @Named("IO") private val io: CoroutineDispatcher,
    private val otpUseCase: OTPUseCase,
) : ViewModel() {

    private val _otpResponse: MutableStateFlow<Resource<ResponseBody?>> = MutableStateFlow(Resource.Initial())
    val otpResponse: StateFlow<Resource<ResponseBody?>> = _otpResponse


    fun requestOtp(mobileNumber: String) {
        viewModelScope.launch(io) {
            _otpResponse.value = Resource.Loading()
            _otpResponse.value = otpUseCase(mobileNumber)
        }
    }
}