package de.iplabs.mobile_sdk_example_app.data.portfolio

import android.content.SharedPreferences
import android.util.Log
import de.iplabs.mobile_sdk.IplabsMobileSdk
import de.iplabs.mobile_sdk.OperationResult.PortfolioRetrievalResult
import de.iplabs.mobile_sdk.portfolio.Portfolio
import de.iplabs.mobile_sdk_example_app.configuration.Configuration
import de.iplabs.mobile_sdk_example_app.data.FileCache
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.periodUntil
import java.security.MessageDigest

class PortfolioDao(private val preferences: SharedPreferences) {
	private val cacheFileName =
		"portfolio_${Configuration.operatorId}_${
			calculateMd5Hash("${Configuration.baseUrl}+${Configuration.portfolioLocale.isoCode}")
		}.json"

	suspend fun obtain(): Portfolio? {
		val cachedPortfolio = readFromCache()

		return if (cachedPortfolio != null && !hasCacheExpired()) {
			cachedPortfolio
		} else {
			val serverPortfolio = retrieveFromServer()

			when {
				(serverPortfolio != null) -> {
					serverPortfolio
				}

				(cachedPortfolio != null) -> {
					Log.w(TAG, "Using outdated portfolio from cache.")

					cachedPortfolio
				}

				else -> {
					Log.w(TAG, "Unable to obtain any portfolio.")

					null
				}
			}
		}
	}

	private suspend fun retrieveFromServer(): Portfolio? {
		Log.d(TAG, "Retrieving portfolio from server…")

		return when (val serverPortfolioResponse = IplabsMobileSdk.retrieveProductPortfolio()) {
			is PortfolioRetrievalResult.Success -> {
				with(serverPortfolioResponse.portfolio) {
					writeToCache(this)
					this
				}
			}

			is PortfolioRetrievalResult.ConnectionError,
			is PortfolioRetrievalResult.HttpError,
			is PortfolioRetrievalResult.DecodingError,
			is PortfolioRetrievalResult.UnknownError -> {
				Log.w("PortfolioDao", "Unable to retrieve a valid portfolio from the server.")

				null
			}
		}
	}

	private fun readFromCache(): Portfolio? {
		return if (FileCache.isFileCached(cacheFileName)) {
			Log.d(TAG, "Loading portfolio from cache…")

			Portfolio.fromJson(FileCache.loadTextFile(cacheFileName))
				.also {
					it ?: run {
						Log.w(TAG, "Cached portfolio is corrupt!")

						deleteFromCache()
					}
				}
		} else {
			Log.d(TAG, "No portfolio available in cache.")

			null
		}
	}

	private fun writeToCache(portfolio: Portfolio) {
		Log.d(TAG, "Storing portfolio in cache…")

		writeTimestamp()

		FileCache.saveTextFile(cacheFileName, portfolio.toJson(), true)
	}

	private fun deleteFromCache() {
		Log.d(TAG, "Deleting portfolio from cache…")

		FileCache.deleteTextFile(cacheFileName)

		deleteTimestamp()
	}

	private fun hasCacheExpired(): Boolean {
		return readTimestamp()?.let {
			it.plus(Configuration.portfolioValidityDuration)
				.periodUntil(Clock.System.now(), TimeZone.currentSystemDefault()).nanoseconds > 0
		} ?: true
	}

	private fun writeTimestamp() {
		with(preferences.edit()) {
			putString(TIMESTAMP_PREFERENCE_KEY, Clock.System.now().toString())

			apply()
		}
	}

	private fun readTimestamp(): Instant? {
		return preferences.getString(TIMESTAMP_PREFERENCE_KEY, null)?.let {
			Instant.parse(it)
		}
	}

	private fun deleteTimestamp() {
		with(preferences.edit()) {
			remove(TIMESTAMP_PREFERENCE_KEY)

			commit()
		}
	}

	private fun calculateMd5Hash(value: String): String {
		val digestedValue = MessageDigest.getInstance("MD5").digest(value.toByteArray())
		val stringBuilder = StringBuilder()

		for (byte in digestedValue) {
			stringBuilder.append(String.format("%02x", byte))
		}

		return stringBuilder.toString()
	}

	companion object {
		private val TAG: String? = PortfolioDao::class.simpleName
		private const val TIMESTAMP_PREFERENCE_KEY = "portfolioTimestamp"
	}
}
