package de.iplabs.mobile_sdk_example_app.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import de.iplabs.mobile_sdk.IplabsMobileSdk
import de.iplabs.mobile_sdk.OperationResult
import de.iplabs.mobile_sdk.thirdPartyComponentLicensing.ThirdPartyComponentLicense
import de.iplabs.mobile_sdk_example_app.configuration.thirdPartyComponentLicenseInventory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

class LicensesViewModel : ViewModel() {
	private val _licenses: MutableStateFlow<List<ThirdPartyComponentLicense>> = MutableStateFlow(
		listOf()
	)
	val licenses = _licenses.asStateFlow()

	suspend fun assembleLicenses() {
		val licenses = withContext(Dispatchers.IO) {
			val appLicenses = collectAppLicenses().toMutableList()
			val mobileSdkLicenses = collectMobileSdkLicenses()

			appLicenses.addAll(mobileSdkLicenses)

			sortLicenses(licenses = deduplicateLicenses(licenses = appLicenses))
		}

		_licenses.update { licenses }
	}

	private fun collectAppLicenses(): List<ThirdPartyComponentLicense> {
		return thirdPartyComponentLicenseInventory
	}

	private fun collectMobileSdkLicenses(): List<ThirdPartyComponentLicense> {
		return when (
			val iplabsMobileSdkThirdPartyComponentLicenses =
				IplabsMobileSdk.listThirdPartyComponentLicenses()
		) {
			is OperationResult.ThirdPartyComponentLicenseListingResult.Success -> {
				iplabsMobileSdkThirdPartyComponentLicenses.thirdPartyComponentLicenses
			}

			is OperationResult.ThirdPartyComponentLicenseListingResult.UnknownError -> {
				Log.e(
					"IplabsMobileSdkExampleApp",
					iplabsMobileSdkThirdPartyComponentLicenses.exception.toString()
				)

				listOf()
			}
		}
	}

	private fun deduplicateLicenses(
		licenses: List<ThirdPartyComponentLicense>
	): List<ThirdPartyComponentLicense> {
		return licenses.distinctBy {
			"${it.componentVendor}:${it.componentName}:${it.componentVersion}:${it.licenseName}"
		}
	}

	private fun sortLicenses(
		licenses: List<ThirdPartyComponentLicense>
	): List<ThirdPartyComponentLicense> {
		return licenses.sortedWith(
			compareBy({ it.componentName }, { it.componentVersion }, { it.componentVendor })
		)
	}
}
