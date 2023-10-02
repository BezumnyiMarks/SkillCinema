package Fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.ItemSnapshotList
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.example.skillcinema.App
import DataModels.DBImage
import com.example.skillcinema.DBViewModel
import PagingSources.GalleryPagingSource
import Adapters.GalleryStaggeredPagingAdapter
import com.example.skillcinema.HomeViewModel
import DataModels.Item
import com.example.skillcinema.R
import com.example.skillcinema.Repository
import com.example.skillcinema.databinding.FragmentGalleryViewPagerBinding
import com.example.skillcinema.utils.Extensions

private const val CURRENT_POSITION = "CURRENT_POSITION"
private const val TYPE = "TYPE"
private const val KINOPOISK_ID = "kinopoiskID"
class GalleryViewPagerFragment(val kinopoiskID: Long, val type: String) : Fragment() {
    private var _binding: FragmentGalleryViewPagerBinding? = null
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
    private val rep = Repository()
    private val galleryAdapter = GalleryStaggeredPagingAdapter { item -> onImageClick(item) }
    private val extensions = Extensions()
    private var galleryItems: ItemSnapshotList<Item>
    private var pagingSourceFactory = GalleryPagingSource("", kinopoiskID.toInt(), type, 0, null, listOf())
    private var itemsList = listOf<Item>()
    private var apiKey = ""


    init {
        galleryItems = galleryAdapter.snapshot()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGalleryViewPagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        apiKey = rep.getApiKey(requireActivity())!!
        binding.galleryRecyclerView.adapter = galleryAdapter

        lifecycleScope.launchWhenCreated {
            itemsList = extensions.getImagesList(dBViewModel.getImagesByType(kinopoiskID.toInt(), type))
            Log.d("itemsList", itemsList.toString())
            pagingSourceFactory = GalleryPagingSource(apiKey, kinopoiskID.toInt(), type, extensions.getPage(itemsList.size), viewModel, itemsList)
            pagingSourceFactory.netLoadingAllowed = false

            viewModel.gallery = Pager(
                config = PagingConfig(pageSize = 20),
                pagingSourceFactory = { pagingSourceFactory }
            ).flow.cachedIn(lifecycleScope)

            lifecycleScope.launchWhenCreated {
                viewModel.gallery.collect { images ->
                    galleryAdapter.submitData(images)
                }
            }
        }

        galleryAdapter.addOnPagesUpdatedListener {
            galleryItems = galleryAdapter.snapshot()
            galleryItems.forEach {
                dBViewModel.addImage(DBImage(it!!.imageURL!!, type, kinopoiskID.toInt()))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launchWhenCreated {
            itemsList = extensions.getImagesList(dBViewModel.getImagesByType(kinopoiskID.toInt(), type))
        }
    }

    private fun onImageClick(selectedItem: Item) {
        var currentPosition = 0
        galleryItems.items.forEachIndexed { index, item ->
            if (item == selectedItem)
                currentPosition = index
        }
        val bundle = bundleOf(
            CURRENT_POSITION to currentPosition,
            TYPE to type,
            KINOPOISK_ID to kinopoiskID.toInt()
        )
        findNavController().navigate(R.id.action_galleryFragment_to_pictureFragment, bundle)
    }
}