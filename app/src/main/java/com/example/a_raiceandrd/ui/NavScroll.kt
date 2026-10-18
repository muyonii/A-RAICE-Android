package com.example.a_raiceandrd.ui

import android.view.View
import androidx.core.widget.NestedScrollView
import com.example.a_raiceandrd.R

// the camera button rests 12dp up (android:translationY="-12dp" in activity_main.xml)
private const val CAMERA_REST_DP = -12f
private const val HIDE_DISTANCE_DP = 120f

// every bottom piece that slides away, paired with its resting translationY
private fun View.navParts(): List<Pair<View, Float>> {
    val root = rootView
    val d = resources.displayMetrics.density
    return listOfNotNull(
        root.findViewById<View>(R.id.adnav_card)?.let { it to 0f },
        root.findViewById<View>(R.id.nav_card)?.let { it to 0f },
        root.findViewById<View>(R.id.camera)?.let { it to CAMERA_REST_DP * d }
    )
}

fun NestedScrollView.hideNavOnScroll(topBar: View? = null) {
    setOnScrollChangeListener { v, _, scrollY, _, oldScrollY ->
        val dy = scrollY - oldScrollY
        val d = v.resources.displayMetrics.density

        if (dy > 8) {            // scrolling down: hide everything
            v.navParts().forEach { (view, rest) ->
                view.animate().translationY(rest + HIDE_DISTANCE_DP * d).setDuration(200).start()
            }
            topBar?.let { it.animate().translationY(-it.height * 1.5f).setDuration(200).start() }
        } else if (dy < -8 || scrollY == 0) {   // scrolling up: bring it all back
            v.navParts().forEach { (view, rest) ->
                view.animate().translationY(rest).setDuration(200).start()
            }
            topBar?.animate()?.translationY(0f)?.setDuration(200)?.start()
        }
    }
}

// put the nav back where it belongs (call when leaving a screen that hides it)
fun View.showNav() {
    navParts().forEach { (view, rest) -> view.translationY = rest }
}

fun View.showAdminNav() = showNav()