package de.iplabs.mobile_sdk_example_app.viewmodels

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.iplabs.mobile_sdk_example_app.configuration.Configuration
import de.iplabs.mobile_sdk_example_app.data.portfolio.PortfolioDao
import de.iplabs.mobile_sdk_example_app.data.portfolio.PortfolioRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn

class ProductSelectionViewModel(preferences: SharedPreferences) : ViewModel() {
	private val portfolioDao = PortfolioDao(preferences)
	private val portfolioRepository = PortfolioRepository(dao = portfolioDao)

	@OptIn(ExperimentalCoroutinesApi::class)
	val products = flow {
		while (true) {
			val portfolio = portfolioRepository.get()

			portfolio?.let {
				emit(value = it)
				delay(timeMillis = Configuration.portfolioEmissionIntervalInMs)
			} ?: run {
				emit(value = null)
				delay(timeMillis = 500L)
			}
		}
	}.mapLatest { portfolio ->
		portfolio?.products?.filter { product ->
			product.id !in Configuration.excludedProductIds
		}
	}.stateIn(
		initialValue = emptyList(),
		scope = viewModelScope,
		started = SharingStarted.WhileSubscribed(Configuration.flowTimeoutInMs)
	)
}
