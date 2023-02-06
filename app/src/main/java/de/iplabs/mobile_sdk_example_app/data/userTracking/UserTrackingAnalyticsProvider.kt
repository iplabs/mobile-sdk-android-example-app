package de.iplabs.mobile_sdk_example_app.data.userTracking

import de.iplabs.mobile_sdk.analytics.AnalyticsEvent

interface UserTrackingAnalyticsProvider {
	val providerName: String

	fun submitEvent(event: AnalyticsEvent)
}
