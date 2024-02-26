package de.iplabs.mobile_sdk_example_app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import de.iplabs.mobile_sdk_example_app.ui.screens.OrderConfirmationScreen
import de.iplabs.mobile_sdk_example_app.ui.theme.MobileSdkExampleAppTheme

class OrderConfirmationFragment : Fragment() {
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		return ComposeView(requireContext()).apply {
			setContent {
				MobileSdkExampleAppTheme {
					OrderConfirmationScreen(onBackToStart = ::navigateToSelectionScreen)
				}
			}
		}
	}

	private fun navigateToSelectionScreen() {
		findNavController().navigate(
			directions = OrderConfirmationFragmentDirections.actionNavOrderConfirmationToNavProductSelection()
		)
	}
}
