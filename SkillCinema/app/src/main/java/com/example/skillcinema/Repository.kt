package com.example.skillcinema

import DataModels.CountryGenreID
import DataModels.DetailedInfo
import DataModels.Films
import DataModels.Items
import DataModels.Series
import DataModels.Staff
import DataModels.StaffInfo
import android.content.Context
import android.content.Context.MODE_PRIVATE
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

private const val BASE_URL = "https://kinopoiskapiunofficial.tech"

private const val PREFS_FIRST_LOAD = "PREFS_FIRST_LOAD"
private const val FIRST_LOAD_KEY = "FIRST_LOAD_CHECK"
private const val API_KEY = "API_KEY"

private const val PREFS_ALLOWABLE_REQUEST_ATTRIBUTES = "PREFS_ALLOWABLE_REQUEST_ATTRIBUTES"
private const val FIRST_ALLOWABLE_REQUEST_ATTRIBUTES_KEY = "FIRST_ALLOWABLE_REQUEST_ATTRIBUTES"
private const val SECOND_ALLOWABLE_REQUEST_ATTRIBUTES_KEY = "SECOND_ALLOWABLE_REQUEST_ATTRIBUTES"
private const val THIRD_ALLOWABLE_REQUEST_ATTRIBUTES_KEY = "THIRD_ALLOWABLE_REQUEST_ATTRIBUTES"

private const val PREFS_COLLECTIONS = "PREFS_COLLECTIONS"
private const val COLLECTIONS_NAMES = "COLLECTIONS_NAMES"

private const val PREFS_DB_DATA_RELEVANCE = "PREFS_DB_DATA_RELEVANCE"
private const val DB_DATA_RELEVANCE = "DB_DATA_RELEVANCE"
class Repository() {

     fun saveFirstLoadCheck(context: Context){
          val prefs = context.getSharedPreferences(PREFS_FIRST_LOAD, MODE_PRIVATE)
          prefs.edit().putBoolean(FIRST_LOAD_KEY, false).apply()
     }

     fun getFirstLoadCheck(context:Context): Boolean{
          val prefs = context.getSharedPreferences(PREFS_FIRST_LOAD, MODE_PRIVATE)
          return prefs.getBoolean(FIRST_LOAD_KEY, true)
     }

     fun saveApiKey(context: Context, regKey: String){
          val prefs = context.getSharedPreferences(PREFS_FIRST_LOAD, MODE_PRIVATE)
          prefs.edit().putString(API_KEY, regKey).apply()
     }

     fun getApiKey(context:Context): String? {
          val prefs = context.getSharedPreferences(PREFS_FIRST_LOAD, MODE_PRIVATE)
          return prefs.getString(API_KEY, "")
     }

     fun saveAllowableRequestAttributes(context: Context){
          val prefs = context.getSharedPreferences(PREFS_ALLOWABLE_REQUEST_ATTRIBUTES, MODE_PRIVATE)
          prefs.edit()
               .putString(FIRST_ALLOWABLE_REQUEST_ATTRIBUTES_KEY, allowableRequestAttributesStr.split(",").shuffled().random())
               .putString(SECOND_ALLOWABLE_REQUEST_ATTRIBUTES_KEY, allowableRequestAttributesStr.split(",").shuffled().random())
               .putString(THIRD_ALLOWABLE_REQUEST_ATTRIBUTES_KEY, allowableRequestAttributesStr.split(",").shuffled().random())
               .apply()
     }

     fun getAllowableRequestAttributes(context: Context): MutableList<String>{
          val prefs = context.getSharedPreferences(PREFS_ALLOWABLE_REQUEST_ATTRIBUTES, MODE_PRIVATE)
          val allowableRequestAttributesList = mutableListOf<String>()

          prefs.getString(FIRST_ALLOWABLE_REQUEST_ATTRIBUTES_KEY, "1 1")
               ?.let { allowableRequestAttributesList.add(it) }

          prefs.getString(SECOND_ALLOWABLE_REQUEST_ATTRIBUTES_KEY, "1 1")
               ?.let { allowableRequestAttributesList.add(it) }

          prefs.getString(THIRD_ALLOWABLE_REQUEST_ATTRIBUTES_KEY, "1 1")
               ?.let { allowableRequestAttributesList.add(it) }

          return allowableRequestAttributesList
     }

     fun saveCollectionsNames(context: Context, collectionsNames: String){
          val prefs = context.getSharedPreferences(PREFS_COLLECTIONS, MODE_PRIVATE)
          prefs.edit().putString(COLLECTIONS_NAMES, collectionsNames).apply()
     }

     fun getCollectionsNames(context: Context): String? {
          val prefs = context.getSharedPreferences(PREFS_COLLECTIONS, MODE_PRIVATE)
          return prefs.getString(COLLECTIONS_NAMES, "")
     }

     fun saveCollection(context: Context, filmsID: String, collectionKey: String){
          val prefs = context.getSharedPreferences(PREFS_COLLECTIONS, MODE_PRIVATE)
          prefs.edit().putString(collectionKey, filmsID).apply()
     }

     fun getCollection(context: Context, collectionKey: String): String? {
          val prefs = context.getSharedPreferences(PREFS_COLLECTIONS, MODE_PRIVATE)
          return prefs.getString(collectionKey, "")
     }

     fun deleteCollection(context: Context, collectionKey: String){
          val prefs = context.getSharedPreferences(PREFS_COLLECTIONS, MODE_PRIVATE)
          prefs.edit().remove(collectionKey).apply()
     }

     fun saveDBDataRelevanceState(context: Context, dataRelevant: Boolean){
          val prefs = context.getSharedPreferences(PREFS_DB_DATA_RELEVANCE, MODE_PRIVATE)
          prefs.edit().putBoolean(DB_DATA_RELEVANCE, dataRelevant).apply()
     }

     fun getDBDataRelevanceState(context: Context): Boolean {
          val prefs = context.getSharedPreferences(PREFS_DB_DATA_RELEVANCE, MODE_PRIVATE)
          return prefs.getBoolean(DB_DATA_RELEVANCE, false)
     }

     object RetrofitInstance{
          private val retrofit = Retrofit.Builder()
               .baseUrl(BASE_URL)
               .addConverterFactory(MoshiConverterFactory.create())
               .build()

          val searchMovies = retrofit.create(SearchMovies::class.java)
     }

     private companion object {
          private const val allowableRequestAttributesStr = "1 1,1 2,1 3,1 4,1 5,1 6,1 7,1 8,1 9,1 10,1 11,1 12,1 13,1 14,1 15,1 16,1 17,1 18,1 19,1 20,1 21,1 22,1 23,1 24,1 28," +
                  "1 33,2 1,2 2,2 3,2 4,2 5,2 6,2 7,2 8,2 10,2 11,2 12,2 13,2 14,2 15,2 16,2 17,2 18,2 19,2 20,2 21,2 22,2 23,2 28,3 1,3 2,3 3,3 4,3 5,3 6,3 7,3 8,3 10,3 11," +
                  "3 12,3 13,3 14,3 15,3 16,3 17,3 18,3 19,3 20,3 21,3 22,3 23,3 28,4 1,4 2,4 3,4 4,4 5,4 6,4 7,4 8,4 10,4 11,4 12,4 13,4 14,4 15,4 16,4 17,4 18,4 19,4 20,4 21," +
                  "4 22,4 23,5 1,5 2,5 3,5 4,5 5,5 6,5 7,5 8,5 9,5 10,5 11,5 12,5 13,5 14,5 15,5 16,5 17,5 18,5 19,5 20,5 21,5 22,5 23,5 28,5 33,6 1,6 2,6 3,6 4,6 5,6 6,6 7,6 8," +
                  "6 10,6 11,6 12,6 13,6 14,6 15,6 16,6 17,6 18,6 19,6 20,6 21,6 22,6 23,6 28,7 1,7 2,7 3,7 4,7 5,7 6,7 7,7 8,7 10,7 11,7 12,7 13,7 14,7 15,7 16,7 17,7 18,7 19," +
                  "7 20,7 21,7 22,7 23,8 1,8 2,8 3,8 4,8 5,8 6,8 7,8 8,8 10,8 11,8 12,8 13,8 14,8 15,8 16,8 17,8 18,8 19,8 20,8 21,8 22,8 23,8 28,8 33,9 1,9 2,9 3,9 4,9 5,9 6," +
                  "9 7,9 8,9 10,9 11,9 12,9 13,9 14,9 15,9 16,9 17,9 18,9 19,9 20,9 21,9 22,9 23,9 28,9 33,10 1,10 2,10 3,10 4,10 5,10 6,10 7,10 8,10 10,10 11,10 12,10 13,10 14," +
                  "10 15,10 16,10 17,10 18,10 19,10 20,10 21,10 22,10 23,10 28,11 1,11 2,11 3,11 4,11 5,11 6,11 7,11 8,11 10,11 11,11 12,11 13,11 14,11 15,11 16,11 17,11 18," +
                  "11 19,11 20,11 21,11 22,11 23,11 28,12 1,12 2,12 3,12 4,12 5,12 6,12 7,12 8,12 10,12 11,12 12,12 13,12 14,12 15,12 16,12 17,12 18,12 19,12 20,12 21,12 22," +
                  "12 23,12 28,13 1,13 2,13 3,13 4,13 5,13 6,13 7,13 8,13 10,13 11,13 12,13 13,13 14,13 15,13 16,13 17,13 18,13 19,13 20,13 21,13 22,13 23,14 1,14 2,14 3,14 4," +
                  "14 5,14 6,14 7,14 8,14 10,14 11,14 12,14 13,14 14,14 15,14 16,14 17,14 18,14 19,14 20,14 21,14 22,14 23,14 28,14 33,15 1,15 2,15 3,15 4,15 5,15 6,15 7,15 8," +
                  "15 10,15 11,15 12,15 13,15 14,15 15,15 16,15 17,15 18,15 19,15 20,15 21,15 22,15 23,16 1,16 2,16 3,16 4,16 5,16 6,16 7,16 8,16 10,16 11,16 12,16 13,16 14," +
                  "16 15,16 16,16 17,16 18,16 19,16 20,16 21,16 22,16 23,16 24,16 28,16 33,17 7,17 8,17 10,17 11,17 12,17 13,17 14,17 15,17 16,17 17,17 18,17 19,17 20,17 21," +
                  "17 22,17 23,17 28,18 7,18 8,18 11,18 12,18 13,18 14,18 15,18 16,18 17,18 18,18 19,18 20,18 21,18 22,18 23,19 12,19 13,19 14,19 15,19 16,19 17,19 18,19 19," +
                  "19 20,19 21,19 22,19 23,20 7,20 8,20 11,20 12,20 13,20 14,20 15,20 16,20 17,20 18,20 19,20 22,20 23,21 7,21 8,21 10,21 11,21 13,21 14,21 15,21 16,21 17," +
                  "21 18,21 19,21 20,21 21,21 22,21 23,21 33,22 7,22 8,22 10,22 11,22 12,22 13,22 14,22 15,22 16,22 17,22 18,22 19,22 20,22 21,22 22,22 23,23 7,23 8,23 10," +
                  "23 11,23 12,23 13,23 14,23 15,23 16,23 17,23 18,23 19,23 20,23 21,23 22,23 23,23 28,24 7,24 8,24 10,24 11,24 12,24 13,24 14,24 15,24 16,24 17,24 18,24 19," +
                  "24 20,24 21,24 22,24 23,25 7,25 8,25 10,25 11,25 12,25 13,25 14,25 15,25 16,25 17,25 18,25 19,25 20,25 21,25 22,25 23,26 7,26 8,26 12,26 13,26 14,26 15," +
                  "26 19,26 22,26 23,27 7,27 8,27 10,27 11,27 12,27 13,27 14,27 15,27 16,27 17,27 18,27 19,27 20,27 21,27 22,27 23,28 7,28 8,28 11,28 12,28 13,28 14,28 15," +
                  "28 16,28 17,28 18,28 19,28 20,28 21,28 22,28 23,29 7,29 8,29 10,29 11,29 12,29 13,29 14,29 15,29 16,29 17,29 18,29 19,29 20,29 21,29 22,29 23,30 7,30 8," +
                  "30 10,30 11,30 12,30 13,30 14,30 15,30 16,30 17,30 18,30 19,30 20,30 21,30 22,30 23,30 28,31 7,31 8,31 12,31 13,31 14,31 15,31 16,31 17,31 18,31 19,31 20," +
                  "31 21,31 22,31 23,32 7,32 11,32 13,32 15,32 17,32 22,32 23,33 7,33 8,33 10,33 11,33 12,33 13,33 14,33 15,33 16,33 17,33 18,33 19,33 20,33 21,33 22,33 23," +
                  "33 33,34 7,34 8,34 10,34 11,34 12,34 13,34 14,34 15,34 16,34 17,34 18,34 19,34 20,34 21,34 22,34 23,34 28,34 33,35 22,35 23,36 22,36 23,37 22,37 23,38 22," +
                  "38 23,39 22,39 23,40 22,40 23,41 22,41 23,42 22,42 23,43 22,43 23,44 22,44 23,44 28,45 22,45 23,45 28,46 22,46 23,47 22,47 23,48 22,48 23,49 22,49 23,49 28," +
                  "49 33,50 22,50 23,51 22,51 23,52 22,52 23,53 22,53 23,55 22,55 23,56 22,56 23,57 22,57 23,58 22,58 23,59 22,59 23,60 22,60 23,62 22,62 23,63 22,63 23,63 28," +
                  "64 22,64 23,66 22,66 23,67 22,67 23,68 22,68 23,69 22,69 23,70 22,70 23,71 22,71 23,72 22,72 23,73 22,73 23,74 22,74 23,75 22,75 23,123 33"
     }
}

interface SearchMovies{
     @GET("/api/v2.2/films/premieres")
     suspend fun getPremieres(@Header("X-API-KEY") apiKey: String, @Query("year") year: Int, @Query("month") month: String): Items

     @GET("/api/v2.2/films/top")
     suspend fun getPopular(@Header("X-API-KEY") apiKey: String, @Query("page") page: Int, @Query("type") type: String): Films

     @GET("/api/v2.2/films/filters")
     suspend fun getCountryGenreID(@Header("X-API-KEY") apiKey: String): CountryGenreID

     @GET("/api/v2.2/films?ratingFrom=5")
     suspend fun getDynamicSelection(
          @Header("X-API-KEY") apiKey: String,
          @Query("page") page: Int,
          @Query("countries") countryID: Long,
          @Query("genres") genreID: Long,
          @Query("type") type: String
     ): Items

     @GET("/api/v2.2/films")
     suspend fun getFilteredSelection(
          @Header("X-API-KEY") apiKey: String,
          @Query("page") page: Int,
          @Query("countries") countryID: Int,
          @Query("genres") genreID: Int,
          @Query("order") order: String,
          @Query("type") type: String,
          @Query("ratingFrom") ratingFrom: Int,
          @Query("ratingTo") ratingTo: Int,
          @Query("yearFrom") yearFrom: Int,
          @Query("yearTo") yearTo: Int,
          @Query("keyword") keyword: String,
     ): Items

     @GET("/api/v2.2/films/{id}/images")
     suspend fun getImages(@Header("X-API-KEY") apiKey: String, @Path("id") id: Int, @Query("page") page: Int, @Query("type") type: String): Items

     @GET("/api/v2.2/films/{id}")
     suspend fun getDetailedInfo(@Header("X-API-KEY") apiKey: String, @Path("id") id: Int): DetailedInfo

     @GET("/api/v1/staff")
     suspend fun getStaffList(@Header("X-API-KEY") apiKey: String, @Query("filmId") filmID: Int): List<Staff>

     @GET("/api/v1/staff/{id}")
     suspend fun getStaffInfo(@Header("X-API-KEY") apiKey: String, @Path("id") staffID: Int): StaffInfo

     @GET("/api/v2.2/films/{id}/seasons")
     suspend fun getSeasons(@Header("X-API-KEY") apiKey: String, @Path("id") id: Int): Series

     @GET("/api/v2.2/films/{id}/similars")
     suspend fun getSimilars(@Header("X-API-KEY") apiKey: String, @Path("id") id: Int): Items

     private companion object {
          private const val api_key = "f60c6e8c-f056-4903-8bb2-a3fa4d165b22"
     }
}

//b828e8cc-ef5d-4c23-b1fb-6fc3dcc23913
//d287a524-0456-4d54-9266-6a5fd063bc34
//f60c6e8c-f056-4903-8bb2-a3fa4d165b22
//a587230a-0a3f-4858-906d-97206bb4f0d7
//dc51dc99-aeb8-4cea-9733-825c5e9ff008
//47bbfb2e-8dc2-4eab-a431-f24431bd71c4

//https://www.youtube.com/watch?v=mVx3_vSxbJU&t=1415s
//https://www.youtube.com/watch?v=Pous1Uyj-9M&t=188s
//https://www.youtube.com/watch?v=DCo1nLUGq8I
//https://www.youtube.com/watch?v=tYl0v8lQcT4