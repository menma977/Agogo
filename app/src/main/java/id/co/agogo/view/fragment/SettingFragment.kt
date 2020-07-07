package id.co.agogo.view.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import id.co.agogo.R
import id.co.agogo.config.Loading
import id.co.agogo.model.Config
import id.co.agogo.view.NavigationActivity

/**
 * class SettingFragment
 * @property activeChart Switch
 * @property activeProgressBar Switch
 * @property loading Loading
 * @property config Config
 */
class SettingFragment : Fragment() {
  private lateinit var activeChart: Switch
  private lateinit var activeProgressBar: Switch

  private lateinit var loading: Loading
  private lateinit var config: Config

  /**
   * override fun onCreateView
   * @param inflater LayoutInflater
   * @param container ViewGroup?
   * @param savedInstanceState Bundle?
   * @return View?
   */
  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val root = inflater.inflate(R.layout.fragment_setting, container, false)

    activeChart = root.findViewById(R.id.switchActiveChart)
    activeProgressBar = root.findViewById(R.id.switchActiveProgressBar)

    val parentActivity = activity as NavigationActivity

    loading = Loading(parentActivity)
    config = Config(parentActivity)

    activeChart.isChecked = config.getBoolean("chart")
    activeProgressBar.isChecked = config.getBoolean("progressBar")

    activeChart.setOnCheckedChangeListener { _, isChecked ->
      config.setBoolean("chart", isChecked)
    }

    activeProgressBar.setOnCheckedChangeListener { _, isChecked ->
      config.setBoolean("progressBar", isChecked)
    }

    return root
  }
}