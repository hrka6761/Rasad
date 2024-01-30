package ir.srp.rasad.core.errors.network_errors

import androidx.fragment.app.Fragment
import ir.srp.rasad.R
import ir.srp.rasad.core.BaseFragment
import ir.srp.rasad.core.errors.Error
import ir.srp.rasad.core.utils.MessageViewer
import ir.srp.rasad.domain.usecases.UserInfoUseCase
import ir.srp.rasad.domain.usecases.UserStateUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named

class E401 @Inject constructor(
    @Named("IO") private val io: CoroutineDispatcher,
    @Named("Main") private val main: CoroutineDispatcher,
    private val userStateUseCase: UserStateUseCase,
    private val userInfoUseCase: UserInfoUseCase,
) : Error {

    override var errorMessage: String = "Authorization failed !!!\n Please login again."

    override fun invoke(fragment: Fragment) {
        MessageViewer.showError(fragment, errorMessage)
        logout(fragment)
    }

    private fun logout(fragment: Fragment) {
        CoroutineScope(io).launch {
            userStateUseCase.setUserLoginState(false)
            userInfoUseCase.clearAllUserData()
            withContext(main) {
                (fragment as BaseFragment).navController.navigate(R.id.loginFragment)
            }
        }
    }
}