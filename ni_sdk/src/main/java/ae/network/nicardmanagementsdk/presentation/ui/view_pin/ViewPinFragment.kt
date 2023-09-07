package ae.network.nicardmanagementsdk.presentation.ui.view_pin

import ae.network.nicardmanagementsdk.R
import ae.network.nicardmanagementsdk.api.interfaces.SuccessErrorResponse
import ae.network.nicardmanagementsdk.api.models.input.NIInput
import ae.network.nicardmanagementsdk.databinding.FragmentViewPinBinding
import ae.network.nicardmanagementsdk.di.Injector
import ae.network.nicardmanagementsdk.presentation.extension_methods.getSerializableCompat
import ae.network.nicardmanagementsdk.presentation.models.Extra
import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


abstract class ViewPinFragment : Fragment() {
    protected var listener: OnFragmentInteractionListener? = null
    private var successErrorResponse: SuccessErrorResponse? = null
    private lateinit var viewModel: ViewPinFragmentViewModel

    private lateinit var niInput: NIInput
    private var _pinViewBinding: FragmentViewPinBinding? = null
    private val pinViewBinding: FragmentViewPinBinding
        get() = _pinViewBinding!!

    abstract fun checkSubscriber(context: Context)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        checkSubscriber(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.getSerializableCompat<NIInput>(Extra.EXTRA_NI_INPUT)?.let {
            niInput = it
        } ?: throw RuntimeException("${this::class.java.simpleName} arguments serializable ${Extra.EXTRA_NI_INPUT} is missing")

        startCountdownTimer()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val factory =
            Injector.getInstance(requireContext()).provideViewPinFragmentViewModelFactory(niInput)
        viewModel = ViewModelProvider(this, factory)[ViewPinFragmentViewModel::class.java]
        _pinViewBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_view_pin, container, false)
        pinViewBinding.lifecycleOwner = this
        pinViewBinding.viewModel = viewModel
        return pinViewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initializeUI()
    }

    private fun startCountdownTimer() {
        object : CountDownTimer(START_TIME, COUNTDOWN_INTERVAL) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsToInt = (millisUntilFinished / 1000).toInt()
                pinViewBinding.countdownTimerTextView.text =
                    resources.getString(
                        R.string.get_pin_countdown_timer_text, secondsToInt
                    )
            }

            override fun onFinish() {
                setPinMasked()
            }
        }.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _pinViewBinding = null
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    private fun initializeUI() {

        viewModel.getPin()

        viewModel.onResultSingleLiveEvent.observe(this) { successErrorResponse ->
            successErrorResponse?.let { response ->
                lifecycleScope.launch {
                    delay(500)
                    this@ViewPinFragment.successErrorResponse = response
                    pinViewBinding.let { viewPinBinding ->
                        niInput.cardIdentifierId.let {
                            val shortPan = it.takeLast(4)
                            viewPinBinding.titleTextView.text =
                                resources.getString(R.string.get_pin_description, shortPan)
                        }
                    }
                }
                listener?.onViewPinFragmentCompletion(response)
            }
        }

        setPinClear()
    }

    private fun setPinClear() {
        pinViewBinding.pinView.pinFirstDigit.text = viewModel.getPinClear[0].toString()
        pinViewBinding.pinView.pinSecondDigit.text = viewModel.getPinClear[1].toString()
        pinViewBinding.pinView.pinThirdDigit.text = viewModel.getPinClear[2].toString()
        pinViewBinding.pinView.pinForthDigit.text = viewModel.getPinClear[3].toString()
    }

    private fun setPinMasked() {
        pinViewBinding.pinView.pinFirstDigit.text = viewModel.getPinMasked[0].toString()
        pinViewBinding.pinView.pinSecondDigit.text = viewModel.getPinMasked[1].toString()
        pinViewBinding.pinView.pinThirdDigit.text = viewModel.getPinMasked[2].toString()
        pinViewBinding.pinView.pinForthDigit.text = viewModel.getPinMasked[3].toString()
    }

    companion object {
        const val START_TIME = 6000L
        const val COUNTDOWN_INTERVAL = 1000L
    }

    interface OnFragmentInteractionListener {
        fun onViewPinFragmentCompletion(response: SuccessErrorResponse)
    }
}