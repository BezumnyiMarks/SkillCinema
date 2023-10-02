package com.example.skillcinema.utils

import DataModels.Country
import DataModels.DBCountry
import DataModels.DBGenre
import DataModels.DBImage
import DataModels.DetailedInfo
import DataModels.DynamicSelectionRequestAttributes
import DataModels.Film
import DataModels.FilmInfo
import DataModels.FilmSelectionItemInfo
import DataModels.Genre
import DataModels.Item
import DataModels.MaxFilmSelectionItemInfo
import android.content.Context
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.skillcinema.*
import java.util.*

class Extensions {
    private val rep = Repository()

    fun getGenreSet(item: List<Genre>): String{
        var genresSet = ""
        if (!item.isEmpty()){
            if (item.size > 1) {
                for (i in 0..item.size - 2) {
                    genresSet += item[i].genre
                    if (genresSet != "")
                        genresSet += ", "
                }
            }
            genresSet += item[item.size - 1].genre
        }
        return genresSet
    }

    fun getCountriesList(dBCountriesList: List<DBCountry>): List<Country>{
        val countries = mutableListOf<Country>()
        dBCountriesList.forEach {
            countries.add(Country(null, it.country))
        }
        return countries
    }

    fun getGenresList(dBGenresList: List<DBGenre>): List<Genre>{
        val genres = mutableListOf<Genre>()
        dBGenresList.forEach {
            genres.add(Genre(null, it.genre))
        }
        return genres
    }

    fun getDBCountriesList(countries: List<Country>, filmID: Long, dBCountryMaxID: Int): List<DBCountry>{
        var dBCountryID = dBCountryMaxID
        val dBCountries = mutableListOf<DBCountry>()
        countries.forEach {
            dBCountryID++
            dBCountries.add(DBCountry(dBCountryID, filmID, it.country))
        }
        return dBCountries
    }

    fun getDBGenresList(genres: List<Genre>, filmID: Long, dBGenreMaxID: Int): List<DBGenre>{
        var dBGenreID = dBGenreMaxID
        val dBGenres = mutableListOf<DBGenre>()
        genres.forEach {
            dBGenreID++
            dBGenres.add(DBGenre(dBGenreID, filmID, it.genre))
        }
        return dBGenres
    }

    fun getFilmInfo(detailedInfo: DetailedInfo): FilmInfo {
        return FilmInfo(
            detailedInfo.kinopoiskID!!, Calendar.getInstance().timeInMillis,
            detailedInfo.posterURL, detailedInfo.logoURL, detailedInfo.ratingKinopoisk, detailedInfo.ratingImdb,
            detailedInfo.ratingFilmCritics, detailedInfo.nameOriginal, detailedInfo.nameRu, detailedInfo.nameEn,
            detailedInfo.year, detailedInfo.filmLength,
            detailedInfo.ratingAgeLimits, detailedInfo.shortDescription, detailedInfo.description, detailedInfo.serial
        )
    }

    fun convertStringToList(string: String?): List<String>{
        var list = listOf<String>()
        if (string != ""){
            when(string!!.contains(',')){
                true -> list = string.split(",")
                else -> list = listOf(string)
            }
        }
        return list
    }

    fun convertListToString(list: List<String>): String{
        var str = ""
        list.forEach { str += "$it,"}
        str = str.replace(".$".toRegex(), "")
        return str
    }

    fun getYearsPage(data: MutableList<String>, position: Int): MutableList<String>{
        val itemsList = mutableListOf<String>()
        for (i in ((position+1)*12 - 12) until (position+1)*12){
            if (i < data.size)
                itemsList.add(data[i])
        }
        return itemsList
    }

    fun setYearsPagerViews(selectedYear: String, yearsPage: List<String>, textView: TextView, viewsList: List<TextView>, context: Context){
        if (textView.text == selectedYear){
            textView.background = ContextCompat.getDrawable(context, R.drawable.year_background)
            textView.setTextColor(ContextCompat.getColor(context, R.color.white))
        }
        viewsList.forEach {
            if (it.text != selectedYear){
                if (yearsPage.contains(it.text)) {
                    it.setTextColor(ContextCompat.getColor(context, R.color.text_black))
                    it.background = null
                }
                else{
                    it.setTextColor(ContextCompat.getColor(context, R.color.transparent))
                    it.background = null
                    it.isClickable = false
                }
            }
        }
    }

    fun getDynamicSelectionRequestAttributes(dynamicSelectionNumber: Int, context: Context): DynamicSelectionRequestAttributes {
        val requestAttributes = rep.getAllowableRequestAttributes(context)
        return when(dynamicSelectionNumber){
            1 -> {
                DynamicSelectionRequestAttributes(
                    requestAttributes[0].split(" ")[0].toLong(),
                    requestAttributes[0].split(" ")[1].toLong(), film
                )
            }

            2 -> {
                DynamicSelectionRequestAttributes(
                    requestAttributes[1].split(" ")[0].toLong(),
                    requestAttributes[1].split(" ")[1].toLong(), film
                )
            }

            3 -> {
                DynamicSelectionRequestAttributes(
                    requestAttributes[2].split(" ")[0].toLong(),
                    requestAttributes[2].split(" ")[1].toLong(), film
                )
            }
            else -> {
                DynamicSelectionRequestAttributes(1L, 1L, film)
            }
        }
    }

    fun getDynamicSelectionName(selectionNum: Int, context: Context): String{
        val requestAttr = getDynamicSelectionRequestAttributes(selectionNum, context)
        val countries = context.resources.getStringArray(R.array.countries)
        val genres = context.resources.getStringArray(R.array.genres)
        return "${genres[requestAttr.genreID.toInt() - 1]} ${countries[requestAttr.countryID.toInt() - 1]}"
    }

    fun getFilmSelectionItemInfo(item: Item?, film: Film?, maxID: Int): FilmSelectionItemInfo {
        return if (item != null && film == null)
            FilmSelectionItemInfo(maxID, item.kinopoiskID!!.toInt(), item.selectionAttachment, item.posterURL, item.ratingKinopoisk.toString(), item.nameRu, item.nameEn, item.nameOriginal)
        else
            FilmSelectionItemInfo(maxID, film!!.filmID.toInt(), film.selectionAttachment, film.posterURL, film.rating, film.nameRu, film.nameEn, null)
    }

    fun getItemsList(maxFilmSelectionItemInfoList: List<MaxFilmSelectionItemInfo>): List<Item>{
        val itemsList = mutableListOf<Item>()
        maxFilmSelectionItemInfoList.forEach { maxFilmSelectionItemInfo ->
            val genres = getGenresList(maxFilmSelectionItemInfo.genre)
            val rating = if (maxFilmSelectionItemInfo.filmInfo.ratingKinopoisk == "null") null else maxFilmSelectionItemInfo.filmInfo.ratingKinopoisk!!.toDouble()
            val item = Item(null, maxFilmSelectionItemInfo.filmInfo.selectionAttachment, maxFilmSelectionItemInfo.filmInfo.filmID.toLong(), maxFilmSelectionItemInfo.filmInfo.filmID.toLong(), null, maxFilmSelectionItemInfo.filmInfo.nameRu, maxFilmSelectionItemInfo.filmInfo.nameEn, maxFilmSelectionItemInfo.filmInfo.nameOriginal, rating,null,null,null, maxFilmSelectionItemInfo.filmInfo.imageURL,null, maxFilmSelectionItemInfo.filmInfo.imageURL,null,null, genres,null,null,null)
            itemsList.add(item)
        }
        return itemsList
    }

    fun getFilmsList(maxFilmSelectionItemInfoList: List<MaxFilmSelectionItemInfo>): List<Film>{
        val filmsList = mutableListOf<Film>()
        maxFilmSelectionItemInfoList.forEach { maxFilmSelectionItemInfo ->
            val genres = getGenresList(maxFilmSelectionItemInfo.genre)
            val film = Film(null, maxFilmSelectionItemInfo.filmInfo.selectionAttachment, maxFilmSelectionItemInfo.filmInfo.filmID.toLong(), maxFilmSelectionItemInfo.filmInfo.nameRu, maxFilmSelectionItemInfo.filmInfo.nameEn, null, null, null, genres, maxFilmSelectionItemInfo.filmInfo.ratingKinopoisk,null, maxFilmSelectionItemInfo.filmInfo.imageURL,null, null,null,null,null)
            filmsList.add(film)
        }
        return filmsList
    }

    fun getImagesList(dBImagesList: List<DBImage>): List<Item>{
        val itemsList = mutableListOf<Item>()
        dBImagesList.forEach {
            itemsList.add(Item(null, null, it.filmAttachment.toLong(), null, null,null,null,null,null,null,null,null,null,null, it.imageUrl,null,null,null,null,null,null))
        }
        return itemsList
    }

    fun getPage(listSize: Int): Int{
        var page = listSize / 20
        if (page == 0)
            page = 1
        return if (listSize % 20 == 0) page else page + 1
    }

    private companion object {
        private const val film = "FILM"
    }
}