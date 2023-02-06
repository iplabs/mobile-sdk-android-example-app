package de.iplabs.mobile_sdk_example_app.data

class Color(
	val red: Int? = 0,
	val green: Int? = 0,
	val blue: Int? = 0,
	val alpha: Int? = 255
) {
	fun toCssRgbaValue(): String {
		return "rgba($red,$green,$blue,$alpha)"
	}

	companion object {
		private val androidColorPattern = Regex(
			pattern = "^#([\\da-f]{2})([\\da-f]{2})([\\da-f]{2})([\\da-f]{2})$",
			option = RegexOption.IGNORE_CASE
		)

		fun fromAndroidColorValue(androidColor: String): Color {
			val matchResult = androidColorPattern.matchEntire(androidColor)

			matchResult?.groups?.let { allMatches ->
				val relevantMatches = allMatches.drop(1)

				val (alpha, red, green, blue) = relevantMatches.map {
					it?.value?.toInt(radix = 16)
				}

				return Color(
					red = red,
					green = green,
					blue = blue,
					alpha = alpha
				)
			} ?: throw IllegalArgumentException(
				"Provided string is not an Android color representation."
			)
		}
	}
}
