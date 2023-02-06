package de.iplabs.mobile_sdk_example_app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import de.iplabs.mobile_sdk.portfolio.Product
import de.iplabs.mobile_sdk_example_app.configuration.Configuration
import de.iplabs.mobile_sdk_example_app.databinding.FragmentSelectionBinding
import de.iplabs.mobile_sdk_example_app.ui.selection.ProductsView
import de.iplabs.mobile_sdk_example_app.viewmodels.SelectionViewModel
import kotlinx.coroutines.flow.collectLatest

class SelectionFragment : Fragment() {
	private var _binding: FragmentSelectionBinding? = null
	private val binding get() = _binding!!

	private lateinit var viewModel: SelectionViewModel
	private val productsRecyclerView = ProductsView()

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentSelectionBinding.inflate(inflater, container, false)

		val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
		viewModel = SelectionViewModel(sharedPreferences)

		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		productsRecyclerView.attach(
			context = requireContext(),
			recyclerView = binding.productList,
			fragment = this
		)

		val productsFlow = viewLifecycleOwner.lifecycleScope.launchWhenStarted {
			viewModel.statePortfolio
				.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
				.collectLatest { portfolio ->
					val products = portfolio?.products?.filter {
						it.id !in Configuration.excludedProductIds
					}

					productsRecyclerView.setProducts(products ?: emptyList())

					binding.apply {
						when (products?.size) {
							null -> {
								productsLoadingGroup.visibility = View.INVISIBLE
								productList.visibility = View.INVISIBLE
								loadingSelectionFailedGroup.visibility = View.VISIBLE
							}
							0 -> {
								loadingSelectionFailedGroup.visibility = View.INVISIBLE
								productList.visibility = View.INVISIBLE
								productsLoadingGroup.visibility = View.VISIBLE
							}
							else -> {
								loadingSelectionFailedGroup.visibility = View.INVISIBLE
								productsLoadingGroup.visibility = View.INVISIBLE
								productList.visibility = View.VISIBLE
							}
						}
					}
				}
		}

		productsFlow.start()
	}

	override fun onDestroyView() {
		super.onDestroyView()

		_binding = null
	}

	fun navigateToDetails(product: Product) {
		val action = SelectionFragmentDirections.actionNavSelectionToNavDetails(product = product)

		findNavController().navigate(action)
	}
}
