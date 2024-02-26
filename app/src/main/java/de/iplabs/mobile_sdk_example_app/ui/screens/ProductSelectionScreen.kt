package de.iplabs.mobile_sdk_example_app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.iplabs.mobile_sdk.portfolio.Product
import de.iplabs.mobile_sdk_example_app.R
import de.iplabs.mobile_sdk_example_app.ui.fadingEdge
import de.iplabs.mobile_sdk_example_app.ui.helpers.toPriceString
import kotlinx.coroutines.flow.StateFlow

@Composable
fun ProductSelectionScreen(
	products: StateFlow<List<Product>?>,
	onLoadProductImage: (Product) -> Int,
	onCardClick: (Product) -> Unit
) {
	Surface(
		modifier = Modifier.fillMaxSize(),
		color = MaterialTheme.colorScheme.background
	) {
		products.collectAsStateWithLifecycle().value.let {
			when (it?.size) {
				null -> {
					ProductsListLoadingFailed()
				}

				0 -> {
					ProductsListLoading()
				}

				else -> {
					ProductList(
						products = it,
						onLoadProductImage = onLoadProductImage,
						onCardClick = onCardClick
					)
				}
			}
		}
	}
}

@Composable
private fun ProductsListLoading() {
	Column(
		modifier = Modifier
			.padding(all = 16.dp)
			.fillMaxSize(),
		verticalArrangement = Arrangement.Center,
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		CircularProgressIndicator(
			modifier = Modifier
				.padding(all = 8.dp)
				.size(size = 48.dp)
		)

		Text(
			text = stringResource(id = R.string.loading_products),
			modifier = Modifier.padding(all = 8.dp),
			textAlign = TextAlign.Center
		)
	}
}

@Composable
private fun ProductsListLoadingFailed() {
	Column(
		modifier = Modifier
			.padding(all = 16.dp)
			.fillMaxSize(),
		verticalArrangement = Arrangement.Center,
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Icon(
			imageVector = Icons.Outlined.WarningAmber,
			contentDescription = null,
			modifier = Modifier
				.padding(bottom = 16.dp)
				.size(size = 64.dp),
			tint = MaterialTheme.colorScheme.outline
		)

		Text(
			text = stringResource(id = R.string.loading_product_selection_failed),
			textAlign = TextAlign.Center,
			style = MaterialTheme.typography.bodyMedium
		)
	}
}

@Composable
private fun ProductList(
	products: List<Product>,
	onLoadProductImage: (Product) -> Int,
	onCardClick: (Product) -> Unit
) {
	val listState = rememberLazyListState()

	LazyColumn(
		modifier = Modifier
			.fillMaxSize()
			.fadingEdge(color = MaterialTheme.colorScheme.surface),
		state = listState,
		verticalArrangement = Arrangement.spacedBy(space = 12.dp),
		contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)
	) {
		items(products) { product ->
			ProductTile(
				product = product,
				onLoadProductImage = onLoadProductImage,
				onClick = onCardClick
			)
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductTile(
	product: Product,
	onLoadProductImage: (Product) -> Int,
	onClick: (Product) -> Unit
) {
	Card(
		onClick = { onClick(product) },
		modifier = Modifier.fillMaxWidth(),
		shape = MaterialTheme.shapes.medium,
		colors = CardDefaults.cardColors(
			containerColor = MaterialTheme.colorScheme.surfaceVariant,
			contentColor = MaterialTheme.colorScheme.onSurfaceVariant
		)
	) {
		Column {
			Image(
				painter = painterResource(id = onLoadProductImage(product)),
				contentDescription = stringResource(id = R.string.product_preview_description),
				modifier = Modifier
					.padding(start = 6.dp, top = 6.dp, end = 6.dp)
					.aspectRatio(ratio = 16f / 9f)
					.fillMaxWidth()
					.clip(MaterialTheme.shapes.small),
				contentScale = ContentScale.Crop
			)

			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(all = 12.dp),
				horizontalArrangement = Arrangement.SpaceBetween
			) {
				Text(
					text = product.name,
					style = MaterialTheme.typography.titleMedium
				)

				Text(
					text = product.bestPrice.toPriceString(),
					style = MaterialTheme.typography.bodyMedium
				)
			}
		}
	}
}
