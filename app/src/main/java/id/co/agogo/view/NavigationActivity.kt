package id.co.agogo.view

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.widget.TextView
import id.co.agogo.MainActivity
import id.co.agogo.R
import id.co.agogo.config.Loading
import id.co.agogo.model.Config
import id.co.agogo.model.User
import id.co.agogo.view.fragment.HomeFragment
import id.co.agogo.view.fragment.WithdrawFragment
import org.json.JSONObject

class NavigationActivity : AppCompatActivity() {
  private lateinit var username: TextView
  private lateinit var balance: TextView
  private lateinit var toolbar: Toolbar
  private lateinit var drawerLayout: DrawerLayout
  private lateinit var navigationView: NavigationView
  private lateinit var user: User
  private lateinit var config: Config
  private lateinit var loading: Loading
  private lateinit var response: JSONObject
  private lateinit var goTo: Intent

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_navigation)

    toolbar = findViewById(R.id.toolbar)
    drawerLayout = findViewById(R.id.drawer_layout)
    navigationView = findViewById(R.id.navigationView)

    loading = Loading(this)
    user = User(this)
    config = Config(this)

    val headerView = navigationView.getHeaderView(0)
    username = headerView.findViewById(R.id.textViewUsernameSide)
    balance = headerView.findViewById(R.id.textViewBalanceSide)


    toolbar.title = ""
    toolbar.setTitleTextColor(getColor(R.color.colorAccent))
    setSupportActionBar(toolbar)
    val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
    drawerLayout.addDrawerListener(toggle)
    toggle.syncState()

    if (savedInstanceState == null) {
      supportFragmentManager.beginTransaction().replace(R.id.frameContainer, HomeFragment()).commit()
      navigationView.setCheckedItem(R.id.nav_home)
      toolbar.title = navigationView.menu.findItem(R.id.nav_home).title
    }

    navigationView.menu.findItem(R.id.nav_withdraw).isVisible = false

    onNavigationItemSelected()
  }

  override fun onBackPressed() {
    if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
      drawerLayout.closeDrawer(GravityCompat.START)
    } else {
      super.onBackPressed()
    }
  }

  private fun onNavigationItemSelected() {
    navigationView.setNavigationItemSelectedListener {
      val itemResponse = when (it.itemId) {
        R.id.nav_home -> {
          supportFragmentManager.beginTransaction().replace(R.id.frameContainer, HomeFragment()).commit()
          navigationView.setCheckedItem(R.id.nav_home)
          toolbar.title = it.title
          true
        }
        R.id.nav_withdraw -> {
          supportFragmentManager.beginTransaction().replace(R.id.frameContainer, WithdrawFragment()).commit()
          navigationView.setCheckedItem(R.id.nav_withdraw)
          toolbar.title = it.title
          true
        }
        R.id.nav_fibonacci -> {
          supportFragmentManager.beginTransaction().replace(R.id.frameContainer, WithdrawFragment()).commit()
          navigationView.setCheckedItem(R.id.nav_fibonacci)
          toolbar.title = it.title
          true
        }
        R.id.nav_marti_angel -> {
          supportFragmentManager.beginTransaction().replace(R.id.frameContainer, WithdrawFragment()).commit()
          navigationView.setCheckedItem(R.id.nav_marti_angel)
          toolbar.title = it.title
          true
        }
        R.id.nav_logout -> {
          user.clear()
          config.clear()
          goTo = Intent(this, MainActivity::class.java)
          startActivity(goTo)
          finishAffinity()
          true
        }
        else -> false
      }

      drawerLayout.closeDrawer(GravityCompat.START)

      return@setNavigationItemSelectedListener itemResponse
    }
  }
}
