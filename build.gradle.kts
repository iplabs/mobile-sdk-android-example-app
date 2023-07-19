buildscript {
	repositories {
		google()
		mavenCentral()
	}

	dependencies {
		classpath(libs.android.gradle)
		classpath(libs.androidx.navigation.safeargs)
		classpath(libs.kotlin.gradle)
	}
}

tasks.register("clean", Delete::class) {
	delete(rootProject.buildDir)
}
