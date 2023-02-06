package de.iplabs.mobile_sdk_example_app.data.storage

import de.iplabs.mobile_sdk.IplabsMobileSdk
import de.iplabs.mobile_sdk.OperationResult.LocalProjectModificationResult
import de.iplabs.mobile_sdk.OperationResult.LocalProjectsRetrievalResult
import de.iplabs.mobile_sdk.projectStorage.Project
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalProjectsDao : ProjectsDao {
	override suspend fun retrieveAll(sessionId: String?): List<Project> {
		val projects =
			when (val projectsResult = IplabsMobileSdk.retrieveLocalProjects()) {
				is LocalProjectsRetrievalResult.Success -> projectsResult.projects
				else -> listOf()
			}

		return projects
	}

	override suspend fun rename(
		project: Project,
		newTitle: String,
		sessionId: String?
	): LocalProjectModificationResult {
		return withContext(Dispatchers.IO) {
			IplabsMobileSdk.renameLocalProject(project = project, newTitle = newTitle)
		}
	}

	override suspend fun remove(
		project: Project,
		sessionId: String?
	): LocalProjectModificationResult {
		return withContext(Dispatchers.IO) {
			IplabsMobileSdk.removeLocalProject(project = project)
		}
	}
}
