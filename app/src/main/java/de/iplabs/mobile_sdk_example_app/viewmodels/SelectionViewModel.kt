package de.iplabs.mobile_sdk_example_app.viewmodels

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.iplabs.mobile_sdk.portfolio.Portfolio
import de.iplabs.mobile_sdk_example_app.configuration.Configuration
import de.iplabs.mobile_sdk_example_app.data.portfolio.PortfolioDao
import de.iplabs.mobile_sdk_example_app.data.portfolio.PortfolioRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

class SelectionViewModel(preferences: SharedPreferences) : ViewModel() {
	private val portfolioDao = PortfolioDao(preferences)
	private val portfolioRepository = PortfolioRepository(dao = portfolioDao)

	val statePortfolio = flow {
		while (true) {
			val portfolio = portfolioRepository.get()

			emit(portfolio?.let { portfolio })
			delay(Configuration.portfolioEmissionIntervalInMs)
		}
	}.stateIn(
		initialValue = Portfolio(emptyList()),
		scope = viewModelScope,
		started = SharingStarted.WhileSubscribed(Configuration.flowTimeoutInMs)
	)
}
