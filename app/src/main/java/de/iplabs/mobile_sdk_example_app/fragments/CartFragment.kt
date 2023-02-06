package de.iplabs.mobile_sdk_example_app.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import de.iplabs.mobile_sdk.editor.CartProject
import de.iplabs.mobile_sdk_example_app.R
import de.iplabs.mobile_sdk_example_app.data.cart.Cart
import de.iplabs.mobile_sdk_example_app.data.cart.CartDao
import de.iplabs.mobile_sdk_example_app.data.cart.CartItem
import de.iplabs.mobile_sdk_example_app.data.cart.PersistedCartDao
import de.iplabs.mobile_sdk_example_app.data.user.UserDao
import de.iplabs.mobile_sdk_example_app.databinding.FragmentCartBinding
import de.iplabs.mobile_sdk_example_app.ui.cart.CartItemsView
import de.iplabs.mobile_sdk_example_app.viewmodels.MainActivityViewModel
import de.iplabs.mobile_sdk_example_app.viewmodels.MainActivityViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import java.io.File

class CartFragment : Fragment() {
	private var _binding: FragmentCartBinding? = null
	private val binding get() = _binding!!

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

	private val cartItemsRecyclerView = CartItemsView()

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentCartBinding.inflate(inflater, container, false)

		val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
		userDao = UserDao(preferences = sharedPreferences)
		cartDao = CartDao(cart = Cart)
		persistedCartDao =
			PersistedCartDao(persistedCartFile = File(requireActivity().filesDir, "cart.json"))

		binding.lifecycleOwner = viewLifecycleOwner
		binding.mainActivityViewModel = mainActivityViewModel

		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		cartItemsRecyclerView.attach(
			context = requireContext(),
			recyclerView = binding.cartItemList,
			fragment = this
		)

		viewLifecycleOwner.lifecycleScope.launchWhenStarted {
			mainActivityViewModel.getCartItems().collectLatest {
				cartItemsRecyclerView.setItems(it)
			}
		}

		with(binding) {
			shopNow.setOnClickListener { continueShopping() }
			continueShopping.setOnClickListener { continueShopping() }
			proceedToCheckout.setOnClickListener { proceedToCheckout() }
		}
	}

	override fun onDestroyView() {
		super.onDestroyView()

		binding.unbind()
		_binding = null
	}

	fun decreaseItemQuantity(item: CartItem) {
		mainActivityViewModel.decreaseCartItemQuantity(item = item)
	}

	fun increaseItemQuantity(item: CartItem) {
		mainActivityViewModel.increaseCartItemQuantity(item = item)
	}

	@SuppressLint("RestrictedApi")
	fun showCartItemModificationPopupMenu(cartItem: CartItem, anchor: View): Boolean {
		val popupMenu = PopupMenu(requireContext(), anchor)
		popupMenu.inflate(R.menu.cart_item_modification)

		val helper = MenuPopupHelper(requireContext(), popupMenu.menu as MenuBuilder, anchor)
		helper.setForceShowIcon(true)

		popupMenu.setOnMenuItemClickListener {
			when (it.itemId) {
				R.id.edit_cart_item -> {
					editItem(cartProject = cartItem.cartProject)

					true
				}
				R.id.remove_cart_item -> {
					removeItem(cartItem = cartItem)

					true
				}
				else -> false
			}
		}

		helper.show()

		return true
	}

	private fun editItem(cartProject: CartProject) {
		findNavController().navigate(
			CartFragmentDirections.actionNavCartToNavEditor(
				cartProject = cartProject.copy(
					previewImage = null
				)
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
		if (!mainActivityViewModel.isCartEmpty().value) {
			Snackbar.make(
				binding.shopNow,
				R.string.cart_reminder,
				Snackbar.LENGTH_SHORT
			).show()
		}

		findNavController().navigate(CartFragmentDirections.actionNavCartToNavSelection())
	}

	private fun proceedToCheckout() {
		findNavController().navigate(CartFragmentDirections.actionNavCartToNavOrderOverview())
	}
}
