package de.iplabs.mobile_sdk_example_app.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.iplabs.mobile_sdk.OperationResult.AuthenticationError
import de.iplabs.mobile_sdk.OperationResult.CartProjectEditingError
import de.iplabs.mobile_sdk.analytics.AnalyticsEvent
import de.iplabs.mobile_sdk.editor.*
import de.iplabs.mobile_sdk.projectStorage.Project
import de.iplabs.mobile_sdk_example_app.MainActivity
import de.iplabs.mobile_sdk_example_app.R
import de.iplabs.mobile_sdk_example_app.configuration.Configuration
import de.iplabs.mobile_sdk_example_app.data.Color
import de.iplabs.mobile_sdk_example_app.data.cart.Cart
import de.iplabs.mobile_sdk_example_app.data.cart.CartDao
import de.iplabs.mobile_sdk_example_app.data.cart.CartItem
import de.iplabs.mobile_sdk_example_app.data.cart.PersistedCartDao
import de.iplabs.mobile_sdk_example_app.data.user.UserDao
import de.iplabs.mobile_sdk_example_app.databinding.FragmentEditorBinding
import de.iplabs.mobile_sdk_example_app.viewmodels.MainActivityViewModel
import de.iplabs.mobile_sdk_example_app.viewmodels.MainActivityViewModelFactory
import kotlinx.coroutines.launch
import java.io.File

class EditorFragment : Fragment() {
	private val navigationArguments: EditorFragmentArgs by navArgs()
	private var _binding: FragmentEditorBinding? = null
	private val binding get() = _binding!!
	private lateinit var backNavigationCallback: OnBackPressedCallback
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

	private lateinit var editor: EditorView
	private var singleFilePicker =
		registerForActivityResult(ActivityResultContracts.OpenDocument()) {
			editor.filePathsCallback?.onReceiveValue(
				it?.let {
					arrayOf(it)
				} ?: run {
					arrayOf()
				}
			)

			editor.filePathsCallback = null
		}
	private var multipleFilesPicker =
		registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) {
			editor.filePathsCallback?.onReceiveValue(it.toTypedArray())
			editor.filePathsCallback = null
		}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		parentActivity = activity as MainActivity

		backNavigationCallback = requireActivity().onBackPressedDispatcher.addCallback(this) {
			isEnabled = false
			initiateTerminationRequest(targetDestinationId = -1)
		}
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentEditorBinding.inflate(inflater, container, false)
		binding.lifecycleOwner = viewLifecycleOwner

		val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
		userDao = UserDao(preferences = sharedPreferences)
		cartDao = CartDao(cart = Cart)
		persistedCartDao =
			PersistedCartDao(persistedCartFile = File(requireActivity().filesDir, "cart.json"))

		editor = binding.editor

		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		val editorCallbacks = object : EditorCallbacks {
			override fun getFilePicker(
				multipleFilesMode: Boolean
			): ActivityResultLauncher<Array<String>> {
				return if (multipleFilesMode) {
					multipleFilesPicker
				} else {
					singleFilePicker
				}
			}

			override fun requestSessionId(
				authenticateCallback: (String) -> Unit,
				cancelAuthenticationCallback: () -> Unit
			) {
				mainActivityViewModel.user.value.let {
					if (it != null) {
						authenticateCallback(it.sessionId)
					} else {
						parentActivity.showLoginDialog(
							authenticateCallback,
							cancelAuthenticationCallback
						)
					}
				}
			}

			override fun authenticationFailed(reason: AuthenticationError) {
				Log.d("authenticationFailed", reason.javaClass.simpleName)

				parentActivity.showLoginFailedDialog()
			}

			override fun transferCartProject(cartProject: CartProject) {
				val cartItem = CartItem(cartProject)
				mainActivityViewModel.putItemIntoCart(item = cartItem)

				requireActivity().runOnUiThread {
					proceedToCart()
				}
			}

			override fun editCartProjectFailed(reason: CartProjectEditingError) {
				Log.d("editCartProjectFailed", reason.javaClass.simpleName)

				viewLifecycleOwner.lifecycleScope.launch {
					MaterialAlertDialogBuilder(requireContext())
						.setTitle(resources.getString(R.string.editing_cart_project_failed_title))
						.setMessage(resources.getString(R.string.editing_cart_project_failed))
						.setPositiveButton(resources.getString(R.string.ok), null)
						.show()

					proceedToCart()
				}
			}

			override fun handleLoadProjectTapped() {
				initiateTerminationRequest(targetDestinationId = R.id.nav_projects)
			}

			override fun handleTerminationRequest(
				state: EditorTerminationState,
				targetDestinationId: Int?
			) {
				when (state) {
					EditorTerminationState.READY -> {
						targetDestinationId?.let {
							requireActivity().runOnUiThread {
								when (it) {
									-1 -> {
										parentActivity.onBackPressedDispatcher.onBackPressed()
									}
									R.id.nav_projects -> {
										findNavController().navigate(
											EditorFragmentDirections.actionNavEditorToNavProjects()
										)
									}
									else -> {
										findNavController().navigate(it)
									}
								}
							}
						}
					}
					EditorTerminationState.PENDING_CHANGES -> {
						backNavigationCallback.isEnabled = true
					}
				}
			}

			override fun receiveAnalyticsEvent(event: AnalyticsEvent) {
				parentActivity.userTracking.processEvent(event = event)
			}
		}

		initializeEditor(
			editorCallbacks = editorCallbacks,
			productConfiguration = navigationArguments.productConfiguration,
			project = navigationArguments.project,
			cartProject = navigationArguments.cartProject,
			editorConfiguration = Configuration.editorConfiguration,
			branding = generateBranding()
		)
	}

	override fun onDestroyView() {
		super.onDestroyView()

		binding.unbind()
		_binding = null
	}

	fun initiateTerminationRequest(targetDestinationId: Int? = null) {
		editor.requestTermination(targetDestinationId = targetDestinationId)
	}

	@SuppressLint("ResourceType")
	private fun generateBranding(): Branding {
		return Branding(
			primaryColor = Color.fromAndroidColorValue(resources.getString(R.color.primary))
				.toCssRgbaValue(),
			secondaryColor = Color.fromAndroidColorValue(resources.getString(R.color.secondary))
				.toCssRgbaValue(),
			fontColorDark = Color.fromAndroidColorValue(resources.getString(R.color.on_background))
				.toCssRgbaValue()
		)
	}

	private fun initializeEditor(
		editorCallbacks: EditorCallbacks,
		productConfiguration: ProductConfiguration? = null,
		project: Project? = null,
		cartProject: CartProject? = null,
		editorConfiguration: EditorConfiguration?,
		branding: Branding? = null
	) {
		when (val editableItem = listOf(
			productConfiguration,
			project,
			cartProject
		).singleOrNull { it != null }) {
			is ProductConfiguration -> {
				editor.initialize(
					productConfiguration = editableItem,
					callbacks = editorCallbacks,
					editorConfiguration = Configuration.editorConfiguration,
					branding = branding
				)
			}
			is Project -> {
				editor.initialize(
					project = editableItem,
					sessionId = mainActivityViewModel.user.value?.sessionId,
					editorCallbacks = editorCallbacks,
					editorConfiguration = editorConfiguration,
					branding = branding
				)
			}
			is CartProject -> {
				editor.initialize(
					cartProject = editableItem,
					editorCallbacks = editorCallbacks,
					editorConfiguration = Configuration.editorConfiguration,
					branding = branding
				)
			}
			else -> {
				throw IllegalArgumentException(
					"You must provide either a ProductConfiguration, a Project, or a CartProject."
				)
			}
		}
	}

	private fun proceedToCart() {
		findNavController().navigate(EditorFragmentDirections.actionNavEditorToNavCart())
	}
}
