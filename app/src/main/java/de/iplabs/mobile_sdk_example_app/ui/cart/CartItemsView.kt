package de.iplabs.mobile_sdk_example_app.ui.cart

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.iplabs.mobile_sdk_example_app.data.cart.CartItem
import de.iplabs.mobile_sdk_example_app.fragments.CartFragment

class CartItemsView {
	private lateinit var recyclerView: RecyclerView
	private lateinit var viewAdapter: CartItemsViewAdapter

	fun attach(context: Context, recyclerView: RecyclerView, fragment: CartFragment) {
		val viewLayoutManager = LinearLayoutManager(context)
		this.viewAdapter = CartItemsViewAdapter(fragment = fragment)

		this.recyclerView = recyclerView.apply {
			layoutManager = viewLayoutManager
			adapter = viewAdapter
		}
	}

	fun setItems(items: List<CartItem>) {
		viewAdapter.setItems(items)
	}
}
