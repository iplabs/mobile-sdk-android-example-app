package de.iplabs.mobile_sdk_example_app.ui.helpers

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.annotation.DrawableRes
import androidx.fragment.app.Fragment
import de.iplabs.mobile_sdk.portfolio.Product
import de.iplabs.mobile_sdk.projectStorage.Project
import de.iplabs.mobile_sdk_example_app.R
import de.iplabs.mobile_sdk_example_app.configuration.Configuration
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.math.RoundingMode

fun Map<String, String>.asString(): String {
	return this.map { (name, value) -> "$name: $value" }.joinToString(", ")
}

fun Double.toPriceString(): String {
	return "%.2f €".format(this)
}

fun Long.toFileSizeString(precision: Int): String {
	this.let {
		return when {
			it < 0 -> {
				throw IllegalArgumentException("File size smaller 0 is not allowed.")
			}

			it < 1024 -> {
				"$it B"
			}

			it < 1048576 -> {
				"${
					(it.toDouble() / 1024L).toBigDecimal()
						.setScale(precision, RoundingMode.HALF_EVEN)
				} KB"
			}

			it < 1073741824 -> {
				"${
					(it.toDouble() / 1048576L).toBigDecimal()
						.setScale(precision, RoundingMode.HALF_EVEN)
				} MB"
			}

			it < 1099511627776 -> {
				"${
					(it.toDouble() / 1073741824L).toBigDecimal()
						.setScale(precision, RoundingMode.HALF_EVEN)
				} GB"
			}

			else -> {
				"${
					(it.toDouble() / 1125899906842624L).toBigDecimal()
						.setScale(precision, RoundingMode.HALF_EVEN)
				} TB"
			}
		}
	}
}

fun Instant.toTimestampString(): String {
	this.toLocalDateTime(TimeZone.of("Europe/Berlin")).let {
		return "${it.year}-${it.monthNumber.toString().padStart(2, '0')}-${
			it.dayOfMonth.toString().padStart(2, '0')
		}, ${it.hour}:${
			it.minute.toString().padStart(2, '0')
		} ${if (it.hour < 12) "am" else "pm"}"
	}
}

fun Bitmap.toDrawable(context: Context): BitmapDrawable {
	return BitmapDrawable(context.resources, this)
}

@SuppressLint("DiscouragedApi")
@DrawableRes
fun Product.getImage(fragment: Fragment): Int {
	val productImage = fragment.activity?.let {
		val operatorId = Configuration.operatorId
		val localeString = with(Configuration.portfolioLocale) {
			"${this.languageCode}_${this.countryCode.lowercase()}"
		}

		it.resources?.getIdentifier(
			"product_image_${operatorId}_${localeString}_${this.id}",
			"drawable",
			it.packageName
		)
	}

	return if (productImage != null && productImage != 0) {
		productImage
	} else {
		R.drawable.product_image_missing
	}
}

fun Project.expiresSoon(): Boolean {
	return if (this.expirationDate != null) {
		val remainingTime = this.expirationDate as Instant - Clock.System.now()
		remainingTime.inWholeDays < Configuration.projectExpirationWarningPeriodInDays
	} else {
		false
	}
}
