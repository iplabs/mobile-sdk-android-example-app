package de.iplabs.mobile_sdk_example_app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.iplabs.mobile_sdk.OperationResult
import de.iplabs.mobile_sdk.OperationResult.CloudSavedProjectModificationResult
import de.iplabs.mobile_sdk.OperationResult.LocalSavedProjectModificationResult
import de.iplabs.mobile_sdk.project.SavedProject
import de.iplabs.mobile_sdk.project.storage.SavedProjectStorageLocation
import de.iplabs.mobile_sdk_example_app.data.storage.CloudProjectsDao
import de.iplabs.mobile_sdk_example_app.data.storage.LocalProjectsDao
import de.iplabs.mobile_sdk_example_app.data.storage.ProjectsDao
import de.iplabs.mobile_sdk_example_app.data.storage.ProjectsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProjectsViewModel : ViewModel() {
	private var _loading = MutableStateFlow(false)
	val loading = _loading.asStateFlow()

	private val localDao = LocalProjectsDao()
	private val cloudDao = CloudProjectsDao()

	private val projectsRepository = ProjectsRepository(localDao = localDao, cloudDao = cloudDao)

	private val _projects = MutableStateFlow<List<SavedProject>>(listOf())
	val projects = _projects.asStateFlow()

	@JvmOverloads
	fun retrieveAllProjects(sessionId: String? = null) {
		viewModelScope.launch {
			_loading.update { true }

			val projects = projectsRepository.retrieveAllProjects(sessionId = sessionId)
			_projects.update { projects }

			_loading.update { false }
		}
	}

	suspend fun renameProject(
		project: SavedProject,
		newTitle: String,
		sessionId: String?
	): OperationResult {
		val renamingResult = provideDaoForStorageLocation(project.location).rename(
			project = project,
			newTitle = newTitle,
			sessionId = sessionId
		)

		if (
			renamingResult is LocalSavedProjectModificationResult.Success
			|| renamingResult is CloudSavedProjectModificationResult.Success
		) {
			val renamedProject = if (project.location == SavedProjectStorageLocation.LOCAL) {
				(renamingResult as LocalSavedProjectModificationResult.Success).modifiedProject!!
			} else {
				(renamingResult as CloudSavedProjectModificationResult.Success).modifiedProject!!
			}

			_projects.update { _projects.value.map { if (it != project) it else renamedProject } }
		}

		return renamingResult
	}

	suspend fun removeProject(
		project: SavedProject,
		sessionId: String?
	): OperationResult {
		val removalResult = provideDaoForStorageLocation(project.location).remove(
			project = project,
			sessionId = sessionId
		)

		if (
			removalResult is LocalSavedProjectModificationResult.Success
			|| removalResult is CloudSavedProjectModificationResult.Success
		) {
			_projects.update { _projects.value.filter { it != project } }
		}

		return removalResult
	}

	private fun provideDaoForStorageLocation(location: SavedProjectStorageLocation): ProjectsDao {
		return when (location) {
			SavedProjectStorageLocation.LOCAL -> localDao
			SavedProjectStorageLocation.CLOUD -> cloudDao
		}
	}
}
