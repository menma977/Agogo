package id.co.agogo.view.fragment

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import id.co.agogo.R
import id.co.agogo.config.Loading
import id.co.agogo.view.NavigationActivity
import id.co.agogo.view.bot.FibonacciActivity
import org.json.JSONObject

class HomeFragment : Fragment() {
  private lateinit var username: TextView
  private lateinit var wallet: TextView
  private lateinit var balance: TextView

  private lateinit var response: JSONObject
  private lateinit var loading: Loading
  private lateinit var goTo: Intent

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val root = inflater.inflate(R.layout.fragment_home, container, false)

    username = root.findViewById(R.id.textViewUsername)
    wallet = root.findViewById(R.id.textViewWallet)
    balance = root.findViewById(R.id.textViewBalance)

    val parentActivity = activity as NavigationActivity

    loading = Loading(parentActivity)

    balance.setOnClickListener {
      loading.openDialog()
      goTo = Intent(parentActivity, FibonacciActivity::class.java)
      startActivity(goTo)
      parentActivity.finish()
      loading.closeDialog()
    }

    return root
  }
}