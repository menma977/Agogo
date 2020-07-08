package id.co.agogo.config

import android.app.IntentService
import android.content.Intent
import id.co.agogo.BuildConfig
import id.co.agogo.controller.WebController
import id.co.agogo.model.User
import org.json.JSONObject
import java.util.*

/**
 * class BackgroundServiceUserPlay
 * @property response JSONObject
 * @property user User
 * @property isStopService Boolean
 */
class BackgroundServiceUserPlay : IntentService("BackgroundServiceUserPlay") {
  private lateinit var response: JSONObject
  private lateinit var user: User
  private var isStopService: Boolean = false

  /**
   * override fun onHandleIntent
   * @param intent Intent
   */
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
      while (true) {
        val delta = System.currentTimeMillis() - time
        if (delta >= 30000) {
          time = System.currentTimeMillis()

          if (isStopService) {
            break
          } else {
            response = WebController(body).execute().get()
            try {
              if (response["code"] == 200) {
                if (response.getJSONObject("data")["versiapk"] == BuildConfig.VERSION_CODE.toString()) {
                  if (user.getString("key").isEmpty()) {
                    trigger.wait(60000)
                  } else {
                    user.setString("limitDeposit", response.getJSONObject("data")["maxdepo"].toString())
                    user.setBoolean("ifPlay", response.getJSONObject("data")["adamain"].toString().toBoolean())
                  }
                } else {
                  trigger.wait(60000)
                }
              } else {
                trigger.wait(60000)
              }
            }catch (e: Exception) {
              trigger.wait(60000)
            }
          }
        }
      }
    }
  }

  /**
   * @override function onDestroy
   * to stop update Balance
   */
  override fun onDestroy() {
    isStopService = true
    super.onDestroy()
  }
}