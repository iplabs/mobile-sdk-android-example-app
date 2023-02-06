package de.iplabs.mobile_sdk_example_app.data.cart

import de.iplabs.mobile_sdk.editor.CartProject
import de.iplabs.mobile_sdk.order.OrderItem
import kotlinx.serialization.Serializable

@Serializable
data class CartItem(val cartProject: CartProject, var quantity: Int = 1) {
	init {
		require(quantity >= 0) { "Quantity must be an integer greater than zero." }
	}

	val totalPrice: Double
		get() = cartProject.price * quantity

	val isQuantityDecreasable: Boolean
		get() = quantity > 1

	fun increaseQuantity() {
		quantity += 1
	}

	fun decreaseQuantity() {
		check(quantity > 1) { "Cart item quantity must be at least 1." }

		quantity -= 1
	}

	fun generateOrderItem(): OrderItem {
		return OrderItem(
			cartProjectRevisionId = cartProject.revisionId,
			quantity = quantity,
			netPriceOverride = cartProject.price
		)
	}
}
