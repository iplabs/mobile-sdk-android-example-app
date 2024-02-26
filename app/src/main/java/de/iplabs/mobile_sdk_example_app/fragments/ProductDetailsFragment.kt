package de.iplabs.mobile_sdk_example_app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import de.iplabs.mobile_sdk.portfolio.Product
import de.iplabs.mobile_sdk_example_app.ui.helpers.getImage
import de.iplabs.mobile_sdk_example_app.ui.screens.ProductDetailsScreen
import de.iplabs.mobile_sdk_example_app.ui.theme.MobileSdkExampleAppTheme
import de.iplabs.mobile_sdk_example_app.viewmodels.ProductDetailsViewModel

class ProductDetailsFragment : Fragment() {
	private val navigationArguments: ProductDetailsFragmentArgs by navArgs()
	private lateinit var viewModel: ProductDetailsViewModel

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		viewModel = ProductDetailsViewModel(product = navigationArguments.product)

		return ComposeView(requireContext()).apply {
			setContent {
				MobileSdkExampleAppTheme {
					ProductDetailsScreen(
						product = navigationArguments.product,
						productOptionsConfiguration = viewModel.productOptionsConfiguration,
						onLoadProductImage = ::loadProductImage,
						onChangeProductOption = viewModel::updateProductOptionsConfiguration,
						onDesignProduct = ::navigateToEditorScreen
					)
				}
			}
		}
	}

	private fun navigateToEditorScreen() {
		findNavController().navigate(
			directions = ProductDetailsFragmentDirections.actionNavProductDetailsToNavEditor(
				project = viewModel.createNewProject()
			)
		)
	}

	@DrawableRes
	private fun loadProductImage(product: Product): Int {
		return product.getImage(fragment = this)
	}
}
