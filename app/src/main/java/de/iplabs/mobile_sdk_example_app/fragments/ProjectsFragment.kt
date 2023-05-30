package de.iplabs.mobile_sdk_example_app.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.iplabs.mobile_sdk.OperationResult.CloudProjectModificationResult
import de.iplabs.mobile_sdk.OperationResult.LocalProjectModificationResult
import de.iplabs.mobile_sdk.projectStorage.Project
import de.iplabs.mobile_sdk.projectStorage.ProjectStorageLocation
import de.iplabs.mobile_sdk_example_app.MainActivity
import de.iplabs.mobile_sdk_example_app.R
import de.iplabs.mobile_sdk_example_app.data.cart.Cart
import de.iplabs.mobile_sdk_example_app.data.cart.CartDao
import de.iplabs.mobile_sdk_example_app.data.cart.PersistedCartDao
import de.iplabs.mobile_sdk_example_app.data.user.UserDao
import de.iplabs.mobile_sdk_example_app.databinding.FragmentProjectsBinding
import de.iplabs.mobile_sdk_example_app.databinding.RenameProjectDialogBinding
import de.iplabs.mobile_sdk_example_app.ui.focusAndShowKeyboard
import de.iplabs.mobile_sdk_example_app.ui.hideKeyboard
import de.iplabs.mobile_sdk_example_app.ui.projects.ProjectsView
import de.iplabs.mobile_sdk_example_app.viewmodels.MainActivityViewModel
import de.iplabs.mobile_sdk_example_app.viewmodels.MainActivityViewModelFactory
import de.iplabs.mobile_sdk_example_app.viewmodels.ProjectsViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

class ProjectsFragment : Fragment() {
	private var _binding: FragmentProjectsBinding? = null
	private val binding get() = _binding!!

	private lateinit var userDao: UserDao
	private lateinit var cartDao: CartDao
	private lateinit var persistedCartDao: PersistedCartDao

	private val mainActivityViewModel: MainActivityViewModel by activityViewModels {
		MainActivityViewModelFactory(
			userDao = userDao,
			cartDao = cartDao,
			persistedCartDao = persistedCartDao
		)
	}

	private val viewModel: ProjectsViewModel by viewModels()
	private val projectsRecyclerView = ProjectsView()

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentProjectsBinding.inflate(inflater, container, false)

		val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
		userDao = UserDao(preferences = sharedPreferences)
		cartDao = CartDao(cart = Cart)
		persistedCartDao = PersistedCartDao(
			persistedCartFile = File(requireActivity().filesDir, "cart.json")
		)

		binding.lifecycleOwner = viewLifecycleOwner
		binding.activity = activity as MainActivity
		binding.mainActivityViewModel = mainActivityViewModel
		binding.viewModel = viewModel

		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		projectsRecyclerView.attach(
			context = requireContext(),
			recyclerView = binding.projectList,
			fragment = this
		)

		lifecycleScope.launch {
			viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
				viewModel.projects.collectLatest {
					projectsRecyclerView.setProjects(it)
				}
			}
		}

		lifecycleScope.launch {
			viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
				mainActivityViewModel.user.collectLatest {
					viewModel.retrieveAllProjects(sessionId = it?.sessionId)
				}
			}
		}
	}

	override fun onDestroyView() {
		super.onDestroyView()

		binding.unbind()
		_binding = null
	}

	@SuppressLint("RestrictedApi")
	fun showProjectModificationPopupMenu(project: Project, anchor: View): Boolean {
		val popupMenu = PopupMenu(requireContext(), anchor)
		popupMenu.inflate(R.menu.project_modification)

		val helper = MenuPopupHelper(requireContext(), popupMenu.menu as MenuBuilder, anchor)
		helper.setForceShowIcon(true)

		popupMenu.setOnMenuItemClickListener {
			when (it.itemId) {
				R.id.rename_project -> {
					renameProject(project = project)

					true
				}

				R.id.remove_project -> {
					removeProject(project = project)

					true
				}

				else -> false
			}
		}

		helper.show()

		return true
	}

	fun loadProject(project: Project) {
		navigateToEditor(project = project)
	}

	private fun renameProject(project: Project) {
		val renameDialogBinding =
			RenameProjectDialogBinding.inflate(LayoutInflater.from(requireContext()))

		renameDialogBinding.newTitleText.setText(project.title)

		val renameDialog = MaterialAlertDialogBuilder(requireContext())
			.setView(renameDialogBinding.root)
			.setCancelable(false)
			.setTitle(resources.getString(R.string.rename_project_title))
			.setMessage(
				resources.getString(R.string.rename_project, project.title)
			)
			.setNegativeButton(resources.getString(R.string.cancel), null)
			.setPositiveButton(resources.getString(R.string.ok), null)
			.show()

		renameDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
			val sessionId = mainActivityViewModel.user.value?.sessionId

			if (project.location == ProjectStorageLocation.CLOUD) {
				check(sessionId != null) {
					"You have to be logged in to rename cloud projects."
				}
			}

			val newTitle = renameDialogBinding.newTitleText.text.toString()

			if (newTitle != "") {
				if (newTitle != project.title) {
					viewLifecycleOwner.lifecycleScope.launch {
						val renamingResult = viewModel.renameProject(
							project = project,
							newTitle = newTitle,
							sessionId = sessionId
						)

						if (
							renamingResult !is LocalProjectModificationResult.Success
							&& renamingResult !is CloudProjectModificationResult.Success
						) {
							MaterialAlertDialogBuilder(requireContext())
								.setTitle(resources.getString(R.string.renaming_project_failed_title))
								.setMessage(resources.getString(R.string.renaming_project_failed))
								.setPositiveButton(resources.getString(R.string.ok), null)
								.show()
						}
					}
				}

				renameDialogBinding.newTitleText.hideKeyboard()
				renameDialog.dismiss()
			} else {
				renameDialogBinding.newTitle.error = resources.getString(
					R.string.renaming_project_validation_warning
				)
			}
		}

		renameDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener {
			renameDialogBinding.newTitleText.hideKeyboard()
			renameDialog.dismiss()
		}

		renameDialogBinding.newTitleText.focusAndShowKeyboard()
	}

	private fun removeProject(project: Project) {
		MaterialAlertDialogBuilder(requireContext())
			.setCancelable(false)
			.setTitle(resources.getString(R.string.remove_project_title))
			.setMessage(resources.getString(R.string.remove_project, project.title))
			.setNegativeButton(resources.getString(R.string.cancel), null)
			.setPositiveButton(resources.getString(R.string.ok)) { _, _ ->
				val sessionId = mainActivityViewModel.user.value?.sessionId

				if (project.location == ProjectStorageLocation.CLOUD) {
					check(sessionId != null) {
						"You have to be logged in to remove cloud projects."
					}
				}

				viewLifecycleOwner.lifecycleScope.launch {
					val removalResult = viewModel.removeProject(
						project = project,
						sessionId = sessionId
					)

					if (
						removalResult !is LocalProjectModificationResult.Success
						&& removalResult !is CloudProjectModificationResult.Success
					) {
						MaterialAlertDialogBuilder(requireContext())
							.setTitle(resources.getString(R.string.removing_project_failed_title))
							.setMessage(resources.getString(R.string.removing_project_failed))
							.setPositiveButton(resources.getString(R.string.ok), null)
							.show()
					}
				}
			}
			.show()
	}

	private fun navigateToEditor(project: Project) {
		val action = ProjectsFragmentDirections.actionNavProjectsToNavEditor(
			project = project.copy(previewImage = null)
		)

		findNavController().navigate(action)
	}
}
