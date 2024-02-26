package de.iplabs.mobile_sdk_example_app.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.iplabs.mobile_sdk.OperationResult.OrderResult
import de.iplabs.mobile_sdk_example_app.data.cart.CartDao
import de.iplabs.mobile_sdk_example_app.data.cart.CartItem
import de.iplabs.mobile_sdk_example_app.data.cart.CartRepository
import de.iplabs.mobile_sdk_example_app.data.cart.PersistedCartDao
import de.iplabs.mobile_sdk_example_app.data.user.User
import de.iplabs.mobile_sdk_example_app.data.user.UserDao
import de.iplabs.mobile_sdk_example_app.data.user.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.net.URL

class MainActivityViewModel(
	userDao: UserDao,
	cartDao: CartDao,
	persistedCartDao: PersistedCartDao
) : ViewModel() {
	private val userRepository = UserRepository(dao = userDao)
	private var _user = MutableStateFlow(userRepository.getUser())
	val user = _user.asStateFlow()

	private val cartRepository = CartRepository(
		cartDao = cartDao,
		persistedCartDao = persistedCartDao
	)

	fun getCartItems(): StateFlow<List<CartItem>> = cartRepository.getItems()

	fun getCartItemCount(): StateFlow<Int> = cartRepository.getItemCount()

	fun isCartEmpty(): StateFlow<Boolean> = cartRepository.isEmpty()

	fun getTotalPrice(): StateFlow<Double> = cartRepository.getTotalPrice()

	private var _loginLoading = MutableStateFlow(false)
	val loginLoading = _loginLoading.asStateFlow()

	suspend fun loginUser(username: String, password: String, backendUrl: URL): User? {
		_loginLoading.update { true }

		val userResult = try {
			userRepository.loginUser(
				username = username,
				password = password,
				backendUrl = backendUrl
			)
		} catch (_: Exception) {
			null
		}

		Log.d("IplabsMobileSdkExampleApp", "Session ID: ${userResult?.sessionId ?: "[none]"}")

		_user.update { userResult }
		_loginLoading.update { false }

		return userResult
	}

	fun logoutUser() {
		_loginLoading.update { true }

		userRepository.logoutUser()

		_user.update { null }

		_loginLoading.update { false }
	}

	fun putItemIntoCart(item: CartItem) {
		cartRepository.putItem(item = item)
	}

	fun increaseCartItemQuantity(item: CartItem) {
		cartRepository.increaseItemQuantity(item = item)
	}

	fun decreaseCartItemQuantity(item: CartItem) {
		cartRepository.decreaseItemQuantity(item = item)
	}

	fun removeItemFromCart(item: CartItem) {
		cartRepository.removeItem(item = item)
	}

	suspend fun placeOrder(sessionId: String, externalCartServiceKey: String): OrderResult {
		return cartRepository.placeOrder(
			sessionId = sessionId,
			externalCartServiceKey = externalCartServiceKey
		)
	}
}

class MainActivityViewModelFactory(
	private val userDao: UserDao,
	private val cartDao: CartDao,
	private val persistedCartDao: PersistedCartDao
) : ViewModelProvider.Factory {
	override fun <T : ViewModel> create(modelClass: Class<T>): T {
		if (modelClass.isAssignableFrom(MainActivityViewModel::class.java)) {
			@Suppress("UNCHECKED_CAST")
			return MainActivityViewModel(
				userDao = userDao,
				cartDao = cartDao,
				persistedCartDao = persistedCartDao
			) as T
		}

		throw IllegalArgumentException("Unknown ViewModel class encountered.")
	}
}
