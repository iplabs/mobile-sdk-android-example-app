package de.iplabs.mobile_sdk_example_app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.iplabs.mobile_sdk_example_app.R
import de.iplabs.mobile_sdk_example_app.configuration.fakeUserInfo
import de.iplabs.mobile_sdk_example_app.data.cart.CartItem
import de.iplabs.mobile_sdk_example_app.ui.fadingEdge
import de.iplabs.mobile_sdk_example_app.ui.helpers.toPriceString
import kotlinx.coroutines.flow.StateFlow

@Composable
fun OrderOverviewScreen(
	cartItems: StateFlow<List<CartItem>>,
	totalCartPrice: StateFlow<Double>,
	canTriggerOrderSubmission: StateFlow<Boolean>,
	onSubmitOrder: () -> Unit
) {
	val currentCartItems = cartItems.collectAsStateWithLifecycle().value
	val currentTotalCartPrice = totalCartPrice.collectAsStateWithLifecycle().value

	Surface(
		modifier = Modifier.fillMaxSize(),
		color = MaterialTheme.colorScheme.background
	) {
		Column {
			val scrollState = rememberScrollState()

			Column(
				modifier = Modifier
					.padding(all = 16.dp)
					.fillMaxSize()
					.weight(
						weight = 1f,
						fill = false
					)
					.fadingEdge(color = MaterialTheme.colorScheme.surface)
					.verticalScroll(state = scrollState)/*,
				verticalArrangement = Arrangement.spacedBy(space = 12.dp)*/
			) {
				Text(
					text = stringResource(id = R.string.shipping_method),
					style = MaterialTheme.typography.headlineMedium
				)

				Text(
					text = stringResource(id = R.string.mail_delivery),
					modifier = Modifier.padding(top = 8.dp),
					style = MaterialTheme.typography.bodyMedium
				)

				Text(
					text = stringResource(id = R.string.shipping_address),
					modifier = Modifier.padding(top = 24.dp),
					style = MaterialTheme.typography.headlineMedium
				)

				Text(
					text = stringResource(id = R.string.shipping_address_description),
					modifier = Modifier.padding(top = 8.dp),
					style = MaterialTheme.typography.bodyMedium
				)

				val shippingAddress = with(fakeUserInfo.shippingAddress) {
					buildString {
						append(salutation)
						append(" ")
						append(firstName)
						append(" ")
						append(lastName)
						append("\n")
						append(street)
						append("\n")
						append(zipCode)
						append(" ")
						append(city)
					}
				}

				Text(
					text = shippingAddress,
					modifier = Modifier.padding(top = 8.dp),
					style = MaterialTheme.typography.bodyMedium
				)

				Text(
					text = stringResource(id = R.string.billing_address),
					modifier = Modifier.padding(top = 24.dp),
					style = MaterialTheme.typography.headlineMedium
				)

				Text(
					text = stringResource(id = R.string.identical_billing_address),
					modifier = Modifier.padding(top = 8.dp),
					style = MaterialTheme.typography.bodyMedium
				)

				Text(
					text = stringResource(id = R.string.cart_items),
					modifier = Modifier.padding(top = 24.dp),
					style = MaterialTheme.typography.headlineMedium
				)

				CartContent(cartItems = currentCartItems, totalCartPrice = currentTotalCartPrice)
			}

			Row(modifier = Modifier.padding(all = 16.dp)) {
				Button(
					onClick = { onSubmitOrder() },
					modifier = Modifier.fillMaxWidth(),
					enabled = canTriggerOrderSubmission.collectAsStateWithLifecycle().value
				) {
					Text(
						text = stringResource(id = R.string.submit_order),
						style = MaterialTheme.typography.labelLarge
					)
				}
			}
		}
	}
}

@Composable
private fun CartContent(cartItems: List<CartItem>, totalCartPrice: Double) {
	Row(
		modifier = Modifier.padding(top = 8.dp),
		horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
	) {
		Text(
			text = stringResource(id = R.string.quantity_abbreviated),
			modifier = Modifier.weight(weight = 0.15f),
			style = MaterialTheme.typography.labelLarge
		)

		Text(
			text = stringResource(id = R.string.article_name),
			modifier = Modifier.weight(weight = 0.6f),
			style = MaterialTheme.typography.labelLarge
		)

		Text(
			text = stringResource(id = R.string.price),
			modifier = Modifier.weight(weight = 0.25f),
			textAlign = TextAlign.End,
			style = MaterialTheme.typography.labelLarge
		)
	}

	Divider(
		modifier = Modifier.padding(vertical = 4.dp),
		color = MaterialTheme.colorScheme.onBackground
	)

	cartItems.forEach {
		Row(horizontalArrangement = Arrangement.spacedBy(space = 12.dp)) {
			Text(
				text = stringResource(
					id = R.string.quantity_multiplier,
					it.quantity
				),
				modifier = Modifier.weight(weight = 0.15f),
				style = MaterialTheme.typography.bodyMedium
			)

			Text(
				text = it.cartProject.productName,
				modifier = Modifier.weight(weight = 0.6f),
				style = MaterialTheme.typography.bodyMedium
			)

			Text(
				text = it.totalPrice.toPriceString(),
				modifier = Modifier.weight(weight = 0.25f),
				textAlign = TextAlign.End,
				style = MaterialTheme.typography.bodyMedium
			)
		}
	}

	Divider(
		modifier = Modifier.padding(vertical = 4.dp),
		color = MaterialTheme.colorScheme.onBackground
	)

	Row(horizontalArrangement = Arrangement.spacedBy(space = 12.dp)) {
		Text(
			text = stringResource(id = R.string.total_price),
			modifier = Modifier.weight(weight = 0.5f),
			fontWeight = FontWeight.Bold,
			style = MaterialTheme.typography.bodyMedium
		)

		Text(
			text = totalCartPrice.toPriceString(),
			modifier = Modifier.weight(weight = 0.5f),
			fontWeight = FontWeight.Bold,
			textAlign = TextAlign.End,
			style = MaterialTheme.typography.bodyMedium
		)
	}
}
