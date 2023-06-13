package de.iplabs.mobile_sdk_example_app.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import de.iplabs.mobile_sdk.IplabsMobileSdk
import de.iplabs.mobile_sdk.OperationResult
import de.iplabs.mobile_sdk.thirdPartyComponentLicensing.ThirdPartyComponentLicense
import de.iplabs.mobile_sdk_example_app.configuration.thirdPartyComponentLicenseInventory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LicensesViewModel : ViewModel() {
	suspend fun assembleLicenses(): List<ThirdPartyComponentLicense> {
		return withContext(Dispatchers.IO) {
			val appLicenses = collectAppLicenses().toMutableList()
			val mobileSdkLicenses = collectMobileSdkLicenses()

			appLicenses.addAll(mobileSdkLicenses)

			deduplicateLicenses(licenses = appLicenses)
		}
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
}
