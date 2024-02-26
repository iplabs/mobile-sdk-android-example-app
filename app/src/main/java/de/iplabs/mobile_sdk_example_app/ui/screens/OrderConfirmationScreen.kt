package de.iplabs.mobile_sdk_example_app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AssignmentTurnedIn
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.iplabs.mobile_sdk_example_app.R
import de.iplabs.mobile_sdk_example_app.ui.fadingEdge

@Composable
fun OrderConfirmationScreen(onBackToStart: () -> Unit) {
	Surface(
		modifier = Modifier.fillMaxSize(),
		color = MaterialTheme.colorScheme.background
	) {
		Column {
			val scrollState = rememberScrollState()

			Column(
				modifier = Modifier
					.padding(all = 16.dp)
					.fillMaxSize()
					.weight(
						weight = 1f,
						fill = false
					)
					.fadingEdge(color = MaterialTheme.colorScheme.surface)
					.verticalScroll(state = scrollState),
				verticalArrangement = Arrangement.spacedBy(space = 16.dp)
			) {
				Icon(
					imageVector = Icons.Outlined.AssignmentTurnedIn,
					contentDescription = null,
					modifier = Modifier
						.padding(top = 64.dp, bottom = 24.dp)
						.size(size = 96.dp)
						.align(alignment = Alignment.CenterHorizontally),
					tint = MaterialTheme.colorScheme.outline
				)

				Text(
					text = stringResource(id = R.string.order_confirmation_heading),
					modifier = Modifier.align(alignment = Alignment.CenterHorizontally),
					style = MaterialTheme.typography.headlineLarge
				)

				Text(
					text = stringResource(id = R.string.order_confirmation_description),
					style = MaterialTheme.typography.bodyMedium
				)
			}

			Row(modifier = Modifier.padding(all = 16.dp)) {
				Button(
					onClick = { onBackToStart() },
					modifier = Modifier.fillMaxWidth()
				) {
					Text(
						text = stringResource(id = R.string.back_to_home),
						style = MaterialTheme.typography.labelLarge
					)
				}
			}
		}
	}
}
