package de.iplabs.mobile_sdk_example_app.data

import androidx.compose.ui.graphics.Color
import kotlin.math.roundToInt

internal fun Color.toCssRgbaValue(): String {
	return buildString {
		append("rgba(")
		append(convertColorChannelFromFloatToInt(fraction = red))
		append(",")
		append(convertColorChannelFromFloatToInt(fraction = green))
		append(",")
		append(convertColorChannelFromFloatToInt(fraction = blue))
		append(",")
		append(convertColorChannelFromFloatToInt(fraction = alpha))
		append(")")
	}
}

private fun convertColorChannelFromFloatToInt(fraction: Float): Int {
	require(fraction in 0f..1f) { "fraction must be in between 0 and 1." }

	return (fraction * 255).roundToInt()
}
