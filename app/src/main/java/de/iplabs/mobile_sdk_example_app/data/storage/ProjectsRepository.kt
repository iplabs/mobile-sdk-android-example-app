package de.iplabs.mobile_sdk_example_app.data.storage

import de.iplabs.mobile_sdk.projectStorage.Project

object ProjectsRepository {
	private lateinit var localDao: LocalProjectsDao
	private lateinit var cloudDao: CloudProjectsDao

	operator fun invoke(
		localDao: LocalProjectsDao,
		cloudDao: CloudProjectsDao
	): ProjectsRepository {
		this.localDao = localDao
		this.cloudDao = cloudDao

		return this
	}

	suspend fun retrieveAllProjects(sessionId: String? = null): List<Project> {
		val projects = localDao.retrieveAll().toMutableList()

		if (sessionId != null) {
			projects += cloudDao.retrieveAll(sessionId = sessionId)
		}

		return projects.sortedByDescending { it.lastModifiedDate }
	}
}
