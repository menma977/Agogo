package id.co.agogo.config

import android.app.IntentService
import android.content.Intent
import android.support.v4.content.LocalBroadcastManager
import id.co.agogo.controller.DogeController
import id.co.agogo.model.User
import org.json.JSONObject
import java.math.BigDecimal
import java.math.MathContext

/**
 * class BackgroundServiceBalance
 * @property response JSONObject
 * @property balanceValue BigDecimal
 * @property user User
 * @property startBackgroundService Boolean
 * @property limitDepositDefault (java.math.BigDecimal..java.math.BigDecimal?)
 */
class BackgroundServiceBalance : IntentService("BackgroundServiceBalance") {
  private lateinit var response: JSONObject
  private lateinit var balanceValue: BigDecimal
  private lateinit var user: User

  private var startBackgroundService: Boolean = true
  private var limitDepositDefault = BigDecimal(0.000000000, MathContext.DECIMAL32).setScale(8, BigDecimal.ROUND_HALF_DOWN)

  /**
   * @override function onDestroy
   * to start Automatic update balance
   * @param intent Intent
   */
  override fun onHandleIntent(intent: Intent) {
    user = User(this)

    val body = HashMap<String, String>()
    body["a"] = "GetBalance"
    body["s"] = user.getString("key")
    body["Currency"] = "doge"
    body["Referrals"] = "0"
    body["Stats"] = "0"

    var time = System.currentTimeMillis()
    val trigger = Object()

    synchronized(trigger) {
      startBackgroundService = true
      while (true) {
        val delta = System.currentTimeMillis() - time
        if (delta >= 5000) {
          time = System.currentTimeMillis()
          response = DogeController(body).execute().get()
          if (startBackgroundService) {
            if (response["code"] == 200) {
              try {
                balanceValue = response.getJSONObject("data")["Balance"].toString().toBigDecimal()
                val balanceLimit = if (user.getString("limitDeposit").isEmpty()) {
                  BitCoinFormat().dogeToDecimal(limitDepositDefault)
                } else {
                  BitCoinFormat().dogeToDecimal(user.getString("limitDeposit").toBigDecimal())
                }

                /** declaration Intent From Broadcast */
                val privateIntent = Intent()

                privateIntent.putExtra("balance", balanceValue.toPlainString())

                if (!user.getBoolean("ifPlay")) {
                  if (BitCoinFormat().decimalToDoge(balanceValue) >= BigDecimal(10000) && balanceValue <= balanceLimit) {
                    privateIntent.putExtra("nav_withdraw", false)
                    privateIntent.putExtra("nav_fibonacci", true)
                    privateIntent.putExtra("nav_marti_angel", true)
                    user.setString("balance", "${BitCoinFormat().decimalToDoge(balanceValue).toPlainString()} DOGE")
                    user.setString("fakeBalance", "0")
                  } else if (balanceValue > balanceLimit) {
                    privateIntent.putExtra("nav_withdraw", true)
                    privateIntent.putExtra("nav_fibonacci", false)
                    privateIntent.putExtra("nav_marti_angel", false)
                    user.setString("balance", "${BitCoinFormat().decimalToDoge(balanceValue).toPlainString()} DOGE terlalu tinggi")
                    user.setString("fakeBalance", "0")
                  } else {
                    if (BitCoinFormat().decimalToDoge(balanceValue) < BigDecimal(10000) && BitCoinFormat().decimalToDoge(balanceValue) > BigDecimal(0)) {
                      privateIntent.putExtra("nav_withdraw", true)
                    } else {
                      privateIntent.putExtra("nav_withdraw", false)
                    }
                    privateIntent.putExtra("nav_fibonacci", false)
                    privateIntent.putExtra("nav_marti_angel", false)
                    user.setString("balance", "${BitCoinFormat().decimalToDoge(balanceValue).toPlainString()} DOGE terlalu kecil")
                    user.setString("fakeBalance", "0")
                  }
                } else {
                  privateIntent.putExtra("nav_withdraw", false)
                  privateIntent.putExtra("nav_fibonacci", false)
                  privateIntent.putExtra("nav_marti_angel", false)
                  if (balanceValue <= BigDecimal(0) || user.getString("fakeBalance").isEmpty() || user.getString("fakeBalance") == "0") {
                    user.setString("balance", "${BitCoinFormat().decimalToDoge(balanceValue).toPlainString()} DOGE")
                    user.setString("fakeBalance", "0")
                  } else {
                    try {
                      user.setString("balance", "${BitCoinFormat().decimalToDoge(user.getString("fakeBalance").toBigDecimal()).toPlainString()} DOGE")
                    } catch (e: Exception) {
                      user.setString("balance", "${BitCoinFormat().decimalToDoge(balanceValue).toPlainString()} DOGE")
                    }
                  }
                }

                user.setString("balanceMax", "${BitCoinFormat().decimalToDoge(balanceLimit).toPlainString()} DOGE")

                /** start Broadcast */
                privateIntent.action = "id.co.agogo"
                LocalBroadcastManager.getInstance(this).sendBroadcast(privateIntent)
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