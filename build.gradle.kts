buildscript {
	repositories {
		google()
		mavenCentral()
	}

	dependencies {
		classpath(appLibs.android.gradle)
		classpath(appLibs.androidx.navigation.safeargs)
		classpath(appLibs.kotlin.gradle)
	}
}

tasks.register("clean", Delete::class) {
	delete(rootProject.buildDir)
}
