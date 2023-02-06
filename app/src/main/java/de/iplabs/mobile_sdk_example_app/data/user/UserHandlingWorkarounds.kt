package de.iplabs.mobile_sdk_example_app.data.user

import de.iplabs.mobile_sdk.user.UserInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

@Suppress("BlockingMethodInNonBlockingContext")
suspend fun addUser(userInfo: UserInfo, backendUrl: URL) {
	withContext(Dispatchers.IO) {
		with(backendUrl.openConnection() as HttpURLConnection) {
			requestMethod = "POST"
			setRequestProperty("Content-Type", "application/json")

			OutputStreamWriter(outputStream).apply {
				write(userInfo.toJson())
				close()
			}

			val responseCode = responseCode

			if (responseCode != HttpURLConnection.HTTP_OK) {
				throw Exception("Failed to add the user with status code $responseCode.")
			}
		}
	}
}
