package de.iplabs.mobile_sdk_example_app.data.storage

import de.iplabs.mobile_sdk.IplabsMobileSdk
import de.iplabs.mobile_sdk.OperationResult.CloudSavedProjectModificationResult
import de.iplabs.mobile_sdk.OperationResult.CloudSavedProjectsRetrievalResult
import de.iplabs.mobile_sdk.project.SavedProject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CloudProjectsDao : ProjectsDao {
	override suspend fun retrieveAll(sessionId: String?): List<SavedProject> {
		require(sessionId != null) { "No session ID provided." }

		val projectsResult = withContext(Dispatchers.IO) {
			IplabsMobileSdk.retrieveCloudSavedProjects(
				sessionId = sessionId
			)
		}

		val projects = when (projectsResult) {
			is CloudSavedProjectsRetrievalResult.Success -> projectsResult.projects
			else -> listOf()
		}

		return projects
	}

	override suspend fun rename(
		project: SavedProject,
		newTitle: String,
		sessionId: String?
	): CloudSavedProjectModificationResult {
		require(sessionId != null) { "No session ID provided." }

		return withContext(Dispatchers.IO) {
			IplabsMobileSdk.renameCloudSavedProject(
				project = project,
				newTitle = newTitle,
				sessionId = sessionId
			)
		}
	}

	override suspend fun remove(
		project: SavedProject,
		sessionId: String?
	): CloudSavedProjectModificationResult {
		require(sessionId != null) { "No session ID provided." }

		return withContext(Dispatchers.IO) {
			IplabsMobileSdk.removeCloudSavedProject(project = project, sessionId = sessionId)
		}
	}
}
