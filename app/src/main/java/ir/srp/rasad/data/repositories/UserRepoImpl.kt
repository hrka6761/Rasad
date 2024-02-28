package ir.srp.rasad.data.repositories

import ir.srp.rasad.core.Constants.TOKEN_HEADER_KEY
import ir.srp.rasad.core.Resource
import ir.srp.rasad.core.errors.ErrorDetector
import ir.srp.rasad.data.data_sources.UserApi
import ir.srp.rasad.data.data_sources.UserWebsocket
import ir.srp.rasad.domain.models.LoginDataModel
import ir.srp.rasad.domain.models.UserModel
import ir.srp.rasad.domain.models.WebsocketDataModel
import ir.srp.rasad.domain.repositories.UserRepo
import okhttp3.Response
import okhttp3.ResponseBody
import okio.ByteString
import javax.inject.Inject
import kotlin.Exception

class UserRepoImpl @Inject constructor(
    private val userApi: UserApi,
    private val userWebsocket: UserWebsocket,
    private val errorDetector: ErrorDetector,
) : UserRepo {

    private val retrofitError = errorDetector.retrofitError


    override suspend fun requestOTP(mobileNumber: String): Resource<ResponseBody?> {
        return try {
            val response = userApi.requestOtp(mobileNumber)
            if (response.isSuccessful)
                Resource.Success(response.body())
            else
                errorDetector[response]
        } catch (e: Exception) {
            retrofitError.errorMessage = e.message.toString()
            Resource.Error(retrofitError)
        }
    }

    override suspend fun loginUser(loginDataModel: LoginDataModel): Resource<UserModel?> {
        return try {
            val response = userApi.login(loginDataModel)
            if (response.isSuccessful) {
                (response.body() as UserModel).token =
                    response.headers()[TOKEN_HEADER_KEY].toString()
                Resource.Success(response.body())
            } else
                errorDetector[response]
        } catch (e: Exception) {
            retrofitError.errorMessage = e.message.toString()
            Resource.Error(retrofitError)
        }
    }

    override suspend fun registerUser(userModel: UserModel): Resource<UserModel?> {
        return try {
            val response = userApi.register(userModel)
            if (response.isSuccessful) {
                (response.body() as UserModel).token =
                    response.headers()[TOKEN_HEADER_KEY].toString()
                Resource.Success(response.body())
            } else
                errorDetector[response]
        } catch (e: Exception) {
            retrofitError.errorMessage = e.message.toString()
            Resource.Error(retrofitError)
        }
    }

    override suspend fun editUsername(token: String, userModel: UserModel): Resource<UserModel?> {
        return try {
            val response =
                userApi.updateUsername(token, userModel)
            if (response.isSuccessful) {
                (response.body() as UserModel).token =
                    response.headers()[TOKEN_HEADER_KEY].toString()
                Resource.Success(response.body())
            } else
                errorDetector[response]
        } catch (e: Exception) {
            retrofitError.errorMessage = e.message.toString()
            Resource.Error(retrofitError)
        }
    }

    override suspend fun editEmail(token: String, userModel: UserModel): Resource<UserModel?> {
        return try {
            val response =
                userApi.updateEmail(token, userModel)
            if (response.isSuccessful) {
                (response.body() as UserModel).token =
                    response.headers()[TOKEN_HEADER_KEY].toString()
                Resource.Success(response.body())
            } else
                errorDetector[response]
        } catch (e: Exception) {
            retrofitError.errorMessage = e.message.toString()
            Resource.Error(retrofitError)
        }
    }

    override suspend fun createChannel(
        url: String,
        successCallback: ((response: Response) -> Unit)?,
        failCallback: ((t: Throwable, response: Response?) -> Unit)?,
        serverDisconnectCallback: ((t: Throwable, response: Response?) -> Unit)?,
        clientDisconnectCallback: ((t: Throwable, response: Response?) -> Unit)?,
    ) {
        userWebsocket.createConnection(
            url,
            successCallback,
            failCallback,
            serverDisconnectCallback,
            clientDisconnectCallback
        )
    }

    override suspend fun removeChannel(
        onClosingConnection: ((code: Int, reason: String) -> Unit)?,
        onClosedConnection: ((code: Int, reason: String) -> Unit)?,
    ) {
        userWebsocket.removeConnection(onClosingConnection, onClosedConnection)
    }

    override suspend fun senData(
        data: WebsocketDataModel,
        onSendMessageFail: ((t: Throwable, response: Response?) -> Unit)?,
    ) {
        userWebsocket.sendData(data, onSendMessageFail)
    }

    override suspend fun receiveData(
        onReceiveTextMessage: ((text: String) -> Unit)?,
        onReceiveBinaryMessage: ((bytes: ByteString) -> Unit)?,
    ) {
        userWebsocket.receiveData(onReceiveTextMessage, onReceiveBinaryMessage)
    }

    override fun isChannelExist(): Boolean = userWebsocket.isConnected()
}