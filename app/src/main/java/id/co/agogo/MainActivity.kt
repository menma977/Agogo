package id.co.agogo

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import id.co.agogo.config.BitCoinFormat
import id.co.agogo.config.MD5
import id.co.agogo.controller.WebController
import id.co.agogo.model.Config
import id.co.agogo.model.User
import id.co.agogo.view.LoginActivity
import id.co.agogo.view.NavigationActivity
import org.json.JSONObject
import java.lang.Exception
import java.util.*
import kotlin.concurrent.schedule

/**
 * class MainActivity
 * @property goTo Intent
 * @property user User
 * @property config Config
 * @property response JSONObject
 * @property bitCoinFormat BitCoinFormat
 */
class MainActivity : AppCompatActivity() {
  private lateinit var goTo: Intent
  private lateinit var user: User
  private lateinit var config: Config
  private lateinit var response: JSONObject
  private lateinit var bitCoinFormat: BitCoinFormat

  /**
   * override fun onCreate
   * @param savedInstanceState Bundle?
   */
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    user = User(this)
    config = Config(this)
    bitCoinFormat = BitCoinFormat()
  }

  /**
   * override fun onStart
   */
  override fun onStart() {
    super.onStart()
    Timer().schedule(100) {
      val body = HashMap<String, String>()
      body["a"] = "VersiTrade"
      body["usertrade"] = user.getString("username")
      body["passwordtrade"] = user.getString("password")
      body["ref"] = MD5().convert(user.getString("username") + user.getString("password") + "versi" + "b0d0nk111179")
      response = WebController(body).execute().get()
      try {
        if (response["code"] == 200) {
          if (response.getJSONObject("data")["versiapk"] == BuildConfig.VERSION_CODE.toString()) {
            if (user.getString("key").isEmpty()) {
              isNotLogin()
            } else {
              isLogin(response)
            }
          } else {
            isLoginWrongVersion(response.getJSONObject("data")["versiapk"].toString())
          }
        } else {
          isLoginFailed()
        }
      }catch (e: Exception) {
        isLoginFailed()
      }
    }
  }

  /**
   * private fun isLogin
   * @param data JSONObject
   */
  private fun isLogin(data: JSONObject) {
    user.setString("wallet", data.getJSONObject("data")["walletdepo"].toString())
    user.setString("limitDeposit", data.getJSONObject("data")["maxdepo"].toString())
    user.setBoolean("ifPlay", response.getJSONObject("data")["adamain"].toString().toBoolean())
    goTo = Intent(applicationContext, NavigationActivity::class.java)
    runOnUiThread {
      startActivity(goTo)
      this.finish()
    }
  }

  /**
   * private fun isNotLogin
   */
  private fun isNotLogin() {
    user.clear()
    config.clear()
    goTo = Intent(applicationContext, LoginActivity::class.java)
    goTo.putExtra("lock", false)
    goTo.putExtra("newAPK", false)
    goTo.putExtra("version", "Build Version ${BuildConfig.VERSION_NAME}")
    goTo.putExtra("massage", "anda up to date")
    runOnUiThread {
      startActivity(goTo)
      this.finish()
    }
  }

  /**
   * private fun isLoginFailed
   */
  private fun isLoginFailed() {
    user.clear()
    config.clear()
    goTo = Intent(applicationContext, LoginActivity::class.java)
    goTo.putExtra("lock", true)
    goTo.putExtra("newAPK", false)
    goTo.putExtra("version", "Build Version ${BuildConfig.VERSION_NAME}")
    goTo.putExtra("massage", "gagal memuat data. silakan tutup aplikasi dan buka lagi")
    runOnUiThread {
      startActivity(goTo)
      this.finish()
    }
  }

  /**
   * private fun isLoginWrongVersion
   * @param newVersion String
   */
  private fun isLoginWrongVersion(newVersion: String) {
    user.clear()
    config.clear()
    goTo = Intent(applicationContext, LoginActivity::class.java)
    goTo.putExtra("lock", true)
    goTo.putExtra("newAPK", true)
    goTo.putExtra("version", "Version ${BuildConfig.VERSION_NAME} New Version $newVersion")
    goTo.putExtra("massage", "ada aplikasi baru silakan perbarui")
    runOnUiThread {
      startActivity(goTo)
      this.finish()
    }
  }
}
