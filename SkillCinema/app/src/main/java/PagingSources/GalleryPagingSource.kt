package PagingSources

import DataModels.Item
import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.skillcinema.HomeViewModel
import com.example.skillcinema.Repository


class GalleryPagingSource(
    private val apiKey: String,
    private val filmID: Int,
    private val type: String,
    private var calculatedPage: Int,
    private val viewModel: HomeViewModel?,
    private val itemsList: List<Item>
    ): PagingSource<Int, Item>() {
    private val typesList = listOf("STILL", "SHOOTING", "POSTER", "FAN_ART", "PROMO", "CONCEPT", "WALLPAPER", "COVER", "SCREENSHOT")
    private var lastTypeIsEmpty = false
    var currentTypeIndex = 0
    var currentPage = 0
    private val lastPageIsFull = itemsList.size % 20 == 0
    var netLoadingAllowed = false

    override fun getRefreshKey(state: PagingState<Int, Item>): Int = 1

    override val keyReuseSupported: Boolean
        get() = true

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Item> {
        var page = params.key ?: calculatedPage

        return when(type){
            "All" -> {
                if (!netLoadingAllowed) {
                    netLoadingAllowed = true
                    LoadResult.Page(
                        data = itemsList,
                        prevKey = null,
                        nextKey = currentPage,
                    )
                } else {
                    kotlin.runCatching {
                        Repository.RetrofitInstance.searchMovies.getImages(
                            apiKey,
                            filmID,
                            page,
                            typesList[currentTypeIndex]
                        ).items
                    }.fold(
                        onSuccess = {
                            if (it.isEmpty() && currentTypeIndex == typesList.lastIndex)
                                lastTypeIsEmpty = true

                            if (it.isEmpty() && !lastTypeIsEmpty) {
                                currentTypeIndex++
                                page = 0
                            }
                            currentPage = page
                            LoadResult.Page(
                                data = it,
                                prevKey = null,
                                nextKey = if (lastTypeIsEmpty) null else page + 1,
                            )
                        },
                        onFailure = {
                            Log.d("GalleryAll", it.message.toString())
                            LoadResult.Error(it)
                        }
                    )
                }
            }
            else -> {
                if (!netLoadingAllowed){
                    netLoadingAllowed = true
                    LoadResult.Page(
                        data = itemsList,
                        prevKey = null,
                        nextKey = if (!lastPageIsFull) null else page + 1,
                    )
                }
                else{
                    viewModel!!._galleryLoading.value = true
                    kotlin.runCatching {
                        Repository.RetrofitInstance.searchMovies.getImages(
                            apiKey,
                            filmID,
                            page,
                            type
                        ).items
                    }.fold(
                        onSuccess = {
                            viewModel._galleryLoading.value = false
                            LoadResult.Page(
                                data = it,
                                prevKey = null,
                                nextKey = if (it.isEmpty()) null else page + 1,
                            )
                        },
                        onFailure = {
                            viewModel._galleryLoading.value = false
                            Log.d("Gallery", it.message.toString())
                            LoadResult.Error(it)
                        }
                    )
                }
            }
        }
    }
}