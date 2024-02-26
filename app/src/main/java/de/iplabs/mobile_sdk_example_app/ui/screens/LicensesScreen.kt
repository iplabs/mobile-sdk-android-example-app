package de.iplabs.mobile_sdk_example_app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.iplabs.mobile_sdk.thirdPartyComponentLicensing.ThirdPartyComponentLicense
import de.iplabs.mobile_sdk_example_app.R
import de.iplabs.mobile_sdk_example_app.ui.fadingEdge
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@Composable
fun LicensesScreen(licenses: StateFlow<List<ThirdPartyComponentLicense>>) {
	val currentLicenses = licenses.collectAsStateWithLifecycle().value

	Surface(
		modifier = Modifier.fillMaxSize(),
		color = MaterialTheme.colorScheme.background
	) {
		val listState = rememberLazyListState()
		val coroutineScope = rememberCoroutineScope()
		var expandedCardId by remember { mutableIntStateOf(value = -1) }

		LazyColumn(
			modifier = Modifier
				.fillMaxSize()
				.fadingEdge(color = MaterialTheme.colorScheme.surface),
			state = listState,
			verticalArrangement = Arrangement.spacedBy(space = 12.dp),
			contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)
		) {
			item {
				Text(
					text = stringResource(id = R.string.licenses_heading),
					style = MaterialTheme.typography.headlineLarge
				)
			}

			item {
				Text(
					text = stringResource(id = R.string.licenses_description),
					modifier = Modifier.padding(bottom = 6.dp),
					style = MaterialTheme.typography.bodyMedium
				)
			}

			itemsIndexed(currentLicenses) { index, license ->
				ExpandableCard(
					id = index,
					title = {
						Column {
							Text(
								text = stringResource(
									id = R.string.license_heading,
									license.componentName,
									license.componentVersion
								),
								style = MaterialTheme.typography.titleSmall
							)

							Text(
								text = stringResource(
									id = R.string.license_vendor,
									license.componentVendor
								),
								style = MaterialTheme.typography.bodySmall
							)
						}
					},
					expandableContent = {
						val localResources = LocalContext.current.resources
						var licenseText by remember { mutableStateOf(value = "") }

						LaunchedEffect(Unit) {
							coroutineScope.launch {
								licenseText = localResources.openRawResource(license.licenseFile)
									.bufferedReader()
									.use { it.readText() }
							}
						}

						Column(
							modifier = Modifier
								.padding(start = 6.dp, end = 6.dp, bottom = 6.dp)
								.fillMaxWidth()
								.clip(MaterialTheme.shapes.small)
								.background(color = MaterialTheme.colorScheme.background)
								.padding(all = 8.dp),
							verticalArrangement = Arrangement.spacedBy(space = 16.dp)
						) {
							ClickableTextWithHtmlLinks(
								htmlText = stringResource(
									id = R.string.license_link,
									license.componentLink
								),
								textStyle = MaterialTheme.typography.bodySmall
							)

							Text(
								text = licenseText,
								fontSize = 12.sp,
								fontFamily = FontFamily.Monospace
							)
						}
					},
					expandedItemId = expandedCardId,
					onExpand = { itemId: Int ->
						if (expandedCardId != itemId) {
							expandedCardId = itemId

							coroutineScope.launch {
								listState.animateScrollToItem(index = itemId + 2)
							}
						}
					},
					onCollapse = { itemId ->
						if (expandedCardId == itemId) expandedCardId = -1
					}
				)
			}
		}
	}
}
