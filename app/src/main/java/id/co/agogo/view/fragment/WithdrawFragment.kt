package id.co.agogo.view.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import id.co.agogo.MainActivity
import id.co.agogo.R
import id.co.agogo.config.BackgroundServiceBalance
import id.co.agogo.config.Loading
import id.co.agogo.controller.DogeController
import id.co.agogo.model.User
import id.co.agogo.view.NavigationActivity
import org.json.JSONObject
import java.util.*
import kotlin.concurrent.schedule

class WithdrawFragment : Fragment() {
  private lateinit var username: TextView
  private lateinit var balance: TextView
  private lateinit var withdraw: Button

  private lateinit var user: User
  private lateinit var loading: Loading
  private lateinit var response: JSONObject
  private lateinit var goTo: Intent
  private lateinit var intentService: Intent
  private lateinit var parentActivity: NavigationActivity

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val root = inflater.inflate(R.layout.fragment_withdraw, container, false)

    username = root.findViewById(R.id.textViewUsername)
    balance = root.findViewById(R.id.textViewBalance)
    withdraw = root.findViewById(R.id.buttonWithdraw)

    parentActivity = activity as NavigationActivity
    loading = Loading(parentActivity)
    user = User(parentActivity)

    username.text = user.getString("usernameWeb")
    balance.text = user.getString("balance")

    intentService = Intent(parentActivity, BackgroundServiceBalance::class.java)

    withdraw.setOnClickListener {
      withdraw()
    }

    return root
  }

  override fun onResume() {
    super.onResume()
    val intentFilter = IntentFilter()
    intentFilter.addAction("id.co.agogo")
    parentActivity.registerReceiver(broadcastReceiver, intentFilter)
  }

  override fun onPause() {
    super.onPause()
    parentActivity.unregisterReceiver(broadcastReceiver)
  }

  private var broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      balance.text = user.getString("balance")
    }
  }

  private fun withdraw() {
    loading.openDialog()
    val body = HashMap<String, String>()
    body["a"] = "Withdraw"
    body["s"] = user.getString("key")
    body["Amount"] = "0"
    body["Address"] = user.getString("walletWithdraw")
    body["Currency"] = "doge"
    Timer().schedule(1000) {
      response = DogeController(body).execute().get()
      when {
        response["code"] == 200 -> {
          parentActivity.runOnUiThread {
            parentActivity.stopService(intentService)
            Toast.makeText(parentActivity.applicationContext, "Jumlah satoshi yang antri untuk ditarik.", Toast.LENGTH_SHORT).show()
            goTo = Intent(parentActivity.applicationContext, MainActivity::class.java)
            startActivity(goTo)
            parentActivity.finishAffinity()
            loading.closeDialog()
          }
        }
        else -> {
          parentActivity.runOnUiThread {
            parentActivity.stopService(intentService)
            Toast.makeText(parentActivity.applicationContext, response["data"].toString(), Toast.LENGTH_SHORT).show()
            goTo = Intent(parentActivity.applicationContext, MainActivity::class.java)
            startActivity(goTo)
            parentActivity.finishAffinity()
            loading.closeDialog()
          }
        }
      }
    }
  }
}