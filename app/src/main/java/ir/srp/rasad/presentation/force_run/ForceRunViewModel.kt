package ir.srp.rasad.presentation.force_run

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.srp.rasad.core.Resource
import ir.srp.rasad.domain.models.ForceRunDataModel
import ir.srp.rasad.domain.usecases.preference_usecases.ForceRunInfoUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class ForceRunViewModel @Inject constructor(
    @Named("IO") private val io: CoroutineDispatcher,
    private val forceRunInfoUseCase: ForceRunInfoUseCase,
) : ViewModel() {

    private val _forceRunInfoResult: MutableStateFlow<Resource<ForceRunDataModel?>> = MutableStateFlow(Resource.Initial())
    val forceRunInfoResult: StateFlow<Resource<ForceRunDataModel?>> = _forceRunInfoResult


    fun saveForceRunInfo(forceRunDataModel: ForceRunDataModel) {
        viewModelScope.launch(io) {
            forceRunInfoUseCase.saveForceRunInfo(forceRunDataModel)
        }
    }

    fun loadForceRunInfo() {
        viewModelScope.launch(io) {
            _forceRunInfoResult.value = Resource.Loading()
            _forceRunInfoResult.value = forceRunInfoUseCase.loadForceRunInfo()
        }
    }
}