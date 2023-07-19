import java.net.URI

dependencyResolutionManagement {
	@Suppress("UnstableApiUsage")
	repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

	versionCatalogs {
		create("appLibs") {
			from(files("gradle/app-libs.versions.toml"))
		}
	}

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
