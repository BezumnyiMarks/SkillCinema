package Fragments

import android.annotation.SuppressLint
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.get
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.example.skillcinema.App
import DataModels.DBImage
import com.example.skillcinema.DBViewModel
import Adapters.GalleryPagingAdapter
import PagingSources.GalleryPagingSource
import DataModels.GalleryTab
import com.example.skillcinema.HomeViewModel
import com.example.skillcinema.R
import Adapters.ViewPagerAdapter
import com.example.skillcinema.Repository
import com.example.skillcinema.databinding.FragmentGalleryBinding
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.*

private const val KINOPOISK_ID = "kinopoiskID"
class GalleryFragment : Fragment() {
    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    private val dBViewModel: DBViewModel by viewModels {
        object : ViewModelProvider.Factory{
            override fun <T : ViewModel> create(modelClass: Class<T>): T{
                val filmInfoDao = (requireActivity().application as App).db.filmInfoDao()
                return DBViewModel(filmInfoDao) as T
            }
        }
    }
    private val rep = Repository()
    private val galleryAdapter = GalleryPagingAdapter { }
    private var apiKey = ""

    private var kinopoiskID: Long? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            kinopoiskID = it.getLong(KINOPOISK_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
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
        val tabTitles = mutableListOf<String>()
        val tabTypes = mutableListOf<String>()
        val tabTitlesList = mutableListOf("КАДРЫ", "СО СЪЁМОК", "ПОСТЕРЫ", "ФАН-АРТЫ", "ПРОМО", "КОНЦЕПТ-АРТЫ", "ОБОИ", "ОБЛОЖКИ", "СКРИНШОТЫ")
        val tabTypesList = mutableListOf("STILL", "SHOOTING", "POSTER", "FAN_ART", "PROMO", "CONCEPT", "WALLPAPER", "COVER", "SCREENSHOT")
        var currentTabType = ""
        var currentTabTitle = ""

        //Делаем загрузку первой страницы для каждого типа фотокарточек, чтобы понять, какие типы фотокарточек есть на серванте кинопоиска,
        //и в соответствии с этим сформировать набор табов и фрагментов под каждый тип фотокарточек.
        lifecycleScope.launchWhenCreated {
            if (dBViewModel.getGalleryTabsByFilmAttachment(kinopoiskID!!.toInt()).isEmpty()){
                galleryAdapter.addOnPagesUpdatedListener {
                    val list = galleryAdapter.snapshot()
                    Log.d("currentType", currentTabType)
                    Log.d("FragmentTeg", "items: $list")
                    Log.d("//", "\n")

                    if (!tabTitles.contains(currentTabTitle) && list.isNotEmpty()){
                        list.forEach {
                            dBViewModel.addImage(DBImage(it!!.imageURL!!, currentTabType, kinopoiskID!!.toInt()))
                        }
                        tabTitles.add(currentTabTitle)
                        tabTypes.add(currentTabType)
                    }

                    if (currentTabTitle == "СКРИНШОТЫ"){
                        val galleryTabs = mutableListOf<GalleryTab>()
                        for (i in tabTitles.indices){
                            galleryTabs.add(GalleryTab(Calendar.getInstance().timeInMillis + i, kinopoiskID!!.toInt(), tabTitles[i], tabTypes[i]))
                        }
                        galleryTabs.forEach {
                            dBViewModel.addGalleryTab(it)
                        }
                        getTabs(tabTitles, tabTypes)
                    }
                }
                viewModel.galleryLoading.collect{ galleryLoading ->
                    delay(100)
                    Log.d("galleryLoading", galleryLoading.toString())
                    if (!galleryLoading && tabTypesList != mutableListOf<String>()){
                        currentTabType = tabTypesList[0]
                        currentTabTitle = tabTitlesList[0]

                        val pagingSourceFactory = GalleryPagingSource(apiKey, kinopoiskID!!.toInt(), currentTabType,1, viewModel, listOf())
                        pagingSourceFactory.netLoadingAllowed = true

                        viewModel.gallery = Pager(
                            config = PagingConfig(pageSize = 20, prefetchDistance = 0),
                            pagingSourceFactory = { pagingSourceFactory }
                        ).flow.cachedIn(lifecycleScope)

                        lifecycleScope.launchWhenCreated {
                            viewModel.gallery.collect { images ->
                                galleryAdapter.submitData(images)
                            }
                        }
                        tabTypesList.removeAt(0)
                        tabTitlesList.removeAt(0)
                    }
                }
            }
            else{
                val galleryTabs = dBViewModel.getGalleryTabsByFilmAttachment(kinopoiskID!!.toInt())
                galleryTabs.forEach {
                    tabTitles.add(it.title)
                    tabTypes.add(it.type)
                }
                getTabs(tabTitles, tabTypes)
            }
        }
    }

    @SuppressLint("InflateParams")
    private fun getTabs(tabTitles: MutableList<String>, tabTypes: MutableList<String>){
        binding.viewPager.adapter = ViewPagerAdapter(this, tabTypes, kinopoiskID!!, null, null)
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()

        for (i in tabTitles.indices){
            val item = LayoutInflater.from(requireContext()).inflate(R.layout.tablayout_item, null) as ConstraintLayout
            binding.tabLayout.getTabAt(i)?.customView = item
        }
    }
}