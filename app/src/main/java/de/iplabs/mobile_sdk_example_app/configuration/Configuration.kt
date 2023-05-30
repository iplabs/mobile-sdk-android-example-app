package de.iplabs.mobile_sdk_example_app.configuration

import de.iplabs.mobile_sdk.editor.EditorConfiguration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

object Configuration {
	const val operatorId = 97000040
	val portfolioLocale = Locale.EN_GB

	// Locale.DE_DE also available for demo purposes
	val translationLocale: Locale? = null

	val excludedProductIds = listOf(50035648, 50035650, 60002077)
	const val baseUrl = "https://$operatorId-staging.iplabs.io"
	val editorConfiguration = EditorConfiguration(
		allowCloudStorage = true,
		allowMultipleElementsForWallDecor = true,
		warnUncoveredWrapAreaWallDecor = true
	)
	const val externalCartServiceBaseUrl =
		"https://external-cart.staging.eu-central-1.iplabs.io"
	const val portfolioEmissionIntervalInMs = 300_000L
	val portfolioValidityDuration = 6.toDuration(DurationUnit.HOURS)
	const val flowTimeoutInMs = 5_000L
	const val projectExpirationWarningPeriodInDays = 7
	const val sdkInfoUrl = "https://www.iplabs.com/photo-commerce-mobile-sdk/"
}
