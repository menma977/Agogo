package id.co.agogo.config

import android.app.IntentService
import android.content.Intent
import id.co.agogo.BuildConfig
import id.co.agogo.controller.WebController
import id.co.agogo.model.User
import org.json.JSONObject
import java.util.*

class BackgroundServiceUserPlay : IntentService("BackgroundServiceUserPlay") {
  private lateinit var response: JSONObject
  private lateinit var user: User

  private var startBackgroundService: Boolean = true

  override fun onHandleIntent(intent: Intent?) {
    user = User(this)

    val body = HashMap<String, String>()
    body["a"] = "VersiTrade"
    body["usertrade"] = user.getString("username")
    body["passwordtrade"] = user.getString("password")
    body["ref"] = MD5().convert(user.getString("username") + user.getString("password") + "versi" + "b0d0nk111179")

    var time = System.currentTimeMillis()
    val trigger = Object()

    synchronized(trigger) {
      startBackgroundService = true
      while (true) {
        val delta = System.currentTimeMillis() - time
        if (delta >= 10000) {
          time = System.currentTimeMillis()
          response = WebController(body).execute().get()
          if (startBackgroundService) {
            if (response["code"] == 200) {
              try {
                if (response.getJSONObject("data")["versiapk"] == BuildConfig.VERSION_CODE.toString()) {
                  if (user.getString("key").isNotEmpty()) {
                    user.setBoolean("ifPlay", response.getJSONObject("data")["adamain"].toString().toBoolean())
                  }
                }
              } catch (e: Exception) {
                trigger.wait(60000)
              }
            } else {
              trigger.wait(60000)
            }
          } else {
            break
          }
        }
      }
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    startBackgroundService = false
  }
}