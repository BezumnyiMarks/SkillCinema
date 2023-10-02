package Fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.skillcinema.HomeViewModel
import com.example.skillcinema.R
import DataModels.Series
import Adapters.ViewPagerAdapter
import com.example.skillcinema.Repository
import com.example.skillcinema.databinding.FragmentSeasonsBinding
import com.google.android.material.tabs.TabLayoutMediator

private const val KINOPOISK_ID = "kinopoiskID"
private const val SERIES_NAME = "SERIES_NAME"
class SeasonsFragment : Fragment() {
    private var _binding: FragmentSeasonsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    private val rep = Repository()
    private var apiKey = ""

    private var kinopoiskID: Long? = null
    private var seriesName: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            kinopoiskID = it.getLong(KINOPOISK_ID)
            seriesName = it.getString(SERIES_NAME)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSeasonsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        apiKey = rep.getApiKey(requireActivity())!!
        showData()

        binding.buttonBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.bottomNav.setOnItemSelectedListener {
            when(it.itemId){
                R.id.home -> findNavController().navigate(R.id.homeFragment)
                R.id.search -> findNavController().navigate(R.id.searchFragment)
                R.id.profile -> findNavController().navigate(R.id.profileFragment)
            }
            true
        }

        binding.bottomNav.menu.get(0).isChecked = false
        binding.bottomNav.menu.get(1).isChecked = false
        binding.bottomNav.menu.get(2).isChecked = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showData() {
        binding.name.text = seriesName
        lifecycleScope.launchWhenCreated {
            viewModel.loadSeasons(apiKey, kinopoiskID!!.toInt())
            viewModel.seasonsLoading.collect{ seasonsLoading ->
                if (!seasonsLoading)
                    viewModel.seasons.collect{
                        getTabs(it)
                    }
            }
        }
    }

    @SuppressLint("InflateParams")
    private fun getTabs(series: Series){
        binding.viewPager.adapter = ViewPagerAdapter(this, null, kinopoiskID!!, null, series)
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = (position + 1).toString()
        }.attach()

        for (i in series.items.indices){
            val item = LayoutInflater.from(requireContext())
                .inflate(R.layout.seasons_tablayout_item, null) as ConstraintLayout
            binding.tabLayout.getTabAt(i)?.customView = item
        }
    }
}