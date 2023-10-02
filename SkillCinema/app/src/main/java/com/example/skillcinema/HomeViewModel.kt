package com.example.skillcinema

import DataModels.CountryGenreID
import DataModels.DetailedInfo
import DataModels.Film
import DataModels.Item
import DataModels.Items
import DataModels.Series
import DataModels.Staff
import DataModels.StaffInfo
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    val _validatingLoading = MutableStateFlow(true)
    val validatingLoading = _validatingLoading.asStateFlow()

    private val _idLoading = MutableStateFlow(true)
    val idLoading = _idLoading.asStateFlow()


    //HomeFragment loading indicators
    val _premieresLoading = MutableStateFlow(true)
    val premieresLoading = _premieresLoading.asStateFlow()

    val _additionalPremieresLoading = MutableStateFlow(true)
    val additionalPremieresLoading = _additionalPremieresLoading.asStateFlow()

    val _popularLoading = MutableStateFlow(true)
    val popularLoading = _popularLoading.asStateFlow()

    val _firstDynamicLoading = MutableStateFlow(true)
    val firstDynamicLoading = _firstDynamicLoading.asStateFlow()

    val _secondDynamicLoading = MutableStateFlow(true)
    val secondDynamicLoading = _secondDynamicLoading.asStateFlow()

    val _thirdDynamicLoading = MutableStateFlow(true)
    val thirdDynamicLoading = _thirdDynamicLoading.asStateFlow()

    val _bestLoading = MutableStateFlow(true)
    val bestLoading = _bestLoading.asStateFlow()

    val _seriesLoading = MutableStateFlow(true)
    val seriesLoading = _seriesLoading.asStateFlow()

    val _homeSelectionsSaving = MutableStateFlow(true)

    private val _firstHomePartLoading = MutableStateFlow(true)
    private val firstHomePartLoading = combine(
        _premieresLoading,
        _additionalPremieresLoading,
        _popularLoading,
        _firstDynamicLoading,
        _secondDynamicLoading
    ){ premieresLoading, additionalPremieresLoading, popularLoading, firstDynamicLoading, secondDynamicLoading ->
        _firstHomePartLoading.value = !(!premieresLoading && !additionalPremieresLoading && !popularLoading && !firstDynamicLoading && !secondDynamicLoading)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = _firstHomePartLoading.value
    )

    private val _secondHomePartLoading = MutableStateFlow(true)
    private val secondHomePartLoading = combine(
        _thirdDynamicLoading,
        _bestLoading,
        _seriesLoading
    ){ thirdLoading, bestLoading, seriesLoading ->
        _secondHomePartLoading.value = !(!thirdLoading && !bestLoading && !seriesLoading)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = _secondHomePartLoading.value
    )

    private val _homeSelectionsLoading = MutableStateFlow(true)
    private val allHomeLoading = combine(
        _firstHomePartLoading,
        _secondHomePartLoading
    ){ firstLoading, secondLoading ->
        _homeSelectionsLoading.value = !(!firstLoading && !secondLoading)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = _homeSelectionsLoading.value
    )
    val homeSelectionsLoading = _homeSelectionsLoading.asStateFlow()

    private val _homeBusy = MutableStateFlow(true)
    private val homeSelectionsLoadingOrSaving = combine(
        _homeSelectionsLoading,
        _homeSelectionsSaving
    ){ homeSelectionsLoading, homeSelectionsSaving ->
        _homeBusy.value = !(!homeSelectionsLoading && !homeSelectionsSaving)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = _homeBusy.value
    )
    val homeBusy = _homeBusy.asStateFlow()

    //FilmInfoFragment loading indicators
    private val _detailedInfoLoading = MutableStateFlow(false)
    val detailedInfoLoading = _detailedInfoLoading.asStateFlow()

    private val _similarsLoading = MutableStateFlow(true)
    val similarsLoading = _similarsLoading.asStateFlow()

    private val _seasonsLoading = MutableStateFlow(true)
    val seasonsLoading = _seasonsLoading.asStateFlow()

    private val _staffListLoading = MutableStateFlow(true)
    val staffListLoading = _staffListLoading.asStateFlow()

    val _galleryLoading = MutableStateFlow(false)
    val galleryLoading = _galleryLoading.asStateFlow()


    //StaffInfoFragment loading indicator
    private val _staffInfoLoading = MutableStateFlow(true)
    val staffInfoLoading = _staffInfoLoading.asStateFlow()

    val _staffInfoSaving = MutableStateFlow(true)
    val staffInfoSaving = _staffInfoSaving.asStateFlow()

    private val _staffInfoBusy = MutableStateFlow(true)
    private val staffInfoLoadingOrSaving = combine(
        _staffInfoLoading,
        _staffInfoSaving
    ){ staffInfoLoading, staffInfoSaving ->
        _staffInfoBusy.value = !(!staffInfoLoading && !staffInfoSaving)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = _staffInfoBusy.value
    )
    val staffInfoBusy = _staffInfoBusy.asStateFlow()


    //SearchFragment loading indicator
    val _filteredFilmsLoading = MutableStateFlow(true)
    val filteredFilmsLoading = _filteredFilmsLoading.asStateFlow()


    //RegistrationFragment loading indicator
    val _countryIDSaving = MutableStateFlow(false)
    val _genreIDSaving = MutableStateFlow(false)

    val _countryGenreIDSaving = MutableStateFlow(false)
    private val countryGenreIDSavingCombine = combine(
        _countryIDSaving,
        _genreIDSaving
    ){ countryIDSaving, genreIDSaving ->
        _countryGenreIDSaving.value = !(!countryIDSaving && !genreIDSaving)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = _countryGenreIDSaving.value
    )
    val countryGenreIDSaving = _countryGenreIDSaving.asStateFlow()

//////////////////////////////////////////////////////////////////////////////////////////////////
    private val _staffList = MutableStateFlow<List<Staff>>(listOf())
    val staffList = _staffList.asStateFlow()

    val _detailedInfo = MutableStateFlow<DetailedInfo>(DetailedInfo(null, null, null, null, null, null,null, null, null,null, null, null,null, null, null,null, null, null, null, null, null, null,null, null, null,null, null, null,null, null, null,null, null, null,null, null, null,null, null, null,null, null, null,null))
    val detailedInfo = _detailedInfo.asStateFlow()

    private val _similars = MutableStateFlow<Items>(Items(0, listOf()))
    val similars = _similars.asStateFlow()

    private val _seasons = MutableStateFlow<Series>(Series(0, listOf()))
    val seasons = _seasons.asStateFlow()

    private val _staffInfo = MutableStateFlow<StaffInfo>(StaffInfo(0L, "", "", "", "", "", 0L, "", "", 0L, "", "", listOf(), 0L, "", listOf(), listOf()))
    val staffInfo = _staffInfo.asStateFlow()

    private val _premieres = MutableStateFlow<Items>(Items(0, listOf()))
    val premieres = _premieres.asStateFlow()

    private val _additionalPremieres = MutableStateFlow<Items>(Items(0, listOf()))
    val additionalPremieres = _additionalPremieres.asStateFlow()

    private val _countryGenreID = MutableStateFlow(CountryGenreID(listOf(), listOf()))
    val countryGenreID = _countryGenreID.asStateFlow()

    var popularFilms = flowOf<PagingData<Film>>()
    var bestFilms = flowOf<PagingData<Film>>()
    var firstDynamicSelection = flowOf<PagingData<Item>>()
    var secondDynamicSelection = flowOf<PagingData<Item>>()
    var thirdDynamicSelection = flowOf<PagingData<Item>>()
    var tvSeries = flowOf<PagingData<Item>>()
    var gallery = flowOf<PagingData<Item>>()
    var dataValidatingSelection = flowOf<PagingData<Item>>()
    var filteredFilms = flowOf<PagingData<Item>>()

    private fun getMonthStr(monthInt: Int): String{
        return when(monthInt){
            0 -> "january"
            1 -> "february"
            2 -> "march"
            3 -> "april"
            4 -> "may"
            5 -> "june"
            6 -> "july"
            7 -> "august"
            8 -> "september"
            9 -> "october"
            10 -> "november"
            else -> "december"
        }
    }

    fun loadPremieres(apikey: String, year: Int, month: Int, additionalPremieresNeeded: Boolean){
        _premieresLoading.value = true
        viewModelScope.launch {
            kotlin.runCatching {
                Repository.RetrofitInstance.searchMovies.getPremieres(apikey, year, getMonthStr(month))
            }.fold(
                onSuccess = {
                    if (additionalPremieresNeeded){
                        _additionalPremieresLoading.value = true
                        kotlin.runCatching {
                            Repository.RetrofitInstance.searchMovies.getPremieres(apikey, year, getMonthStr(month + 1))
                        }.fold(
                            onSuccess = {
                                _additionalPremieres.value = it
                                delay(1000)
                                _additionalPremieresLoading.value = false
                            },
                            onFailure = {
                                Log.d("HomeViewModelLoadAdditionalPremiers", it.message ?: "")
                                _additionalPremieresLoading.value = false
                            }
                        )
                    }
                    else _additionalPremieresLoading.value = false
                    _premieres.value = it
                    _premieresLoading.value = false
                },
                onFailure = {
                    Log.d("HomeViewModelLoadPremiers", it.message ?: "")
                    _premieresLoading.value = false
                }
            )
        }
    }

    fun loadSimilars(apikey: String, filmID: Int){
        _similarsLoading.value = true
        viewModelScope.launch {
            kotlin.runCatching {
                Repository.RetrofitInstance.searchMovies.getSimilars(apikey, filmID)
            }.fold(
                onSuccess = {_similars.value = it},
                onFailure = { Log.d("HomeViewModelLoadSimilars", it.message ?:"")}
            )
            _similarsLoading.value = false
        }
    }

    fun loadSeasons(apikey: String, seriesID: Int){
        _seasonsLoading.value = true
        viewModelScope.launch {
            kotlin.runCatching {
                Repository.RetrofitInstance.searchMovies.getSeasons(apikey, seriesID)
            }.fold(
                onSuccess = {_seasons.value = it},
                onFailure = { Log.d("HomeViewModelLoadSeasons", it.message ?:"")}
            )
            _seasonsLoading.value = false
        }
    }

    fun loadStaff(apikey: String, filmID: Int){
        _staffListLoading.value = true
        viewModelScope.launch {
            kotlin.runCatching {
                Repository.RetrofitInstance.searchMovies.getStaffList(apikey, filmID)
            }.fold(
                onSuccess = {_staffList.value = it},
                onFailure = {Log.d("HomeViewModelLoadStaff", it.message ?:"")}
            )
            _staffListLoading.value = false
        }
    }

    fun loadStaffInfo(apikey: String, staffID: Int){
        _staffInfoLoading.value = true
        viewModelScope.launch {
            kotlin.runCatching {
                Repository.RetrofitInstance.searchMovies.getStaffInfo(apikey, staffID)
            }.fold(
                onSuccess = {_staffInfo.value = it},
                onFailure = {Log.d("HomeViewModelLoadStaffInfo", it.message ?:"")}
            )
            _staffInfoLoading.value = false
        }
    }

    fun loadDetailedInfo(apikey: String, filmID: Int){
        _detailedInfoLoading.value = true
        viewModelScope.launch {
            kotlin.runCatching {
                Repository.RetrofitInstance.searchMovies.getDetailedInfo(apikey, filmID)
            }.fold(
                onSuccess = {_detailedInfo.value = it},
                onFailure = {Log.d("HomeViewModelLoadDetailedInfo", it.message ?:"")
                    if (it.message == "HTTP 404 ")
                        _detailedInfo.value = DetailedInfo(null, null, null, null, null, null,null, null, null,null, null, null,null, null, null,null, null, null, null, null, null, null,null, null, null,null, null, null,null, null, null,null, null, null,null, null, null,null, null, null,null, null, null,null)
                }
            )
            _detailedInfoLoading.value = false
        }
    }

    fun loadCountryGenreID(apikey: String){
        _countryIDSaving.value = true
        _genreIDSaving.value = true
        _idLoading.value = true
        viewModelScope.launch {
            kotlin.runCatching {
                Repository.RetrofitInstance.searchMovies.getCountryGenreID(apikey)
            }.fold(
                onSuccess = {_countryGenreID.value = it},
                onFailure = {
                    Log.d("HomeViewModelLoadID", it.message ?:"")
                    _idLoading.value = false
                }
            )
            _idLoading.value = false
        }
    }
}