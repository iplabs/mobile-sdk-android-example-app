package de.iplabs.mobile_sdk_example_app.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class OrderOverviewViewModel : ViewModel() {
	private val _canTriggerOrderSubmission = MutableStateFlow(true)
	val canTriggerOrderSubmission = _canTriggerOrderSubmission.asStateFlow()

	fun deactivateTriggeringOrderSubmission() {
		_canTriggerOrderSubmission.update { false }
	}

	fun activateTriggeringOrderSubmission() {
		_canTriggerOrderSubmission.update { true }
	}
}
