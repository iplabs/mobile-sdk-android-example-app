package de.iplabs.mobile_sdk_example_app.data

import java.io.File

object FileCache {
	private var directory: File? = null
		get() = field ?: error("Cache has not been initialized yet.")
		set(directory) {
			check(field == null) { "Cache has already been initialized." }

			field = directory
		}

	fun setup(directory: File) {
		FileCache.directory = directory
	}

	fun isFileCached(name: String): Boolean = File(assembleFullFilePath(name)).exists()

	fun saveTextFile(name: String, content: String, overwrite: Boolean = false) {
		val file = File(assembleFullFilePath(name))

		check(!file.exists() or overwrite) {
			"File “$name” already exists in cache and overwriting it was not allowed."
		}

		file.writeText(content)
	}

	fun loadTextFile(name: String): String {
		val file = File(assembleFullFilePath(name))

		return file.readText()
	}

	fun deleteTextFile(name: String) {
		val file = File(assembleFullFilePath(name))

		file.delete()
	}

	private fun assembleFullFilePath(name: String): String {
		return "$directory/$name"
	}
}
