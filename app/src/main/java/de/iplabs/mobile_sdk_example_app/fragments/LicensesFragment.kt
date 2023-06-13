package de.iplabs.mobile_sdk_example_app.fragments

import android.graphics.Typeface
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import de.iplabs.mobile_sdk.thirdPartyComponentLicensing.ThirdPartyComponentLicense
import de.iplabs.mobile_sdk_example_app.R
import de.iplabs.mobile_sdk_example_app.databinding.FragmentLicensesBinding
import de.iplabs.mobile_sdk_example_app.viewmodels.LicensesViewModel
import kotlinx.coroutines.launch

class LicensesFragment : Fragment() {
	private var _binding: FragmentLicensesBinding? = null
	private val binding get() = _binding!!

	private val viewModel: LicensesViewModel by viewModels()

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentLicensesBinding.inflate(inflater, container, false)

		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		lifecycleScope.launch {
			val licenses = viewModel.assembleLicenses().sortedWith(
				compareBy({ it.componentName }, { it.componentVersion }, { it.componentVendor })
			)

			addLicenses(
				licenses = licenses,
				parentLayout = binding.thirdPartyComponentLicenses
			)

			binding.licensesLoadingGroup.visibility = View.GONE
		}
	}

	private fun addLicenses(
		licenses: List<ThirdPartyComponentLicense>,
		parentLayout: LinearLayout
	) {
		for (license in licenses) {
			addLicenseDetails(license = license, parentLayout = parentLayout)
		}
	}

	private fun addLicenseDetails(license: ThirdPartyComponentLicense, parentLayout: LinearLayout) {
		val titleText = TextView(requireContext()).apply {
			layoutParams = LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT
			).apply {
				topMargin = 64
			}
			text = resources.getString(
				R.string.license_heading,
				license.componentName,
				license.componentVersion
			)
			textSize = 18.0f
			typeface = Typeface.defaultFromStyle(Typeface.BOLD)
		}

		parentLayout.addView(titleText)

		val vendorText = TextView(requireContext()).apply {
			layoutParams = LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT
			)
			text = resources.getString(R.string.license_vendor, license.componentVendor)
			textSize = 16.0f
		}

		parentLayout.addView(vendorText)

		val linkText = TextView(requireContext()).apply {
			layoutParams = LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT
			).apply {
				topMargin = 32
			}
			text = Html.fromHtml(
				resources.getString(R.string.license_link, license.componentLink),
				Html.FROM_HTML_MODE_LEGACY
			)
			textSize = 16.0f
			movementMethod = LinkMovementMethod.getInstance()
		}

		parentLayout.addView(linkText)

		val licenseText = TextView(requireContext()).apply {
			layoutParams = LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT
			).apply {
				topMargin = 32
			}
			text = resources.openRawResource(license.licenseFile).bufferedReader().use {
				it.readText()
			}
			textSize = 12.0f
			typeface = Typeface.MONOSPACE
			setHorizontallyScrolling(true)
			movementMethod = ScrollingMovementMethod.getInstance()
		}

		parentLayout.addView(licenseText)
	}

	override fun onDestroyView() {
		super.onDestroyView()

		_binding = null
	}
}
