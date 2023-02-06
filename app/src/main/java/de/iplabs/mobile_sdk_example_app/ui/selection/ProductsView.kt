package de.iplabs.mobile_sdk_example_app.ui.selection

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.iplabs.mobile_sdk.portfolio.Product
import de.iplabs.mobile_sdk_example_app.fragments.SelectionFragment

class ProductsView {
	private lateinit var recyclerView: RecyclerView
	private lateinit var viewAdapter: ProductsViewAdapter

	fun attach(context: Context, recyclerView: RecyclerView, fragment: SelectionFragment) {
		val viewLayoutManager = LinearLayoutManager(context)
		this.viewAdapter = ProductsViewAdapter(fragment)

		this.recyclerView = recyclerView.apply {
			layoutManager = viewLayoutManager
			adapter = viewAdapter
		}
	}

	fun setProducts(products: List<Product>) {
		viewAdapter.setProducts(products)
	}
}
