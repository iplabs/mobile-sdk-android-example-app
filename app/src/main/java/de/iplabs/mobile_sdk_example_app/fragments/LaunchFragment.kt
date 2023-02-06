package de.iplabs.mobile_sdk_example_app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import de.iplabs.mobile_sdk_example_app.databinding.FragmentLaunchBinding

class LaunchFragment : Fragment() {
	private var _binding: FragmentLaunchBinding? = null
	private val binding get() = _binding!!

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentLaunchBinding.inflate(inflater, container, false)

		binding.lifecycleOwner = viewLifecycleOwner

		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		navigateToSelection()
	}

	override fun onDestroyView() {
		super.onDestroyView()

		binding.unbind()
		_binding = null
	}

	private fun navigateToSelection() {
		val action = LaunchFragmentDirections.actionNavLaunchToNavSelection()

		findNavController().navigate(action)
	}
}
