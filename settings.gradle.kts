import java.net.URI

dependencyResolutionManagement {
	@Suppress("UnstableApiUsage")
	repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

	@Suppress("UnstableApiUsage")
	repositories {
		maven {
			name = "AmazonS3Releases"
			url = URI("https://s3.eu-central-1.amazonaws.com/mobile-sdk/android/releases")
		}

		google()
		mavenCentral()
	}
}

rootProject.name = "Mobile SDK - Example App"
include(":app")
