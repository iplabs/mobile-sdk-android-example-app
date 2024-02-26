package de.iplabs.mobile_sdk_example_app.viewmodels

import android.net.Uri
import android.webkit.ValueCallback
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.iplabs.mobile_sdk.editor.EditorState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class EditorViewModel(sessionId: String? = null) : ViewModel() {
	var filePathsCallback: ValueCallback<Array<Uri>>? = null

	private val _editorState: MutableStateFlow<EditorState> = MutableStateFlow(
		value = EditorState.provide(sessionId = sessionId)
	)
	val editorState = _editorState.asStateFlow()

	fun authenticate(sessionId: String) {
		_editorState.update { it.authenticate(sessionId = sessionId) }
	}

	fun cancelAuthentication() {
		_editorState.update { it.cancelAuthentication() }
	}

	fun consumeAuthentication() {
		_editorState.update { it.consumeAuthentication() }
	}

	fun requestTermination(destinationId: Int) {
		_editorState.update { it.requestTermination(correlationId = destinationId) }
	}

	fun consumeTerminationRequestResult() {
		_editorState.update { it.consumeTerminationRequestResult() }
	}
}

class EditorViewModelFactory(private val sessionId: String? = null) : ViewModelProvider.Factory {
	override fun <T : ViewModel> create(modelClass: Class<T>): T {
		if (modelClass.isAssignableFrom(EditorViewModel::class.java)) {
			@Suppress("UNCHECKED_CAST")
			return EditorViewModel(sessionId = sessionId) as T
		}

		throw IllegalArgumentException("Unknown ViewModel class encountered.")
	}
}
