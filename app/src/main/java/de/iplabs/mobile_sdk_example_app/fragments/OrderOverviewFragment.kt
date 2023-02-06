package de.iplabs.mobile_sdk_example_app.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.iplabs.mobile_sdk.OperationResult.OrderResult
import de.iplabs.mobile_sdk_example_app.MainActivity
import de.iplabs.mobile_sdk_example_app.R
import de.iplabs.mobile_sdk_example_app.data.cart.Cart
import de.iplabs.mobile_sdk_example_app.data.cart.CartDao
import de.iplabs.mobile_sdk_example_app.data.cart.PersistedCartDao
import de.iplabs.mobile_sdk_example_app.data.user.UserDao
import de.iplabs.mobile_sdk_example_app.databinding.FragmentOrderOverviewBinding
import de.iplabs.mobile_sdk_example_app.viewmodels.MainActivityViewModel
import de.iplabs.mobile_sdk_example_app.viewmodels.MainActivityViewModelFactory
import kotlinx.coroutines.launch
import java.io.File

class OrderOverviewFragment : Fragment() {
	private var _binding: FragmentOrderOverviewBinding? = null
	private val binding get() = _binding!!

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

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		parentActivity = activity as MainActivity
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentOrderOverviewBinding.inflate(inflater, container, false)

		val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
		userDao = UserDao(preferences = sharedPreferences)
		cartDao = CartDao(cart = Cart)
		persistedCartDao = PersistedCartDao(
			persistedCartFile = File(requireActivity().filesDir, "cart.json")
		)

		binding.lifecycleOwner = viewLifecycleOwner
		binding.submitOrder.setOnClickListener {
			val externalCartServiceKey = parentActivity.externalCartServiceKey

			if (externalCartServiceKey != null) {
				triggerOrder(externalCartServiceKey = externalCartServiceKey)
			} else {
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
						}
				}
			}
		}

		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()

		binding.unbind()
		_binding = null
	}

	private fun triggerOrder(externalCartServiceKey: String) {
		binding.submitOrder.isEnabled = false
		binding.orderSubmittingGroup.visibility = View.VISIBLE

		mainActivityViewModel.user.value.let {
			if (it != null) {
				submitOrder(sessionId = it.sessionId)
			} else {
				parentActivity.showLoginDialog(
					::submitOrder,
					::abortOrderSubmission
				)
			}
		}
	}

	private fun abortOrderSubmission() {
		binding.submitOrder.isEnabled = true
		binding.orderSubmittingGroup.visibility = View.GONE
	}

	private fun submitOrder(sessionId: String) {
		viewLifecycleOwner.lifecycleScope.launch {
			val externalCartServiceKey = parentActivity.externalCartServiceKey

			when (
				externalCartServiceKey?.let {
					mainActivityViewModel.placeOrder(
						sessionId = sessionId,
						externalCartServiceKey = it
					)
				}
			) {
				OrderResult.Success -> {
					findNavController().navigate(
						OrderOverviewFragmentDirections.actionNavOrderOverviewToNavOrderConfirmation()
					)
				}
				is OrderResult.ConnectionError, is OrderResult.HttpError -> {
					MaterialAlertDialogBuilder(requireContext())
						.setTitle(resources.getString(R.string.placing_order_failed_title))
						.setMessage(resources.getString(R.string.placing_order_failed_temporarily))
						.setPositiveButton(resources.getString(R.string.ok), null)
						.show()
				}
				OrderResult.DuplicateOrderIdError, OrderResult.InvalidSignatureError,
				OrderResult.RevisionIdNotFoundError, is OrderResult.UnknownError -> {
					Log.e("Cart", "An unknown error occurred during order submission.")

					MaterialAlertDialogBuilder(requireContext())
						.setTitle(resources.getString(R.string.placing_order_failed_title))
						.setMessage(resources.getString(R.string.placing_order_failed_permanently))
						.setPositiveButton(resources.getString(R.string.ok), null)
						.show()
				}
				null -> {
					Log.e(
						"SubmitOrder",
						"Unable to submit order because no External Cart Service key was set."
					)
				}
			}
		}

		abortOrderSubmission()
	}
}
