package de.iplabs.mobile_sdk_example_app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val LightColorScheme = lightColorScheme(
	primary = Blue,
	secondaryContainer = LemonChiffon,
	onSecondaryContainer = Black,
	secondary = LightBlue,
	tertiary = DarkRose,
	background = White,
	onBackground = Black,
	surface = White,
	onSurface = DarkGrey,
	surfaceVariant = Lavender,
	onSurfaceVariant = Black,
	outline = DarkSlateGrey
)

@Composable
fun MobileSdkExampleAppTheme(
	darkTheme: Boolean = isSystemInDarkTheme(),
	content: @Composable () -> Unit
) {
	val colorScheme = LightColorScheme
	val view = LocalView.current

	if (!view.isInEditMode) {
		SideEffect {
			val window = (view.context as Activity).window
			window.statusBarColor = colorScheme.primary.toArgb()
			WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
		}
	}

	MaterialTheme(
		colorScheme = colorScheme,
		shapes = Shapes,
		typography = Typography,
		content = content
	)
}
