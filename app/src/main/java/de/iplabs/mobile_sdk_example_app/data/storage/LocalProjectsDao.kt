package de.iplabs.mobile_sdk_example_app.data.storage

import de.iplabs.mobile_sdk.IplabsMobileSdk
import de.iplabs.mobile_sdk.OperationResult.LocalSavedProjectModificationResult
import de.iplabs.mobile_sdk.OperationResult.LocalSavedProjectsRetrievalResult
import de.iplabs.mobile_sdk.project.SavedProject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalProjectsDao : ProjectsDao {
	override suspend fun retrieveAll(sessionId: String?): List<SavedProject> {
		val projects =
			when (val projectsResult = IplabsMobileSdk.retrieveLocalSavedProjects()) {
				is LocalSavedProjectsRetrievalResult.Success -> projectsResult.projects
				else -> listOf()
			}

		return projects
	}

	override suspend fun rename(
		project: SavedProject,
		newTitle: String,
		sessionId: String?
	): LocalSavedProjectModificationResult {
		return withContext(Dispatchers.IO) {
			IplabsMobileSdk.renameLocalSavedProject(project = project, newTitle = newTitle)
		}
	}

	override suspend fun remove(
		project: SavedProject,
		sessionId: String?
	): LocalSavedProjectModificationResult {
		return withContext(Dispatchers.IO) {
			IplabsMobileSdk.removeLocalSavedProject(project = project)
		}
	}
}
