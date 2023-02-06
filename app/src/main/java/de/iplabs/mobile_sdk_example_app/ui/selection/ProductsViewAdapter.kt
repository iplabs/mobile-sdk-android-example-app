package de.iplabs.mobile_sdk_example_app.ui.selection

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.iplabs.mobile_sdk.portfolio.Product
import de.iplabs.mobile_sdk_example_app.databinding.ProductItemBinding
import de.iplabs.mobile_sdk_example_app.fragments.SelectionFragment

class ProductsViewAdapter constructor(private val fragment: SelectionFragment) :
	RecyclerView.Adapter<ProductsViewAdapter.ProductViewHolder>() {
	private lateinit var binding: ProductItemBinding
	private val productDataSet: MutableList<Product> = mutableListOf()

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
		val inflater = LayoutInflater.from(parent.context)
		binding = ProductItemBinding.inflate(inflater, parent, false)

		return ProductViewHolder(binding)
	}

	override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
		holder.bind(product = productDataSet[holder.adapterPosition], fragment = fragment)
	}

	override fun getItemCount(): Int = productDataSet.size

	fun setProducts(products: List<Product>) {
		productDataSet.clear()
		productDataSet.addAll(products)

		notifyDataSetChanged()
	}

	class ProductViewHolder(
		private val binding: ProductItemBinding
	) : RecyclerView.ViewHolder(binding.root) {
		fun bind(
			product: Product,
			fragment: SelectionFragment
		) {
			binding.product = product
			binding.fragment = fragment
			binding.executePendingBindings()
		}
	}
}
