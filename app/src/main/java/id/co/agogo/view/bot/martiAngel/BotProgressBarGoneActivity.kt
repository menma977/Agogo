package id.co.agogo.view.bot.martiAngel

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import id.co.agogo.R
import id.co.agogo.config.BitCoinFormat
import id.co.agogo.config.Loading
import id.co.agogo.controller.DogeController
import id.co.agogo.model.User
import id.co.agogo.view.ResultActivity
import org.eazegraph.lib.charts.ValueLineChart
import org.eazegraph.lib.models.ValueLinePoint
import org.eazegraph.lib.models.ValueLineSeries
import org.json.JSONObject
import java.math.BigDecimal
import java.util.*

class BotProgressBarGoneActivity : AppCompatActivity() {
  private lateinit var cubicLineChart: ValueLineChart
  private lateinit var series: ValueLineSeries
  private lateinit var goTo: Intent
  private lateinit var user: User
  private lateinit var loading: Loading
  private lateinit var response: JSONObject
  private lateinit var bitCoinFormat: BitCoinFormat

  private lateinit var balance: BigDecimal
  private lateinit var fakeBalance: BigDecimal
  private lateinit var balanceTarget: BigDecimal
  private lateinit var balanceRemaining: BigDecimal
  private lateinit var payIn: BigDecimal
  private lateinit var payOut: BigDecimal
  private lateinit var profit: BigDecimal

  private lateinit var usernameView: TextView
  private lateinit var balanceView: TextView
  private lateinit var balanceRemainingView: TextView
  private lateinit var payInLinearLayout: LinearLayout
  private lateinit var payOutLinearLayout: LinearLayout
  private lateinit var profitLinearLayout: LinearLayout

  private lateinit var uniqueCode: String

  private var rowChart = 1
  private val maxRow = 10
  private var loseBot = false
  private var balanceLimitTarget = BigDecimal(0.06)
  private var balanceLimitTargetLow = BigDecimal(0)
  private var formula = 1
  private var seed = (0..99999).random().toString()
  private var thread = Thread()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_bot_progress_bar_gone)

    loading = Loading(this)
    user = User(this)
    bitCoinFormat = BitCoinFormat()

    uniqueCode = intent.getSerializableExtra("uniqueCode").toString()

    usernameView = findViewById(R.id.textViewUsername)
    balanceView = findViewById(R.id.textViewBalance)
    balanceRemainingView = findViewById(R.id.textViewRemainingBalance)
    cubicLineChart = findViewById(R.id.cubicLineChart)
    payInLinearLayout = findViewById(R.id.LinearLayoutContentPayIn)
    payOutLinearLayout = findViewById(R.id.LinearLayoutContentPayOut)
    profitLinearLayout = findViewById(R.id.LinearLayoutContentProfit)

    payInLinearLayout.removeAllViews()
    payOutLinearLayout.removeAllViews()
    profitLinearLayout.removeAllViews()

    setDefaultView()

    series = ValueLineSeries()

    loading.openDialog()
    balance = intent.getSerializableExtra("balance").toString().toBigDecimal()
    balanceRemaining = balance
    fakeBalance = balance
    balanceTarget = bitCoinFormat.dogeToDecimal(bitCoinFormat.decimalToDoge((balance * balanceLimitTarget) + balance))
    payIn = bitCoinFormat.dogeToDecimal(bitCoinFormat.decimalToDoge(balance) * BigDecimal(0.001))
    balanceLimitTargetLow = bitCoinFormat.dogeToDecimal(bitCoinFormat.decimalToDoge(balance) * balanceLimitTargetLow)

    usernameView.text = user.getString("usernameWeb")
    balanceView.text = "${bitCoinFormat.decimalToDoge(balance).toPlainString()} DOGE"
    balanceRemainingView.text = bitCoinFormat.decimalToDoge(balanceRemaining).toPlainString()

    configChart()
    loading.closeDialog()
    thread = Thread {
      onBotMode()
    }
    thread.start()
  }

  override fun onBackPressed() {
    Toast.makeText(this, "Tidak Bisa Kembali Ketika memainkan bot", Toast.LENGTH_LONG).show()
  }

  private fun configChart() {
    series.color = getColor(R.color.colorAccent)
    cubicLineChart.axisTextColor = getColor(R.color.textPrimary)
    cubicLineChart.containsPoints()
    cubicLineChart.isUseDynamicScaling = true
    cubicLineChart.addSeries(series)
    cubicLineChart.startAnimation()
    series.addPoint(ValueLinePoint("0", bitCoinFormat.decimalToDoge(balanceRemaining).toFloat()))
  }

  private fun onBotMode() {
    var time = System.currentTimeMillis()
    val trigger = Object()
    synchronized(trigger) {
      while (balanceRemaining in balanceLimitTargetLow..balanceTarget) {
        val delta = System.currentTimeMillis() - time
        if (delta >= 1000) {
          time = System.currentTimeMillis()
          payIn *= formula.toBigDecimal()
          val body = HashMap<String, String>()
          body["a"] = "PlaceBet"
          body["s"] = user.getString("key")
          body["Low"] = "0"
          body["High"] = "499999"
          body["PayIn"] = payIn.toPlainString()
          body["ProtocolVersion"] = "2"
          body["ClientSeed"] = seed
          body["Currency"] = "doge"
          response = DogeController(body).execute().get()
          if (response["code"] == 200) {
            seed = response.getJSONObject("data")["Next"].toString()
            payOut = response.getJSONObject("data")["PayOut"].toString().toBigDecimal()
            balanceRemaining = response.getJSONObject("data")["StartingBalance"].toString().toBigDecimal()
            profit = payOut - payIn
            balanceRemaining += profit
            loseBot = profit < BigDecimal(0)
            payIn = bitCoinFormat.dogeToDecimal(bitCoinFormat.decimalToDoge(balance) * BigDecimal(0.001))

            if (loseBot) {
              series.color = getColor(R.color.Danger)
              formula *= 2
            } else {
              series.color = getColor(R.color.Success)
              formula = 1
              payIn = bitCoinFormat.dogeToDecimal(bitCoinFormat.decimalToDoge(balance) * BigDecimal(0.001))
            }

            runOnUiThread {
              fakeBalance += profit / BigDecimal(2)
              user.setString("fakeBalance", fakeBalance.toPlainString())
              balanceRemainingView.text = "${bitCoinFormat.decimalToDoge(fakeBalance).toPlainString()} DOGE"

              if (rowChart >= 39) {
                series.series.removeAt(0)
              }
              series.addPoint(ValueLinePoint("$rowChart", bitCoinFormat.decimalToDoge(fakeBalance).toFloat()))
              cubicLineChart.addSeries(series)
              cubicLineChart.refreshDrawableState()

              setBalanceView(bitCoinFormat.decimalToDoge(payIn).toPlainString(), payInLinearLayout, false)
              setBalanceView(bitCoinFormat.decimalToDoge(payOut).toPlainString(), payOutLinearLayout, false)
              setBalanceView(bitCoinFormat.decimalToDoge(profit).toPlainString(), profitLinearLayout, false)
            }

            rowChart++
          } else if (response["code"] == 404) {
            break
          } else {
            runOnUiThread {
              balanceRemainingView.text = "sleep mode Active"
              Toast.makeText(applicationContext, "sleep mode Active Wait to continue", Toast.LENGTH_LONG).show()
            }
            trigger.wait(60000)
          }
        }
      }

      goTo = Intent(applicationContext, ResultActivity::class.java)
      if (balanceRemaining >= balanceTarget) {
        goTo.putExtra("status", "WIN")
      } else {
        goTo.putExtra("status", "CUT LOSS")
      }
      goTo.putExtra("startBalance", balance)
      goTo.putExtra("balanceRemaining", balanceRemaining)
      goTo.putExtra("uniqueCode", intent.getSerializableExtra("uniqueCode").toString())
      runOnUiThread {
        startActivity(goTo)
        finish()
      }
    }
  }

  private fun setBalanceView(balanceValue: String, linearLayout: LinearLayout, isNew: Boolean) {

    val text = LinearLayout.LayoutParams(
      LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
    )

    val balance = TextView(applicationContext)
    balance.text = balanceValue
    balance.layoutParams = text
    balance.gravity = Gravity.CENTER
    if (isNew) {
      balance.setTextColor(getColor(R.color.colorAccent))
    } else {
      if (loseBot) {
        balance.setTextColor(getColor(R.color.Danger))
      } else {
        balance.setTextColor(getColor(R.color.Success))
      }
    }

    if ((linearLayout.childCount - 1) == maxRow) {
      linearLayout.removeViewAt(linearLayout.childCount - 1)
      linearLayout.addView(balance, 1)
    } else {
      linearLayout.addView(balance)
    }
  }

  private fun setDefaultView() {
    setBalanceView("PayIn", payInLinearLayout, true)
    setBalanceView("PayOut", payOutLinearLayout, true)
    setBalanceView("Profit", profitLinearLayout, true)
    for (i in 0..maxRow) {
      setBalanceView("0", payInLinearLayout, false)
      setBalanceView("0", payOutLinearLayout, false)
      setBalanceView("0", profitLinearLayout, false)
    }
  }
}
