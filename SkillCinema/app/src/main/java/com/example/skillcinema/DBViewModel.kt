package com.example.skillcinema

import DataModels.CountryID
import DataModels.DBCountry
import DataModels.DBGenre
import DataModels.DBImage
import DataModels.FilmInfo
import DataModels.FilmSelectionItemInfo
import DataModels.GalleryTab
import DataModels.GenreID
import DataModels.MaxFilmInfo
import DataModels.MaxFilmSelectionItemInfo
import DataModels.SearchSettings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DBViewModel(private val filmInfoDao: FilmInfoDao): ViewModel() {

    val allMaxFilmInfo = this.filmInfoDao.getMaxFilmSelectionItemInfoFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    val _dBBusy = MutableStateFlow(false)
    val dBBusy = _dBBusy.asStateFlow()

    val _dBSetupInProcess = MutableStateFlow(true)
    val dBSetupInProcess = _dBSetupInProcess.asStateFlow()

    fun addMaxFilmInfo(filmInfo: FilmInfo, countries: List<DBCountry>?, genres: List<DBGenre>?){
        viewModelScope.launch {
            _dBBusy.value = true
            filmInfoDao.insertFilmInfo(filmInfo)
            if (!countries.isNullOrEmpty())
                countries.forEach { filmInfoDao.insertCountry(it) }
            if (!genres.isNullOrEmpty())
                genres.forEach { filmInfoDao.insertGenre(it) }
            _dBBusy.value = false
        }
    }

    suspend fun getMaxFilmInfoByID(filmID: Long): MaxFilmInfo {
        return filmInfoDao.getMaxFilmInfoByID(filmID)
    }

    suspend fun getMaxFilmInfoList(): List<MaxFilmInfo>{
        return filmInfoDao.getAllMaxFilmInfoList()
    }

    fun deleteDeprecatedMaxFilmInfo(filmInfoList: List<FilmInfo>, countries: List<DBCountry>, genres: List<DBGenre>){
        viewModelScope.launch {
            filmInfoDao.deleteFilmInfo(filmInfoList)
            filmInfoDao.deleteCountries(countries)
            filmInfoDao.deleteGenres(genres)
        }
    }

    suspend fun getDBGenresMaxID(): Int{
        val genresIDList = mutableListOf<Int>()
        filmInfoDao.getAllGenresList().forEach {
            genresIDList.add(it.id)
        }
        return if (genresIDList.isNotEmpty())
            genresIDList.max()
        else 0
    }

    suspend fun getAllDBGenres(): List<DBGenre>{
        return filmInfoDao.getAllGenresList()
    }

    fun addMaxFilmSelectionItemInfo(filmSelectionItemInfo: FilmSelectionItemInfo, genres: List<DBGenre>?){
        viewModelScope.launch {
            _dBBusy.value = true
            filmInfoDao.insertFilmSelectionItemInfo(filmSelectionItemInfo)
            if (!genres.isNullOrEmpty())
                genres.forEach { filmInfoDao.insertGenre(it) }
            _dBBusy.value = false
        }
    }

    suspend fun getMaxFilmSelectionItemInfoListBySelectionAttachment(selectionName: String): List<MaxFilmSelectionItemInfo> {
       return filmInfoDao.getMaxFilmSelectionItemInfoListBySelectionAttachment(selectionName)
    }

    fun getMaxFilmSelectionItemInfoFlow(): StateFlow<List<MaxFilmSelectionItemInfo>> {
        return filmInfoDao.getMaxFilmSelectionItemInfoFlow()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = emptyList()
            )
    }

    suspend fun getMaxFilmSelectionItemInfoList(): List<MaxFilmSelectionItemInfo>{
        return filmInfoDao.getAllMaxFilmSelectionItemInfoList()
    }

    suspend fun getFilmSelectionItemInfoMaxID(): Int{
        val iDList = mutableListOf<Int>()
        filmInfoDao.getAllMaxFilmSelectionItemInfoList().forEach {
            iDList.add(it.filmInfo.id)
        }
        return if (iDList.isNotEmpty())
            iDList.max()
        else 0
    }

    fun deleteMaxFilmSelectionItemInfo(maxFilmSelectionItemInfoList: List<MaxFilmSelectionItemInfo>){
        viewModelScope.launch {
            val filmSelectionItemInfoList = mutableListOf<FilmSelectionItemInfo>()
            val genresList = mutableListOf<DBGenre>()
            maxFilmSelectionItemInfoList.forEach { maxFilmSelectionItemInfoList ->
                filmSelectionItemInfoList.add(maxFilmSelectionItemInfoList.filmInfo)
                maxFilmSelectionItemInfoList.genre.forEach { genresList.add(it) }
            }
            filmInfoDao.deleteFilmSelectionItemInfo(filmSelectionItemInfoList)
            filmInfoDao.deleteGenres(genresList)
            _dBSetupInProcess.value = false
        }
    }

    fun addSearchSettings(searchSettings: SearchSettings){
        viewModelScope.launch {
            filmInfoDao.insertSearchSettings(searchSettings)
        }
    }

    suspend fun getSearchSettings(): SearchSettings {
        return filmInfoDao.getSearchSettings()
    }

    fun addCountryID(countryID: CountryID){
        viewModelScope.launch {
            filmInfoDao.insertCountryID(countryID)
        }
    }

    suspend fun getAllCountriesID(): List<CountryID>{
        return filmInfoDao.getAllCountriesID()
    }

    fun addGenreID(genreID: GenreID){
        viewModelScope.launch {
            filmInfoDao.insertGenreID(genreID)
        }
    }

    suspend fun getAllGenresID(): List<GenreID>{
        return filmInfoDao.getAllGenresID()
    }

    val allCountriesIDFlow = this.filmInfoDao.getAllCountriesIDFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    val allGenresIDFlow = this.filmInfoDao.getAllGenresIDFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    fun addImage(image: DBImage){
        viewModelScope.launch {
            filmInfoDao.insertImage(image)
        }
    }

    suspend fun getAllImages(): List<DBImage>{
        return filmInfoDao.getAllImagesList()
    }

    suspend fun getImagesByFilmAttachment(filmID: Int): List<DBImage>{
        return filmInfoDao.getImagesByFilmAttachment(filmID)
    }

    suspend fun getImagesByType(filmID: Int, type: String): List<DBImage>{
        return filmInfoDao.getImagesByType(filmID, type)
    }

    fun deleteImages(imagesList: List<DBImage>){
        viewModelScope.launch {
            filmInfoDao.deleteImages(imagesList)
        }
    }

    fun addGalleryTab(galleryTab: GalleryTab){
        viewModelScope.launch {
            filmInfoDao.insertTab(galleryTab)
        }
    }

    suspend fun getAllGalleryTabs(): List<GalleryTab>{
        return filmInfoDao.getAllTabs()
    }

    suspend fun getGalleryTabsByFilmAttachment(filmID: Int): List<GalleryTab>{
        return filmInfoDao.getTabsByFilmAttachment(filmID)
    }

    fun deleteGalleryTabs(tabsList: List<GalleryTab>){
        viewModelScope.launch {
            filmInfoDao.deleteTabs(tabsList)
        }
    }
}