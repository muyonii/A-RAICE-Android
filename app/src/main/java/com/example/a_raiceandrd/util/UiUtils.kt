package com.example.a_raiceandrd.util

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment

fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    imm?.hideSoftInputFromWindow(windowToken, 0)
}

fun Fragment.hideKeyboard() {
    view?.hideKeyboard()
}

fun Activity.hideKeyboard() {
    currentFocus?.hideKeyboard()
}
