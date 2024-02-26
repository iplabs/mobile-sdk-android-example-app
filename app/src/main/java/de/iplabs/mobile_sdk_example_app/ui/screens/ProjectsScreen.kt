package de.iplabs.mobile_sdk_example_app.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DeveloperBoardOff
import androidx.compose.material.icons.outlined.DriveFileRenameOutline
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.RunningWithErrors
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.iplabs.mobile_sdk.project.SavedProject
import de.iplabs.mobile_sdk.project.storage.SavedProjectStorageLocation
import de.iplabs.mobile_sdk_example_app.R
import de.iplabs.mobile_sdk_example_app.data.user.User
import de.iplabs.mobile_sdk_example_app.ui.fadingEdge
import de.iplabs.mobile_sdk_example_app.ui.helpers.asString
import de.iplabs.mobile_sdk_example_app.ui.helpers.expiresSoon
import de.iplabs.mobile_sdk_example_app.ui.helpers.toFileSizeString
import de.iplabs.mobile_sdk_example_app.ui.helpers.toTimestampString
import de.iplabs.mobile_sdk_example_app.ui.warning
import kotlinx.coroutines.flow.StateFlow

@Composable
fun ProjectsScreen(
	isLoading: StateFlow<Boolean>,
	projects: StateFlow<List<SavedProject>>,
	user: StateFlow<User?>,
	onTriggerLogin: () -> Unit,
	onLoadProject: (SavedProject) -> Unit,
	onRenameProject: (SavedProject) -> Unit,
	onRemoveProject: (SavedProject) -> Unit
) {
	val isCurrentlyLoading = isLoading.collectAsStateWithLifecycle().value
	val currentProjects = projects.collectAsStateWithLifecycle().value
	val currentUser = user.collectAsStateWithLifecycle().value

	Surface(
		modifier = Modifier.fillMaxSize(),
		color = MaterialTheme.colorScheme.background
	) {
		Column {
			if (currentUser == null) {
				NotLoggedInBar(onTriggerLogin = onTriggerLogin)
			}

			if (isCurrentlyLoading) {
				ProjectListLoading()
			} else {
				currentProjects.let {
					if (it.isNotEmpty()) {
						ProjectList(
							projects = currentProjects,
							onLoadProject = onLoadProject,
							onRenameProject = onRenameProject,
							onRemoveProject = onRemoveProject
						)
					} else {
						NoProjects()
					}
				}
			}
		}
	}
}

@Composable
fun ProjectListLoading() {
	Column(
		modifier = Modifier
			.padding(all = 16.dp)
			.fillMaxSize(),
		verticalArrangement = Arrangement.Center,
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		CircularProgressIndicator(
			modifier = Modifier
				.padding(all = 8.dp)
				.size(size = 48.dp)
		)

		Text(
			text = stringResource(id = R.string.loading_projects),
			modifier = Modifier.padding(all = 8.dp),
			textAlign = TextAlign.Center,
			style = MaterialTheme.typography.bodyMedium
		)
	}
}

@Composable
fun ProjectList(
	projects: List<SavedProject>,
	onLoadProject: (SavedProject) -> Unit,
	onRenameProject: (SavedProject) -> Unit,
	onRemoveProject: (SavedProject) -> Unit
) {
	val listState = rememberLazyListState()

	LazyColumn(
		modifier = Modifier
			.fillMaxSize()
			.fadingEdge(color = MaterialTheme.colorScheme.surface),
		state = listState,
		verticalArrangement = Arrangement.spacedBy(space = 12.dp),
		contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)
	) {
		items(projects) { project ->
			ProjectTile(
				project = project,
				onLoadProject = { onLoadProject(project) },
				onRenameProject = { onRenameProject(project) },
				onRemoveProject = { onRemoveProject(project) }
			)
		}
	}
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ProjectTile(
	project: SavedProject,
	onLoadProject: (SavedProject) -> Unit,
	onRenameProject: () -> Unit,
	onRemoveProject: () -> Unit
) {
	val cardShape = MaterialTheme.shapes.medium

	var dropdownExpanded by remember { mutableStateOf(value = false) }

	Card(
		modifier = Modifier
			.fillMaxWidth()
			.clip(cardShape)
			.combinedClickable(
				onClick = { onLoadProject(project) },
				onLongClick = { dropdownExpanded = true }
			),
		shape = cardShape,
		colors = CardDefaults.cardColors(
			containerColor = MaterialTheme.colorScheme.surfaceVariant,
			contentColor = MaterialTheme.colorScheme.onSurfaceVariant
		)
	) {
		Row(verticalAlignment = Alignment.CenterVertically) {
			val previewImageModifier = Modifier
				.padding(start = 6.dp, top = 6.dp, end = 6.dp)
				.size(size = 82.dp)
				.clip(MaterialTheme.shapes.small)
			val previewImageCrop = ContentScale.Fit

			project.previewImage?.let {
				Image(
					bitmap = it.asImageBitmap(),
					contentDescription = stringResource(id = R.string.project_preview_description),
					modifier = previewImageModifier,
					contentScale = previewImageCrop
				)
			} ?: run {
				Image(
					painter = painterResource(id = R.drawable.icon_no_preview),
					contentDescription = stringResource(id = R.string.project_preview_description),
					modifier = previewImageModifier,
					contentScale = previewImageCrop
				)
			}

			Column(
				modifier = Modifier
					.fillMaxWidth()
					.padding(all = 12.dp),
				verticalArrangement = Arrangement.SpaceBetween
			) {
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.SpaceBetween,
					verticalAlignment = Alignment.CenterVertically
				) {
					Text(
						text = project.title,
						modifier = Modifier.weight(weight = 1f, fill = false),
						overflow = TextOverflow.Ellipsis,
						maxLines = 1,
						style = MaterialTheme.typography.titleMedium
					)

					Box(contentAlignment = Alignment.Center) {
						IconButton(
							onClick = { dropdownExpanded = true },
							modifier = Modifier.size(size = 32.dp)
						) {
							Icon(
								imageVector = Icons.Outlined.MoreVert,
								contentDescription = stringResource(id = R.string.popup_menu_trigger_description),
								modifier = Modifier.size(size = 24.dp)
							)
						}

						DropdownMenu(
							expanded = dropdownExpanded,
							onDismissRequest = { dropdownExpanded = false }
						) {
							DropdownMenuItem(
								text = {
									Text(
										text = stringResource(id = R.string.rename),
										style = MaterialTheme.typography.labelMedium
									)
								},
								onClick = {
									onRenameProject()

									dropdownExpanded = false
								},
								leadingIcon = {
									Icon(
										imageVector = Icons.Outlined.DriveFileRenameOutline,
										contentDescription = null,
										modifier = Modifier
											.padding(end = 8.dp)
											.size(size = 24.dp)
									)
								}
							)

							DropdownMenuItem(
								text = {
									Text(
										text = stringResource(id = R.string.remove),
										style = MaterialTheme.typography.labelMedium
									)
								},
								onClick = {
									onRemoveProject()

									dropdownExpanded = false
								},
								leadingIcon = {
									Icon(
										imageVector = Icons.Outlined.Delete,
										contentDescription = null,
										modifier = Modifier
											.padding(end = 8.dp)
											.size(size = 24.dp)
									)
								}
							)
						}
					}
				}

				Text(
					text = project.appliedProductOptions.asString(),
					overflow = TextOverflow.Ellipsis,
					maxLines = 1,
					style = MaterialTheme.typography.bodyMedium
				)

				Row(
					modifier = Modifier.fillMaxWidth(),
					verticalAlignment = Alignment.CenterVertically
				) {
					Icon(
						imageVector = if (project.location == SavedProjectStorageLocation.LOCAL) Icons.Outlined.PhoneAndroid else Icons.Outlined.CloudDownload,
						contentDescription = stringResource(
							id = R.string.storage_location_indicator_description
						),
						modifier = Modifier
							.size(size = 24.dp)
					)

					Text(
						text = stringResource(id = R.string.dot_separator),
						modifier = Modifier.padding(horizontal = 8.dp),
						maxLines = 1,
						style = MaterialTheme.typography.bodyMedium
					)

					Text(
						text = project.sizeInBytes.toFileSizeString(precision = 1),
						maxLines = 1,
						style = MaterialTheme.typography.bodyMedium
					)

					Text(
						text = stringResource(id = R.string.dot_separator),
						modifier = Modifier.padding(horizontal = 8.dp),
						maxLines = 1,
						style = MaterialTheme.typography.bodyMedium
					)

					if (project.expiresSoon()) {
						Icon(
							imageVector = Icons.Outlined.RunningWithErrors,
							contentDescription = stringResource(
								id = R.string.impending_project_expiration_indicator_description
							),
							modifier = Modifier
								.padding(end = 6.dp)
								.size(size = 20.dp),
							tint = MaterialTheme.colorScheme.warning
						)
					}

					Text(
						text = project.lastModifiedDate.toTimestampString(),
						overflow = TextOverflow.Ellipsis,
						maxLines = 1,
						style = MaterialTheme.typography.bodyMedium
					)
				}
			}
		}
	}
}

@Composable
fun NotLoggedInBar(onTriggerLogin: () -> Unit) {
	Row(
		modifier = Modifier
			.shadow(elevation = 8.dp)
			.fillMaxWidth()
			.background(color = MaterialTheme.colorScheme.secondaryContainer)
			.padding(all = 12.dp),
		horizontalArrangement = Arrangement.spacedBy(space = 12.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		Icon(
			imageVector = Icons.Outlined.CloudOff,
			contentDescription = null,
			modifier = Modifier
				.padding(end = 8.dp)
				.size(size = 48.dp)
		)

		Text(
			text = stringResource(id = R.string.log_in_to_see_cloud_projects),
			modifier = Modifier.weight(
				weight = 1f,
				fill = false
			),
			color = MaterialTheme.colorScheme.onSecondaryContainer,
			style = MaterialTheme.typography.labelSmall
		)

		Button(onClick = { onTriggerLogin() }) {
			Text(
				text = stringResource(id = R.string.login),
				style = MaterialTheme.typography.labelLarge
			)
		}
	}
}

@Composable
fun NoProjects() {
	Column(
		modifier = Modifier
			.padding(all = 16.dp)
			.fillMaxSize(),
		verticalArrangement = Arrangement.Center,
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Icon(
			imageVector = Icons.Outlined.DeveloperBoardOff,
			contentDescription = null,
			modifier = Modifier
				.padding(bottom = 16.dp)
				.size(size = 64.dp),
			tint = MaterialTheme.colorScheme.outline
		)

		Text(
			text = stringResource(id = R.string.no_projects),
			textAlign = TextAlign.Center,
			style = MaterialTheme.typography.bodyMedium
		)
	}
}
