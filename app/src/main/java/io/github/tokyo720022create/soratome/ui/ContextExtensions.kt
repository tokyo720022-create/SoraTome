package io.github.tokyo720022create.soratome.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

/** The Activity behind this Context, or null if there is none. */
tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
