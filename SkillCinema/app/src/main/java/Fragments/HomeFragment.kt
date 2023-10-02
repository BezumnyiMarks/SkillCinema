package Fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.example.skillcinema.App
import DataModels.Country
import DataModels.DBCountry
import DataModels.DBGenre
import com.example.skillcinema.DBViewModel
import DataModels.DynamicSelectionRequestAttributes
import DataModels.Film
import DataModels.FilmInfo
import Adapters.FilmsPagingAdapter
import PagingSources.FilmsPagingSource
import DataModels.Genre
import com.example.skillcinema.HomeViewModel
import DataModels.Item
import Adapters.ItemsPagingAdapter
import PagingSources.ItemsPagingSource
import Adapters.ListAdapter
import DataModels.MaxFilmInfo
import com.example.skillcinema.R
import com.example.skillcinema.Repository
import DataModels.SearchSettings
import androidx.navigation.ui.navigateUp
import com.example.skillcinema.databinding.FragmentHomeBinding
import com.example.skillcinema.utils.Extensions
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onStart
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.*


private const val ADAPTER_NUM = "adapterNum"
private const val KINOPOISK_ID = "kinopoiskID"
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
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
    private val premieresAdapter = ListAdapter { item -> onItemClick(item) }
    private val popularFilmsAdapter = FilmsPagingAdapter { film -> onFilmClick(film) }
    private val firstDynamicSelectionAdapter = ItemsPagingAdapter { item -> onItemClick(item) }
    private val secondDynamicSelectionAdapter = ItemsPagingAdapter { item -> onItemClick(item) }
    private val thirdDynamicSelectionAdapter = ItemsPagingAdapter { item -> onItemClick(item) }
    private val bestFilmsAdapter = FilmsPagingAdapter { film -> onFilmClick(film) }
    private val tvSeriesAdapter = ItemsPagingAdapter { item -> onItemClick(item) }
    private val dataValidatingAdapter = ItemsPagingAdapter {}
    private var validatingCountryID = 0L
    private var validatingGenreID = 0L
    private var validatingData = ""
    private val rep = Repository()
    private val extensions = Extensions()
    private var items = mutableListOf<Item>()
    private var films = mutableListOf<Film>()
    private var apiKey = ""

    private var dynamicSelectionsRequestAttributes: MutableList<DynamicSelectionRequestAttributes> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //deleteDeprecatedFilmInfo()

        apiKey = rep.getApiKey(requireActivity())!!
        setAdapters()
        getPagesUpdate()
        setButtonsListeners()

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.bottomNav.setOnItemSelectedListener {
            when(it.itemId){
                R.id.search -> findNavController().navigate(R.id.searchFragment)
                R.id.profile -> findNavController().navigate(R.id.profileFragment)
            }
            true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onResume() {
        super.onResume()

        binding.bottomNav.menu.get(0).isChecked = true
        lifecycleScope.launchWhenCreated {
            dBViewModel.dBSetupInProcess.collect{ dBSetupInProcess ->
                if (!dBSetupInProcess){
                    showSelections()
                    this.cancel()
                }
                else if (rep.getDBDataRelevanceState(requireActivity())){
                    viewModel._homeSelectionsSaving.value = false
                    showSelections()
                    this.cancel()
                }
                else{
                    dBViewModel.addSearchSettings(SearchSettings(1, 33,13,"RATING", "FILM", 1, 10, 1917, 1991, false))
                    dBViewModel.deleteImages(dBViewModel.getAllImages())
                    dBViewModel.deleteGalleryTabs(dBViewModel.getAllGalleryTabs())
                    dBViewModel.deleteMaxFilmSelectionItemInfo(dBViewModel.getMaxFilmSelectionItemInfoList())
                }
            }
        }

        //loadAllowableRequestAttributes()
    }

    private fun onItemClick(item: Item) {
        val bundle = bundleOf(KINOPOISK_ID to item.kinopoiskID)
        findNavController().navigate(R.id.action_homeFragment_to_filmInfoFragment, bundle)
    }

    private fun onFilmClick(film: Film) {
        val bundle = bundleOf(KINOPOISK_ID to film.filmID)
        findNavController().navigate(R.id.action_homeFragment_to_filmInfoFragment, bundle)
    }

    private fun setAdapters() {
        binding.premieres.binding.recyclerView.adapter = premieresAdapter
        binding.popular.binding.recyclerView.adapter = popularFilmsAdapter
        binding.dynamic1.binding.recyclerView.adapter = firstDynamicSelectionAdapter
        binding.dynamic2.binding.recyclerView.adapter = secondDynamicSelectionAdapter
        binding.dynamic3.binding.recyclerView.adapter = thirdDynamicSelectionAdapter
        binding.best.binding.recyclerView.adapter = bestFilmsAdapter
        binding.tvSeries.binding.recyclerView.adapter = tvSeriesAdapter
        binding.dataValidatingRecyclerView.adapter = dataValidatingAdapter
    }

    private fun getPagesUpdate() {
        lifecycleScope.launchWhenCreated {
            viewModel.homeSelectionsLoading.collect{ homeSelectionsLoading ->
                if (!homeSelectionsLoading && dBViewModel.getMaxFilmSelectionItemInfoList().isEmpty()){
                    saveSelection(items, null)
                }
            }
        }

        popularFilmsAdapter.addOnPagesUpdatedListener {
            lifecycleScope.launchWhenCreated {
                delay(1000)
                val snapshot = popularFilmsAdapter.snapshot()
                Log.d("snapshotPOPULAR", snapshot.items.size.toString())
                snapshot.forEach {
                    it!!.selectionAttachment = binding.popular.binding.selectionName.text.toString()
                    if (films.contains(it))
                        films.remove(it)
                }
                snapshot.forEach {
                    it!!.selectionAttachment = binding.popular.binding.selectionName.text.toString()
                    films.add(it)
                }
                viewModel._popularLoading.value = false
                this.cancel()
            }
            if (popularFilmsAdapter.itemCount > 20)
                binding.popular.binding.buttonAll.isVisible = true
        }

        firstDynamicSelectionAdapter.addOnPagesUpdatedListener {
            lifecycleScope.launchWhenCreated {
                delay(1000)
                val snapshot = firstDynamicSelectionAdapter.snapshot()
                snapshot.forEach {
                    it!!.selectionAttachment = binding.dynamic1.binding.selectionName.text.toString()
                    if (items.contains(it))
                        items.remove(it)
                }
                snapshot.forEach {
                    it!!.selectionAttachment = binding.dynamic1.binding.selectionName.text.toString()
                    items.add(it)
                }
                viewModel._firstDynamicLoading.value = false
                this.cancel()
            }
            if (firstDynamicSelectionAdapter.itemCount > 20)
                binding.dynamic1.binding.buttonAll.isVisible = true
        }

        secondDynamicSelectionAdapter.addOnPagesUpdatedListener {
            lifecycleScope.launchWhenCreated {
                delay(1000)
                val snapshot = secondDynamicSelectionAdapter.snapshot()
                snapshot.forEach {
                    it!!.selectionAttachment = binding.dynamic2.binding.selectionName.text.toString()
                    if (items.contains(it))
                        items.remove(it)
                }
                snapshot.forEach {
                    it!!.selectionAttachment = binding.dynamic2.binding.selectionName.text.toString()
                    items.add(it)
                }
                viewModel._secondDynamicLoading.value = false
                this.cancel()
            }
            if (secondDynamicSelectionAdapter.itemCount > 20)
                binding.dynamic2.binding.buttonAll.isVisible = true
        }

        thirdDynamicSelectionAdapter.addOnPagesUpdatedListener {
            lifecycleScope.launchWhenCreated {
                delay(1000)
                val snapshot = thirdDynamicSelectionAdapter.snapshot()
                snapshot.forEach {
                    it!!.selectionAttachment = binding.dynamic3.binding.selectionName.text.toString()
                    if (items.contains(it))
                        items.remove(it)
                }
                snapshot.forEach {
                    it!!.selectionAttachment = binding.dynamic3.binding.selectionName.text.toString()
                    items.add(it)
                }
                viewModel._thirdDynamicLoading.value = false
                this.cancel()
            }
            if (thirdDynamicSelectionAdapter.itemCount > 20)
                binding.dynamic3.binding.buttonAll.isVisible = true
        }

        bestFilmsAdapter.addOnPagesUpdatedListener {
            lifecycleScope.launchWhenCreated {
                delay(1000)
                val snapshot = bestFilmsAdapter.snapshot()
                snapshot.forEach {
                    it!!.selectionAttachment = binding.best.binding.selectionName.text.toString()
                    if (films.contains(it))
                        films.remove(it)
                }
                snapshot.forEach {
                    it!!.selectionAttachment = binding.best.binding.selectionName.text.toString()
                    films.add(it)
                }
                viewModel._bestLoading.value = false
                this.cancel()
            }
            if (bestFilmsAdapter.itemCount > 20)
                binding.best.binding.buttonAll.isVisible = true
        }

        tvSeriesAdapter.addOnPagesUpdatedListener {
            lifecycleScope.launchWhenCreated {
                val snapshot = tvSeriesAdapter.snapshot()
                snapshot.forEach {
                    it!!.selectionAttachment = binding.tvSeries.binding.selectionName.text.toString()
                    if (items.contains(it))
                        items.remove(it)
                }
                snapshot.forEach {
                    it!!.selectionAttachment = binding.tvSeries.binding.selectionName.text.toString()
                    items.add(it)
                }
                viewModel._seriesLoading.value = false
                this.cancel()
            }
            if (tvSeriesAdapter.itemCount > 20)
                binding.tvSeries.binding.buttonAll.isVisible = true
        }
    }

    private fun saveSelection(itemsList: List<Item>?, filmsList: List<Film>?){
        var i = 0
        var coroutineStarted = false
        lifecycleScope.launchWhenCreated {
            var maxID = dBViewModel.getFilmSelectionItemInfoMaxID()
            val mainCoroutine = this
            if (!itemsList.isNullOrEmpty()) {
                dBViewModel.dBBusy.collect { dBBusy ->
                    if (!dBBusy && i <= itemsList.size - 1) {
                        dBViewModel.addMaxFilmSelectionItemInfo(
                            extensions.getFilmSelectionItemInfo(
                                itemsList[i],
                                null,
                                maxID + 1
                            ),
                            extensions.getDBGenresList(
                                itemsList[i].genres!!,
                                (maxID + 1).toLong(),
                                dBViewModel.getDBGenresMaxID()
                            )
                        )
                        i++
                        maxID++
                    }
                    if (i == itemsList.lastIndex && !coroutineStarted){
                        coroutineStarted = true
                        lifecycleScope.launchWhenCreated {
                            dBViewModel.allMaxFilmInfo.collect { allMaxFilmInfo ->
                                if (allMaxFilmInfo.isNotEmpty()) {
                                    if (allMaxFilmInfo[allMaxFilmInfo.lastIndex].filmInfo.filmID == itemsList[itemsList.lastIndex].kinopoiskID!!.toInt()) {
                                        saveSelection(null, films)
                                        mainCoroutine.cancel()
                                        this.cancel()
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (!filmsList.isNullOrEmpty()) {
                dBViewModel.dBBusy.collect { dBBusy ->
                    if (!dBBusy && i <= filmsList.size - 1) {
                        dBViewModel.addMaxFilmSelectionItemInfo(
                            extensions.getFilmSelectionItemInfo(
                                null,
                                filmsList[i],
                                maxID + 1
                            ),
                            extensions.getDBGenresList(
                                filmsList[i].genres!!,
                                (maxID + 1).toLong(),
                                dBViewModel.getDBGenresMaxID()
                            )
                        )
                        i++
                        maxID++
                    }
                    if (i == filmsList.lastIndex && !coroutineStarted){
                        coroutineStarted = true
                        lifecycleScope.launchWhenCreated {
                            dBViewModel.allMaxFilmInfo.collect{ allMaxFilmInfo ->
                                if (allMaxFilmInfo.isNotEmpty()){
                                    if (allMaxFilmInfo[allMaxFilmInfo.lastIndex].filmInfo.filmID == filmsList[filmsList.lastIndex].filmID.toInt()){
                                        viewModel._homeSelectionsSaving.value = false
                                        rep.saveDBDataRelevanceState(requireActivity(), true)
                                        mainCoroutine.cancel()
                                        this.cancel()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setButtonsListeners() {
        binding.premieres.binding.buttonAll.setOnClickListener {
            val bundle = bundleOf(ADAPTER_NUM to 1)
            findNavController().navigate(R.id.action_homeFragment_to_allItemsFragment, bundle)
        }

        binding.popular.binding.buttonAll.setOnClickListener {
            val bundle = bundleOf(ADAPTER_NUM to 2)
            findNavController().navigate(R.id.action_homeFragment_to_allItemsFragment, bundle)
        }

        binding.dynamic1.binding.buttonAll.setOnClickListener {
            val bundle = bundleOf(ADAPTER_NUM to 3)
            findNavController().navigate(R.id.action_homeFragment_to_allItemsFragment, bundle)
        }

        binding.dynamic2.binding.buttonAll.setOnClickListener {
            val bundle = bundleOf(ADAPTER_NUM to 4)
            findNavController().navigate(R.id.action_homeFragment_to_allItemsFragment, bundle)
        }

        binding.dynamic3.binding.buttonAll.setOnClickListener {
            val bundle = bundleOf(ADAPTER_NUM to 5)
            findNavController().navigate(R.id.action_homeFragment_to_allItemsFragment, bundle)
        }

        binding.best.binding.buttonAll.setOnClickListener {
            val bundle = bundleOf(ADAPTER_NUM to 6)
            findNavController().navigate(R.id.action_homeFragment_to_allItemsFragment, bundle)
        }

        binding.tvSeries.binding.buttonAll.setOnClickListener {
            val bundle = bundleOf(ADAPTER_NUM to 7)
            findNavController().navigate(R.id.action_homeFragment_to_allItemsFragment, bundle)
        }
    }

    private fun showSelections() {
        showPremieres()
        loadSelections()

        lifecycleScope.launchWhenCreated {
            viewModel.popularFilms.collect {
                popularFilmsAdapter.submitData(it)
            }
            this.cancel()
        }

        lifecycleScope.launchWhenCreated {
            viewModel.bestFilms.collect {
                bestFilmsAdapter.submitData(it)
            }
            this.cancel()
        }

        lifecycleScope.launchWhenCreated {
            viewModel.firstDynamicSelection.collect {
                firstDynamicSelectionAdapter.submitData(it)
            }
            this.cancel()
        }

        lifecycleScope.launchWhenCreated {
            viewModel.secondDynamicSelection.collect {
                secondDynamicSelectionAdapter.submitData(it)
            }
            this.cancel()
        }

        lifecycleScope.launchWhenCreated {
            viewModel.thirdDynamicSelection.collect {
                thirdDynamicSelectionAdapter.submitData(it)
            }
            this.cancel()
        }

        lifecycleScope.launchWhenCreated {
            viewModel.tvSeries.collect {
                tvSeriesAdapter.submitData(it)
            }
            this.cancel()
        }
    }

    private fun showPremieres() {
        val twoWeeksPremieresList = mutableListOf<Item>()
        val extendedPremieresList = mutableListOf<Item>()
        val collectionWatchedIDList = extensions.convertStringToList(rep.getCollection(requireContext(), WATCHED))
        val additionalPremieresNeeded = checkAdditionalPremieresNeed()
        var dBIsEmpty = false

        lifecycleScope.launchWhenCreated {
            val dBPremieresList = dBViewModel.getMaxFilmSelectionItemInfoListBySelectionAttachment(binding.premieres.binding.selectionName.text.toString())
            Log.d("dBPremieresList", dBPremieresList.toString())
            dBIsEmpty = dBPremieresList.isEmpty()
            Log.d("dBIsEmpty", dBIsEmpty.toString())
            if (dBIsEmpty){
                Log.d("loadPremieres", "loadPremieres")
                viewModel.loadPremieres(
                    apiKey,
                    Calendar.getInstance().get(Calendar.YEAR),
                    Calendar.getInstance().get(Calendar.MONTH),
                    additionalPremieresNeeded
                )

                viewModel.premieresLoading.collect { premieresLoading ->
                    if (!premieresLoading) {
                        viewModel.premieres.collect { premieres ->
                            if (!additionalPremieresNeeded){
                                premieres.items.forEach {
                                    if (it.premiereRu != null) {
                                        if (twoWeeksCheck(it))
                                            twoWeeksPremieresList.add(it)
                                    }
                                }
                                if (twoWeeksPremieresList.size > 20)
                                    binding.premieres.binding.buttonAll.isVisible = true

                                twoWeeksPremieresList.forEach {
                                    it.watched = collectionWatchedIDList.contains(it.kinopoiskID.toString())
                                    it.selectionAttachment = binding.premieres.binding.selectionName.text.toString()
                                    items.add(it)
                                }
                                Log.d("premieres", twoWeeksPremieresList.size.toString())
                                premieresAdapter.submitList(twoWeeksPremieresList)
                            }
                            else premieres.items.forEach { extendedPremieresList.add(it) }
                            this.cancel()
                        }
                    }
                }
            }
            else{
                Log.d("DBPremieres", "DBPremieres")
                val premieresList = extensions.getItemsList(dBPremieresList)
                Log.d("premieresListDB", premieresList.size.toString())
                premieresList.forEach {
                    it.watched = collectionWatchedIDList.contains(it.kinopoiskID.toString())
                }
                viewModel._premieresLoading.value = false
                viewModel._additionalPremieresLoading.value = false
                premieresAdapter.submitList(premieresList)
            }
        }

        lifecycleScope.launchWhenCreated {
            viewModel.premieresLoading.collect{ premieresLoading ->
                if (!premieresLoading && additionalPremieresNeeded && dBIsEmpty){
                    delay(100)
                    viewModel.additionalPremieresLoading.collect { additionalPremieresLoading ->
                        if (!additionalPremieresLoading){
                            viewModel.additionalPremieres.collect { additionalPremieres ->
                                additionalPremieres.items.forEach { extendedPremieresList.add(it) }
                                extendedPremieresList.forEach {
                                    if (it.premiereRu != null) {
                                        if (twoWeeksCheck(it))
                                            twoWeeksPremieresList.add(it)
                                    }
                                }
                                if (twoWeeksPremieresList.size > 20)
                                    binding.premieres.binding.buttonAll.isVisible = true

                                twoWeeksPremieresList.forEach {
                                    it.watched = collectionWatchedIDList.contains(it.kinopoiskID.toString())
                                    it.selectionAttachment = binding.premieres.binding.selectionName.text.toString()
                                    items.add(it)
                                }
                                Log.d("twoWeeksPremieresList", twoWeeksPremieresList.toString())
                                premieresAdapter.submitList(twoWeeksPremieresList)
                                this.cancel()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun loadSelections() {
        lifecycleScope.launchWhenCreated {
            binding.popular.binding.selectionName.text = "Популярное"
            val popularFilmsPSF = FilmsPagingSource(
                apiKey,
                top100Popular,
                extensions.convertStringToList(rep.getCollection(requireActivity(), WATCHED)),
                viewModel,
                dBViewModel,
                binding.popular.binding.selectionName.text.toString()
            )
            popularFilmsPSF.netLoadingAllowed = false
            viewModel.popularFilms = Pager(
                config = PagingConfig(pageSize = 20),
                pagingSourceFactory = { popularFilmsPSF }
            ).flow.cachedIn(lifecycleScope)
                .onStart { viewModel._popularLoading.value = true }


            binding.best.binding.selectionName.text = "Топ-250"
            val bestFilmsPSF = FilmsPagingSource(
                apiKey,
                top250Best,
                extensions.convertStringToList(rep.getCollection(requireActivity(), WATCHED)),
                viewModel,
                dBViewModel,
                binding.best.binding.selectionName.text.toString()
            )
            bestFilmsPSF.netLoadingAllowed = false
            viewModel.bestFilms = Pager(
                config = PagingConfig(pageSize = 20),
                pagingSourceFactory = { bestFilmsPSF }
            ).flow.cachedIn(lifecycleScope)
                .onStart { viewModel._bestLoading.value = true; delay(300) }


            binding.dynamic1.binding.selectionName.text = extensions.getDynamicSelectionName(1, requireActivity())
            val firstDynamicRequestAttributes = extensions.getDynamicSelectionRequestAttributes(1, requireActivity())
            val firstDynamicPSF = ItemsPagingSource(
                apiKey,
                firstDynamicRequestAttributes,
                extensions.convertStringToList(rep.getCollection(requireActivity(), WATCHED)),
                false,
                SearchSettings(0, 0, 0, "", "", 0, 0, 0, 0, false),
                "",
                viewModel,
                dBViewModel,
                firstDynamicSelection,
                binding.dynamic1.binding.selectionName.text.toString()
            )
            firstDynamicPSF.netLoadingAllowed = false
            viewModel.firstDynamicSelection = Pager(
                config = PagingConfig(pageSize = 20),
                pagingSourceFactory = { firstDynamicPSF }
            ).flow.cachedIn(lifecycleScope)
                .onStart { viewModel._firstDynamicLoading.value = true; delay(400) }


            binding.dynamic2.binding.selectionName.text = extensions.getDynamicSelectionName(2, requireActivity())
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
                binding.dynamic2.binding.selectionName.text.toString()
            )
            secondDynamicPSF.netLoadingAllowed = false
            viewModel.secondDynamicSelection = Pager(
                config = PagingConfig(pageSize = 20),
                pagingSourceFactory = { secondDynamicPSF }
            ).flow.cachedIn(lifecycleScope)
                .onStart { viewModel._secondDynamicLoading.value = true; delay(500) }


            binding.dynamic3.binding.selectionName.text = extensions.getDynamicSelectionName(3, requireActivity())
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
                binding.dynamic3.binding.selectionName.text.toString()
            )
            thirdDynamicPSF.netLoadingAllowed = false
            viewModel.thirdDynamicSelection = Pager(
                config = PagingConfig(pageSize = 20),
                pagingSourceFactory = { thirdDynamicPSF }
            ).flow.cachedIn(lifecycleScope)
                .onStart { viewModel._thirdDynamicLoading.value = true; delay(600) }


            binding.tvSeries.binding.selectionName.text = "Сериалы"
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
                binding.tvSeries.binding.selectionName.text.toString()
            )
            tvSeriesPSF.netLoadingAllowed = false
            viewModel.tvSeries = Pager(
                config = PagingConfig(pageSize = 20),
                pagingSourceFactory = { tvSeriesPSF }
            ).flow.cachedIn(lifecycleScope)
                .onStart { viewModel._seriesLoading.value = true; delay(700) }
        }
    }

    private fun twoWeeksCheck(item: Item): Boolean{
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val premiereDate = item.premiereRu?.let { formatter.parse(it) }
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = premiereDate!!.time
        val currentDateLocal = LocalDate.now()
        val premiereDateLocal = LocalDate.of(
            calendar.get(Calendar.YEAR),
            (calendar.get(Calendar.MONTH) + 1),
            calendar.get(Calendar.DAY_OF_MONTH))
        return ChronoUnit.DAYS.between(currentDateLocal, premiereDateLocal) in 0..14
    }

    private fun checkAdditionalPremieresNeed(): Boolean{
        val calendar = Calendar.getInstance()
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        val currentMonthLastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        return (currentMonthLastDay - currentDay) < 14
    }

    private fun deleteDeprecatedFilmInfo(){
        lifecycleScope.launchWhenCreated {
            val deprecatedMaxFilmInfoList = mutableListOf<MaxFilmInfo>()
            dBViewModel.getMaxFilmInfoList().forEach {
                if (Calendar.getInstance().timeInMillis >= it.filmInfo.updateTimeInMillis + 604800000)
                    deprecatedMaxFilmInfoList.add(it)
            }
            if (!deprecatedMaxFilmInfoList.isEmpty()){
                val deprecatedFilmInfoList = mutableListOf<FilmInfo>()
                val deprecatedCountriesList = mutableListOf<DBCountry>()
                val deprecatedGenresList = mutableListOf<DBGenre>()
                deprecatedMaxFilmInfoList.forEach {  maxFilmInfo ->
                    deprecatedFilmInfoList.add(maxFilmInfo.filmInfo)
                    maxFilmInfo.country.forEach { deprecatedCountriesList.add(it) }
                    maxFilmInfo.genre.forEach { deprecatedGenresList.add(it) }
                }
                dBViewModel.deleteDeprecatedMaxFilmInfo(deprecatedFilmInfoList, deprecatedCountriesList, deprecatedGenresList)
            }
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//Это для инициализации списка допустимых параметров загрузки динамических подборок.
    private fun loadAllowableRequestAttributes(){
        rep.getApiKey(requireActivity())?.let { viewModel.loadCountryGenreID(it) }
        lifecycleScope.launchWhenCreated {
            viewModel.idLoading.collect { idLoading ->
                if (!idLoading) {
                    lifecycleScope.launchWhenCreated {
                        viewModel.countryGenreID.collect { countryGenere ->
                            val countries = mutableListOf<Country>()
                            val genres = mutableListOf<Genre>()

                            for (i in 74..countryGenere.countries.lastIndex)
                                countries.add(countryGenere.countries[i])
                            for (i in 30..countryGenere.genres.lastIndex)
                                genres.add(countryGenere.genres[i])

                            countries.forEach { country ->
                                genres.forEach { genre ->
                                    validatingCountryID = country.id!!
                                    validatingGenreID = genre.id!!

                                    viewModel.dataValidatingSelection = Pager(
                                        config = PagingConfig(pageSize = 20, prefetchDistance = 0),
                                        pagingSourceFactory = {
                                            ItemsPagingSource(
                                                apiKey,
                                                DynamicSelectionRequestAttributes(validatingCountryID, validatingGenreID, "FILM"),
                                                listOf(),
                                                false,
                                                SearchSettings(0,0,0,"","",0,0,0,0, false),
                                                "",
                                                viewModel,
                                                dBViewModel,
                                                validating,
                                               ""
                                            )
                                        }
                                    ).flow.cachedIn(lifecycleScope)

                                    lifecycleScope.launchWhenCreated {
                                        viewModel.dataValidatingSelection.collect {
                                            dataValidatingAdapter.submitData(it)
                                        }
                                    }
                                    delay(3000)
                                }
                            }
                        }
                    }
                }
            }
        }
        lifecycleScope.launchWhenCreated {
            viewModel.validatingLoading.collect{
                if (!it)
                    showAllowableRequestAttributes()
            }
        }
        dataValidatingAdapter.addOnPagesUpdatedListener {
            if (dataValidatingAdapter.itemCount >= 10){
                validatingData += "$validatingCountryID $validatingGenreID,"
                Log.d("ValidatingData", validatingData)
            }
        }
    }

    private fun showAllowableRequestAttributes(){
        if (validatingData[validatingData.lastIndex] == ',')
            validatingData = validatingData.replace(".$".toRegex(), "")

        var validatingDataList = validatingData.split(",")
        validatingDataList = validatingDataList.toSet().toList()
        validatingData = ""

        for (i in validatingDataList.indices){
            validatingData += "${validatingDataList[i]},"
        }
        validatingData = validatingData.replace(".$".toRegex(), "")
        Log.d("ValidatingDataFull", validatingData)
        Log.d("ValidatingCountryID", validatingCountryID.toString())
        Log.d("ValidatingGenreID", validatingGenreID.toString())
    }

    private companion object {
        private const val top100Popular = "top_100_popular_films"
        private const val top250Best = "top_250_best_films"
        private const val firstDynamicSelection = "FIRST_DYNAMIC"
        private const val secondDynamicSelection = "SECOND_DYNAMIC"
        private const val thirdDynamicSelection = "THIRD_DYNAMIC"
        private const val series = "TV_SERIES"
        private const val validating = "VALIDATING"
        private const val WATCHED = "Просмотрено"
    }
}
