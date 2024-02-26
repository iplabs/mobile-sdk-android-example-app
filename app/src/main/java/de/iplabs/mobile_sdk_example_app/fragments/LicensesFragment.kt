package de.iplabs.mobile_sdk_example_app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import de.iplabs.mobile_sdk_example_app.ui.screens.LicensesScreen
import de.iplabs.mobile_sdk_example_app.ui.theme.MobileSdkExampleAppTheme
import de.iplabs.mobile_sdk_example_app.viewmodels.LicensesViewModel
import kotlinx.coroutines.launch

class LicensesFragment : Fragment() {
	private val viewModel: LicensesViewModel by viewModels()

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		return ComposeView(requireContext()).apply {
			viewLifecycleOwner.lifecycleScope.launch {
				viewModel.assembleLicenses()
			}

			setContent {
				MobileSdkExampleAppTheme { LicensesScreen(licenses = viewModel.licenses) }
			}
		}
	}
}
