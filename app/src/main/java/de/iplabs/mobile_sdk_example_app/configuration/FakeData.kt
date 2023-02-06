package de.iplabs.mobile_sdk_example_app.configuration

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import de.iplabs.mobile_sdk.user.Address
import de.iplabs.mobile_sdk.user.UserInfo
import de.iplabs.mobile_sdk_example_app.R
import java.util.*

const val fakeUserPassword = "Secret#123"

private val fakePostalAddress = Address(
	countryIsoCode = "DE",
	salutation = "Mr.",
	firstName = "John",
	lastName = "Doe",
	street = "Main Street 123",
	zipCode = "12345",
	city = "Largetown",
	phoneNumber = "0049123456789"
)

val fakeUserInfo = UserInfo(
	eMailAddress = "j.doe@iplabs.com",
	billingAddress = fakePostalAddress,
	shippingAddress = fakePostalAddress,
	firstName = "John",
	lastName = "Doe"
)

val fakeTestUserInfo = UserInfo(
	eMailAddress = "e2e-tests@iplabs.com",
	billingAddress = fakePostalAddress,
	shippingAddress = fakePostalAddress,
	firstName = "E2E",
	lastName = "Tests"
)

lateinit var fakeProfilePicture: Bitmap

fun loadFakeProfilePicture(context: Context) {
	fakeProfilePicture = BitmapFactory.decodeResource(
		context.resources,
		R.drawable.fake_profile_picture_jdoe
	)
}

fun generateFakeOrderId(): String {
	return UUID.randomUUID().toString()
}
