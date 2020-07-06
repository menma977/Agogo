package id.co.agogo.view

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.widget.TextView
import android.widget.Toast
import id.co.agogo.MainActivity
import id.co.agogo.R
import id.co.agogo.config.BackgroundServiceBalance
import id.co.agogo.config.BitCoinFormat
import id.co.agogo.config.Loading
import id.co.agogo.config.MD5
import id.co.agogo.controller.DogeController
import id.co.agogo.controller.WebController
import id.co.agogo.model.Config
import id.co.agogo.model.User
import id.co.agogo.view.fragment.HomeFragment
import id.co.agogo.view.fragment.SettingFragment
import id.co.agogo.view.fragment.WithdrawFragment
import org.json.JSONObject
import java.math.BigDecimal
import java.math.MathContext
import java.util.*
import kotlin.collections.HashMap
import kotlin.concurrent.schedule
import id.co.agogo.view.bot.fibonacci.BotActivity as BotFibonacciActivity
import id.co.agogo.view.bot.fibonacci.BotChartAndProgressBarGoneActivity as BotFibonacciChartAndProgressBarGoneActivity
import id.co.agogo.view.bot.fibonacci.BotChartGoneActivity as BotFibonacciChartGoneActivity
import id.co.agogo.view.bot.fibonacci.BotProgressBarGoneActivity as BotFibonacciProgressBarGoneActivity
import id.co.agogo.view.bot.martiAngel.BotActivity as BotMartiAngelActivity
import id.co.agogo.view.bot.martiAngel.BotChartAndProgressBarGoneActivity as BotMartiAngelChartAndProgressBarGoneActivity
import id.co.agogo.view.bot.martiAngel.BotChartGoneActivity as BotMartiAngelChartGoneActivity
import id.co.agogo.view.bot.martiAngel.BotProgressBarGoneActivity as BotMartiAngelProgressBarGoneActivity


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
  private lateinit var balanceValue: BigDecimal
  private lateinit var uniqueCode: String
  private lateinit var intentService: Intent

  private var limitDepositDefault = BigDecimal(0.000000000, MathContext.DECIMAL32).setScale(8, BigDecimal.ROUND_HALF_DOWN)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_navigation)

    toolbar = findViewById(R.id.toolbar)
    drawerLayout = findViewById(R.id.drawer_layout)
    navigationView = findViewById(R.id.navigationView)

    loading = Loading(this)
    user = User(this)
    config = Config(this)

    intentService = Intent(this, BackgroundServiceBalance::class.java)
    intentService.putExtra("key", user.getString("key"))
    startService(intentService)

    val headerView = navigationView.getHeaderView(0)
    username = headerView.findViewById(R.id.textViewUsernameSide)
    balance = headerView.findViewById(R.id.textViewBalanceSide)

    toolbar.title = ""
    setSupportActionBar(toolbar)
    val toggle = ActionBarDrawerToggle(
      this,
      drawerLayout,
      toolbar,
      R.string.navigation_drawer_open,
      R.string.navigation_drawer_close
    )
    drawerLayout.addDrawerListener(toggle)
    toggle.syncState()

    onNavigationItemSelected()

    getBalance(savedInstanceState)
  }

  override fun onStart() {
    super.onStart()
    val intentFilter = IntentFilter()
    intentFilter.addAction("id.co.agogo")
    registerReceiver(broadcastReceiver, intentFilter)
  }

  override fun onDestroy() {
    super.onDestroy()
    unregisterReceiver(broadcastReceiver)
  }

  override fun onBackPressed() {
    if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
      drawerLayout.closeDrawer(GravityCompat.START)
    } else {
      super.onBackPressed()
    }
  }

  private var broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      navigationView.menu.findItem(R.id.nav_withdraw).isVisible = intent.getBooleanExtra("nav_withdraw", false)
      navigationView.menu.findItem(R.id.nav_fibonacci).isVisible = intent.getBooleanExtra("nav_fibonacci", false)
      navigationView.menu.findItem(R.id.nav_marti_angel).isVisible = intent.getBooleanExtra("nav_marti_angel", false)
      balance.text = user.getString("balance")
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
        R.id.nav_setting -> {
          supportFragmentManager.beginTransaction().replace(R.id.frameContainer, SettingFragment()).commit()
          navigationView.setCheckedItem(R.id.nav_setting)
          toolbar.title = it.title
          true
        }
        R.id.nav_fibonacci -> {
          stopService(intentService)
          unregisterReceiver(broadcastReceiver)
          startBotFibonacci()
          true
        }
        R.id.nav_marti_angel -> {
          stopService(intentService)
          unregisterReceiver(broadcastReceiver)
          startBotMartiAngel()
          true
        }
        R.id.nav_logout -> {
          stopService(intentService)
          unregisterReceiver(broadcastReceiver)
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

  private fun getBalance(savedInstanceState: Bundle?) {
    loading.openDialog()
    val body = HashMap<String, String>()
    body["a"] = "GetBalance"
    body["s"] = user.getString("key")
    body["Currency"] = "doge"
    body["Referrals"] = "0"
    body["Stats"] = "0"
    Timer().schedule(100) {
      response = DogeController(body).execute().get()
      if (response["code"] == 200) {
        balanceValue = response.getJSONObject("data")["Balance"].toString().toBigDecimal()
        val balanceLimit = if (user.getString("limitDeposit").isEmpty()) {
          BitCoinFormat().dogeToDecimal(limitDepositDefault)
        } else {
          BitCoinFormat().dogeToDecimal(user.getString("limitDeposit").toBigDecimal())
        }
        if (user.getBoolean("ifPlay")) {
          runOnUiThread {
            navigationView.menu.findItem(R.id.nav_withdraw).isVisible = false
            navigationView.menu.findItem(R.id.nav_fibonacci).isVisible = false
            navigationView.menu.findItem(R.id.nav_marti_angel).isVisible = false
            if (user.getString("fakeBalance").isEmpty()) {
              user.setString("balance", "${BitCoinFormat().decimalToDoge(balanceValue).toPlainString()} DOGE")
            } else {
              user.setString(
                "balance",
                "${BitCoinFormat().decimalToDoge(user.getString("fakeBalance").toBigDecimal()).toPlainString()} DOGE"
              )
            }
          }
        } else if (BitCoinFormat().decimalToDoge(balanceValue) >= BigDecimal(100) && balanceValue <= balanceLimit) {
          runOnUiThread {
            user.setString("fakeBalance", "0")
            navigationView.menu.findItem(R.id.nav_withdraw).isVisible = false
            navigationView.menu.findItem(R.id.nav_fibonacci).isVisible = true
            navigationView.menu.findItem(R.id.nav_marti_angel).isVisible = true
            user.setString("balance", "${BitCoinFormat().decimalToDoge(balanceValue).toPlainString()} DOGE")
          }
        } else if (balanceValue > balanceLimit) {
          runOnUiThread {
            user.setString("fakeBalance", "0")
            navigationView.menu.findItem(R.id.nav_withdraw).isVisible = true
            navigationView.menu.findItem(R.id.nav_fibonacci).isVisible = false
            navigationView.menu.findItem(R.id.nav_marti_angel).isVisible = false
            user.setString("balance", "${BitCoinFormat().decimalToDoge(balanceValue).toPlainString()} DOGE terlalu tinggi")
          }
        } else {
          runOnUiThread {
            user.setString("fakeBalance", "0")
            navigationView.menu.findItem(R.id.nav_withdraw).isVisible =
              BitCoinFormat().decimalToDoge(balanceValue) < BigDecimal(10000) && BitCoinFormat().decimalToDoge(balanceValue) > BigDecimal(0)
            navigationView.menu.findItem(R.id.nav_fibonacci).isVisible = false
            navigationView.menu.findItem(R.id.nav_marti_angel).isVisible = false
            user.setString("balance", "${BitCoinFormat().decimalToDoge(balanceValue).toPlainString()} DOGE terlalu kecil")
          }
        }
        user.setString("balanceMax", "${BitCoinFormat().decimalToDoge(balanceLimit).toPlainString()} DOGE")
      } else {
        runOnUiThread {
          navigationView.menu.findItem(R.id.nav_withdraw).isVisible = false
          navigationView.menu.findItem(R.id.nav_fibonacci).isVisible = false
          navigationView.menu.findItem(R.id.nav_marti_angel).isVisible = false
          user.setString("balance", "ERROR 404")
          user.setString("balanceMax", "${BigDecimal(0)} DOGE")
        }
      }

      runOnUiThread {
        username.text = user.getString("usernameWeb")
        balance.text = user.getString("balance")
        loading.closeDialog()

        if (savedInstanceState == null) {
          supportFragmentManager.beginTransaction().replace(R.id.frameContainer, HomeFragment()).commit()
          navigationView.setCheckedItem(R.id.nav_home)
          toolbar.title = navigationView.menu.findItem(R.id.nav_home).title
        }
      }
    }
  }

  private fun startBotFibonacci() {
    loading.openDialog()
    Timer().schedule(100) {
      response = WebController(bodyBot("StartTrading")).execute().get()
      try {
        if (response["code"] == 200) {
          if (response.getJSONObject("data")["main"] == true) {
            isPlaying(response)
          } else {
            goTo = if (config.getBoolean("chart") && config.getBoolean("progressBar")) {
              Intent(applicationContext, BotFibonacciActivity::class.java)
            } else if (config.getBoolean("chart")) {
              Intent(applicationContext, BotFibonacciProgressBarGoneActivity::class.java)
            } else if (config.getBoolean("progressBar")) {
              Intent(applicationContext, BotFibonacciChartGoneActivity::class.java)
            } else {
              Intent(applicationContext, BotFibonacciChartAndProgressBarGoneActivity::class.java)
            }
            goTo.putExtra("uniqueCode", uniqueCode)
            goTo.putExtra("balance", balanceValue)
            runOnUiThread {
              startActivity(goTo)
              finish()
              loading.closeDialog()
            }
          }
        } else {
          runOnUiThread {
            Toast.makeText(applicationContext, response["data"].toString(), Toast.LENGTH_LONG).show()
            loading.closeDialog()
          }
        }
      } catch (e: Exception) {
        runOnUiThread {
          Toast.makeText(applicationContext, e.message, Toast.LENGTH_LONG).show()
          loading.closeDialog()
        }
      }
    }
  }

  private fun startBotMartiAngel() {
    loading.openDialog()
    Timer().schedule(100) {
      response = WebController(bodyBot("StartTrading2")).execute().get()
      try {
        if (response["code"] == 200) {
          if (response.getJSONObject("data")["main"] == true) {
            isPlaying(response)
          } else {
            goTo = if (config.getBoolean("chart") && config.getBoolean("progressBar")) {
              Intent(applicationContext, BotMartiAngelActivity::class.java)
            } else if (config.getBoolean("chart")) {
              Intent(applicationContext, BotMartiAngelProgressBarGoneActivity::class.java)
            } else if (config.getBoolean("progressBar")) {
              Intent(applicationContext, BotMartiAngelChartGoneActivity::class.java)
            } else {
              Intent(applicationContext, BotMartiAngelChartAndProgressBarGoneActivity::class.java)
            }
            goTo.putExtra("uniqueCode", uniqueCode)
            goTo.putExtra("balance", balanceValue)
            runOnUiThread {
              startActivity(goTo)
              finish()
              loading.closeDialog()
            }
          }
        } else {
          runOnUiThread {
            Toast.makeText(applicationContext, response["data"].toString(), Toast.LENGTH_LONG).show()
            loading.closeDialog()
          }
        }
      } catch (e: Exception) {
        runOnUiThread {
          Toast.makeText(applicationContext, e.message, Toast.LENGTH_LONG).show()
          loading.closeDialog()
        }
      }
    }
  }

  private fun bodyBot(target: String): HashMap<String, String> {
    uniqueCode = UUID.randomUUID().toString()
    val body = HashMap<String, String>()
    body["a"] = target
    body["usertrade"] = user.getString("username")
    body["passwordtrade"] = user.getString("password")
    body["notrx"] = uniqueCode
    body["balanceawal"] = BitCoinFormat().decimalToDoge(balanceValue).toPlainString()
    body["ref"] = MD5().convert(user.getString("username") + user.getString("password") + uniqueCode + "balanceawalb0d0nk111179")
    return body
  }

  private fun isPlaying(data: JSONObject) {
    val oldBalanceData = BigDecimal(data.getJSONObject("data")["saldoawalmain"].toString(), MathContext.DECIMAL32)
    uniqueCode = response.getJSONObject("data")["notrxlama"].toString()
    val profit = balanceValue - BitCoinFormat().decimalToDoge(oldBalanceData)
    goTo = Intent(applicationContext, ResultActivity::class.java)
    if (profit < BigDecimal(0)) {
      goTo.putExtra("status", "CUT LOSS")
      goTo.putExtra("uniqueCode", uniqueCode)
      goTo.putExtra("startBalance", oldBalanceData)
    } else {
      goTo.putExtra("status", "WIN")
      goTo.putExtra("uniqueCode", uniqueCode)
      goTo.putExtra("startBalance", oldBalanceData)
    }
    runOnUiThread {
      startActivity(goTo)
      finish()
      loading.closeDialog()
    }
  }
}
