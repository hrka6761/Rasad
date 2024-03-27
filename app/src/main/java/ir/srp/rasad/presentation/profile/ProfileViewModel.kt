package ir.srp.rasad.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.srp.rasad.core.Resource
import ir.srp.rasad.domain.models.UserModel
import ir.srp.rasad.domain.usecases.preference_usecases.UserInfoUseCase
import ir.srp.rasad.domain.usecases.user_usecases.EditEmailUseCase
import ir.srp.rasad.domain.usecases.user_usecases.EditUserNameUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class ProfileViewModel @Inject constructor(
    @Named("IO") private val io: CoroutineDispatcher,
    private val userInfoUseCase: UserInfoUseCase,
    private val editUserNameUseCase: EditUserNameUseCase,
    private val editEmailUseCase: EditEmailUseCase
) : ViewModel() {

    private val _loadUserDataResult: MutableStateFlow<Resource<UserModel?>> = MutableStateFlow(Resource.Initial())
    val loadUserDataResult: StateFlow<Resource<UserModel?>> = _loadUserDataResult
    private val _updateUsernameResponse: MutableStateFlow<Resource<UserModel?>> = MutableStateFlow(Resource.Initial())
    val updateUsernameResponse: MutableStateFlow<Resource<UserModel?>> = _updateUsernameResponse
    private val _updateEmailResponse: MutableStateFlow<Resource<UserModel?>> = MutableStateFlow(Resource.Initial())
    val updateEmailResponse: MutableStateFlow<Resource<UserModel?>> = _updateEmailResponse
    private val _updateUserDataResult: MutableStateFlow<Resource<Boolean>> = MutableStateFlow(Resource.Initial())
    val updateUserDataResult: MutableStateFlow<Resource<Boolean>> = _updateUserDataResult


    fun loadUserData() {
        viewModelScope.launch(io) {
            _loadUserDataResult.value = Resource.Loading()
            _loadUserDataResult.value = userInfoUseCase.loadUserAccountInfo()
        }
    }

    fun updateUserName(token: String, userModel: UserModel) {
        viewModelScope.launch(io) {
            _updateUsernameResponse.value = Resource.Loading()
            _updateUsernameResponse.value = editUserNameUseCase(token, userModel)
        }
    }

    fun updateEmail(token: String, userModel: UserModel) {
        viewModelScope.launch(io) {
            _updateEmailResponse.value = Resource.Loading()
            _updateEmailResponse.value = editEmailUseCase(token, userModel)
        }
    }

    fun updateUserData(userModel: UserModel) {
        viewModelScope.launch(io) {
            _updateUserDataResult.value = Resource.Loading()
            userInfoUseCase.saveUserAccountInfo(userModel)
            _updateUserDataResult.value = Resource.Success(true)
        }
    }
}