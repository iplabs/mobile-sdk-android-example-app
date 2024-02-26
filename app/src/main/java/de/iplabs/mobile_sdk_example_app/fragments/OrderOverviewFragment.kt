package de.iplabs.mobile_sdk_example_app.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.iplabs.mobile_sdk.OperationResult.OrderResult
import de.iplabs.mobile_sdk_example_app.MainActivity
import de.iplabs.mobile_sdk_example_app.R
import de.iplabs.mobile_sdk_example_app.configuration.Configuration
import de.iplabs.mobile_sdk_example_app.data.cart.Cart
import de.iplabs.mobile_sdk_example_app.data.cart.CartDao
import de.iplabs.mobile_sdk_example_app.data.cart.PersistedCartDao
import de.iplabs.mobile_sdk_example_app.data.user.UserDao
import de.iplabs.mobile_sdk_example_app.ui.screens.OrderOverviewScreen
import de.iplabs.mobile_sdk_example_app.ui.theme.MobileSdkExampleAppTheme
import de.iplabs.mobile_sdk_example_app.viewmodels.MainActivityViewModel
import de.iplabs.mobile_sdk_example_app.viewmodels.MainActivityViewModelFactory
import de.iplabs.mobile_sdk_example_app.viewmodels.OrderOverviewViewModel
import kotlinx.coroutines.launch
import java.io.File

class OrderOverviewFragment : Fragment() {
	private lateinit var parentActivity: MainActivity
	private lateinit var userDao: UserDao
	private lateinit var cartDao: CartDao
	private lateinit var persistedCartDao: PersistedCartDao

	private val mainActivityViewModel: MainActivityViewModel by activityViewModels {
		MainActivityViewModelFactory(
			userDao = userDao,
			cartDao = cartDao,
			persistedCartDao = persistedCartDao
		)
	}

	private val viewModel: OrderOverviewViewModel by viewModels()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		parentActivity = activity as MainActivity
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
		userDao = UserDao(preferences = sharedPreferences)
		cartDao = CartDao(cart = Cart)
		persistedCartDao = PersistedCartDao(
			persistedCartFile = File(requireActivity().filesDir, Configuration.cartCacheFile)
		)

		viewLifecycleOwner.lifecycleScope.launch {
			mainActivityViewModel.user.collect {
				if (it == null) {
					navigateToProductSelectionScreen()
				}
			}
		}

		return ComposeView(requireContext()).apply {
			setContent {
				MobileSdkExampleAppTheme {
					OrderOverviewScreen(
						cartItems = mainActivityViewModel.getCartItems(),
						totalCartPrice = mainActivityViewModel.getTotalPrice(),
						canTriggerOrderSubmission = viewModel.canTriggerOrderSubmission,
						onSubmitOrder = ::triggerOrderSubmission
					)
				}
			}
		}
	}

	private fun triggerOrderSubmission() {
		viewModel.deactivateTriggeringOrderSubmission()

		val externalCartServiceKey = parentActivity.externalCartServiceKey

		if (externalCartServiceKey != null) {
			mainActivityViewModel.user.value?.let {
				submitOrder(
					externalCartServiceKey = externalCartServiceKey,
					sessionId = it.sessionId
				)
			} ?: run {
				Log.e(
					"IplabsMobileSdkExampleApp",
					"There must be a valid user session present during order submission attempts."
				)

				navigateToProductSelectionScreen()
			}
		} else {
			showDemoModeDialog()
		}
	}

	private fun submitOrder(externalCartServiceKey: String, sessionId: String) {
		viewLifecycleOwner.lifecycleScope.launch {
			when (
				mainActivityViewModel.placeOrder(
					sessionId = sessionId,
					externalCartServiceKey = externalCartServiceKey
				)
			) {
				OrderResult.Success -> {
					findNavController().navigate(
						directions = OrderOverviewFragmentDirections.actionNavOrderOverviewToNavOrderConfirmation()
					)
				}

				is OrderResult.ConnectionError, is OrderResult.HttpError -> {
					showPlacingOrderFailedDialog(
						reason = resources.getString(R.string.placing_order_failed_temporarily)
					)
				}

				OrderResult.DuplicateOrderIdError, OrderResult.InvalidSignatureError,
				OrderResult.RevisionIdNotFoundError, is OrderResult.UnknownError -> {
					Log.e(
						"IplabsMobileSdkExampleApp",
						"An unknown error occurred during the order submission attempt."
					)

					showPlacingOrderFailedDialog(
						reason = resources.getString(R.string.placing_order_failed_permanently)
					)
				}
			}
		}
	}

	private fun showDemoModeDialog() {
		viewLifecycleOwner.lifecycleScope.launch {
			val orderingUnavailableDialog = MaterialAlertDialogBuilder(requireContext())
				.setTitle(resources.getString(R.string.ordering_unavailable_title))
				.setMessage(resources.getString(R.string.ordering_unavailable_message))
				.setNegativeButton(resources.getString(R.string.cancel), null)
				.setPositiveButton(
					resources.getString(R.string.function_unavailable_more),
					null
				)
				.show()

			orderingUnavailableDialog.getButton(AlertDialog.BUTTON_POSITIVE)
				.setOnClickListener {
					parentActivity.openSdkLandingPage()
					orderingUnavailableDialog.dismiss()
					viewModel.activateTriggeringOrderSubmission()
				}

			orderingUnavailableDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
				.setOnClickListener {
					orderingUnavailableDialog.dismiss()
					viewModel.activateTriggeringOrderSubmission()
				}
		}
	}

	private fun showPlacingOrderFailedDialog(reason: String) {
		val placingOrderFailedDialog = MaterialAlertDialogBuilder(requireContext())
			.setTitle(resources.getString(R.string.placing_order_failed_title))
			.setMessage(reason)
			.setPositiveButton(resources.getString(R.string.ok), null)
			.show()

		placingOrderFailedDialog.getButton(AlertDialog.BUTTON_POSITIVE)
			.setOnClickListener {
				placingOrderFailedDialog.dismiss()
				viewModel.activateTriggeringOrderSubmission()
			}
	}

	private fun navigateToProductSelectionScreen() {
		findNavController().navigate(
			directions = OrderConfirmationFragmentDirections.actionGlobalNavProductSelection()
		)
	}
}
