package de.iplabs.mobile_sdk_example_app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import de.iplabs.mobile_sdk_example_app.ui.screens.LaunchScreen
import de.iplabs.mobile_sdk_example_app.ui.theme.MobileSdkExampleAppTheme

class LaunchFragment : Fragment() {
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		return ComposeView(requireContext()).apply {
			setContent {
				MobileSdkExampleAppTheme {
					LaunchScreen()
				}
			}
		}
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		navigateToProductSelection()
	}

	private fun navigateToProductSelection() {
		val action = LaunchFragmentDirections.actionNavLaunchToNavProductSelection()

		findNavController().navigate(directions = action)
	}
}
