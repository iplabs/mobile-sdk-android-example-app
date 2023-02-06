package de.iplabs.mobile_sdk_example_app.data.cart

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class PersistedCartDao(private val persistedCartFile: File) {
	fun saveItems(items: List<CartItem>) {
		val jsonString = Json.encodeToString(items)

		persistedCartFile.writeText(jsonString)
	}

	fun loadItems(): List<CartItem> {
		return if (persistedCartFile.exists() && persistedCartFile.isFile) {
			val jsonString = persistedCartFile.readText(Charsets.UTF_8)

			Json.decodeFromString(jsonString)
		} else {
			listOf()
		}
	}
}
