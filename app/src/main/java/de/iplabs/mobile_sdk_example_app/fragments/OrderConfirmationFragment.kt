package de.iplabs.mobile_sdk_example_app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import de.iplabs.mobile_sdk_example_app.databinding.FragmentOrderConfirmationBinding

class OrderConfirmationFragment : Fragment() {
	private var _binding: FragmentOrderConfirmationBinding? = null
	private val binding get() = _binding!!

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentOrderConfirmationBinding.inflate(inflater, container, false)

		binding.lifecycleOwner = viewLifecycleOwner
		binding.navigateToHome.setOnClickListener { navigateToHome() }

		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()

		binding.unbind()
		_binding = null
	}

	private fun navigateToHome() {
		findNavController().navigate(OrderConfirmationFragmentDirections.actionNavOrderConfirmationToNavSelection())
	}
}
