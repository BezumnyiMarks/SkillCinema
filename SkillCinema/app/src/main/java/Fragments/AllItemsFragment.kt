package Fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.example.skillcinema.App
import com.example.skillcinema.DBViewModel
import DataModels.DynamicSelectionRequestAttributes
import DataModels.Film
import Adapters.FilmsPagingAdapter
import PagingSources.FilmsPagingSource
import com.example.skillcinema.HomeViewModel
import DataModels.Item
import Adapters.ItemsPagingAdapter
import PagingSources.ItemsPagingSource
import Adapters.ListAdapter
import DataModels.MaxFilmInfo
import com.example.skillcinema.R
import com.example.skillcinema.Repository
import DataModels.SearchSettings
import com.example.skillcinema.databinding.FragmentAllItemsBinding
import com.example.skillcinema.utils.Extensions
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import java.util.*

private const val KINOPOISK_ID = "kinopoiskID"
private const val ADAPTER_NUM = "adapterNum"
private const val WATCHED = "Просмотрено"
private const val COLLECTION_NAME = "COLLECTION_NAME"
class AllItemsFragment : Fragment() {
    private var _binding: FragmentAllItemsBinding? = null
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
    private val listAdapter = ListAdapter{ item -> onItemClick(item)}
    private val filmsPagingAdapter = FilmsPagingAdapter{ film -> onFilmClick(film)}
    private val itemsPagingAdapter = ItemsPagingAdapter{ item -> onItemClick(item)}
    private val rep = Repository()
    private val extensions = Extensions()
    private var apiKey = ""

    private var adapterNum: Int? = null
    private var collectionName: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            adapterNum = it.getInt(ADAPTER_NUM)
            collectionName = it.getString(COLLECTION_NAME)
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAllItemsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        apiKey = rep.getApiKey(requireActivity())!!
        loadData()

        binding.buttonBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.bottomNav.setOnItemSelectedListener {
            when(it.itemId){
                R.id.home -> findNavController().navigate(R.id.homeFragment)
                R.id.search -> findNavController().navigate(R.id.searchFragment)
                R.id.profile -> findNavController().navigate(R.id.searchSettingsFragment)
            }
            true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onItemClick(item: Item) {
        val bundle = bundleOf(KINOPOISK_ID to item.kinopoiskID)
        findNavController().navigate(R.id.action_allItemsFragment_to_filmInfoFragment, bundle)
    }

    private fun onFilmClick(film: Film) {
        val bundle = bundleOf(KINOPOISK_ID to film.filmID)
        findNavController().navigate(R.id.action_allItemsFragment_to_filmInfoFragment, bundle)
    }

    private fun loadData() {
        if (collectionName != null) {
            binding.name.text = collectionName
            binding.recyclerView.adapter = listAdapter

            var watched = false
            lifecycleScope.launchWhenCreated {
                val filmsIDList = extensions.convertStringToList(rep.getCollection(requireActivity(), collectionName!!))
                val watchedFilmsIDList = extensions.convertStringToList(rep.getCollection(requireActivity(), WATCHED))
                val dBFilmsList = mutableListOf<MaxFilmInfo>()
                val filmsList = mutableListOf<Item>()

                Log.d("WATCHED", watchedFilmsIDList.toString())
                filmsIDList.forEach {
                    dBFilmsList.add(dBViewModel.getMaxFilmInfoByID(it.toLong()))
                }

                dBFilmsList.forEach {
                    val countries = extensions.getCountriesList(it.country)
                    val genres = extensions.getGenresList(it.genre)

                    watched = watchedFilmsIDList.contains(it.filmInfo.filmID.toString())
                    filmsList.add(Item(watched, null, it.filmInfo.filmID, null, null, it.filmInfo.nameRu, it.filmInfo.nameEn, it.filmInfo.nameOriginal, it.filmInfo.ratingKinopoisk, it.filmInfo.ratingImdb, null,null, it.filmInfo.posterURL, null,null, null, countries, genres, null, null, null))
                }

                filmsList.reverse()
                listAdapter.submitList(filmsList)
            }
        }

        when(adapterNum){
            1 -> {
                binding.name.text = "Премьеры"
                binding.recyclerView.adapter = listAdapter
                viewModel.loadPremieres(apiKey, Calendar.getInstance().get(Calendar.YEAR),
                    Calendar.getInstance().get(Calendar.MONTH), false)
                lifecycleScope.launch {
                    viewModel.premieresLoading.collect{ premieresLoading ->
                        if (!premieresLoading){
                            viewModel.premieres.collect{ premieres ->
                                listAdapter.submitList(premieres.items)
                            }
                        }
                    }
                }
            }

            2 -> {
                binding.name.text = "Популярное"
                binding.recyclerView.adapter = filmsPagingAdapter
                val popularFilmsPSF = FilmsPagingSource(
                    apiKey,
                    top100Popular,
                    extensions.convertStringToList(rep.getCollection(requireActivity(), WATCHED)),
                    viewModel,
                    dBViewModel,
                    binding.name.text.toString()
                )
                popularFilmsPSF.netLoadingAllowed = false
                viewModel.popularFilms = Pager(
                    config = PagingConfig(pageSize = 20),
                    pagingSourceFactory = { popularFilmsPSF }
                ).flow.cachedIn(lifecycleScope)
                    .onStart { viewModel._popularLoading.value = true }

                lifecycleScope.launchWhenCreated {
                    viewModel.popularFilms.collect {
                        filmsPagingAdapter.submitData(it)
                    }
                    this.cancel()
                }
            }

            3 -> {
                binding.name.text = extensions.getDynamicSelectionName(1, requireActivity())
                binding.recyclerView.adapter = itemsPagingAdapter
                val firstDynamicRequestAttributes = extensions.getDynamicSelectionRequestAttributes(1, requireActivity())
                val firstDynamicPSF = ItemsPagingSource(
                    apiKey,
                    firstDynamicRequestAttributes, extensions.convertStringToList(rep.getCollection(requireActivity(), WATCHED)),
                    false,
                    SearchSettings(0, 0, 0, "", "", 0, 0, 0, 0, false),
                    "",
                    viewModel,
                    dBViewModel,
                    firstDynamicSelection,
                    binding.name.text.toString()
                )
                firstDynamicPSF.netLoadingAllowed = false
                viewModel.firstDynamicSelection = Pager(
                    config = PagingConfig(pageSize = 20),
                    pagingSourceFactory = { firstDynamicPSF }
                ).flow.cachedIn(lifecycleScope)
                    .onStart { viewModel._firstDynamicLoading.value = true; delay(400) }

                lifecycleScope.launchWhenCreated {
                    viewModel.firstDynamicSelection.collect {
                        itemsPagingAdapter.submitData(it)
                    }
                    this.cancel()
                }
            }

            4 -> {
                binding.name.text = extensions.getDynamicSelectionName(2, requireActivity())
                binding.recyclerView.adapter = itemsPagingAdapter
                val secondDynamicRequestAttributes = extensions.getDynamicSelectionRequestAttributes(2, requireActivity())
                val secondDynamicPSF = ItemsPagingSource(
                    apiKey,
                    secondDynamicRequestAttributes,
                    extensions.convertStringToList(rep.getCollection(requireActivity(), WATCHED)),
                    false,
                    SearchSettings(0, 0, 0, "", "", 0, 0, 0, 0, false),
                    "",
                    viewModel,
                    dBViewModel,
                    secondDynamicSelection,
                    binding.name.text.toString()
                )
                secondDynamicPSF.netLoadingAllowed = false
                viewModel.secondDynamicSelection = Pager(
                    config = PagingConfig(pageSize = 20),
                    pagingSourceFactory = { secondDynamicPSF }
                ).flow.cachedIn(lifecycleScope)
                    .onStart { viewModel._secondDynamicLoading.value = true; delay(500) }

                lifecycleScope.launchWhenCreated {
                    viewModel.secondDynamicSelection.collect {
                        itemsPagingAdapter.submitData(it)
                    }
                    this.cancel()
                }
            }

            5 -> {
                binding.name.text = extensions.getDynamicSelectionName(3, requireActivity())
                binding.recyclerView.adapter = itemsPagingAdapter
                val thirdDynamicRequestAttributes = extensions.getDynamicSelectionRequestAttributes(3, requireActivity())
                val thirdDynamicPSF = ItemsPagingSource(
                    apiKey,
                    thirdDynamicRequestAttributes,
                    extensions.convertStringToList(rep.getCollection(requireActivity(), WATCHED)),
                    false,
                    SearchSettings(0, 0, 0, "", "", 0, 0, 0, 0, false),
                    "",
                    viewModel,
                    dBViewModel,
                    thirdDynamicSelection,
                    binding.name.text.toString()
                )
                thirdDynamicPSF.netLoadingAllowed = false
                viewModel.thirdDynamicSelection = Pager(
                    config = PagingConfig(pageSize = 20),
                    pagingSourceFactory = { thirdDynamicPSF }
                ).flow.cachedIn(lifecycleScope)
                    .onStart { viewModel._thirdDynamicLoading.value = true; delay(600) }

                lifecycleScope.launchWhenCreated {
                    viewModel.thirdDynamicSelection.collect {
                        itemsPagingAdapter.submitData(it)
                    }
                    this.cancel()
                }
            }

            6 -> {
                binding.name.text = "Топ-250"
                binding.recyclerView.adapter = filmsPagingAdapter
                val bestFilmsPSF = FilmsPagingSource(
                    apiKey,
                    top250Best, extensions.convertStringToList(rep.getCollection(requireActivity(), WATCHED)),
                    viewModel,
                    dBViewModel,
                    binding.name.text.toString()
                )
                bestFilmsPSF.netLoadingAllowed = false
                viewModel.bestFilms = Pager(
                    config = PagingConfig(pageSize = 20),
                    pagingSourceFactory = { bestFilmsPSF }
                ).flow.cachedIn(lifecycleScope)
                    .onStart { viewModel._bestLoading.value = true; delay(300) }

                lifecycleScope.launchWhenCreated {
                    viewModel.bestFilms.collect {
                        filmsPagingAdapter.submitData(it)
                    }
                    this.cancel()
                }
            }

            7 -> {
                binding.name.text = "Сериалы"
                binding.recyclerView.adapter = itemsPagingAdapter
                val tvSeriesPSF = ItemsPagingSource(
                    apiKey,
                    DynamicSelectionRequestAttributes(1L, 11L, series),
                    extensions.convertStringToList(rep.getCollection(requireActivity(), WATCHED)),
                    false,
                    SearchSettings(0, 0, 0, "", "", 0, 0, 0, 0, false),
                    "",
                    viewModel,
                    dBViewModel,
                    series,
                    binding.name.text.toString()
                )
                tvSeriesPSF.netLoadingAllowed = false
                viewModel.tvSeries = Pager(
                    config = PagingConfig(pageSize = 20),
                    pagingSourceFactory = { tvSeriesPSF }
                ).flow.cachedIn(lifecycleScope)
                    .onStart { viewModel._seriesLoading.value = true; delay(700) }

                lifecycleScope.launchWhenCreated {
                    viewModel.tvSeries.collect {
                        itemsPagingAdapter.submitData(it)
                    }
                    this.cancel()
                }
            }
        }
    }

    private companion object {
        private const val top100Popular = "top_100_popular_films"
        private const val top250Best = "top_250_best_films"
        private const val firstDynamicSelection = "FIRST_DYNAMIC"
        private const val secondDynamicSelection = "SECOND_DYNAMIC"
        private const val thirdDynamicSelection = "THIRD_DYNAMIC"
        private const val series = "TV_SERIES"
    }
}