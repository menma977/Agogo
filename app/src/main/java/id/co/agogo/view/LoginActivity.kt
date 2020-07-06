package id.co.agogo.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.KeyEvent
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import id.co.agogo.R
import id.co.agogo.config.Loading
import id.co.agogo.config.MD5
import id.co.agogo.controller.DogeController
import id.co.agogo.controller.WebController
import id.co.agogo.model.Config
import id.co.agogo.model.User
import org.json.JSONObject
import java.lang.Exception
import java.util.*
import kotlin.concurrent.schedule

/**
 * class LoginActivity
 * @property version TextView
 * @property massage TextView
 * @property username EditText
 * @property password EditText
 * @property login Button
 * @property getAPK Button
 * @property response JSONObject
 * @property goTo Intent
 * @property loading Loading
 * @property user User
 * @property config Config
 */
class LoginActivity : AppCompatActivity() {
  private lateinit var version: TextView
  private lateinit var massage: TextView
  private lateinit var username: EditText
  private lateinit var password: EditText
  private lateinit var login: Button
  private lateinit var getAPK: Button
  private lateinit var response: JSONObject
  private lateinit var goTo: Intent
  private lateinit var loading: Loading
  private lateinit var user: User
  private lateinit var config: Config

  /**
   * override fun onCreate
   * @param savedInstanceState Bundle?
   */
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_login)

    version = findViewById(R.id.textViewVersion)
    massage = findViewById(R.id.textViewMassage)
    username = findViewById(R.id.editTextUsername)
    password = findViewById(R.id.editTextPassword)
    login = findViewById(R.id.buttonLogin)
    getAPK = findViewById(R.id.buttonWebDownloadAPK)

    username.setText("agogo2")
    password.setText("Qwerty123321")

    loading = Loading(this)
    user = User(this)
    config = Config(this)

    config.setBoolean("chart", true)

    doRequestPermission()

    loading.openDialog()

    if (intent.getSerializableExtra("lock").toString().toBoolean()) {
      login.visibility = Button.GONE
      username.isEnabled = false
      password.isEnabled = false
    } else {
      login.visibility = Button.VISIBLE
      username.isEnabled = true
      password.isEnabled = true
    }

    if (intent.getSerializableExtra("newAPK").toString().toBoolean()) {
      getAPK.visibility = Button.VISIBLE
    } else {
      getAPK.visibility = Button.GONE
    }

    version.text = intent.getSerializableExtra("version").toString()
    massage.text = intent.getSerializableExtra("massage").toString()

    password.setOnKeyListener { _, keyCode, event ->
      if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
        loginWeb()
        return@setOnKeyListener true
      }
      return@setOnKeyListener false
    }

    login.setOnClickListener {
      loginWeb()
    }

    getAPK.setOnClickListener {
      user.clear()
      config.clear()
      goTo = Intent(Intent.ACTION_VIEW, Uri.parse("https://agogo.co.id/download"))
      startActivity(goTo)
      finish()
    }

    loading.closeDialog()
  }

  private fun loginWeb() {
    loading.openDialog()
    when {
      username.text.isEmpty() -> {
        Toast.makeText(this, "username pengguna Anda tidak boleh kosong", Toast.LENGTH_SHORT).show()
        loading.closeDialog()
      }
      password.text.isEmpty() -> {
        Toast.makeText(this, "Kata sandi Anda tidak boleh kosong", Toast.LENGTH_SHORT).show()
        loading.closeDialog()
      }
      else -> {
        val body = HashMap<String, String>()
        body["a"] = "LoginSession"
        body["username"] = username.text.toString()
        body["password"] = password.text.toString()
        body["ref"] = MD5().convert(username.text.toString() + password.text.toString() + "b0d0nk111179")
        Timer().schedule(100) {
          response = WebController(body).execute().get()
          if (response["code"] == 200) {
            user.setString("usernameWeb", body["username"].toString())
            user.setBoolean("ifPlay", response.getJSONObject("data")["adamain"].toString().toBoolean())
            loginDoge(response)
          } else {
            runOnUiThread {
              try {
                Toast.makeText(applicationContext, response.getJSONObject("data")["Pesan"].toString(), Toast.LENGTH_SHORT).show()
              } catch (e: Exception) {
                Toast.makeText(applicationContext, response["data"].toString(), Toast.LENGTH_SHORT).show()
              }
              loading.closeDialog()
            }
          }
        }
      }
    }
  }

  /**
   * private fun loginDoge
   * @param data JSONObject
   */
  private fun loginDoge(data: JSONObject) {
    val body = HashMap<String, String>()
    body["a"] = "Login"
    body["key"] = "56f1816842b340a6bc07246801552702"
    body["username"] = data.getJSONObject("data")["userdoge"].toString()
    body["password"] = data.getJSONObject("data")["passdoge"].toString()
    body["Totp"] = "''"
    Timer().schedule(100) {
      response = DogeController(body).execute().get()
      if (response["code"] == 200) {
        user.setString("wallet", data.getJSONObject("data")["walletdepo"].toString())
        user.setString("walletWithdraw", data.getJSONObject("data")["walletwdall"].toString())
        user.setString("limitDeposit", data.getJSONObject("data")["maxdepo"].toString())
        user.setString("username", data.getJSONObject("data")["userdoge"].toString())
        user.setString("password", data.getJSONObject("data")["passdoge"].toString())
        user.setString("key", response.getJSONObject("data")["SessionCookie"].toString())
        goTo = Intent(applicationContext, NavigationActivity::class.java)
        runOnUiThread {
          startActivity(goTo)
          finish()
          loading.closeDialog()
        }
      } else {
        runOnUiThread {
          Toast.makeText(applicationContext, response["data"].toString(), Toast.LENGTH_SHORT).show()
          loading.closeDialog()
        }
      }
    }
  }

  private fun doRequestPermission() {
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.WAKE_LOCK) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
      requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WAKE_LOCK, Manifest.permission.INTERNET), 100)
    }
  }
}
