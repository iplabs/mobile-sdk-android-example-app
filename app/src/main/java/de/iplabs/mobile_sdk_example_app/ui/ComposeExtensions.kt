package de.iplabs.mobile_sdk_example_app.ui

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import de.iplabs.mobile_sdk_example_app.ui.theme.DarkGoldenrod

enum class Orientation {
	HORIZONTAL, VERTICAL
}

fun Modifier.fadingEdge(
	color: Color,
	size: Float = 0.015f,
	orientation: Orientation = Orientation.VERTICAL
) = this
	.graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
	.drawWithContent {
		drawContent()
		drawRect(
			brush = constructFadingEdgeBrush(color = color, size = size, orientation = orientation),
			blendMode = BlendMode.DstOut
		)
	}

private fun constructFadingEdgeBrush(color: Color, size: Float, orientation: Orientation): Brush {
	val colorStops = arrayOf(
		0f to color,
		size to Color.Transparent,
		(1 - size) to Color.Transparent,
		1f to color
	)

	return when (orientation) {
		Orientation.VERTICAL -> Brush.verticalGradient(colorStops = colorStops)
		Orientation.HORIZONTAL -> Brush.horizontalGradient(colorStops = colorStops)
	}
}

@get:Composable
val ColorScheme.warning: Color
	get() = DarkGoldenrod
