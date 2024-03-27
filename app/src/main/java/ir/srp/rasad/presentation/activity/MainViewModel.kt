package ir.srp.rasad.presentation.activity

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.srp.rasad.domain.usecases.preference_usecases.UserStateUseCase
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class MainViewModel @Inject constructor(
    @Named("IO") private val io: CoroutineDispatcher,
    private val userStateUseCase: UserStateUseCase,
) : ViewModel() {

    fun getUserState() = userStateUseCase.getUserLoginState()
}
