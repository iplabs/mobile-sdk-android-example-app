package de.iplabs.mobile_sdk_example_app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import de.iplabs.mobile_sdk.portfolio.Product
import de.iplabs.mobile_sdk_example_app.ui.helpers.getImage
import de.iplabs.mobile_sdk_example_app.ui.screens.ProductSelectionScreen
import de.iplabs.mobile_sdk_example_app.ui.theme.MobileSdkExampleAppTheme
import de.iplabs.mobile_sdk_example_app.viewmodels.ProductSelectionViewModel

class ProductSelectionFragment : Fragment() {
	private lateinit var viewModel: ProductSelectionViewModel

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
		viewModel = ProductSelectionViewModel(sharedPreferences)

		return ComposeView(requireContext()).apply {
			setContent {
				MobileSdkExampleAppTheme {
					ProductSelectionScreen(
						products = viewModel.products,
						onLoadProductImage = ::loadProductImage,
						onCardClick = ::navigateToProductDetailsScreen
					)
				}
			}
		}
	}

	@DrawableRes
	private fun loadProductImage(product: Product): Int {
		return product.getImage(fragment = this)
	}

	private fun navigateToProductDetailsScreen(product: Product) {
		val action =
			ProductSelectionFragmentDirections.actionNavProductSelectionToNavProductDetails(
				product = product
			)

		findNavController().navigate(directions = action)
	}
}
