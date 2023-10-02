package com.example.skillcinema

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.example.skillcinema.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {
    private val rep = Repository()
    lateinit var binding: ActivityHomeBinding
    private val dBViewModel: DBViewModel by viewModels {
        object : ViewModelProvider.Factory{
            override fun <T : ViewModel> create(modelClass: Class<T>): T{
                val filmInfoDao = (this@HomeActivity.applicationContext as  App).db.filmInfoDao()
                return DBViewModel(filmInfoDao) as T
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_navigation) as NavHostFragment
        val navController = navHostFragment.navController
        val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)

        if (rep.getFirstLoadCheck(this))
            navGraph.setStartDestination(R.id.onboardingFragment)
        else if (!rep.getFirstLoadCheck(this) && (rep.getApiKey(this) == null || rep.getApiKey(this) == ""))
            navGraph.setStartDestination(R.id.registrationFragment)
        else navGraph.setStartDestination(R.id.homeFragment)
        navController.graph = navGraph

        rep.saveAllowableRequestAttributes(this)
        rep.saveDBDataRelevanceState(this, false)

        //hideSystemBars()
    }

    private fun hideSystemBars() {
        val windowInsetsController =
            ViewCompat.getWindowInsetsController(window.decorView) ?: return
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars())
    }
}