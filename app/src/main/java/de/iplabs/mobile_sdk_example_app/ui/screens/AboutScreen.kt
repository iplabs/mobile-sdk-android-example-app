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
import de.iplabs.mobile_sdk_example_app.BuildConfig
import de.iplabs.mobile_sdk_example_app.R
import de.iplabs.mobile_sdk.BuildConfig as SdkBuildConfig

@Composable
fun AboutScreen() {
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
				text = stringResource(id = R.string.about_app_heading),
				style = MaterialTheme.typography.headlineLarge
			)

			Text(
				text = stringResource(id = R.string.about_app_copyright_company),
				style = MaterialTheme.typography.bodyMedium
			)

			Text(
				text = stringResource(id = R.string.about_app_copyright_address),
				style = MaterialTheme.typography.bodyMedium
			)

			Text(
				text = stringResource(id = R.string.about_app_copyright_rights),
				style = MaterialTheme.typography.bodyMedium
			)

			ClickableTextWithHtmlLinks(
				htmlText = stringResource(id = R.string.about_app_copyright_link)
			)

			Text(
				text = stringResource(id = R.string.about_app_version_information_subheading),
				modifier = Modifier.padding(top = 16.dp),
				style = MaterialTheme.typography.headlineMedium
			)

			Text(
				text = stringResource(
					id = R.string.about_app_component_information,
					"Mobile SDK example app",
					BuildConfig.VERSION_NAME,
					BuildConfig.BUILD_TYPE,
					BuildConfig.VERSION_CODE
				),
				style = MaterialTheme.typography.bodyMedium
			)

			Text(
				text = stringResource(
					id = R.string.about_app_component_information,
					"Mobile SDK library",
					SdkBuildConfig.VERSION_NAME,
					SdkBuildConfig.BUILD_TYPE,
					SdkBuildConfig.VERSION_CODE
				),
				style = MaterialTheme.typography.bodyMedium
			)
		}
	}
}
