package de.iplabs.mobile_sdk_example_app.configuration

enum class Locale(
	val languageCode: String,
	val countryCode: String,
	val languageName: String,
	val countryName: String
) {
	DE_DE(
		languageCode = "de",
		countryCode = "DE",
		languageName = "German",
		countryName = "Germany"
	),
	EN_GB(
		languageCode = "en",
		countryCode = "GB",
		languageName = "English",
		countryName = "United Kingdom"
	),
	EN_US(
		languageCode = "en",
		countryCode = "US",
		languageName = "English",
		countryName = "United States"
	);

	val isoCode: String
		get() = "${this.languageCode}_${this.countryCode}"

	val description: String
		get() = "${this.languageName} (${this.countryName})"
}
