package de.iplabs.mobile_sdk_example_app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.iplabs.mobile_sdk_example_app.R

@Composable
fun LaunchScreen() {
	Surface(
		modifier = Modifier.fillMaxSize(),
		color = MaterialTheme.colorScheme.background
	) {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(all = 16.dp),
			verticalArrangement = Arrangement.Center,
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			Image(
				painter = painterResource(id = R.drawable.logo_launch),
				contentDescription = null,
				modifier = Modifier.width(width = 80.dp)
			)

			Text(
				text = stringResource(id = R.string.nav_header_title),
				modifier = Modifier.padding(top = 32.dp),
				color = MaterialTheme.colorScheme.primary,
				style = MaterialTheme.typography.titleMedium
			)

			Text(
				text = stringResource(id = R.string.nav_header_subtitle),
				style = MaterialTheme.typography.bodyMedium
			)

			Row(
				modifier = Modifier.padding(top = 96.dp),
				horizontalArrangement = Arrangement.spacedBy(space = 16.dp),
				verticalAlignment = Alignment.CenterVertically
			) {
				CircularProgressIndicator(modifier = Modifier.size(size = 32.dp))

				Text(
					text = stringResource(id = R.string.initializing_app),
					style = MaterialTheme.typography.bodyMedium
				)
			}
		}
	}
}
