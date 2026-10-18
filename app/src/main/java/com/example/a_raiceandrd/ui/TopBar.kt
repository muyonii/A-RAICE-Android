package com.example.a_raiceandrd.ui

import android.view.View
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.a_raiceandrd.MainActivity
import com.example.a_raiceandrd.R
import com.example.a_raiceandrd.ui.auth.FLogin
import com.example.a_raiceandrd.ui.auth.Role

// Wires the shared top bar: menu opens the drawer, bell goes to the Alerts tab of the current role.
// Pass the screen's scroll view so the bar slides away together with the bottom nav.
fun Fragment.setupTopBar(root: View, scroll: NestedScrollView? = null) {
    root.findViewById<View>(R.id.menuBtn).setOnClickListener {
        (requireActivity() as MainActivity).openDrawer()
    }
    root.findViewById<View>(R.id.notifBtn).setOnClickListener {
        val dest = if (FLogin.getCurrentRole() == Role.ADMIN) R.id.adnotif else R.id.notif
        val nav = findNavController()
        if (nav.currentDestination?.id != dest) nav.navigate(dest)
    }
    scroll?.hideNavOnScroll(root.findViewById<View>(R.id.topBar))
}