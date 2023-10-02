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
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FilmInfoDao {

//FilmInfo

    @Transaction
    @Query("SELECT * FROM FilmInfo")
    suspend fun getAllMaxFilmInfoList(): List<MaxFilmInfo>

    @Transaction
    @Query("SELECT * FROM FilmInfo WHERE filmID = :filmID")
    suspend fun getMaxFilmInfoByID(filmID: Long): MaxFilmInfo

    @Query("SELECT * FROM DBGenre")
    suspend fun getAllGenresList(): List<DBGenre>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFilmInfo(maxFilmInfo: FilmInfo)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCountry(country: DBCountry)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGenre(genre: DBGenre)

    @Delete
    suspend fun deleteFilmInfo(filmInfoList: List<FilmInfo>)

    @Delete
    suspend fun deleteCountries(countriesList: List<DBCountry>)

    @Delete
    suspend fun deleteGenres(genresList: List<DBGenre>)


//FilmSelectionItemInfo

    @Transaction
    @Query("SELECT * FROM FilmSelectionItemInfo")
    suspend fun getAllMaxFilmSelectionItemInfoList(): List<MaxFilmSelectionItemInfo>

    @Transaction
    @Query("SELECT * FROM FilmSelectionItemInfo WHERE selectionAttachment = :selectionName")
    suspend fun getMaxFilmSelectionItemInfoListBySelectionAttachment(selectionName: String): List<MaxFilmSelectionItemInfo>

    @Transaction
    @Query("SELECT * FROM FilmSelectionItemInfo")
    fun getMaxFilmSelectionItemInfoFlow(): Flow<List<MaxFilmSelectionItemInfo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFilmSelectionItemInfo(maxFilmSelectionItemInfo: FilmSelectionItemInfo)

    @Delete
    suspend fun deleteFilmSelectionItemInfo(filmSelectionItemInfoList: List<FilmSelectionItemInfo>)


//SearchSettings

    @Query("SELECT * FROM SearchSettings")
    suspend fun getSearchSettings(): SearchSettings

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchSettings(searchSettings: SearchSettings)


//AllCountriesGenresID

    @Query("SELECT * FROM CountryID")
    fun getAllCountriesIDFlow(): Flow<List<CountryID>>

    @Query("SELECT * FROM GenreID")
    fun getAllGenresIDFlow(): Flow<List<GenreID>>

    @Query("SELECT * FROM CountryID")
    suspend fun getAllCountriesID(): List<CountryID>

    @Query("SELECT * FROM GenreID")
    suspend fun getAllGenresID(): List<GenreID>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCountryID(countryID: CountryID)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGenreID(genreID: GenreID)


//Images

    @Query("SELECT * FROM Image")
    suspend fun getAllImagesList(): List<DBImage>

    @Query("SELECT * FROM Image WHERE filmAttachment = :filmId")
    suspend fun getImagesByFilmAttachment(filmId: Int): List<DBImage>

    @Query("SELECT * FROM Image WHERE filmAttachment = :filmId AND type = :type")
    suspend fun getImagesByType(filmId: Int, type: String): List<DBImage>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImage(image: DBImage)

    @Delete
    suspend fun deleteImages(imagesList: List<DBImage>)


//GalleryTabs

    @Query("SELECT * FROM GalleryTab")
    suspend fun getAllTabs(): List<GalleryTab>

    @Query("SELECT * FROM GalleryTab WHERE filmAttachment = :filmId")
    suspend fun getTabsByFilmAttachment(filmId: Int): List<GalleryTab>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTab(tab: GalleryTab)

    @Delete
    suspend fun deleteTabs(galleryTabsList: List<GalleryTab>)

}