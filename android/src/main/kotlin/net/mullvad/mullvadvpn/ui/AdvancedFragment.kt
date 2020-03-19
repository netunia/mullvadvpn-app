package net.mullvad.mullvadvpn.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import net.mullvad.mullvadvpn.R
import net.mullvad.mullvadvpn.model.Settings

class AdvancedFragment : ServiceDependentFragment(OnNoService.GoBack) {
    private lateinit var enableIpv6Toggle: CellSwitch

    private var subscriptionId: Int? = null
    private var updateUiJob: Job? = null

    override fun onSafelyCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.advanced, container, false)

        view.findViewById<View>(R.id.back).setOnClickListener {
            parentActivity.onBackPressed()
        }

        enableIpv6Toggle = view.findViewById<CellSwitch>(R.id.enable_ipv6_toggle).apply {
            listener = { state ->
                when (state) {
                    CellSwitch.State.ON -> daemon.setEnableIpv6(true)
                    CellSwitch.State.OFF -> daemon.setEnableIpv6(false)
                }
            }
        }

        settingsListener.subscribe({ settings -> updateUi(settings) })

        return view
    }

    private fun updateUi(settings: Settings) {
        updateUiJob?.cancel()
        updateUiJob = GlobalScope.launch(Dispatchers.Main) {
            if (settings.tunnelOptions.generic.enableIpv6) {
                enableIpv6Toggle.state = CellSwitch.State.ON
            } else {
                enableIpv6Toggle.state = CellSwitch.State.OFF
            }
        }
    }

    override fun onSafelyDestroyView() {
        subscriptionId?.let { id -> settingsListener.unsubscribe(id) }
    }
}
