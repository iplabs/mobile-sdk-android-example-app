package de.iplabs.mobile_sdk_example_app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.iplabs.mobile_sdk.OperationResult
import de.iplabs.mobile_sdk.OperationResult.CloudProjectModificationResult
import de.iplabs.mobile_sdk.OperationResult.LocalProjectModificationResult
import de.iplabs.mobile_sdk.projectStorage.Project
import de.iplabs.mobile_sdk.projectStorage.ProjectStorageLocation
import de.iplabs.mobile_sdk_example_app.data.storage.CloudProjectsDao
import de.iplabs.mobile_sdk_example_app.data.storage.LocalProjectsDao
import de.iplabs.mobile_sdk_example_app.data.storage.ProjectsDao
import de.iplabs.mobile_sdk_example_app.data.storage.ProjectsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProjectsViewModel : ViewModel() {
	private var _loading = MutableStateFlow(false)
	val loading
		get() = _loading.asStateFlow()

	private val localDao = LocalProjectsDao()
	private val cloudDao = CloudProjectsDao()

	private val projectsRepository = ProjectsRepository(localDao = localDao, cloudDao = cloudDao)

	private val _projects = MutableStateFlow<List<Project>>(listOf())
	val projects = _projects.asStateFlow()

	@JvmOverloads
	fun retrieveAllProjects(sessionId: String? = null) {
		viewModelScope.launch {
			_loading.value = true

			val projects = projectsRepository.retrieveAllProjects(sessionId = sessionId)
			_projects.value = projects

			_loading.value = false
		}
	}

	suspend fun renameProject(
		project: Project,
		newTitle: String,
		sessionId: String?
	): OperationResult {
		val renamingResult = provideDaoForStorageLocation(project.location).rename(
			project = project,
			newTitle = newTitle,
			sessionId = sessionId
		)

		if (
			renamingResult is LocalProjectModificationResult.Success
			|| renamingResult is CloudProjectModificationResult.Success
		) {
			val renamedProject = if (project.location == ProjectStorageLocation.LOCAL) {
				(renamingResult as LocalProjectModificationResult.Success).modifiedProject!!
			} else {
				(renamingResult as CloudProjectModificationResult.Success).modifiedProject!!
			}

			_projects.value =
				_projects.value.map { if (it != project) it else renamedProject }
		}

		return renamingResult
	}

	suspend fun removeProject(
		project: Project,
		sessionId: String?
	): OperationResult {
		val removalResult = provideDaoForStorageLocation(project.location).remove(
			project = project,
			sessionId = sessionId
		)

		if (
			removalResult is LocalProjectModificationResult.Success
			|| removalResult is CloudProjectModificationResult.Success
		) {
			_projects.value = _projects.value.filter { it != project }
		}

		return removalResult
	}

	private fun provideDaoForStorageLocation(location: ProjectStorageLocation): ProjectsDao {
		return when (location) {
			ProjectStorageLocation.LOCAL -> localDao
			ProjectStorageLocation.CLOUD -> cloudDao
		}
	}
}
