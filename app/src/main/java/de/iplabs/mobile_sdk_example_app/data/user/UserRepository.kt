package de.iplabs.mobile_sdk_example_app.data.user

import java.net.URL

object UserRepository {
	private lateinit var dao: UserDao

	operator fun invoke(dao: UserDao): UserRepository {
		this.dao = dao

		return this
	}

	suspend fun loginUser(username: String, password: String, backendUrl: URL): User? {
		return dao.loginUser(
			username = username,
			password = password,
			backendUrl = backendUrl
		)
	}

	fun getUser(): User? {
		return dao.getUser()
	}

	fun logoutUser() {
		return dao.logoutUser()
	}
}
