package de.iplabs.mobile_sdk_example_app.data.cart

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object Cart {
	private var _items = MutableStateFlow<MutableList<CartItem>>(mutableListOf())

	fun getItems() = _items.asStateFlow()

	fun addItem(item: CartItem) {
		_items.value = (_items.value + item).toMutableList()
	}

	fun replaceItem(index: Int, item: CartItem) {
		_items.value = _items.value.mapIndexed { currentIndex, cartItem ->
			if (currentIndex == index) item else cartItem
		}.toMutableList()
	}

	fun increaseItemQuantity(item: CartItem) {
		_items.value = _items.value.map { cartItem ->
			if (item == cartItem) cartItem.copy(quantity = cartItem.quantity + 1) else cartItem
		}.toMutableList()
	}

	fun decreaseItemQuantity(item: CartItem) {
		check(item.isQuantityDecreasable) { "Quantity must be greater 1 to be decreasable." }

		_items.value = _items.value.map { cartItem ->
			if (item == cartItem) cartItem.copy(quantity = cartItem.quantity - 1) else cartItem
		}.toMutableList()
	}

	fun removeItem(item: CartItem) {
		_items.value = _items.value.filter { it != item }.toMutableList()
	}

	fun clear() {
		_items.value = mutableListOf()
	}
}
