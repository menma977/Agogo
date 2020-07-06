package id.co.agogo.view.fragment

import android.content.*
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import id.co.agogo.MainActivity
import id.co.agogo.R
import id.co.agogo.config.BackgroundServiceBalance
import id.co.agogo.config.Loading
import id.co.agogo.model.User
import id.co.agogo.view.NavigationActivity
import java.util.*
import kotlin.concurrent.schedule


class HomeFragment : Fragment() {
  private lateinit var username: TextView
  private lateinit var wallet: TextView
  private lateinit var balance: TextView
  private lateinit var balanceMax: TextView

  private lateinit var clipboardManager: ClipboardManager
  private lateinit var clipData: ClipData
  private lateinit var loading: Loading
  private lateinit var parentActivity: NavigationActivity
  private lateinit var user: User
  private lateinit var goTo: Intent
  private lateinit var intentService: Intent

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val root = inflater.inflate(R.layout.fragment_home, container, false)

    username = root.findViewById(R.id.textViewUsername)
    wallet = root.findViewById(R.id.textViewWallet)
    balance = root.findViewById(R.id.textViewBalance)
    balanceMax = root.findViewById(R.id.textViewBalanceMax)

    parentActivity = activity as NavigationActivity

    loading = Loading(parentActivity)
    user = User(parentActivity)

    username.text = user.getString("usernameWeb")
    wallet.text = user.getString("wallet")
    balance.text = user.getString("balance")
    balanceMax.text = user.getString("balanceMax")

    wallet.setOnClickListener {
      clipboardManager = parentActivity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
      clipData = ClipData.newPlainText("Wallet", wallet.text.toString())
      clipboardManager.primaryClip = clipData
      Toast.makeText(parentActivity, "Dompet Doge telah disalin", Toast.LENGTH_LONG).show()
    }

    balance.setOnClickListener {
      loading.openDialog()
      Timer().schedule(1000) {
        parentActivity.runOnUiThread {
          intentService = Intent(parentActivity, BackgroundServiceBalance::class.java)
          parentActivity.stopService(intentService)

          goTo = Intent(parentActivity, MainActivity::class.java)
          startActivity(goTo)
          parentActivity.finishAffinity()
          loading.closeDialog()
        }
      }
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
    if (broadcastReceiver.isOrderedBroadcast) {
      parentActivity.unregisterReceiver(broadcastReceiver)
    }
  }

  private var broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      balance.text = user.getString("balance")
    }
  }
}