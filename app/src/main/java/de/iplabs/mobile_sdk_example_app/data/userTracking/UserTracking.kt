package de.iplabs.mobile_sdk_example_app.data.userTracking

import android.util.Log
import de.iplabs.mobile_sdk.UserTrackingPermission
import de.iplabs.mobile_sdk.analytics.AnalyticsEvent

object UserTracking {
	private var analyticsProvider: UserTrackingAnalyticsProvider? = null

	var permissionLevel = UserTrackingPermission.FORBID
		set(userTrackingPermissionLevel) {
			field = if (
				analyticsProvider == null
				&& userTrackingPermissionLevel != UserTrackingPermission.FORBID
			) {
				Log.e(
					"IplabsMobileSdkExampleApp",
					"User tracking permission level cannot be set to anything but “UserTrackingPermission.FORBID” because no analytics provider was set."
				)

				UserTrackingPermission.FORBID
			} else {
				userTrackingPermissionLevel
			}
		}

	operator fun invoke(
		analyticsProvider: UserTrackingAnalyticsProvider? = null,
		permissionLevel: UserTrackingPermission = UserTrackingPermission.FORBID
	): UserTracking {
		this.analyticsProvider = analyticsProvider
		this.permissionLevel = permissionLevel

		return this
	}

	init {
		this.permissionLevel = permissionLevel
	}

	fun processEvent(event: AnalyticsEvent) {
		if (permissionLevel == UserTrackingPermission.ALLOW) {
			analyticsProvider?.submitEvent(event = event)
		}
	}
}
