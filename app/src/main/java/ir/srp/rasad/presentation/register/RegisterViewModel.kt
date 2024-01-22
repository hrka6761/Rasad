package ir.srp.rasad.presentation.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.srp.rasad.core.Resource
import ir.srp.rasad.domain.models.UserModel
import ir.srp.rasad.domain.usecases.RegisterUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class RegisterViewModel @Inject constructor(
    @Named("IO") private val io: CoroutineDispatcher,
    private val registerUseCase: RegisterUseCase,
) : ViewModel() {

    private val _registerResponse: MutableStateFlow<Resource<UserModel?>> = MutableStateFlow(Resource.Initial())
    val registerResponse: StateFlow<Resource<UserModel?>> = _registerResponse
    private val _saveUserDataResult: MutableStateFlow<Resource<Boolean>> = MutableStateFlow(Resource.Initial())
    val saveUserDataResult: MutableStateFlow<Resource<Boolean>> = _saveUserDataResult


    fun register(userModel: UserModel) {
        viewModelScope.launch(io) {
            _registerResponse.value = Resource.Loading()
            _registerResponse.value = registerUseCase(userModel)
        }
    }

    fun saveUserData(userModel: UserModel) {
        viewModelScope.launch(io) {
            _saveUserDataResult.value = Resource.Loading()
            _saveUserDataResult.value = Resource.Success(true)
        }
    }
}