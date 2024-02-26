package de.iplabs.mobile_sdk_example_app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.iplabs.mobile_sdk.OperationResult.CloudSavedProjectModificationResult
import de.iplabs.mobile_sdk.OperationResult.LocalSavedProjectModificationResult
import de.iplabs.mobile_sdk.project.SavedProject
import de.iplabs.mobile_sdk.project.storage.SavedProjectStorageLocation
import de.iplabs.mobile_sdk_example_app.MainActivity
import de.iplabs.mobile_sdk_example_app.R
import de.iplabs.mobile_sdk_example_app.configuration.Configuration
import de.iplabs.mobile_sdk_example_app.data.cart.Cart
import de.iplabs.mobile_sdk_example_app.data.cart.CartDao
import de.iplabs.mobile_sdk_example_app.data.cart.PersistedCartDao
import de.iplabs.mobile_sdk_example_app.data.user.UserDao
import de.iplabs.mobile_sdk_example_app.databinding.RenameProjectDialogBinding
import de.iplabs.mobile_sdk_example_app.ui.focusAndShowKeyboard
import de.iplabs.mobile_sdk_example_app.ui.hideKeyboard
import de.iplabs.mobile_sdk_example_app.ui.screens.ProjectsScreen
import de.iplabs.mobile_sdk_example_app.ui.theme.MobileSdkExampleAppTheme
import de.iplabs.mobile_sdk_example_app.viewmodels.MainActivityViewModel
import de.iplabs.mobile_sdk_example_app.viewmodels.MainActivityViewModelFactory
import de.iplabs.mobile_sdk_example_app.viewmodels.ProjectsViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

class ProjectsFragment : Fragment() {
	private lateinit var parentActivity: MainActivity
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

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		parentActivity = activity as MainActivity
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
		userDao = UserDao(preferences = sharedPreferences)
		cartDao = CartDao(cart = Cart)
		persistedCartDao = PersistedCartDao(
			persistedCartFile = File(requireActivity().filesDir, Configuration.cartCacheFile)
		)

		return ComposeView(requireContext()).apply {
			setContent {
				MobileSdkExampleAppTheme {
					ProjectsScreen(
						isLoading = viewModel.loading,
						projects = viewModel.projects,
						user = mainActivityViewModel.user,
						onTriggerLogin = parentActivity::showLoginDialog,
						onLoadProject = ::loadProject,
						onRenameProject = ::renameProject,
						onRemoveProject = ::removeProject
					)
				}
			}
		}
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		viewLifecycleOwner.lifecycleScope.launch {
			viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
				mainActivityViewModel.user.collectLatest {
					viewModel.retrieveAllProjects(sessionId = it?.sessionId)
				}
			}
		}
	}

	private fun loadProject(project: SavedProject) {
		navigateToEditorScreen(project = project)
	}

	private fun renameProject(project: SavedProject) {
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

			if (project.location == SavedProjectStorageLocation.CLOUD) {
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
							renamingResult !is LocalSavedProjectModificationResult.Success
							&& renamingResult !is CloudSavedProjectModificationResult.Success
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

	private fun removeProject(project: SavedProject) {
		MaterialAlertDialogBuilder(requireContext())
			.setCancelable(false)
			.setTitle(resources.getString(R.string.remove_project_title))
			.setMessage(resources.getString(R.string.remove_project, project.title))
			.setNegativeButton(resources.getString(R.string.cancel), null)
			.setPositiveButton(resources.getString(R.string.ok)) { _, _ ->
				val sessionId = mainActivityViewModel.user.value?.sessionId

				if (project.location == SavedProjectStorageLocation.CLOUD) {
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
						removalResult !is LocalSavedProjectModificationResult.Success
						&& removalResult !is CloudSavedProjectModificationResult.Success
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

	private fun navigateToEditorScreen(project: SavedProject) {
		findNavController().navigate(
			directions = ProjectsFragmentDirections.actionNavProjectsToNavEditor(
				project = project.copy(previewImage = null)
			)
		)
	}
}
