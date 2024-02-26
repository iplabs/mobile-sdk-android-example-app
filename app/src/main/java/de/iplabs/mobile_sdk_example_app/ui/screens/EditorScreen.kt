package de.iplabs.mobile_sdk_example_app.ui.screens

import android.net.Uri
import android.webkit.ValueCallback
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.iplabs.mobile_sdk.editor.Branding
import de.iplabs.mobile_sdk.editor.EditorConfiguration
import de.iplabs.mobile_sdk.editor.EditorEvents
import de.iplabs.mobile_sdk.editor.EditorState
import de.iplabs.mobile_sdk.editor.IplabsEditor
import de.iplabs.mobile_sdk.project.Project
import kotlinx.coroutines.flow.StateFlow

@Composable
fun EditorScreen(
	editorState: StateFlow<EditorState>,
	editorEvents: EditorEvents,
	editorProject: Project,
	filePathsCallback: ValueCallback<Array<Uri>>?,
	editorConfiguration: EditorConfiguration,
	branding: Branding
) {
	Surface(
		modifier = Modifier.fillMaxSize(),
		color = MaterialTheme.colorScheme.background
	) {
		val currentEditorState = editorState.collectAsStateWithLifecycle().value

		IplabsEditor(
			state = currentEditorState,
			events = editorEvents,
			project = editorProject,
			filePathsCallback = filePathsCallback,
			configuration = editorConfiguration,
			branding = branding
		)
	}
}
