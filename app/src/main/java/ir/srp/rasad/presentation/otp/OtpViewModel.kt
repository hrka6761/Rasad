package ir.srp.rasad.presentation.otp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.srp.rasad.core.Resource
import ir.srp.rasad.domain.models.LoginDataModel
import ir.srp.rasad.domain.models.UserModel
import ir.srp.rasad.domain.usecases.LoginUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class OtpViewModel @Inject constructor(
    @Named("IO") private val io: CoroutineDispatcher,
    private val loginUseCase: LoginUseCase,
) : ViewModel() {

    private val _sendOtpResponse: MutableStateFlow<Resource<UserModel?>> = MutableStateFlow(Resource.Initial())
    val sendOtpResponse: StateFlow<Resource<UserModel?>> = _sendOtpResponse
    private val _saveUserDataState: MutableStateFlow<Resource<Boolean>> = MutableStateFlow(Resource.Initial())
    val saveUserDataState: MutableStateFlow<Resource<Boolean>> = _saveUserDataState


    fun login(loginDataModel: LoginDataModel) {
        viewModelScope.launch(io) {
            _sendOtpResponse.value = Resource.Loading()
            _sendOtpResponse.value = loginUseCase(loginDataModel)
        }
    }

    fun saveUserData(userModel: UserModel) {
        viewModelScope.launch(io) {
            _saveUserDataState.value = Resource.Loading()
            _saveUserDataState.value = Resource.Success(true)
        }
    }
}