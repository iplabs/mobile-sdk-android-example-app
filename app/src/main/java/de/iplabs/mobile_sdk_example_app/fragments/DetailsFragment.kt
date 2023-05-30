package de.iplabs.mobile_sdk_example_app.fragments

import android.graphics.Typeface
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import de.iplabs.mobile_sdk.editor.ProductConfiguration
import de.iplabs.mobile_sdk.portfolio.Product
import de.iplabs.mobile_sdk.portfolio.ProductOption
import de.iplabs.mobile_sdk.portfolio.ProductOptionValue
import de.iplabs.mobile_sdk_example_app.R
import de.iplabs.mobile_sdk_example_app.databinding.FragmentDetailsBinding
import de.iplabs.mobile_sdk_example_app.ui.helpers.getImage
import de.iplabs.mobile_sdk_example_app.ui.helpers.toPriceString
import de.iplabs.mobile_sdk_example_app.viewmodels.DetailsViewModel

class DetailsFragment : Fragment() {
	private val navigationArguments: DetailsFragmentArgs by navArgs()
	private var _binding: FragmentDetailsBinding? = null
	private val binding get() = _binding!!
	private val viewModel: DetailsViewModel by viewModels()

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentDetailsBinding.inflate(inflater, container, false)

		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		setupUi(product = navigationArguments.product)
	}

	override fun onDestroyView() {
		super.onDestroyView()

		_binding = null
	}

	private fun setupUi(product: Product?) {
		removeAllOptions()

		binding.apply {
			product?.let {
				productImage.setImageResource(product.getImage(fragment = this@DetailsFragment))
				productName.text = it.name

				if (it.description != "") {
					productDescription.apply {
						text = Html.fromHtml(it.description, Html.FROM_HTML_MODE_LEGACY)
						visibility = View.VISIBLE
					}
				}

				it.options.forEach { productOption ->
					addOption(productOption, projectProductOptions)
				}

				productPrice.text = it.bestPrice.toPriceString()

				productDetails.visibility = View.VISIBLE
				detailsLoadingSpinner.visibility = View.GONE

				createProduct.setOnClickListener {
					navigateToEditor(
						productConfiguration = ProductConfiguration(
							id = navigationArguments.product.id,
							options = viewModel.productOptions
						)
					)
				}

				createProduct.isEnabled = true
			} ?: run {
				createProduct.apply {
					isEnabled = false
					setOnClickListener(null)
				}

				productDetails.visibility = View.GONE
				detailsLoadingSpinner.visibility = View.VISIBLE
			}
		}
	}

	private fun removeAllOptions() {
		binding.projectProductOptions.removeAllViewsInLayout()
	}

	private fun addOption(option: ProductOption, parent: LinearLayout) {
		viewModel.productOptions[option.id] = option.defaultValue

		when (option.values.size) {
			0 -> {
				Log.d(
					"DetailsFragment",
					"Product option “${option.id}” does not offer any values and will therefore be skipped."
				)
			}
			1 -> {
				Log.d(
					"DetailsFragment",
					"Product option “${option.id}” only offers the single value “${option.values.first()}” and will therefore be skipped."
				)
			}
			2 -> addRadioGroupOption(
				option.id,
				option.name,
				option.values,
				option.defaultValue,
				parent,
				option.description
			)
			else -> {
				addComboBoxOption(
					option.id,
					option.name,
					option.values,
					option.defaultValue,
					parent,
					option.description
				)
			}
		}
	}

	private fun addOptionLabels(name: String, parent: LinearLayout, description: String?) {
		val formElementLayoutParams = LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.MATCH_PARENT,
			LinearLayout.LayoutParams.WRAP_CONTENT
		)

		formElementLayoutParams.bottomMargin = 16

		val optionNameLabel = TextView(context).apply {
			layoutParams = formElementLayoutParams
			textSize = 20F
			text = name
		}

		optionNameLabel.setTypeface(optionNameLabel.typeface, Typeface.BOLD)

		parent.addView(optionNameLabel)

		description?.let {
			val optionDescriptionLabel = TextView(context).apply {
				layoutParams = formElementLayoutParams
				textSize = 18F
				text = it
			}

			optionDescriptionLabel.setTypeface(optionNameLabel.typeface, Typeface.ITALIC)

			parent.addView(optionDescriptionLabel)
		}
	}

	private fun addRadioGroupOption(
		id: String,
		name: String,
		values: List<ProductOptionValue>,
		selectedValue: String,
		parent: LinearLayout,
		description: String?
	) {
		addOptionLabels(name, parent, description)

		val formElementLayoutParams = LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.MATCH_PARENT,
			LinearLayout.LayoutParams.WRAP_CONTENT
		)

		formElementLayoutParams.bottomMargin = 32

		val radioGroup = RadioGroup(context).apply {
			layoutParams = formElementLayoutParams
			contentDescription = name
		}

		val optionValueMapping = hashMapOf<Int, String>()

		values.forEach {
			val optionId = View.generateViewId()
			optionValueMapping[optionId] = it.id

			val radioButton = RadioButton(context).apply {
				this.id = optionId
				text = it.name
				isChecked = (it.id == selectedValue)
				layoutParams = formElementLayoutParams
				textSize = 18F
				contentDescription = it.name
			}

			radioGroup.addView(radioButton)
		}

		radioGroup.setOnCheckedChangeListener { _, checkedId ->
			viewModel.productOptions[id] = optionValueMapping[checkedId]
				?: throw IllegalArgumentException("No value found for the selected option.")
		}

		parent.addView(radioGroup)
	}

	private fun addComboBoxOption(
		id: String,
		name: String,
		values: List<ProductOptionValue>,
		selectedValue: String,
		parent: LinearLayout,
		description: String?
	) {
		addOptionLabels(name, parent, description)

		val formElementLayoutParams = LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.MATCH_PARENT,
			LinearLayout.LayoutParams.WRAP_CONTENT
		)

		formElementLayoutParams.bottomMargin = 32

		val optionValueMapping = values.map { it.id }
		val entries = values.map { it.name }

		val dropdown = Spinner(context).apply {
			layoutParams = formElementLayoutParams
			adapter = ArrayAdapter(
				requireContext(),
				R.layout.dropdown_item,
				entries
			)
			contentDescription = name
		}

		dropdown.setSelection(entries.indexOf(selectedValue))

		dropdown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
			override fun onItemSelected(
				_parent: AdapterView<*>?,
				_view: View?,
				position: Int,
				_id: Long
			) {
				viewModel.productOptions[id] = optionValueMapping[position]
			}

			override fun onNothingSelected(parent: AdapterView<*>?) {}
		}

		parent.addView(dropdown)
	}

	private fun navigateToEditor(productConfiguration: ProductConfiguration) {
		val action =
			DetailsFragmentDirections.actionNavDetailsToNavEditor(productConfiguration = productConfiguration)

		findNavController().navigate(action)
	}
}
