package de.iplabs.mobile_sdk_example_app.data.storage

import de.iplabs.mobile_sdk.OperationResult
import de.iplabs.mobile_sdk.projectStorage.Project

interface ProjectsDao {
	suspend fun retrieveAll(sessionId: String? = null): List<Project>

	suspend fun rename(
		project: Project,
		newTitle: String,
		sessionId: String? = null
	): OperationResult

	suspend fun remove(project: Project, sessionId: String? = null): OperationResult
}
