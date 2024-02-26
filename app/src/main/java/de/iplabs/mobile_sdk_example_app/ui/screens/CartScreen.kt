package de.iplabs.mobile_sdk_example_app.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.NavigateNext
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.iplabs.mobile_sdk.project.CartProject
import de.iplabs.mobile_sdk_example_app.R
import de.iplabs.mobile_sdk_example_app.data.cart.CartItem
import de.iplabs.mobile_sdk_example_app.ui.fadingEdge
import de.iplabs.mobile_sdk_example_app.ui.helpers.asString
import de.iplabs.mobile_sdk_example_app.ui.helpers.toPriceString
import kotlinx.coroutines.flow.StateFlow

@Composable
fun CartScreen(
	cartItems: StateFlow<List<CartItem>>,
	totalPrice: StateFlow<Double>,
	onDecreaseCartItemQuantity: (CartItem) -> Unit,
	onIncreaseCartItemQuantity: (CartItem) -> Unit,
	onEditCartItem: (CartProject) -> Unit,
	onRemoveCartItem: (CartItem) -> Unit,
	canTriggerCheckout: StateFlow<Boolean>,
	onContinueShopping: () -> Unit,
	onProceedToCheckout: () -> Unit
) {
	val currentCartItems = cartItems.collectAsStateWithLifecycle().value

	Surface(
		modifier = Modifier.fillMaxSize(),
		color = MaterialTheme.colorScheme.background
	) {
		currentCartItems.let {
			if (it.isNotEmpty()) {
				FilledCart(
					cartItems = currentCartItems,
					totalPrice = totalPrice,
					onDecreaseCartItemQuantity = onDecreaseCartItemQuantity,
					onIncreaseCartItemQuantity = onIncreaseCartItemQuantity,
					onEditCartItem = onEditCartItem,
					onRemoveCartItem = onRemoveCartItem,
					canTriggerCheckout = canTriggerCheckout,
					onContinueShopping = onContinueShopping,
					onProceedToCheckout = onProceedToCheckout
				)
			} else {
				EmptyCart(onContinueShopping = onContinueShopping)
			}
		}
	}
}

@Composable
private fun FilledCart(
	cartItems: List<CartItem>,
	totalPrice: StateFlow<Double>,
	onDecreaseCartItemQuantity: (CartItem) -> Unit,
	onIncreaseCartItemQuantity: (CartItem) -> Unit,
	onEditCartItem: (CartProject) -> Unit,
	onRemoveCartItem: (CartItem) -> Unit,
	canTriggerCheckout: StateFlow<Boolean>,
	onContinueShopping: () -> Unit,
	onProceedToCheckout: () -> Unit
) {
	val currentTotalPrice = totalPrice.collectAsStateWithLifecycle().value

	Column {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.weight(
					weight = 1f,
					fill = false
				)
				.fadingEdge(color = MaterialTheme.colorScheme.surface)
		) {
			CartItemList(
				cartItems = cartItems,
				onDecreaseCartItemQuantity = onDecreaseCartItemQuantity,
				onIncreaseCartItemQuantity = onIncreaseCartItemQuantity,
				onEditCartItem = onEditCartItem,
				onRemoveCartItem = onRemoveCartItem
			)
		}

		Column(
			modifier = Modifier.padding(all = 16.dp),
			verticalArrangement = Arrangement.spacedBy(space = 12.dp)
		) {
			Text(
				text = "${stringResource(id = R.string.total_price)} ${currentTotalPrice.toPriceString()}",
				modifier = Modifier.fillMaxWidth(),
				textAlign = TextAlign.End,
				style = MaterialTheme.typography.headlineMedium
			)

			Row(horizontalArrangement = Arrangement.spacedBy(space = 16.dp)) {
				Button(
					onClick = { onContinueShopping() },
					modifier = Modifier.weight(weight = 0.5f),
					colors = ButtonDefaults.buttonColors(
						containerColor = MaterialTheme.colorScheme.secondary
					)
				) {
					Icon(
						imageVector = Icons.Outlined.Add,
						contentDescription = null,
						modifier = Modifier
							.padding(end = 8.dp)
							.size(size = 24.dp)
					)

					Text(
						text = stringResource(id = R.string.continue_shopping),
						style = MaterialTheme.typography.labelLarge
					)
				}

				Button(
					onClick = { onProceedToCheckout() },
					modifier = Modifier.weight(weight = 0.5f),
					enabled = canTriggerCheckout.collectAsStateWithLifecycle().value
				) {
					Text(
						text = stringResource(id = R.string.proceed_to_checkout),
						style = MaterialTheme.typography.labelLarge
					)

					Icon(
						imageVector = Icons.Outlined.NavigateNext,
						contentDescription = null,
						modifier = Modifier
							.padding(start = 8.dp)
							.size(size = 24.dp)
					)
				}
			}
		}
	}
}

@Composable
private fun CartItemList(
	cartItems: List<CartItem>,
	onDecreaseCartItemQuantity: (CartItem) -> Unit,
	onIncreaseCartItemQuantity: (CartItem) -> Unit,
	onEditCartItem: (CartProject) -> Unit,
	onRemoveCartItem: (CartItem) -> Unit
) {
	val listState = rememberLazyListState()

	LazyColumn(
		modifier = Modifier.fillMaxSize(),
		state = listState,
		verticalArrangement = Arrangement.spacedBy(space = 12.dp),
		contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)
	) {
		items(cartItems) { cartItem ->
			CartItemTile(
				cartItem = cartItem,
				onDecreaseQuantity = { onDecreaseCartItemQuantity(cartItem) },
				onIncreaseQuantity = { onIncreaseCartItemQuantity(cartItem) },
				onEditCartItem = { onEditCartItem(cartItem.cartProject) },
				onRemoveCartItem = { onRemoveCartItem(cartItem) }
			)
		}
	}
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CartItemTile(
	cartItem: CartItem,
	onDecreaseQuantity: () -> Unit,
	onIncreaseQuantity: () -> Unit,
	onEditCartItem: () -> Unit,
	onRemoveCartItem: () -> Unit
) {
	val cardShape = MaterialTheme.shapes.medium

	var dropdownExpanded by remember { mutableStateOf(value = false) }

	Card(
		modifier = Modifier
			.fillMaxWidth()
			.clip(cardShape)
			.combinedClickable(
				onClick = {},
				onLongClick = { dropdownExpanded = true }
			),
		shape = cardShape,
		colors = CardDefaults.cardColors(
			containerColor = MaterialTheme.colorScheme.surfaceVariant,
			contentColor = MaterialTheme.colorScheme.onSurfaceVariant
		)
	) {
		Row(verticalAlignment = Alignment.CenterVertically) {
			val previewImageModifier = Modifier
				.padding(start = 6.dp, top = 6.dp, end = 6.dp)
				.size(size = 82.dp)
				.clip(MaterialTheme.shapes.small)
			val previewImageCrop = ContentScale.Fit

			cartItem.cartProject.previewImage?.let {
				Image(
					bitmap = it.asImageBitmap(),
					contentDescription = stringResource(id = R.string.cart_item_preview_description),
					modifier = previewImageModifier,
					contentScale = previewImageCrop
				)
			} ?: run {
				Image(
					painter = painterResource(id = R.drawable.icon_no_preview),
					contentDescription = stringResource(id = R.string.cart_item_preview_description),
					modifier = previewImageModifier,
					contentScale = previewImageCrop
				)
			}

			Column(
				modifier = Modifier
					.fillMaxWidth()
					.padding(all = 12.dp),
				verticalArrangement = Arrangement.SpaceBetween
			) {
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.SpaceBetween,
					verticalAlignment = Alignment.CenterVertically
				) {
					Text(
						text = cartItem.cartProject.title ?: cartItem.cartProject.productName,
						modifier = Modifier.weight(weight = 1f, fill = false),
						overflow = TextOverflow.Ellipsis,
						maxLines = 1,
						style = MaterialTheme.typography.titleMedium
					)

					Box(contentAlignment = Alignment.Center) {
						IconButton(
							onClick = { dropdownExpanded = true },
							modifier = Modifier.size(size = 32.dp)
						) {
							Icon(
								imageVector = Icons.Outlined.MoreVert,
								contentDescription = stringResource(id = R.string.popup_menu_trigger_description),
								modifier = Modifier.size(size = 24.dp)
							)
						}

						DropdownMenu(
							expanded = dropdownExpanded,
							onDismissRequest = { dropdownExpanded = false }
						) {
							DropdownMenuItem(
								text = {
									Text(
										text = stringResource(id = R.string.edit),
										style = MaterialTheme.typography.labelMedium
									)
								},
								onClick = {
									onEditCartItem()

									dropdownExpanded = false
								},
								leadingIcon = {
									Icon(
										imageVector = Icons.Outlined.Edit,
										contentDescription = null,
										modifier = Modifier
											.padding(end = 8.dp)
											.size(size = 24.dp)
									)
								}
							)

							DropdownMenuItem(
								text = {
									Text(
										text = stringResource(id = R.string.remove),
										style = MaterialTheme.typography.labelMedium
									)
								},
								onClick = {
									onRemoveCartItem()

									dropdownExpanded = false
								},
								leadingIcon = {
									Icon(
										imageVector = Icons.Outlined.Delete,
										contentDescription = null,
										modifier = Modifier
											.padding(end = 8.dp)
											.size(size = 24.dp)
									)
								}
							)
						}
					}

				}

				Text(
					text = cartItem.cartProject.options.asString(),
					overflow = TextOverflow.Ellipsis,
					maxLines = 1,
					style = MaterialTheme.typography.bodyMedium
				)

				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.SpaceBetween,
					verticalAlignment = Alignment.CenterVertically
				) {
					PlusMinusCounter(
						count = cartItem.quantity,
						onDecrease = onDecreaseQuantity,
						onIncrease = onIncreaseQuantity
					)

					Text(
						text = cartItem.totalPrice.toPriceString(),
						maxLines = 1,
						style = MaterialTheme.typography.bodyMedium
					)
				}
			}
		}
	}
}

@Composable
private fun EmptyCart(onContinueShopping: () -> Unit) {
	Column(
		modifier = Modifier
			.padding(all = 16.dp)
			.fillMaxSize(),
		verticalArrangement = Arrangement.Center,
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Icon(
			imageVector = Icons.Outlined.ShoppingBag,
			contentDescription = null,
			modifier = Modifier
				.padding(bottom = 16.dp)
				.size(size = 96.dp)
				.align(alignment = Alignment.CenterHorizontally),
			tint = MaterialTheme.colorScheme.outline
		)

		Text(
			text = stringResource(id = R.string.cart_empty),
			modifier = Modifier.padding(bottom = 32.dp),
			textAlign = TextAlign.Center,
			style = MaterialTheme.typography.bodyMedium
		)

		Button(onClick = { onContinueShopping() }) {
			Text(
				text = stringResource(id = R.string.shop_now),
				style = MaterialTheme.typography.labelLarge
			)
		}
	}
}
