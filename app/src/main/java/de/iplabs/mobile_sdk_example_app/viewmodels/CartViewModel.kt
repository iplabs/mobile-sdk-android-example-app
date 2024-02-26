package de.iplabs.mobile_sdk_example_app.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CartViewModel : ViewModel() {
	private val _canTriggerCheckout = MutableStateFlow(true)
	val canTriggerCheckout = _canTriggerCheckout.asStateFlow()

	fun deactivateTriggeringCheckout() {
		_canTriggerCheckout.update { false }
	}

	fun activateTriggeringCheckout() {
		_canTriggerCheckout.update { true }
	}
}
