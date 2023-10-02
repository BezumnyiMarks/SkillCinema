package PagingSources

import DataModels.Film
import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.skillcinema.DBViewModel
import com.example.skillcinema.HomeViewModel
import com.example.skillcinema.Repository
import com.example.skillcinema.utils.Extensions
import kotlinx.coroutines.delay

class FilmsPagingSource(
    private val apiKey: String,
    private val requestType: String,
    private val collectionWatchedIDList: List<String>,
    private val viewModel: HomeViewModel,
    private val dBViewModel: DBViewModel,
    private val selectionAttachment: String
): PagingSource<Int, Film>() {
    private val extensions = Extensions()
    var netLoadingAllowed = false
    private var lastPageIsFull = false

    override fun getRefreshKey(state: PagingState<Int, Film>): Int = 1

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Film> {
        val filmsList = extensions.getFilmsList(dBViewModel.getMaxFilmSelectionItemInfoListBySelectionAttachment(selectionAttachment))
        Log.d("SIZE", filmsList.size.toString())
        val calculatedPage = extensions.getPage(filmsList.size)
        lastPageIsFull = filmsList.size % 20 == 0
        val page = params.key?: calculatedPage

        return when (requestType){
            top100Popular -> {
                if (!netLoadingAllowed && filmsList.isNotEmpty()) {
                    filmsList.forEach { film ->
                        film.watched = collectionWatchedIDList.contains(film.filmID.toString())
                    }
                    netLoadingAllowed = true
                    LoadResult.Page(
                        data = filmsList,
                        prevKey = null,
                        nextKey = if (!lastPageIsFull) null else page + 1,
                    )
                }
                else {
                    netLoadingAllowed = true
                    kotlin.runCatching {
                        val delayTime = (1..10).random() * 100
                        delay(delayTime.toLong())
                        Repository.RetrofitInstance.searchMovies.getPopular(apiKey, page, requestType).films
                    }.fold(
                        onSuccess = {
                            it.forEach { film ->
                                film.watched =
                                    collectionWatchedIDList.contains(film.filmID.toString())
                            }
                            LoadResult.Page(
                                data = it,
                                prevKey = null,
                                nextKey = if (it.isEmpty() || page == 5) null else page + 1,
                            )
                        },
                        onFailure = {
                            viewModel._popularLoading.value = false
                            Log.d("Popular", it.message.toString())
                            LoadResult.Error(it)
                        }
                    )
                }
            }
            else ->{
                if (!netLoadingAllowed && filmsList.isNotEmpty()){
                    netLoadingAllowed = true
                    filmsList.forEach { film ->
                        film.watched = collectionWatchedIDList.contains(film.filmID.toString())
                    }
                    LoadResult.Page(
                        data = filmsList,
                        prevKey = null,
                        nextKey = if (!lastPageIsFull) null else page + 1,
                    )
                }
                else{
                    netLoadingAllowed = true
                    kotlin.runCatching {
                        val delayTime = (1..10).random() * 100
                        delay(delayTime.toLong())
                        Repository.RetrofitInstance.searchMovies.getPopular(apiKey, page, requestType).films
                    }.fold(
                        onSuccess = {
                            it.forEach { film ->
                                film.watched = collectionWatchedIDList.contains(film.filmID.toString())
                            }
                            LoadResult.Page(
                                data = it,
                                prevKey = null,
                                nextKey = if (it.isEmpty()) null else page + 1,
                            )
                        },
                        onFailure = {
                            viewModel._bestLoading.value = false
                            Log.d("Best", it.message.toString())
                            LoadResult.Error(it)
                        }
                    )
                }
            }
        }
    }

    private companion object {
        private const val top100Popular = "top_100_popular_films"
    }
}