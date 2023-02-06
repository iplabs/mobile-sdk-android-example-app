package de.iplabs.mobile_sdk_example_app.data.user

import android.graphics.Bitmap
import de.iplabs.mobile_sdk.user.UserInfo

data class User(
	val username: String,
	var sessionId: String,
	val userInfo: UserInfo,
	val profilePicture: Bitmap? = null
)
