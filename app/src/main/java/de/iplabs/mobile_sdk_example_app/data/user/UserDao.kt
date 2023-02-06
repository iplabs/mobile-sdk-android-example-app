package de.iplabs.mobile_sdk_example_app.data.user

import android.content.SharedPreferences
import de.iplabs.mobile_sdk.user.UserInfo
import de.iplabs.mobile_sdk_example_app.configuration.fakeProfilePicture
import de.iplabs.mobile_sdk_example_app.configuration.fakeTestUserInfo
import de.iplabs.mobile_sdk_example_app.configuration.fakeUserInfo
import de.iplabs.mobile_sdk_example_app.configuration.fakeUserPassword
import java.net.URL

class UserDao(private val preferences: SharedPreferences) {
	fun getUser(): User? {
		val username = preferences.getString("username", null)
		val sessionId = preferences.getString("sessionId", null)
		val userInfoString = preferences.getString("userInfo", "{}")

		val userInfo = try {
			UserInfo.fromJson(userInfoString!!)
		} catch (e: Exception) {
			null
		}

		return if (username != null && sessionId != null && userInfo != null) {
			User(
				username = username,
				sessionId = sessionId,
				userInfo = userInfo,
				profilePicture = if (username == fakeUserInfo.eMailAddress) fakeProfilePicture else null
			)
		} else {
			null
		}
	}

	suspend fun loginUser(username: String, password: String, backendUrl: URL): User? {
		val user = retrieveUser(username = username, password = password)

		return if (user != null) {
			addUser(userInfo = user.userInfo, backendUrl = backendUrl)
			persistUser(user)

			user
		} else {
			null
		}
	}

	private fun retrieveUser(username: String, password: String): User? {
		// Just for demo purposes; this would normally be a back-end call
		return if (username == fakeUserInfo.eMailAddress && password == fakeUserPassword) {
			User(
				username = fakeUserInfo.eMailAddress,
				sessionId = fakeUserInfo.sessionId,
				userInfo = fakeUserInfo,
				profilePicture = fakeProfilePicture
			)
		} else if (username == fakeTestUserInfo.eMailAddress && password == fakeUserPassword) {
			User(
				username = fakeTestUserInfo.eMailAddress,
				sessionId = fakeTestUserInfo.sessionId,
				userInfo = fakeTestUserInfo
			)
		} else {
			null
		}
	}

	private fun persistUser(user: User) {
		with(preferences.edit()) {
			putString("username", user.username)
			putString("sessionId", user.sessionId)
			putString("userInfo", user.userInfo.toJson())

			apply()
		}
	}

	fun logoutUser() {
		removePersistedUser()
	}

	private fun removePersistedUser() {
		with(preferences.edit()) {
			remove("username")
			remove("sessionId")
			remove("userInfo")

			apply()
		}
	}
}
