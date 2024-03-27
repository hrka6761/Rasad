package ir.srp.rasad.presentation.observers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.srp.rasad.core.Resource
import ir.srp.rasad.domain.models.ObserverOperationModel
import ir.srp.rasad.domain.models.PermittedObserversModel
import ir.srp.rasad.domain.models.UserModel
import ir.srp.rasad.domain.usecases.observer_usecases.AddObserverUseCase
import ir.srp.rasad.domain.usecases.observer_usecases.DeleteObserverUseCase
import ir.srp.rasad.domain.usecases.observer_usecases.GetObserversUseCase
import ir.srp.rasad.domain.usecases.preference_usecases.UserInfoUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class ObserversViewModel @Inject constructor(
    @Named("IO") private val io: CoroutineDispatcher,
    private val userInfoUseCase: UserInfoUseCase,
    private val getObserversUseCase: GetObserversUseCase,
    private val deleteObserverUseCase: DeleteObserverUseCase,
    private val addObserverUseCase: AddObserverUseCase
) : ViewModel() {

    private val _loadUserDataResult: MutableStateFlow<Resource<UserModel?>> = MutableStateFlow(Resource.Initial())
    val loadUserDataResult: StateFlow<Resource<UserModel?>> = _loadUserDataResult
    private val _observers: MutableStateFlow<Resource<List<PermittedObserversModel>?>> = MutableStateFlow(Resource.Initial())
    val observers: StateFlow<Resource<List<PermittedObserversModel>?>> = _observers
    private val _deleteObserverResponse: MutableStateFlow<Resource<ResponseBody?>> = MutableStateFlow(Resource.Initial())
    val deleteObserverResponse: StateFlow<Resource<ResponseBody?>> = _deleteObserverResponse


    fun loadUserData() {
        viewModelScope.launch(io) {
            _loadUserDataResult.value = Resource.Loading()
            _loadUserDataResult.value = userInfoUseCase.loadUserAccountInfo()
        }
    }

    fun getObservers(token: String, observerOperationModel: ObserverOperationModel) {
        viewModelScope.launch(io) {
            _observers.value = Resource.Loading()
            _observers.value = getObserversUseCase(token, observerOperationModel)
        }
    }

    fun deleteObserver(token: String, observerOperationModel: ObserverOperationModel) {
        viewModelScope.launch(io) {
            _deleteObserverResponse.value = Resource.Loading()
            _deleteObserverResponse.value = deleteObserverUseCase(token, observerOperationModel)
        }
    }
}