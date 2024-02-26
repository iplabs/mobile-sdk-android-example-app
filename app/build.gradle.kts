import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
	id(libs.plugins.android.application.get().pluginId)
	id(libs.plugins.kotlin.android.get().pluginId)
	id(libs.plugins.kotlin.kapt.get().pluginId)
	alias(libs.plugins.kotlin.serialization)
	id(libs.plugins.androidx.navigation.safeargs.get().pluginId)
}

enum class ReleaseChannel(val channelName: String) {
	RELEASE("release"),
	BETA("beta"),
	ALPHA("alpha")
}

/**
 * Semantic version definition
 *
 * @property[scheme] Integer between 0 and 21 that represents the semantic version definition’s
 * 	scheme; this only gets raised when the assembly mechanisms for versionCode or versionName
 * 	change.
 * @property[major] Integer between 0 and 99 that represents the major part of the semantic version.
 * @property[minor] Integer between 0 and 99 that represents the minor part of the semantic version;
 * 	must be greater than 0 if major part is 0.
 * @property[patch] Integer between 0 and 99 that represents the patch part of the semantic version.
 * @property[channel] [ReleaseChannel] part of the semantic version; must be [ReleaseChannel.ALPHA]
 * 	or [ReleaseChannel.BETA] if major version is 0.
 * @property[iteration] Integer between 0 and 48 that represents the iteration part of the semantic
 * 	version in case of alpha / beta releases; must remain 0 for release versions.
 */
data class SemanticVersion(
	val scheme: Int,
	val major: Int,
	val minor: Int,
	val patch: Int,
	val channel: ReleaseChannel,
	val iteration: Int
) {
	init {
		require(scheme in 0..21) { "Semantic version scheme must be between 0 and 21." }
		require(major in 0..99) { "Semantic version major must be between 0 and 99." }
		require(minor in 0..99) { "Semantic version minor must be between 0 and 99." }
		require(patch in 0..99) { "Semantic version patch must be between 0 and 99." }
		require(iteration in 0..48) {
			"Semantic version iteration must be between 0 and 48."
		}
		require(
			(major == 0 && minor > 0)
				|| major > 0
		) {
			"Semantic version minor must be greater than 0 if major is 0."
		}
		require(
			(channel == ReleaseChannel.RELEASE && iteration == 0)
				|| channel != ReleaseChannel.RELEASE
		) {
			"Semantic version iteration must be 0 if channel is set to release."
		}
		require(
			(major == 0 && channel != ReleaseChannel.RELEASE)
				|| major > 0
		) {
			"Semantic versions 0.x.x must not be in channel release."
		}
	}

	val name: String
		get() = "${version.major}.${version.minor}.${version.patch}${if (version.channel == ReleaseChannel.BETA || version.channel == ReleaseChannel.ALPHA) "-" + version.channel.channelName + "." + version.iteration else ""}"

	val code: Int
		get() = (
			version.scheme * 100000000
				+ version.major * 1000000
				+ version.minor * 10000
				+ version.patch * 100
				+ (
					if (version.channel == ReleaseChannel.ALPHA) version.iteration else (
						if (version.channel == ReleaseChannel.BETA) 50 + version.iteration else 99
					)
				)
		)
}

val version = SemanticVersion(
	scheme = 0,
	major = 1,
	minor = 3,
	patch = 0,
	channel = ReleaseChannel.RELEASE,
	iteration = 0
)

android {
	namespace = "de.iplabs.mobile_sdk_example_app"

	compileSdk = 34

	defaultConfig {
		applicationId = "de.iplabs.mobile_sdk_example_app"

		minSdk = 29
		targetSdk = 34

		versionCode = version.code
		buildConfigField("String", "VERSION_CODE", "\"${version.code}\"")
		versionName = version.name
		buildConfigField("String", "VERSION_NAME", "\"${version.name}\"")

		val secretKeys = listOf(
			"IPLABS_MOBILE_SDK_ADD_USER_INFO_URL",
			"IPLABS_MOBILE_SDK_AMPLITUDE_API_KEY",
			"IPLABS_MOBILE_SDK_EXTERNAL_CART_SERVICE_KEY"
		)

		secretKeys.forEach {
			val value = determineSecretValue(it)
			buildConfigField("String", it, (value ?: "null"))
		}
	}

	buildTypes {
		getByName("release") {
			isShrinkResources = true
			isMinifyEnabled = true

			proguardFiles(
				getDefaultProguardFile("proguard-android-optimize.txt"),
				"proguard-rules.pro"
			)
		}
	}

	buildFeatures {
		dataBinding = true
		viewBinding = true
	}

	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_20
		targetCompatibility = JavaVersion.VERSION_20
	}

	kotlinOptions {
		jvmTarget = "20"
	}
}

dependencies {
	implementation(libs.amplitude.analytics)
	implementation(libs.android.material)
	implementation(libs.androidx.appcompat)
	implementation(libs.androidx.constraintlayout)
	implementation(libs.androidx.core)
	implementation(libs.androidx.lifecycle.livedata)
	implementation(libs.androidx.lifecycle.viewmodel)
	implementation(libs.androidx.navigation.fragment)
	implementation(libs.androidx.navigation.ui)
	implementation(libs.androidx.preference)
	implementation(libs.androidx.webkit)
	implementation(libs.kotlinx.datetime)
	implementation(libs.kotlinx.serialization.json)
	implementation(libs.iplabs.mobile.sdk)
}

fun loadSecretFromFile(key: String): String? {
	return gradleLocalProperties(rootDir).getProperty(key)
}

fun determineSecretValue(key: String): String? {
	return loadSecretFromFile(key = key)
		?: if (System.getenv(key) != null) "\"${System.getenv(key)}\"" else null
}
