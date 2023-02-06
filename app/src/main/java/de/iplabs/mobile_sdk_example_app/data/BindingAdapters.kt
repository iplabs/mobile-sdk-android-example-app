package de.iplabs.mobile_sdk_example_app.data

import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.databinding.BindingAdapter

@BindingAdapter("src")
fun setImageDrawable(view: ImageView, drawable: Drawable) {
	view.setImageDrawable(drawable)
}
