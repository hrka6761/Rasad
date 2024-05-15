package ir.srp.rasad.presentation.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.srp.rasad.core.Resource
import ir.srp.rasad.domain.models.ObserverTargetModel
import ir.srp.rasad.domain.usecases.preference_usecases.UserTargetsUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class HomeViewModel @Inject constructor(
    @Named("IO") private val io: CoroutineDispatcher,
    private val userTargetsUseCase: UserTargetsUseCase,
) : ViewModel() {

    private val _targets: MutableStateFlow<Resource<HashSet<ObserverTargetModel>?>> = MutableStateFlow(Resource.Initial())
    val targets: StateFlow<Resource<HashSet<ObserverTargetModel>?>> = _targets
    val isTargetsLoaded: MutableLiveData<Boolean> = MutableLiveData(false)


    fun loadTargets() {
        viewModelScope.launch(io) {
            _targets.value = Resource.Loading()
            _targets.value = userTargetsUseCase.loadUserTargets()
        }
    }

    fun saveTargets(targets: HashSet<ObserverTargetModel>) {
        viewModelScope.launch(io) {
            userTargetsUseCase.saveUserTargets(targets)
        }
    }
}