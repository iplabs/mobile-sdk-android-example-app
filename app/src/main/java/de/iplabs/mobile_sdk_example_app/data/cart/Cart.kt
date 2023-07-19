package de.iplabs.mobile_sdk_example_app.data.cart

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object Cart {
	// Underscore warning doesn’t make sense here, so it’s being suppressed, cf.
	// https://discuss.kotlinlang.org/t/object-or-top-level-property-name-warning/6621 or
	// https://youtrack.jetbrains.com/issue/KTIJ-18148/Dont-show-Object-or-top-level-property-name-should-not-start-with-an-underscore-for-private-properties
	@Suppress("ObjectPropertyName")
	private var _items = MutableStateFlow<MutableList<CartItem>>(mutableListOf())
	val items = _items.asStateFlow()

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
