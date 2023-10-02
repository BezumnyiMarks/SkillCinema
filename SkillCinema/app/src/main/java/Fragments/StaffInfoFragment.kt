package Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.skillcinema.App
import DataModels.DBCountry
import DataModels.DBGenre
import com.example.skillcinema.DBViewModel
import DataModels.DetailedInfo
import DataModels.Film
import com.example.skillcinema.HomeViewModel
import DataModels.Item
import Adapters.ListAdapter
import DataModels.MaxFilmInfo
import com.example.skillcinema.R
import com.example.skillcinema.Repository
import com.example.skillcinema.databinding.FragmentStaffInfoBinding
import com.example.skillcinema.utils.Extensions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val STAFF_ID = "staffID"
private const val STAFF_FILMS_ID = "staffFilmsID"
private const val PROFESSION_KEYS = "profesionKeys"
private const val SEX = "sex"
private const val NAME = "name"
private const val IMAGE_URL = "IMAGE_URL"
private const val KINOPOISK_ID = "kinopoiskID"
private const val WATCHED = "Просмотрено"
class StaffInfoFragment : Fragment() {
    private var _binding: FragmentStaffInfoBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    private val dBViewModel: DBViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T{
                val filmInfoDao = (requireActivity().application as App).db.filmInfoDao()
                return DBViewModel(filmInfoDao) as T
            }
        }
    }
    private val bestFilmsAdapter = ListAdapter { item -> onItemClick(item) }
    private val rep = Repository()
    private val extensions = Extensions()
    private var requestScope = CoroutineScope(Dispatchers.IO)
    private var responseScope = CoroutineScope(Dispatchers.IO)
    private var apiKey = ""

    private var staffID: Long? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            staffID = it.getLong(STAFF_ID)
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStaffInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        apiKey = rep.getApiKey(requireActivity())!!
        showData()

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onItemClick(item: Item) {
        val bundle = bundleOf(KINOPOISK_ID to item.kinopoiskID)
        findNavController().navigate(R.id.action_staffInfoFragment_to_filmInfoFragment, bundle)
    }

    private fun showData() {
        binding.bestFilmsRecyclerView.adapter = bestFilmsAdapter
        val collectionWatchedIDList = extensions.convertStringToList(rep.getCollection(requireContext(),
            WATCHED
        ))
        val needToRequestFilmsIDList = mutableListOf<Long>()
        val notRequestedFilmsIDList = mutableListOf<Long>()
        val receivedFilmsIDList = mutableListOf<Long>()
        val detailedFilmInfoList = mutableListOf<DetailedInfo>()
        val maxFilmInfoList = mutableListOf<MaxFilmInfo>()
        val maxFilmInfoIDList = mutableListOf<Long>()
        val unicFilms = mutableListOf<Film>()
        var noFilmsRequested = true
        var dataSaved = false
        var staffFilmsID = ""
        var professionKeys = ""

        viewModel.loadStaffInfo(apiKey, staffID!!.toInt())
        lifecycleScope.launchWhenCreated {
            viewModel.staffInfoLoading.collect { staffInfoLoading ->
                if (!staffInfoLoading) {
                    viewModel.staffInfo.collect { staffInfo ->
                        if (staffInfo.posterURL != null) {
                            Glide.with(this@StaffInfoFragment)
                                .load(android.net.Uri.parse(staffInfo.posterURL))
                                .centerCrop()
                                .into(binding.imageView)
                        }

                        binding.name.text = staffInfo.nameRu ?: staffInfo.nameEn ?: ""
                        binding.profession.text = staffInfo.profession
                        binding.imageView.setOnClickListener {
                            val bundle = bundleOf(IMAGE_URL to staffInfo.posterURL)
                            findNavController().navigate(R.id.action_staffInfoFragment_to_pictureFragment, bundle)
                        }

                        dBViewModel.getMaxFilmInfoList().forEach {
                            maxFilmInfoIDList.add(it.filmInfo.filmID)
                        }

                        if (staffInfo.films != null && staffInfo.films != listOf<Film>()) {
                            var prevFilm: Film? = null
                            staffInfo.films.forEach {
                                if (it.filmID != prevFilm?.filmID)
                                    unicFilms.add(it)
                                prevFilm = it
                            }
                            var filmsCounter = 0
                            unicFilms.forEach { film ->
                                filmsCounter++
                               //Log.d("unicFilmID", film.filmID.toString())
                                if (maxFilmInfoIDList.contains(film.filmID)) {
                                    maxFilmInfoList.add(dBViewModel.getMaxFilmInfoByID(film.filmID))
                                }
                                else {
                                    noFilmsRequested = false
                                    needToRequestFilmsIDList.add(film.filmID)
                                    notRequestedFilmsIDList.add(film.filmID)
                                }
                                if (notRequestedFilmsIDList.isNotEmpty() && filmsCounter == unicFilms.size){
                                    requestScope.launch {
                                        viewModel.detailedInfoLoading.collect { detailedInfoLoading ->
                                            if (!detailedInfoLoading && notRequestedFilmsIDList.isNotEmpty()) {
                                                viewModel.loadDetailedInfo(apiKey, notRequestedFilmsIDList[0].toInt())
                                                //Log.d("notRequested", notRequestedFilmsIDList[0].toString())
                                                notRequestedFilmsIDList.removeAt(0)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        responseScope.launch {
                            var waitingForResponse = true
                            while (waitingForResponse){
                                if (!noFilmsRequested){
                                    if (dataSaved){
                                        receivedFilmsIDList.forEach { maxFilmInfoList.add(dBViewModel.getMaxFilmInfoByID(it)) }
                                        waitingForResponse = false
                                    }
                                } else waitingForResponse = false
                            }

                            viewModel._staffInfoSaving.value = false
                            val ratingsList = mutableListOf<Double>()
                            maxFilmInfoList.forEach {
                                ratingsList.add(it.filmInfo.ratingKinopoisk?:0.0)
                            //Log.d("finalCountries", it.country.toString())
                            //Log.d("finalGenres", it.genre.toString())
                                }

                            val map =  mutableMapOf<Long,Double>()
                            for (i in ratingsList.indices){
                                map[maxFilmInfoList[i].filmInfo.filmID] = ratingsList[i]
                            }

                            val bestFilmsList = mutableListOf<Item>()
                            var counter = 0
                            while (counter != ratingsList.size){
                                val maxRating = map.maxBy { it.value }
                                var maxFilmInfo: MaxFilmInfo? = null
                                maxFilmInfoList.forEach { if (it.filmInfo.filmID == maxRating.key) maxFilmInfo = it }
                                val countries = maxFilmInfo?.let { extensions.getCountriesList(it.country) }
                                val genres = maxFilmInfo?.let { extensions.getGenresList(it.genre) }
                                bestFilmsList.add(
                                    Item(
                                        null,
                                        null,
                                        maxFilmInfo?.filmInfo?.filmID,
                                        null,
                                        null,
                                        maxFilmInfo?.filmInfo?.nameRu,
                                        maxFilmInfo?.filmInfo?.nameEn,
                                        maxFilmInfo?.filmInfo?.nameOriginal,
                                        maxFilmInfo?.filmInfo?.ratingKinopoisk,
                                        maxFilmInfo?.filmInfo?.ratingImdb,
                                        null,
                                        null,
                                        maxFilmInfo?.filmInfo?.posterURL,
                                        null,
                                        null,
                                        null,
                                        countries,
                                        genres,
                                        null,
                                        null,
                                        null
                                    )
                                )
                                //Log.d("map", maxRating.toString())
                                map.remove(maxRating.key)
                                counter++
                                if (counter == 10)
                                    break
                            }
                            lifecycleScope.launchWhenCreated {
                                val filmsAmountText = resources.getQuantityString(R.plurals.films, maxFilmInfoList.size, maxFilmInfoList.size)
                                binding.filmsAmountTextView.text = filmsAmountText

                                bestFilmsList.forEach {
                                    it.watched = collectionWatchedIDList.contains(it.kinopoiskID.toString())
                                }
                                bestFilmsAdapter.submitList(bestFilmsList)
                                binding.bestFilmsAmount.text = bestFilmsList.size.toString()

                                unicFilms.forEach { film ->
                                    staffFilmsID += "${film.filmID},"
                                    professionKeys += "${film.professionKey},"
                                }
                                staffFilmsID = staffFilmsID.replace(".$".toRegex(), "")
                                professionKeys = professionKeys.replace(".$".toRegex(), "")

                                binding.buttonAllFilms.setOnClickListener {
                                    val bundle = bundleOf(
                                        STAFF_FILMS_ID to staffFilmsID,
                                        PROFESSION_KEYS to professionKeys,
                                        NAME to staffInfo.nameRu,
                                        SEX to staffInfo.sex,
                                    )
                                    findNavController().navigate(R.id.action_staffInfoFragment_to_filmographyFragment, bundle)
                                }
                            }
                        }

                        viewModel.detailedInfoLoading.collect{
                            if (!it){
                                viewModel.detailedInfo.collect { detailedInfo ->
                                    if (detailedInfo.kinopoiskID != null) {
                                        detailedFilmInfoList.add(detailedInfo)
                                        receivedFilmsIDList.add(detailedInfo.kinopoiskID)
                                        //Log.d("netReceived", detailedInfo.kinopoiskID.toString())

                                        if (receivedFilmsIDList.containsAll(needToRequestFilmsIDList) && notRequestedFilmsIDList.isEmpty()) {
                                            viewModel._staffInfoSaving.value = true
                                            dBViewModel.dBBusy.collect{ dBBusy ->
                                                if (!dBBusy && detailedFilmInfoList.isNotEmpty()){
                                                    //Log.d("detailedFilmInfo", detailedFilmInfoList[0].kinopoiskID.toString())
                                                    val dBCountryIDList = mutableListOf<Int>()
                                                    val dBGenreIDList = mutableListOf<Int>()
                                                    dBViewModel.getMaxFilmInfoList().forEach { maxFilmInfo ->
                                                        maxFilmInfo.country.forEach { dBCountryIDList.add(it.id) }
                                                        maxFilmInfo.genre.forEach { dBGenreIDList.add(it.id) }
                                                        //Log.d("MaxFilmInfoCountry", maxFilmInfo.country.toString())
                                                        //Log.d("MaxFilmInfoGenre", maxFilmInfo.genre.toString())
                                                    }
                                                    //Log.d("dBCountryIDListSize", dBCountryIDList.size.toString())
                                                    //Log.d("dBGenreIDListSize", dBGenreIDList.size.toString())
                                                    var countries = listOf<DBCountry>()
                                                    var genres = listOf<DBGenre>()
                                                    if (dBCountryIDList.isEmpty() || dBGenreIDList.isEmpty()){
                                                        countries = extensions.getDBCountriesList(detailedFilmInfoList[0].countries!!, detailedFilmInfoList[0].kinopoiskID!!,0)
                                                        genres = extensions.getDBGenresList(detailedFilmInfoList[0].genres!!, detailedFilmInfoList[0].kinopoiskID!!,0)
                                                    }
                                                    else{
                                                        countries = extensions.getDBCountriesList(detailedFilmInfoList[0].countries!!, detailedFilmInfoList[0].kinopoiskID!!, dBCountryIDList.max())
                                                        genres = extensions.getDBGenresList(detailedFilmInfoList[0].genres!!, detailedFilmInfoList[0].kinopoiskID!!, dBViewModel.getDBGenresMaxID())
                                                    }
                                                    //Log.d("countries", countries.toString())
                                                    //Log.d("genres", genres.toString())

                                                    dBViewModel.addMaxFilmInfo(
                                                        extensions.getFilmInfo(detailedFilmInfoList[0]),
                                                        countries, genres
                                                    )
                                                    detailedFilmInfoList.removeAt(0)
                                                    if (detailedFilmInfoList.isEmpty()){
                                                        viewModel._staffInfoSaving.value = false
                                                        dataSaved = true
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}