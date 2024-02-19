package ir.srp.rasad.presentation.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.srp.rasad.domain.usecases.preference_usecase.UserStateUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class MainViewModel @Inject constructor(
    @Named("IO") private val io: CoroutineDispatcher,
    private val userStateUseCase: UserStateUseCase,
) : ViewModel() {

    fun getUserState(): Deferred<Boolean> =
        viewModelScope.async(io) {
            return@async userStateUseCase.getUserLoginState()
        }
}
