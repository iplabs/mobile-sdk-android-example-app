package de.iplabs.mobile_sdk_example_app.ui

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT

fun View.focusAndShowKeyboard(retry: Boolean = true) {
	fun View.showTheKeyboardNow() {
		if (isFocused) {
			post {
				val inputMethodManager =
					context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
				val isInputMethodManagerSetup = inputMethodManager.showSoftInput(
					this,
					SHOW_IMPLICIT
				)

				if (!isInputMethodManagerSetup && retry) {
					this.focusAndShowKeyboard(retry = false)
				}
			}
		}
	}

	requestFocus()

	if (hasWindowFocus()) {
		showTheKeyboardNow()
	} else {
		viewTreeObserver.addOnWindowFocusChangeListener(
			object : ViewTreeObserver.OnWindowFocusChangeListener {
				override fun onWindowFocusChanged(hasFocus: Boolean) {
					if (hasFocus) {
						this@focusAndShowKeyboard.showTheKeyboardNow()

						viewTreeObserver.removeOnWindowFocusChangeListener(this)
					}
				}
			}
		)
	}
}

fun View.hideKeyboard() {
	val inputMethodManager =
		context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
	inputMethodManager.hideSoftInputFromWindow(this.windowToken, 0)
}
