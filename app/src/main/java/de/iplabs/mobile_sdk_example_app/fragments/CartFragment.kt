package de.iplabs.mobile_sdk_example_app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.iplabs.mobile_sdk.project.CartProject
import de.iplabs.mobile_sdk_example_app.MainActivity
import de.iplabs.mobile_sdk_example_app.R
import de.iplabs.mobile_sdk_example_app.configuration.Configuration
import de.iplabs.mobile_sdk_example_app.data.cart.Cart
import de.iplabs.mobile_sdk_example_app.data.cart.CartDao
import de.iplabs.mobile_sdk_example_app.data.cart.CartItem
import de.iplabs.mobile_sdk_example_app.data.cart.PersistedCartDao
import de.iplabs.mobile_sdk_example_app.data.user.UserDao
import de.iplabs.mobile_sdk_example_app.ui.screens.CartScreen
import de.iplabs.mobile_sdk_example_app.ui.theme.MobileSdkExampleAppTheme
import de.iplabs.mobile_sdk_example_app.viewmodels.CartViewModel
import de.iplabs.mobile_sdk_example_app.viewmodels.MainActivityViewModel
import de.iplabs.mobile_sdk_example_app.viewmodels.MainActivityViewModelFactory
import java.io.File

class CartFragment : Fragment() {
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

	private val viewModel: CartViewModel by viewModels()

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

		return ComposeView(requireContext()).apply {
			setContent {
				MobileSdkExampleAppTheme {
					CartScreen(
						cartItems = mainActivityViewModel.getCartItems(),
						totalPrice = mainActivityViewModel.getTotalPrice(),
						onDecreaseCartItemQuantity = ::decreaseItemQuantity,
						onIncreaseCartItemQuantity = ::increaseItemQuantity,
						onEditCartItem = ::editItem,
						onRemoveCartItem = ::removeItem,
						canTriggerCheckout = viewModel.canTriggerCheckout,
						onContinueShopping = ::continueShopping,
						onProceedToCheckout = ::triggerCheckout
					)
				}
			}
		}
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		viewModel.activateTriggeringCheckout()
	}

	private fun decreaseItemQuantity(item: CartItem) {
		mainActivityViewModel.decreaseCartItemQuantity(item = item)
	}

	private fun increaseItemQuantity(item: CartItem) {
		mainActivityViewModel.increaseCartItemQuantity(item = item)
	}

	private fun editItem(cartProject: CartProject) {
		findNavController().navigate(
			directions = CartFragmentDirections.actionNavCartToNavEditor(
				project = cartProject.copy(previewImage = null)
			)
		)
	}

	private fun removeItem(cartItem: CartItem) {
		MaterialAlertDialogBuilder(requireContext())
			.setCancelable(false)
			.setTitle(resources.getString(R.string.remove_cart_item_title))
			.setMessage(
				resources.getString(
					R.string.remove_cart_item,
					cartItem.cartProject.title ?: cartItem.cartProject.productName
				)
			)
			.setNegativeButton(resources.getString(R.string.cancel), null)
			.setPositiveButton(resources.getString(R.string.ok)) { _, _ ->
				mainActivityViewModel.removeItemFromCart(item = cartItem)
			}
			.show()
	}

	private fun continueShopping() {
		findNavController().navigate(
			directions = CartFragmentDirections.actionNavCartToNavProductSelection()
		)
	}

	private fun triggerCheckout() {
		viewModel.deactivateTriggeringCheckout()

		mainActivityViewModel.user.value.let {
			if (it != null) {
				proceedToCheckout(sessionId = "")
			} else {
				parentActivity.showLoginDialog(
					authenticateCallback = ::proceedToCheckout,
					cancelAuthenticationCallback = ::abortCheckout
				)
			}
		}
	}

	private fun proceedToCheckout(sessionId: String) {
		findNavController().navigate(
			directions = CartFragmentDirections.actionNavCartToNavOrderOverview()
		)
	}

	private fun abortCheckout() {
		viewModel.activateTriggeringCheckout()
	}
}
