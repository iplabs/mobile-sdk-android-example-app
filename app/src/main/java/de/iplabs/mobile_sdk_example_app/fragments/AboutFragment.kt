package de.iplabs.mobile_sdk_example_app.fragments

import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import de.iplabs.mobile_sdk_example_app.databinding.FragmentAboutBinding

class AboutFragment : Fragment() {
	private var _binding: FragmentAboutBinding? = null
	private val binding get() = _binding!!

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentAboutBinding.inflate(inflater, container, false)

		binding.aboutAppText.apply {
			text = Html.fromHtml(
				this.text.toString(),
				Html.FROM_HTML_MODE_LEGACY
			)
			movementMethod = LinkMovementMethod.getInstance()
		}

		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()

		_binding = null
	}
}
