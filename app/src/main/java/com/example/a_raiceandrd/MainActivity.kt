package com.example.a_raiceandrd

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.a_raiceandrd.ui.auth.Role
import com.example.a_raiceandrd.ui.auth.FLogin
import androidx.activity.OnBackPressedCallback
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.materialswitch.MaterialSwitch

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    private lateinit var drawer: DrawerLayout

    fun openDrawer() = drawer.openDrawer(GravityCompat.START)

    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val darkSaved = prefs.getBoolean("dark_mode", false)
        AppCompatDelegate.setDefaultNightMode(
            if (darkSaved) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
        super.onCreate(savedInstanceState)
        FLogin.init(this)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        //para clear content ang taas at baba—hindi collided sa clock and gesture pills
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
                )
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        drawer = findViewById(R.id.drawer_layout)

        // the drawer draws edge to edge, so add the system bar heights on top of its own padding
        val panel = findViewById<View>(R.id.drawer)   // use your id if you renamed it
        val padL = panel.paddingLeft
        val padT = panel.paddingTop
        val padR = panel.paddingRight
        val padB = panel.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(panel) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(padL, padT + bars.top, padR, padB + bars.bottom)
            insets
        }

        // back button closes the drawer first
        val backCallback = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() = drawer.closeDrawer(GravityCompat.START)
        }
        onBackPressedDispatcher.addCallback(this, backCallback)
        drawer.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
            override fun onDrawerOpened(drawerView: View) { backCallback.isEnabled = true }
            override fun onDrawerClosed(drawerView: View) { backCallback.isEnabled = false }
        })


        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        navController = navHostFragment.navController

        // drawer items that don't have a screen yet
        val soon = View.OnClickListener {
            Toast.makeText(this, R.string.Drawer_Soon, Toast.LENGTH_SHORT).show()
        }
        findViewById<View>(R.id.drawerFarmerRecords).setOnClickListener(soon)
        findViewById<View>(R.id.drawerResources).setOnClickListener(soon)
        findViewById<View>(R.id.drawerAccountSettings).setOnClickListener(soon)

        findViewById<View>(R.id.drawerLogout).setOnClickListener {
            drawer.closeDrawer(GravityCompat.START, false)
            FLogin.logout()
            navController.navigate(R.id.action_global_loginFragment)
        }

        findViewById<View>(R.id.drawerClose).setOnClickListener {
            drawer.closeDrawer(GravityCompat.START)
        }

        val themeSwitch = findViewById<MaterialSwitch>(R.id.drawerThemeSwitch)
        val themeLabel = findViewById<TextView>(R.id.drawerThemeLabel)

        // set the starting state first, then attach the listener so it doesn't fire on setup
        themeSwitch.isChecked = darkSaved
        themeLabel.setText(if (darkSaved) R.string.Drawer_DarkMode else R.string.Drawer_LightMode)

        themeSwitch.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean("dark_mode", checked).apply()
            AppCompatDelegate.setDefaultNightMode(
                if (checked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        val userNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val userCard = findViewById<MaterialCardView>(R.id.nav_card)
        val adminNav = findViewById<BottomNavigationView>(R.id.bottom_navigation_ad)
        val adminCard = findViewById<MaterialCardView>(R.id.adnav_card)
        val fab = findViewById<FloatingActionButton>(R.id.camera)
        fab.setOnClickListener { navController.navigate(R.id.scan) }

        userNav.setupWithNavController(navController)
        adminNav.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val hideNav = destination.id == R.id.loginFragment ||
                    destination.id == R.id.registerFragment ||
                    destination.id == R.id.profile ||
                    destination.id == R.id.scan ||
                    destination.id == R.id.scanResultsFragment ||
                    destination.id == R.id.encyDetailsFragment

            val isAdmin = FLogin.getCurrentRole() == Role.ADMIN

            animateView(userCard, !hideNav && !isAdmin)
            animateView(fab, !hideNav && !isAdmin)
            animateView(adminCard, !hideNav && isAdmin)

            val drawerAllowed = !hideNav && destination.id != R.id.encyDetailsFragment
            drawer.closeDrawer(GravityCompat.START, false)
            drawer.setDrawerLockMode(
                if (drawerAllowed) DrawerLayout.LOCK_MODE_UNLOCKED
                else DrawerLayout.LOCK_MODE_LOCKED_CLOSED
            )
            if (FLogin.getCurrentRole() != null) bindDrawer(isAdmin)
        }
    }

    // fills the drawer's account row from the logged-in user
    private fun bindDrawer(isAdmin: Boolean) {
        val name = FLogin.getCurrentName() ?: getString(R.string.Not_Set)
        findViewById<TextView>(R.id.drawerName).text = name
        findViewById<TextView>(R.id.drawerInitials).text = initialsOf(name)
        findViewById<TextView>(R.id.drawerRole).setText(
            if (isAdmin) R.string.Drawer_RoleAdmin else R.string.Drawer_RoleFarmer
        )

        // Farmer records + Resources are admin-only (farmers already have them on their nav bar)
        val workspace = if (isAdmin) View.VISIBLE else View.GONE
        findViewById<View>(R.id.drawerWorkspaceLabel).visibility = workspace
        findViewById<View>(R.id.drawerFarmerRecords).visibility = workspace
        findViewById<View>(R.id.drawerResources).visibility = workspace

        // farmers open their Profile (details, edit) from the account row; admins just see their name
        val row = findViewById<View>(R.id.drawerProfileRow)
        if (isAdmin) {
            row.setOnClickListener(null)
            row.isClickable = false
        } else {
            row.setOnClickListener {
                drawer.closeDrawer(GravityCompat.START)
                navController.navigate(R.id.profile)
            }
        }
    }

    // "Alleah Jane" -> "AJ" (same logic as ProfileFragment)
    private fun initialsOf(name: String): String =
        name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
            .take(2).joinToString("") { it.first().uppercase() }
            .ifEmpty { "?" }

    private fun animateView(view: View, show: Boolean) {
        view.animate().cancel()
        if (show) {
            if (view.visibility == View.VISIBLE && view.alpha == 1f) return
            view.visibility = View.VISIBLE
            view.alpha = 0f
            view.animate().alpha(1f).setDuration(120).setStartDelay(0).start()
        } else {
            if (view.visibility == View.GONE) return
            view.animate().alpha(0f).setDuration(120).setStartDelay(0)
                .withEndAction {
                    view.visibility = View.GONE
                    view.alpha = 1f
                }.start()
        }
    }
}