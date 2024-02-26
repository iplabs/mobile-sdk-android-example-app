package de.iplabs.mobile_sdk_example_app.fragments

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.ValueCallback
import android.webkit.WebChromeClient.FileChooserParams
import android.webkit.WebChromeClient.FileChooserParams.MODE_OPEN
import android.webkit.WebChromeClient.FileChooserParams.MODE_OPEN_MULTIPLE
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.IdRes
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.iplabs.mobile_sdk.OperationResult.AuthenticationError
import de.iplabs.mobile_sdk.OperationResult.CartProjectEditingError
import de.iplabs.mobile_sdk.analytics.AnalyticsEvent
import de.iplabs.mobile_sdk.editor.Branding
import de.iplabs.mobile_sdk.editor.EditorEvents
import de.iplabs.mobile_sdk.project.CartProject
import de.iplabs.mobile_sdk_example_app.MainActivity
import de.iplabs.mobile_sdk_example_app.R
import de.iplabs.mobile_sdk_example_app.configuration.Configuration
import de.iplabs.mobile_sdk_example_app.data.cart.Cart
import de.iplabs.mobile_sdk_example_app.data.cart.CartDao
import de.iplabs.mobile_sdk_example_app.data.cart.CartItem
import de.iplabs.mobile_sdk_example_app.data.cart.PersistedCartDao
import de.iplabs.mobile_sdk_example_app.data.toCssRgbaValue
import de.iplabs.mobile_sdk_example_app.data.user.UserDao
import de.iplabs.mobile_sdk_example_app.ui.screens.EditorScreen
import de.iplabs.mobile_sdk_example_app.ui.theme.LightColorScheme
import de.iplabs.mobile_sdk_example_app.ui.theme.MobileSdkExampleAppTheme
import de.iplabs.mobile_sdk_example_app.viewmodels.EditorViewModel
import de.iplabs.mobile_sdk_example_app.viewmodels.EditorViewModelFactory
import de.iplabs.mobile_sdk_example_app.viewmodels.MainActivityViewModel
import de.iplabs.mobile_sdk_example_app.viewmodels.MainActivityViewModelFactory
import kotlinx.coroutines.launch
import java.io.File

class EditorFragment : Fragment() {
	private val navigationArguments: EditorFragmentArgs by navArgs()
	private lateinit var backNavigationCallback: OnBackPressedCallback
	private lateinit var parentActivity: MainActivity
	private lateinit var userDao: UserDao
	private lateinit var cartDao: CartDao
	private lateinit var persistedCartDao: PersistedCartDao
	private lateinit var singleFileChooser: ActivityResultLauncher<Array<String>>
	private lateinit var multiFileChooser: ActivityResultLauncher<Array<String>>

	private val mainActivityViewModel: MainActivityViewModel by activityViewModels {
		MainActivityViewModelFactory(
			userDao = userDao,
			cartDao = cartDao,
			persistedCartDao = persistedCartDao
		)
	}

	private val viewModel: EditorViewModel by viewModels {
		EditorViewModelFactory(sessionId = mainActivityViewModel.user.value?.sessionId)
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		parentActivity = activity as MainActivity

		backNavigationCallback = requireActivity().onBackPressedDispatcher.addCallback(this) {
			isEnabled = false
			initiateTerminationRequest(targetDestinationId = -1)
		}

		singleFileChooser = registerForActivityResult(ActivityResultContracts.OpenDocument()) {
			viewModel.filePathsCallback?.onReceiveValue(
				it?.let { arrayOf(it) } ?: run { arrayOf() }
			)
			viewModel.filePathsCallback = null
		}

		multiFileChooser = registerForActivityResult(
			ActivityResultContracts.OpenMultipleDocuments()
		) {
			viewModel.filePathsCallback?.onReceiveValue(it.toTypedArray())
			viewModel.filePathsCallback = null
		}
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

		val editorEvents = object : EditorEvents {
			override fun onRequestFileChooser(
				params: FileChooserParams
			): ActivityResultLauncher<Array<String>> {
				return when (params.mode) {
					MODE_OPEN -> singleFileChooser

					MODE_OPEN_MULTIPLE -> multiFileChooser

					else -> throw IllegalArgumentException("Invalid file picker mode requested.")
				}
			}

			override fun onFileChoosing(callback: ValueCallback<Array<Uri>>?) {
				viewModel.filePathsCallback = callback
			}

			override fun onRequestAuthentication() {
				mainActivityViewModel.user.value.let {
					if (it != null) {
						viewModel.authenticate(sessionId = it.sessionId)
					} else {
						parentActivity.showLoginDialog(
							viewModel::authenticate,
							viewModel::cancelAuthentication
						)
					}
				}
			}

			override fun onAuthenticationFailed(error: AuthenticationError) {
				Log.d(
					"IplabsMobileSdkExampleApp",
					"Authentication failed: ${error.javaClass.simpleName}"
				)

				parentActivity.showLoginFailedDialog()
			}

			override fun onConsumeAuthentication() {
				viewModel.consumeAuthentication()
			}

			override fun onEditCartProjectFailed(error: CartProjectEditingError) {
				Log.d(
					"IplabsMobileSdkExampleApp",
					"Edit cart project failed: ${error.javaClass.simpleName}"
				)

				viewLifecycleOwner.lifecycleScope.launch {
					MaterialAlertDialogBuilder(requireContext())
						.setTitle(resources.getString(R.string.editing_cart_project_failed_title))
						.setMessage(resources.getString(R.string.editing_cart_project_failed))
						.setPositiveButton(resources.getString(R.string.ok), null)
						.show()

					proceedToCart()
				}
			}

			override fun onLoadProjectTapped() {
				navigateToProjectsScreen()
			}

			override fun onTransferCartProject(project: CartProject) {
				mainActivityViewModel.putItemIntoCart(item = CartItem(project))

				requireActivity().runOnUiThread {
					proceedToCart()
				}
			}

			override fun onHandleTerminationRequest(
				canTerminateSafely: Boolean,
				correlationId: Int
			) {
				viewModel.consumeTerminationRequestResult()

				if (canTerminateSafely) {
					correlationId.let {
						requireActivity().runOnUiThread {
							when (it) {
								-1 -> {
									parentActivity.onBackPressedDispatcher.onBackPressed()
								}

								R.id.nav_projects -> {
									findNavController().navigate(
										directions = EditorFragmentDirections.actionNavEditorToNavProjects()
									)
								}

								else -> {
									findNavController().navigate(resId = it)
								}
							}
						}
					}
				} else {
					backNavigationCallback.isEnabled = true
				}
			}

			override fun onReceiveAnalyticsEvent(event: AnalyticsEvent) {
				parentActivity.userTracking.processEvent(event = event)
			}
		}

		return ComposeView(requireContext()).apply {
			setContent {
				MobileSdkExampleAppTheme {
					EditorScreen(
						editorState = viewModel.editorState,
						editorEvents = editorEvents,
						editorProject = navigationArguments.project,
						filePathsCallback = viewModel.filePathsCallback,
						editorConfiguration = Configuration.editorConfiguration,
						branding = generateBranding(),
					)
				}
			}
		}
	}

	fun initiateTerminationRequest(@IdRes targetDestinationId: Int) {
		viewModel.requestTermination(destinationId = targetDestinationId)
	}

	private fun generateBranding(): Branding {
		return Branding(
			primaryColor = LightColorScheme.primary.toCssRgbaValue(),
			secondaryColor = LightColorScheme.secondary.toCssRgbaValue(),
			fontColorDark = LightColorScheme.onSurface.toCssRgbaValue()
		)
	}

	private fun navigateToProjectsScreen() {
		initiateTerminationRequest(targetDestinationId = R.id.nav_projects)
	}

	private fun proceedToCart() {
		findNavController().navigate(
			directions = EditorFragmentDirections.actionNavEditorToNavCart()
		)
	}
}
