package ir.srp.rasad.data.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build.VERSION_CODES.S
import android.os.Bundle
import android.os.Parcelable
import android.telephony.SmsMessage
import androidx.annotation.RequiresApi
import dagger.hilt.android.AndroidEntryPoint
import ir.srp.rasad.core.Constants.FORCE_START_SERVICE_OBSERVABLE
import ir.srp.rasad.core.Constants.SERVICE_BUNDLE_KEY
import ir.srp.rasad.core.Constants.SERVICE_DATA_KEY
import ir.srp.rasad.core.Constants.SERVICE_TYPE_KEY
import ir.srp.rasad.domain.usecases.preference_usecases.ForceRunInfoUseCase
import ir.srp.rasad.presentation.services.MainService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@Suppress("UNCHECKED_CAST")
@RequiresApi(S)
@AndroidEntryPoint
class SmsReceiver : BroadcastReceiver() {


    @Inject
    @Named("IO")
    lateinit var io: CoroutineDispatcher

    @Inject
    lateinit var forceRunInfoUseCase: ForceRunInfoUseCase


    override fun onReceive(context: Context, intent: Intent) {
        CoroutineScope(io).launch {
            if (intent.action == "android.provider.Telephony.SMS_RECEIVED") {
                val forceRunDataModel = forceRunInfoUseCase.loadForceRunInfo().data
                val isForceRunActivated = forceRunDataModel?.state ?: false
                val trustedMobileNumber = forceRunDataModel?.mobileNumber?.replaceFirst("0", "+98")
                val password = forceRunDataModel?.password
                val bundle = intent.extras

                if (isForceRunActivated && bundle != null) {
                    val pdus = bundle["pdus"] as Array<ByteArray>?

                    if (pdus != null) {
                        for (pdu in pdus) {
                            val smsMessage: SmsMessage = SmsMessage.createFromPdu(pdu)
                            val sender: String = smsMessage.displayOriginatingAddress
                            val messageBody: String = smsMessage.messageBody

                            if (sender == trustedMobileNumber && messageBody == password)
                                startServiceWithParam(context, FORCE_START_SERVICE_OBSERVABLE)
                        }
                    }
                }

            }
        }
    }

    private fun startServiceWithParam(context: Context, type: String, data: Any? = null) {
        val intent = Intent(context, MainService::class.java)
        val bundle = Bundle()
        bundle.putString(SERVICE_TYPE_KEY, type)
        data?.let { bundle.putParcelableArray(SERVICE_DATA_KEY, data as Array<Parcelable>) }
        intent.putExtra(SERVICE_BUNDLE_KEY, bundle)
        context.startForegroundService(intent)
    }
}