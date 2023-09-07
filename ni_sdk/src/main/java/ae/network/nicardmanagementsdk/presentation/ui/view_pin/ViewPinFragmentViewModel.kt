package ae.network.nicardmanagementsdk.presentation.ui.view_pin

import ae.network.nicardmanagementsdk.api.interfaces.SuccessErrorResponse
import ae.network.nicardmanagementsdk.api.interfaces.ViewPinErrorResponse
import ae.network.nicardmanagementsdk.api.interfaces.asSuccessErrorResponse
import ae.network.nicardmanagementsdk.api.models.output.ViewPinResponse
import ae.network.nicardmanagementsdk.api.models.output.asClearViewModel
import ae.network.nicardmanagementsdk.api.models.output.asMaskedViewModel
import ae.network.nicardmanagementsdk.core.IViewPinCore
import ae.network.nicardmanagementsdk.network.utils.ConnectionModel
import ae.network.nicardmanagementsdk.network.utils.IConnection
import ae.network.nicardmanagementsdk.presentation.components.SingleLiveEvent
import ae.network.nicardmanagementsdk.presentation.ui.base_class.BaseViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ViewPinFragmentViewModel(
    private val viewPinCore: IViewPinCore,
    private val connectionLiveData: IConnection<ConnectionModel>
) : BaseViewModel() {
    private lateinit var pinClear: String
    private lateinit var pinMasked: String

    val onResultSingleLiveEvent = SingleLiveEvent<SuccessErrorResponse>()

    val getPinClear: String
        get() = pinClear

    val getPinMasked: String
        get() = pinMasked

    fun getPin() {
        viewModelScope.launch {
            if (connectionLiveData.hasInternetConnectivity) {
                isVisibleProgressBar.value = true
               // val result = viewPinCore.makeNetworkRequest()

                /**  the result variable is manually built, until the view pin API is done. */
                val result = ViewPinErrorResponse(
                    pin = ViewPinResponse("5360"),
                    error = null
                )
                isVisibleProgressBar.value = false
                result.pin?.let {
                    pinClear = it.asClearViewModel().encryptedPin
                    pinMasked = it.asMaskedViewModel().encryptedPin
                }
                onResultSingleLiveEvent.value = result.asSuccessErrorResponse()
            }
        }
    }
}