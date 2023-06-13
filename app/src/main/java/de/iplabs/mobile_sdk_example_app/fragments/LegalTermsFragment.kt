package de.iplabs.mobile_sdk_example_app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import de.iplabs.mobile_sdk_example_app.R
import de.iplabs.mobile_sdk_example_app.databinding.FragmentLegalTermsBinding

class LegalPagerAdapter(
	fragmentManager: FragmentManager,
	lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {
	override fun getItemCount(): Int = 3

	override fun createFragment(position: Int): Fragment {
		return when (position) {
			1 -> PrivacyFragment()
			2 -> LicensesFragment()
			else -> AboutFragment()
		}
	}
}

class LegalTermsFragment : Fragment() {
	private var _binding: FragmentLegalTermsBinding? = null
	private val binding get() = _binding!!

	private lateinit var viewPager: ViewPager2

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentLegalTermsBinding.inflate(inflater, container, false)

		viewPager = binding.legalPager
		val tabLayout = binding.legalTabs

		viewPager.adapter = LegalPagerAdapter(parentFragmentManager, lifecycle)

		TabLayoutMediator(tabLayout, viewPager) { tab, position ->
			tab.text = resources.getString(
				when (position) {
					1 -> R.string.privacy_fragment_title
					2 -> R.string.licenses_fragment_title
					else -> R.string.about_fragment_title
				}
			)
		}.attach()

		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()

		_binding = null
	}
}
