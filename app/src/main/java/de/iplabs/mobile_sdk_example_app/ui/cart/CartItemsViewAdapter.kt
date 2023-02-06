package de.iplabs.mobile_sdk_example_app.ui.cart

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.iplabs.mobile_sdk_example_app.R
import de.iplabs.mobile_sdk_example_app.data.cart.CartItem
import de.iplabs.mobile_sdk_example_app.databinding.CartItemBinding
import de.iplabs.mobile_sdk_example_app.fragments.CartFragment

class CartItemsViewAdapter(
	private val fragment: CartFragment
) : RecyclerView.Adapter<CartItemsViewAdapter.CartItemViewHolder>() {
	private lateinit var binding: CartItemBinding
	private val cartItemDataSet: MutableList<CartItem> = mutableListOf()

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartItemViewHolder {
		val inflater = LayoutInflater.from(parent.context)
		binding = CartItemBinding.inflate(inflater, parent, false)

		return CartItemViewHolder(binding)
	}

	override fun onBindViewHolder(holder: CartItemViewHolder, position: Int) {
		holder.bind(
			cartItem = cartItemDataSet[holder.adapterPosition],
			fragment = fragment,
			popupMenuAnchor = holder.itemView.findViewById(R.id.cart_item_popup_trigger)
		)
	}

	override fun getItemCount(): Int = cartItemDataSet.size

	fun setItems(items: List<CartItem>) {
		with(cartItemDataSet) {
			clear()
			addAll(items)
		}

		notifyDataSetChanged()
	}

	class CartItemViewHolder(
		private val binding: CartItemBinding
	) : RecyclerView.ViewHolder(binding.root) {
		fun bind(cartItem: CartItem, fragment: CartFragment, popupMenuAnchor: View) {
			binding.cartItem = cartItem
			binding.fragment = fragment
			binding.popupMenuAnchor = popupMenuAnchor

			binding.executePendingBindings()
		}
	}
}
