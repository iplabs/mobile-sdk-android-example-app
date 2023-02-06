package de.iplabs.mobile_sdk_example_app.data.cart

import android.util.Log
import de.iplabs.mobile_sdk.IplabsMobileSdk
import de.iplabs.mobile_sdk.OperationResult.OrderResult
import de.iplabs.mobile_sdk_example_app.configuration.generateFakeOrderId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CartDao(private val cart: Cart) {
	private var _itemCount = MutableStateFlow(getItems().value.size)
	private var _isEmpty = MutableStateFlow(getItemCount().value < 1)
	private var _totalPrice = MutableStateFlow(calculateTotalPrice())

	fun getItems(): StateFlow<List<CartItem>> = cart.getItems()

	fun getItemCount(): StateFlow<Int> = _itemCount.asStateFlow()

	fun isEmpty(): StateFlow<Boolean> = _isEmpty.asStateFlow()

	fun getTotalPrice(): StateFlow<Double> = _totalPrice.asStateFlow()

	fun putItem(item: CartItem) {
		when (val itemIndex = getItemIndex(item = item)) {
			-1 -> {
				cart.addItem(item = item)

				_itemCount.value = _itemCount.value + 1
			}
			else -> {
				val originalQuantity = getItems().value[itemIndex].quantity

				cart.replaceItem(index = itemIndex, item = item.copy(quantity = originalQuantity))
			}
		}

		_isEmpty.value = false
		_totalPrice.value = calculateTotalPrice()
	}

	fun increaseItemQuantity(item: CartItem) {
		cart.increaseItemQuantity(item = item)

		_totalPrice.value = calculateTotalPrice()
	}

	fun decreaseItemQuantity(item: CartItem) {
		cart.decreaseItemQuantity(item = item)

		_totalPrice.value = calculateTotalPrice()
	}

	fun removeItem(item: CartItem) {
		cart.removeItem(item = item)

		_itemCount.value = _itemCount.value - 1
		_isEmpty.value = _itemCount.value < 1
		_totalPrice.value = calculateTotalPrice()
	}

	suspend fun placeOrder(sessionId: String, externalCartServiceKey: String): OrderResult {
		val orderItems = getItems().value.map {
			it.generateOrderItem()
		}.toSet()

		val orderId = generateFakeOrderId()

		Log.d("Order", "Order ID: $orderId")

		val orderResult = IplabsMobileSdk.submitOrder(
			id = orderId,
			items = orderItems,
			secret = externalCartServiceKey,
			sessionId = sessionId
		)

		if (orderResult is OrderResult.Success) {
			clear()
		}

		return orderResult
	}

	private fun getItemIndex(item: CartItem): Int {
		return getItems().value.indexOfFirst { it.cartProject.id == item.cartProject.id }
	}

	private fun calculateTotalPrice(): Double {
		return getItems().value.map {
			it.totalPrice
		}.fold(0.0) { total_price, item_price ->
			total_price + item_price
		}
	}

	private fun clear() {
		cart.clear()

		_itemCount.value = 0
		_isEmpty.value = true
		_totalPrice.value = 0.0
	}
}
