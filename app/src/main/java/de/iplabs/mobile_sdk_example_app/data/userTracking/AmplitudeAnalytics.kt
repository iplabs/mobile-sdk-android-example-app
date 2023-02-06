package de.iplabs.mobile_sdk_example_app.data.userTracking

import android.content.Context
import com.amplitude.android.Amplitude
import com.amplitude.android.Configuration
import de.iplabs.mobile_sdk.analytics.AnalyticsEvent

class AmplitudeAnalytics(apiKey: String, context: Context) : UserTrackingAnalyticsProvider {
	override val providerName = "Amplitude"
	private val amplitudeAnalytics: Amplitude

	init {
		amplitudeAnalytics = Amplitude(Configuration(apiKey = apiKey, context = context))
	}

	override fun submitEvent(event: AnalyticsEvent) {
		amplitudeAnalytics.track(event.name, event.properties)
	}
}
