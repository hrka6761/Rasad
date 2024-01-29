package ir.srp.rasad.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.srp.rasad.core.Resource
import ir.srp.rasad.domain.models.UserModel
import ir.srp.rasad.domain.usecases.UserInfoUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class ProfileViewModel @Inject constructor(
    @Named("IO") private val io: CoroutineDispatcher,
    private val userInfoUseCase: UserInfoUseCase
) : ViewModel() {

    private val _loadUserDataResult: MutableStateFlow<Resource<UserModel?>> = MutableStateFlow(Resource.Initial())
    val loadUserDataResult: StateFlow<Resource<UserModel?>> = _loadUserDataResult


    fun loadUserData() {
        viewModelScope.launch(io) {
            _loadUserDataResult.value = Resource.Loading()
            _loadUserDataResult.value = userInfoUseCase.loadUserAccountInfo()
        }
    }
}