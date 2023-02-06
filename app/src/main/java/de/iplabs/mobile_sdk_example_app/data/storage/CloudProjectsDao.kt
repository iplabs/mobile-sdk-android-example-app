package de.iplabs.mobile_sdk_example_app.data.storage

import de.iplabs.mobile_sdk.IplabsMobileSdk
import de.iplabs.mobile_sdk.OperationResult.CloudProjectModificationResult
import de.iplabs.mobile_sdk.OperationResult.CloudProjectsRetrievalResult
import de.iplabs.mobile_sdk.projectStorage.Project
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CloudProjectsDao : ProjectsDao {
	override suspend fun retrieveAll(sessionId: String?): List<Project> {
		require(sessionId != null) { "No session ID provided." }

		val projectsResult = withContext(Dispatchers.IO) {
			IplabsMobileSdk.retrieveCloudProjects(
				sessionId = sessionId
			)
		}

		val projects = when (projectsResult) {
			is CloudProjectsRetrievalResult.Success -> projectsResult.projects
			else -> listOf()
		}

		return projects
	}

	override suspend fun rename(
		project: Project,
		newTitle: String,
		sessionId: String?
	): CloudProjectModificationResult {
		require(sessionId != null) { "No session ID provided." }

		return withContext(Dispatchers.IO) {
			IplabsMobileSdk.renameCloudProject(
				project = project,
				newTitle = newTitle,
				sessionId = sessionId
			)
		}
	}

	override suspend fun remove(
		project: Project,
		sessionId: String?
	): CloudProjectModificationResult {
		require(sessionId != null) { "No session ID provided." }

		return withContext(Dispatchers.IO) {
			IplabsMobileSdk.removeCloudProject(project = project, sessionId = sessionId)
		}
	}
}
