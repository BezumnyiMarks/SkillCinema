package PagingSources

import DataModels.DynamicSelectionRequestAttributes
import DataModels.Item
import DataModels.SearchSettings
import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.skillcinema.DBViewModel
import com.example.skillcinema.HomeViewModel
import com.example.skillcinema.Repository
import com.example.skillcinema.utils.Extensions
import kotlinx.coroutines.delay


class ItemsPagingSource(
    private val apiKey: String,
    private val dynamicSelectionRequestAttributes: DynamicSelectionRequestAttributes,
    private val collectionWatchedIDList: List<String>,
    private val filtered: Boolean,
    private val searchSettings: SearchSettings,
    private val keyword: String,
    private val viewModel: HomeViewModel,
    private val dBViewModel: DBViewModel,
    private val initType: String?,
    private val selectionAttachment: String
    ): PagingSource<Int, Item>() {
    val extensions = Extensions()
    var netLoadingAllowed = false
    private var lastPageIsFull = false

    override fun getRefreshKey(state: PagingState<Int, Item>): Int = 1

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Item> {
        val itemsList = extensions.getItemsList(dBViewModel.getMaxFilmSelectionItemInfoListBySelectionAttachment(selectionAttachment))
        val calculatedPage = extensions.getPage(itemsList.size)
        lastPageIsFull = itemsList.size % 20 == 0
        val page = params.key?: calculatedPage

        if (filtered){
            return kotlin.runCatching {
                viewModel._filteredFilmsLoading.value = true
                delay(500)
                Repository.RetrofitInstance.searchMovies.getFilteredSelection(
                    apiKey,
                    page,
                    searchSettings.countryID,
                    searchSettings.genreID,
                    searchSettings.order,
                    searchSettings.type,
                    searchSettings.ratingFrom,
                    searchSettings.ratingTo,
                    searchSettings.yearFrom,
                    searchSettings.yearTo,
                    keyword
                ).items
            }.fold(
                onSuccess = {
                    val watchedFilmsList = mutableListOf<Item>()
                    viewModel._filteredFilmsLoading.value = false
                    it.forEach { item ->
                        item.watched = collectionWatchedIDList.contains(item.kinopoiskID.toString())
                        if (searchSettings.watched && item.watched == true)
                            watchedFilmsList.add(item)
                    }

                    LoadResult.Page(
                        data = if (searchSettings.watched) watchedFilmsList else it,
                        prevKey = null,
                        nextKey = if (it.isEmpty()) null else page + 1,
                    )
                },
                onFailure = {
                    viewModel._filteredFilmsLoading.value = false
                    Log.d("ItemsPagingSource", it.message.toString())
                    LoadResult.Error(it)
                }
            )
        }
        else{
            if (!netLoadingAllowed && itemsList.isNotEmpty()){
                netLoadingAllowed = true
                itemsList.forEach { item ->
                    item.watched = collectionWatchedIDList.contains(item.kinopoiskID.toString())
                }
                return LoadResult.Page(
                    data = itemsList,
                    prevKey = null,
                    nextKey = if (!lastPageIsFull) null else page + 1,
                )
            }
            else{
                netLoadingAllowed = true
                return kotlin.runCatching {
                    val delayTime = (1..10).random() * 100
                    delay(delayTime.toLong())
                    Repository.RetrofitInstance.searchMovies.getDynamicSelection(
                        apiKey,
                        page,
                        dynamicSelectionRequestAttributes.countryID,
                        dynamicSelectionRequestAttributes.genreID,
                        dynamicSelectionRequestAttributes.type).items
                }.fold(
                    onSuccess = {
                        it.forEach { item ->
                            item.watched = collectionWatchedIDList.contains(item.kinopoiskID.toString())
                        }
                        LoadResult.Page(
                            data = it,
                            prevKey = null,
                            nextKey = if (it.isEmpty()) null else page + 1,
                        )
                    },
                    onFailure = {
                        when(initType){
                            firstDynamicSelection -> {
                                Log.d("FirstDynamicSelection", it.message.toString())
                                viewModel._firstDynamicLoading.value = false
                            }
                            secondDynamicSelection -> {
                                Log.d("SecondDynamicSelection", it.message.toString())
                                viewModel._secondDynamicLoading.value = false
                            }
                            thirdDynamicSelection -> {
                                Log.d("ThirdDynamicSelection", it.message.toString())
                                viewModel._thirdDynamicLoading.value = false
                            }
                            series -> {
                                Log.d("Series", it.message.toString())
                                viewModel._seriesLoading.value = false
                            }
                            validating -> {
                                Log.d("validating", it.message.toString())
                                viewModel._validatingLoading.value = false
                            }
                        }
                        LoadResult.Error(it)
                    }
                )
            }
        }
    }

    private companion object {
        private const val firstDynamicSelection = "FIRST_DYNAMIC"
        private const val secondDynamicSelection = "SECOND_DYNAMIC"
        private const val thirdDynamicSelection = "THIRD_DYNAMIC"
        private const val series = "TV_SERIES"
        private const val validating = "VALIDATING"
    }
}