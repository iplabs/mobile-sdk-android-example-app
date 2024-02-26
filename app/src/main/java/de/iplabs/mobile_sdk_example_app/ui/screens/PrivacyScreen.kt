package de.iplabs.mobile_sdk_example_app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.iplabs.mobile_sdk_example_app.R

@Composable
fun PrivacyScreen() {
	Surface(
		modifier = Modifier.fillMaxSize(),
		color = MaterialTheme.colorScheme.background
	) {
		val scrollState = rememberScrollState()

		Column(
			modifier = Modifier
				.fillMaxSize()
				.verticalScroll(state = scrollState)
				.padding(all = 16.dp),
			verticalArrangement = Arrangement.spacedBy(space = 12.dp),
		) {
			Text(
				text = stringResource(id = R.string.privacy_heading),
				style = MaterialTheme.typography.headlineLarge
			)

			ClickableTextWithHtmlLinks(htmlText = stringResource(id = R.string.privacy_content))
		}
	}
}
