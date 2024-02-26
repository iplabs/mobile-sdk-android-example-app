package de.iplabs.mobile_sdk_example_app.ui.screens

import android.graphics.Color
import android.util.Log
import android.widget.TextView
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.iplabs.mobile_sdk.portfolio.Product
import de.iplabs.mobile_sdk.portfolio.ProductOption
import de.iplabs.mobile_sdk.portfolio.ProductOptionValue
import de.iplabs.mobile_sdk_example_app.R
import de.iplabs.mobile_sdk_example_app.ui.fadingEdge
import de.iplabs.mobile_sdk_example_app.ui.helpers.toPriceString
import kotlinx.coroutines.flow.StateFlow

@Composable
fun ProductDetailsScreen(
	product: Product,
	productOptionsConfiguration: StateFlow<Map<String, String>>,
	onLoadProductImage: (Product) -> Int,
	onChangeProductOption: (String, String) -> Unit,
	onDesignProduct: () -> Unit
) {
	Surface(
		modifier = Modifier.fillMaxSize(),
		color = MaterialTheme.colorScheme.background
	) {
		Column(modifier = Modifier.fillMaxSize()) {
			val scrollState = rememberScrollState()

			Column(
				modifier = Modifier
					.fillMaxSize()
					.weight(
						weight = 1f,
						fill = false
					)
					.fadingEdge(color = MaterialTheme.colorScheme.surface)
					.verticalScroll(state = scrollState)
			) {
				Image(
					painter = painterResource(id = onLoadProductImage(product)),
					contentDescription = stringResource(id = R.string.product_preview_description),
					modifier = Modifier
						.padding(start = 6.dp, top = 6.dp, end = 6.dp)
						.aspectRatio(ratio = 16f / 9f)
						.fillMaxWidth(),
					contentScale = ContentScale.Crop
				)

				val formModifier = Modifier
					.padding(horizontal = 16.dp)
					.fillMaxWidth()

				Text(
					text = product.name,
					modifier = formModifier.padding(vertical = 16.dp),
					style = MaterialTheme.typography.headlineLarge
				)

				product.description?.let {
					HtmlText(
						html = it,
						modifier = formModifier
					)
				}

				ProductOptions(
					options = product.options,
					optionsConfiguration = productOptionsConfiguration
						.collectAsStateWithLifecycle().value,
					onChangeOption = onChangeProductOption,
					modifier = formModifier
				)
			}

			Row(modifier = Modifier.padding(all = 16.dp)) {
				Button(
					onClick = { onDesignProduct() },
					modifier = Modifier.fillMaxWidth()
				) {
					Text(
						text = stringResource(
							id = R.string.create_product,
							product.bestPrice.toPriceString()
						),
						style = MaterialTheme.typography.labelLarge
					)
				}
			}
		}
	}
}

@Composable
private fun HtmlText(html: String, modifier: Modifier = Modifier) {
	val sp = MaterialTheme.typography.bodyMedium.fontSize.value
	val color = MaterialTheme.colorScheme.onBackground

	AndroidView(
		modifier = modifier,
		factory = { context -> TextView(context) },
		update = {
			it.text = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_COMPACT)
			it.textSize = sp
			it.setTextColor(Color.argb(color.alpha, color.red, color.green, color.blue))
		}
	)
}

@Composable
private fun ProductOptions(
	options: List<ProductOption>,
	optionsConfiguration: Map<String, String>,
	onChangeOption: (String, String) -> Unit,
	modifier: Modifier = Modifier
) {
	options.forEach {
		ProductOption(
			option = it,
			selectedValue = optionsConfiguration.getOrDefault(it.id, ""),
			onChangeOption = onChangeOption,
			modifier = modifier
		)
	}
}

@Composable
private fun ProductOption(
	option: ProductOption,
	selectedValue: String,
	onChangeOption: (String, String) -> Unit,
	modifier: Modifier = Modifier
) {
	if (option.values.size > 1) {
		Text(
			text = option.name,
			style = MaterialTheme.typography.headlineMedium,
			modifier = modifier.padding(top = 16.dp, bottom = 0.dp)
		)

		option.description?.let {
			Text(
				text = it,
				style = MaterialTheme.typography.bodyMedium,
				modifier = modifier.padding(vertical = 8.dp)
			)
		}
	}

	when (option.values.size) {
		0 -> {
			Log.d(
				"IplabsMobileSdkExampleApp",
				"Product option “${option.id}” does not offer any values and will therefore be skipped."
			)
		}

		1 -> {
			Log.d(
				"IplabsMobileSdkExampleApp",
				"Product option “${option.id}” only offers the single value “${option.values.first()}” and will therefore be skipped."
			)
		}

		2 -> {
			RadioGroupOption(
				id = option.id,
				values = option.values,
				selectedValue = selectedValue,
				onChange = onChangeOption,
				modifier = modifier
			)
		}

		else -> {
			ComboBoxOption(
				id = option.id,
				values = option.values,
				selectedValue = selectedValue,
				onChange = onChangeOption,
				modifier = modifier
			)
		}
	}
}

@Composable
private fun RadioGroupOption(
	id: String,
	values: List<ProductOptionValue>,
	selectedValue: String,
	onChange: (String, String) -> Unit,
	modifier: Modifier = Modifier
) {
	Column(
		modifier = modifier.selectableGroup(),
		horizontalAlignment = Alignment.Start
	) {
		values.forEach { option ->
			Row(
				modifier = Modifier
					.clickable(
						interactionSource = remember { MutableInteractionSource() },
						indication = null,
						role = Role.RadioButton,
						onClick = { onChange(id, option.name) }
					)
					.padding(vertical = 4.dp),
				horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
				verticalAlignment = Alignment.CenterVertically
			) {
				RadioButton(
					selected = (option.name == selectedValue),
					onClick = null
				)

				option.description?.let {
					Column {
						Text(
							text = option.name,
							style = MaterialTheme.typography.labelMedium
						)

						Text(
							text = it,
							color = MaterialTheme.colorScheme.onBackground,
							style = MaterialTheme.typography.labelSmall
						)
					}
				} ?: run {
					Text(
						text = option.name,
						style = MaterialTheme.typography.labelMedium
					)
				}
			}
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ComboBoxOption(
	id: String,
	values: List<ProductOptionValue>,
	selectedValue: String,
	onChange: (String, String) -> Unit,
	modifier: Modifier = Modifier
) {
	var dropdownExpanded by remember { mutableStateOf(value = false) }

	ExposedDropdownMenuBox(
		expanded = dropdownExpanded,
		onExpandedChange = { dropdownExpanded = !dropdownExpanded },
		modifier = modifier
			.padding(vertical = 8.dp)
			.fillMaxWidth()
	) {
		OutlinedTextField(
			value = selectedValue,
			onValueChange = {},
			modifier = Modifier
				.fillMaxWidth()
				.menuAnchor()
				.focusProperties { canFocus = false },
			readOnly = true,
			textStyle = MaterialTheme.typography.labelMedium,
			trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) }
		)

		ExposedDropdownMenu(
			expanded = dropdownExpanded,
			onDismissRequest = { dropdownExpanded = false },
			modifier = Modifier.fillMaxWidth()
		) {
			values.forEach { option ->
				DropdownMenuItem(
					text = {
						Text(
							text = option.name,
							style = MaterialTheme.typography.labelMedium,
							modifier = Modifier.fillMaxWidth()
						)
					},
					onClick = {
						onChange(id, option.name)

						dropdownExpanded = false
					},
					modifier.fillMaxWidth()
				)
			}
		}
	}
}
