package de.iplabs.mobile_sdk_example_app.data.storage

import de.iplabs.mobile_sdk.OperationResult
import de.iplabs.mobile_sdk.project.SavedProject

interface ProjectsDao {
	suspend fun retrieveAll(sessionId: String? = null): List<SavedProject>

	suspend fun rename(
		project: SavedProject,
		newTitle: String,
		sessionId: String? = null
	): OperationResult

	suspend fun remove(project: SavedProject, sessionId: String? = null): OperationResult
}
