package id.co.agogo.config

import android.R.style.Theme_Translucent_NoTitleBar
import android.app.Dialog
import android.support.v4.app.FragmentActivity
import id.co.agogo.R

class LoadingFragment(fragmentActivity: FragmentActivity) {
  private val dialog = Dialog(fragmentActivity, Theme_Translucent_NoTitleBar)

  init {
    val view = fragmentActivity.layoutInflater.inflate(R.layout.activity_main, null)
    dialog.setContentView(view)
    dialog.setCancelable(false)
  }

  fun openDialog() {
    dialog.show()
  }

  fun closeDialog() {
    dialog.dismiss()
  }
}