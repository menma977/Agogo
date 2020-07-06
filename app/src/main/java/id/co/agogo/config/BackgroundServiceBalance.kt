package id.co.agogo.config

import android.app.IntentService
import android.content.Intent
import id.co.agogo.controller.DogeController
import id.co.agogo.model.User
import org.json.JSONObject
import java.math.BigDecimal
import java.math.MathContext

class BackgroundServiceBalance : IntentService("BackgroundServiceBalance") {
  private lateinit var response: JSONObject
  private lateinit var balanceValue: BigDecimal
  private lateinit var user: User

  private var isStopService: Boolean = false
  private var limitDepositDefault = BigDecimal(0.000000000, MathContext.DECIMAL32).setScale(8, BigDecimal.ROUND_HALF_DOWN)

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
      while (true) {
        val delta = System.currentTimeMillis() - time
        if (delta >= 5000) {
          time = System.currentTimeMillis()
          if (isStopService) {
            break
          } else {
            response = DogeController(body).execute().get()
            if (response["code"] == 200) {
              balanceValue = response.getJSONObject("data")["Balance"].toString().toBigDecimal()
              val balanceLimit = if (user.getString("limitDeposit").isEmpty()) {
                BitCoinFormat().dogeToDecimal(limitDepositDefault)
              } else {
                BitCoinFormat().dogeToDecimal(user.getString("limitDeposit").toBigDecimal())
              }
              val privateIntent = Intent()
              if (!user.getBoolean("ifPlay")) {
                if (BitCoinFormat().decimalToDoge(balanceValue) >= BigDecimal(1000) && balanceValue <= balanceLimit) {
                  privateIntent.putExtra("nav_withdraw", false)
                  privateIntent.putExtra("nav_fibonacci", true)
                  privateIntent.putExtra("nav_marti_angel", true)
                  user.setString("balance", "${BitCoinFormat().decimalToDoge(balanceValue).toPlainString()} DOGE")
                } else if (balanceValue > balanceLimit) {
                  privateIntent.putExtra("nav_withdraw", true)
                  privateIntent.putExtra("nav_fibonacci", false)
                  privateIntent.putExtra("nav_marti_angel", false)
                  user.setString("balance", "${BitCoinFormat().decimalToDoge(balanceValue).toPlainString()} DOGE terlalu tinggi")
                } else {
                  if (BitCoinFormat().decimalToDoge(balanceValue) < BigDecimal(10000) && BitCoinFormat().decimalToDoge(balanceValue) > BigDecimal(0)) {
                    privateIntent.putExtra("nav_withdraw", true)
                  } else {
                    privateIntent.putExtra("nav_withdraw", false)
                  }
                  privateIntent.putExtra("nav_fibonacci", false)
                  privateIntent.putExtra("nav_marti_angel", false)
                  user.setString("balance", "${BitCoinFormat().decimalToDoge(balanceValue).toPlainString()} DOGE terlalu kecil")
                }
              } else {
                privateIntent.putExtra("nav_withdraw", false)
                privateIntent.putExtra("nav_fibonacci", false)
                privateIntent.putExtra("nav_marti_angel", false)
                user.setString("fakeBalance", "0")
              }
              privateIntent.action = "id.co.agogo"
              sendBroadcast(privateIntent)
            } else {
              trigger.wait(60000)
            }
          }
        }
      }
    }
  }

  override fun onDestroy() {
    isStopService = true
    super.onDestroy()
  }
}