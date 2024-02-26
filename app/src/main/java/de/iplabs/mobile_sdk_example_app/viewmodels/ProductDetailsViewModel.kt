package de.iplabs.mobile_sdk_example_app.viewmodels

import androidx.lifecycle.ViewModel
import de.iplabs.mobile_sdk.portfolio.Product
import de.iplabs.mobile_sdk.project.NewProject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ProductDetailsViewModel(private val product: Product) : ViewModel() {
	private val _productOptionsConfiguration = MutableStateFlow(
		product.options.associateBy(
			{ it.id },
			{ product.getOptionValueNameById(optionId = it.id, valueId = it.defaultValue)!! }
		)
	)
	val productOptionsConfiguration = _productOptionsConfiguration.asStateFlow()

	fun updateProductOptionsConfiguration(key: String, value: String) {
		_productOptionsConfiguration.update { it.toMutableMap().apply { put(key, value) } }
	}

	fun createNewProject(): NewProject {
		return NewProject(
			productId = product.id,
			options = productOptionsConfiguration.value.mapValues {
				product.getOptionValueIdByName(optionId = it.key, optionName = it.value)!!
			}
		)
	}
}
