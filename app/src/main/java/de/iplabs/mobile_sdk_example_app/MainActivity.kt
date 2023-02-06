package de.iplabs.mobile_sdk_example_app

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import de.iplabs.mobile_sdk.IplabsMobileSdk
import de.iplabs.mobile_sdk.OperationResult.InitializationResult
import de.iplabs.mobile_sdk.UserTrackingPermission
import de.iplabs.mobile_sdk.analytics.AnalyticsEvent
import de.iplabs.mobile_sdk_example_app.configuration.Configuration
import de.iplabs.mobile_sdk_example_app.configuration.fakeUserInfo
import de.iplabs.mobile_sdk_example_app.configuration.fakeUserPassword
import de.iplabs.mobile_sdk_example_app.configuration.loadFakeProfilePicture
import de.iplabs.mobile_sdk_example_app.data.FileCache
import de.iplabs.mobile_sdk_example_app.data.cart.Cart
import de.iplabs.mobile_sdk_example_app.data.cart.CartDao
import de.iplabs.mobile_sdk_example_app.data.cart.PersistedCartDao
import de.iplabs.mobile_sdk_example_app.data.user.UserDao
import de.iplabs.mobile_sdk_example_app.data.userTracking.AmplitudeAnalytics
import de.iplabs.mobile_sdk_example_app.data.userTracking.UserTracking
import de.iplabs.mobile_sdk_example_app.databinding.ActivityMainBinding
import de.iplabs.mobile_sdk_example_app.databinding.LoginDialogBinding
import de.iplabs.mobile_sdk_example_app.databinding.NavHeaderMainBinding
import de.iplabs.mobile_sdk_example_app.fragments.CartFragment
import de.iplabs.mobile_sdk_example_app.fragments.EditorFragment
import de.iplabs.mobile_sdk_example_app.ui.focusAndShowKeyboard
import de.iplabs.mobile_sdk_example_app.ui.hideKeyboard
import de.iplabs.mobile_sdk_example_app.viewmodels.MainActivityViewModel
import de.iplabs.mobile_sdk_example_app.viewmodels.MainActivityViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import java.io.File
import java.net.URL

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
	private var _binding: ActivityMainBinding? = null
	private val binding get() = _binding!!

	private lateinit var appBarConfiguration: AppBarConfiguration
	private lateinit var navController: NavController
	private lateinit var userDao: UserDao
	private lateinit var cartDao: CartDao
	private lateinit var persistedCartDao: PersistedCartDao

	private val viewModel: MainActivityViewModel by viewModels {
		MainActivityViewModelFactory(
			userDao = userDao,
			cartDao = cartDao,
			persistedCartDao = persistedCartDao
		)
	}

	lateinit var userTracking: UserTracking
		private set

	private var addUserInfoUrl: URL? = null

	var externalCartServiceKey: String? = null
		private set

	@SuppressLint("DiscouragedApi")
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		_binding = ActivityMainBinding.inflate(layoutInflater)

		provideFileCache()

		userTracking = initializeAnalytics(permissionLevel = UserTrackingPermission.ALLOW)

		val launchEvent = AnalyticsEvent(
			name = "client_launched",
			timestamp = Clock.System.now(),
			properties = mapOf(
				"osName" to "Android",
				"osVersion" to android.os.Build.VERSION.RELEASE,
				"appVersion" to BuildConfig.VERSION_NAME,
				"iplabsMobileSdkVersion" to de.iplabs.mobile_sdk.BuildConfig.VERSION_NAME
			)
		)

		userTracking.processEvent(event = launchEvent)

		@Suppress("USELESS_CAST")
		addUserInfoUrl = (BuildConfig.ADD_USER_INFO_URL as String?)?.let { URL(it) }

		@Suppress("USELESS_CAST")
		externalCartServiceKey = BuildConfig.EXTERNAL_CART_SERVICE_KEY as String?

		when (
			IplabsMobileSdk.initialize(
				operatorId = Configuration.operatorId,
				locale = Configuration.portfolioLocale.isoCode,
				baseUrl = Configuration.baseUrl,
				externalCartServiceBaseUrl = Configuration.externalCartServiceBaseUrl,
				localProjectsLocation = filesDir,
				translationsSource = Configuration.translationLocale?.let {
					resources.openRawResource(
						resources.getIdentifier(
							"translations_${it.isoCode.lowercase()}",
							"raw",
							packageName
						)
					)
				},
				userTrackingPermission = userTracking.permissionLevel
			)
		) {
			InitializationResult.Success -> Unit
			InitializationResult.AlreadyInitializedError -> {
				Log.w("MainActivity", "An attempt to reinitialize the ip.labs Mobile SDK occurred.")
			}
			is InitializationResult.UnknownError -> {
				Log.e(
					"MainActivity",
					"An unknown error occurred during initialization of the ip.labs Mobile SDK."
				)
			}
		}

		Log.d("MOBILE_SDK_VERSION", de.iplabs.mobile_sdk.BuildConfig.VERSION_NAME)

		loadFakeProfilePicture(this)

		val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
		userDao = UserDao(sharedPreferences)
		cartDao = CartDao(cart = Cart)
		persistedCartDao = PersistedCartDao(persistedCartFile = File(filesDir, "cart.json"))

		val headerBinding = NavHeaderMainBinding.bind(binding.navView.getHeaderView(0))
		headerBinding.lifecycleOwner = this
		headerBinding.activity = this
		headerBinding.viewModel = viewModel

		setContentView(binding.root)
		setSupportActionBar(binding.appBarMain.toolbar)

		val navHostFragment =
			supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
		navController = navHostFragment.navController

		appBarConfiguration = AppBarConfiguration(
			setOf(R.id.nav_launch, R.id.nav_selection, R.id.nav_editor, R.id.nav_cart),
			binding.drawerLayout
		)

		setupActionBarWithNavController(navController, appBarConfiguration)

		val navView = binding.navView
		navView.setupWithNavController(navController)
		navView.setNavigationItemSelectedListener(this)
	}

	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		menuInflater.inflate(R.menu.action_bar, menu)

		val menuItem = menu.findItem(R.id.action_item_cart)
		val menuItemView = menuItem.actionView

		menuItemView?.let {
			it.setOnClickListener {
				when (val currentFragment = getActiveFragment()) {
					is CartFragment -> {}
					is EditorFragment -> currentFragment.initiateTerminationRequest(
						targetDestinationId = R.id.action_global_nav_cart
					)
					else -> navController.navigate(NavGraphDirections.actionGlobalNavCart())
				}
			}

			val badge = it.findViewById<TextView>(R.id.cart_badge)

			lifecycleScope.launchWhenStarted {
				viewModel.getCartItemCount().collectLatest { itemCount ->
					with(badge) {
						visibility = if (itemCount != 0) View.VISIBLE else View.GONE
						text = if (itemCount < 100) itemCount.toString() else "99+"
					}
				}
			}
		}

		return true
	}

	override fun onSupportNavigateUp(): Boolean {
		return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
	}

	override fun onNavigationItemSelected(item: MenuItem): Boolean {
		closeDrawerMenu()

		val currentFragment = getActiveFragment()

		return if (currentFragment is EditorFragment) {
			currentFragment.initiateTerminationRequest(targetDestinationId = item.itemId)

			false
		} else {
			navController.navigate(item.itemId)

			true
		}
	}

	override fun onDestroy() {
		super.onDestroy()

		_binding = null
	}

	@JvmOverloads
	fun showLoginDialog(
		authenticateCallback: ((String) -> Unit)? = null,
		cancelAuthenticationCallback: (() -> Unit)? = null
	) {
		closeDrawerMenu()

		if (addUserInfoUrl != null) {
			val loginDialogBinding = LoginDialogBinding.inflate(LayoutInflater.from(this))

			lifecycleScope.launch {
				val loginDialog = MaterialAlertDialogBuilder(this@MainActivity)
					.setView(loginDialogBinding.root)
					.setCancelable(false)
					.setTitle(resources.getString(R.string.login))
					.setMessage(
						resources.getString(
							R.string.login_description,
							fakeUserInfo.eMailAddress,
							fakeUserPassword
						)
					)
					.setNegativeButton(resources.getString(R.string.cancel), null)
					.setPositiveButton(resources.getString(R.string.ok), null)
					.show()

				loginDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
					val username = loginDialogBinding.usernameText.text.toString()
					val password = loginDialogBinding.passwordText.text.toString()

					if (username.isNotEmpty() && password.isNotEmpty()) {
						lifecycleScope.launch {
							val user = addUserInfoUrl?.let {
								viewModel.loginUser(
									username = loginDialogBinding.usernameText.text.toString(),
									password = loginDialogBinding.passwordText.text.toString(),
									backendUrl = it
								)
							}

							if (user != null) {
								Snackbar.make(
									binding.navView,
									resources.getString(
										R.string.login_confirmation,
										user.userInfo.firstName,
										user.userInfo.lastName
									),
									Snackbar.LENGTH_SHORT
								).show()

								authenticateCallback?.let { it(user.sessionId) }
							} else {
								cancelAuthenticationCallback?.let { it() }

								showLoginFailedDialog()
							}
						}

						loginDialogBinding.usernameText.hideKeyboard()
						loginDialog.dismiss()
					} else {
						if (username.isEmpty()) {
							loginDialogBinding.username.error = resources.getString(
								R.string.login_username_validation_warning
							)
						}

						if (password.isEmpty()) {
							loginDialogBinding.password.error = resources.getString(
								R.string.login_password_validation_warning
							)
						}
					}
				}

				loginDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener {
					cancelAuthenticationCallback?.let { it() }

					loginDialogBinding.usernameText.hideKeyboard()
					loginDialog.dismiss()
				}
			}

			loginDialogBinding.usernameText.focusAndShowKeyboard()
		} else {
			lifecycleScope.launch {
				val loginUnavailableDialog = MaterialAlertDialogBuilder(this@MainActivity)
					.setTitle(resources.getString(R.string.login_unavailable_title))
					.setMessage(resources.getString(R.string.login_unavailable_message))
					.setNegativeButton(resources.getString(R.string.cancel), null)
					.setPositiveButton(
						resources.getString(R.string.function_unavailable_more),
						null
					)
					.show()

				cancelAuthenticationCallback?.let { it() }

				loginUnavailableDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
					openSdkLandingPage()

					loginUnavailableDialog.dismiss()
				}
			}
		}
	}

	fun showLoginFailedDialog() {
		lifecycleScope.launch {
			MaterialAlertDialogBuilder(this@MainActivity)
				.setTitle(resources.getString(R.string.login_failed))
				.setMessage(resources.getString(R.string.login_failed_description))
				.setPositiveButton(resources.getString(R.string.ok), null)
				.show()
		}
	}

	fun showLogoutDialog() {
		closeDrawerMenu()

		MaterialAlertDialogBuilder(this)
			.setTitle(resources.getString(R.string.logout_prompt_title))
			.setMessage(resources.getString(R.string.logout_prompt))
			.setNegativeButton(resources.getString(R.string.no), null)
			.setPositiveButton(resources.getString(R.string.yes)) { _, _ ->
				logout()

				Snackbar.make(
					binding.navView,
					R.string.logout_confirmation,
					Snackbar.LENGTH_SHORT
				).show()
			}
			.show()
	}

	fun openSdkLandingPage() {
		val browserIntent = Intent(
			Intent.ACTION_VIEW,
			Uri.parse(Configuration.sdkInfoUrl)
		)

		startActivity(browserIntent)
	}

	@Suppress("SameParameterValue")
	private fun initializeAnalytics(permissionLevel: UserTrackingPermission): UserTracking {
		@Suppress("USELESS_CAST")
		val amplitudeKey = BuildConfig.AMPLITUDE_API_KEY as String?

		val amplitudeAnalytics = amplitudeKey?.let {
			AmplitudeAnalytics(apiKey = it, context = applicationContext)
		}

		if (amplitudeAnalytics == null) {
			Log.d(
				"IplabsMobileSdk",
				"User tracking will not be available, because no Amplitude API key was defined."
			)
		}

		return UserTracking(
			analyticsProvider = amplitudeAnalytics,
			permissionLevel = permissionLevel
		)
	}

	private fun provideFileCache() {
		try {
			FileCache.setup(cacheDir)
		} catch (e: IllegalStateException) {
			Log.d("Cache", "Reusing existing cache.")
		}
	}

	private fun closeDrawerMenu() {
		binding.drawerLayout.closeDrawers()
	}

	private fun getActiveFragment(): Fragment? {
		val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)

		return navHostFragment?.childFragmentManager?.fragments?.get(0)
	}

	private fun logout() {
		lifecycleScope.launch {
			IplabsMobileSdk.logout()
		}

		viewModel.logoutUser()
	}
}
