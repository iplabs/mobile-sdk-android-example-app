package de.iplabs.mobile_sdk_example_app.data.cart

import de.iplabs.mobile_sdk.OperationResult.OrderResult
import kotlinx.coroutines.flow.StateFlow

object CartRepository {
	private lateinit var cartDao: CartDao
	private lateinit var persistedCartDao: PersistedCartDao

	operator fun invoke(cartDao: CartDao, persistedCartDao: PersistedCartDao): CartRepository {
		this.cartDao = cartDao
		this.persistedCartDao = persistedCartDao

		initializeCart()

		return this
	}

	fun getItems(): StateFlow<List<CartItem>> = cartDao.getItems()

	fun getItemCount(): StateFlow<Int> = cartDao.getItemCount()

	fun isEmpty(): StateFlow<Boolean> = cartDao.isEmpty()

	fun getTotalPrice(): StateFlow<Double> = cartDao.getTotalPrice()

	fun putItem(item: CartItem) {
		cartDao.putItem(item = item)

		persistItems()
	}

	fun increaseItemQuantity(item: CartItem) {
		cartDao.increaseItemQuantity(item = item)

		persistItems()
	}

	fun decreaseItemQuantity(item: CartItem) {
		cartDao.decreaseItemQuantity(item = item)

		persistItems()
	}

	fun removeItem(item: CartItem) {
		cartDao.removeItem(item = item)

		persistItems()
	}

	suspend fun placeOrder(sessionId: String, externalCartServiceKey: String): OrderResult {
		val orderResult = cartDao.placeOrder(
			sessionId = sessionId,
			externalCartServiceKey = externalCartServiceKey
		)

		if (orderResult is OrderResult.Success) {
			persistItems()
		}

		return orderResult
	}

	private fun initializeCart() {
		val persistedItems = persistedCartDao.loadItems()

		persistedItems.forEach {
			cartDao.putItem(it)
		}
	}

	private fun persistItems() {
		persistedCartDao.saveItems(cartDao.getItems().value)
	}
}
