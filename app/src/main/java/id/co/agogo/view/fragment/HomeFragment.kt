package id.co.agogo.view.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import id.co.agogo.R
import id.co.agogo.config.Loading
import id.co.agogo.view.NavigationActivity
import org.json.JSONObject

class HomeFragment : Fragment() {
  private lateinit var username: TextView
  private lateinit var wallet: TextView
  private lateinit var balance: TextView

  private lateinit var response: JSONObject
  private lateinit var loading: Loading

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val root = inflater.inflate(R.layout.fragment_home, container, false)

    username = root.findViewById(R.id.textViewUsername)
    wallet = root.findViewById(R.id.textViewWallet)
    balance = root.findViewById(R.id.textViewBalance)

    loading = Loading(activity as NavigationActivity)

    balance.setOnClickListener {
      loading.openDialog()
    }

    return root
  }
}