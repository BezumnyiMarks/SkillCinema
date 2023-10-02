package Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.bumptech.glide.Glide
import com.example.skillcinema.App
import DataModels.DBImage
import com.example.skillcinema.DBViewModel
import PagingSources.GalleryPagingSource
import com.example.skillcinema.HomeViewModel
import Adapters.PicturePagingAdapter
import com.example.skillcinema.Repository
import com.example.skillcinema.databinding.FragmentPictureBinding
import com.example.skillcinema.utils.Extensions

private const val CURRENT_POSITION = "CURRENT_POSITION"
private const val CURRENT_PAGE = "CURRENT_PAGE"
private const val CURRENT_TYPE_INDEX = "CURRENT_TYPE_INDEX"
private const val TYPE = "TYPE"
private const val KINOPOISK_ID = "kinopoiskID"
private const val IMAGE_URL = "IMAGE_URL"
class PictureFragment : Fragment() {

    private var _binding: FragmentPictureBinding? = null
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
    private val extensions = Extensions()
    private var adapter = PicturePagingAdapter { }
    private var apiKey = ""

    private var currentPosition: Int? = null
    private var currentPage: Int? = null
    private var currentTypeIndex: Int? = null
    private var type: String? = null
    private var kinopoiskID: Int? = null
    private var imageURL: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            currentPosition = it.getInt(CURRENT_POSITION)
            currentPage = it.getInt(CURRENT_PAGE)
            currentTypeIndex = it.getInt(CURRENT_TYPE_INDEX)
            type = it.getString(TYPE)
            kinopoiskID = it.getInt(KINOPOISK_ID)
            imageURL = it.getString(IMAGE_URL)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentPictureBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        apiKey = rep.getApiKey(requireActivity())!!
        binding.viewPager.adapter = adapter

        if (imageURL != null){
            binding.viewPager.visibility = View.GONE
            binding.imageView.visibility = View.VISIBLE
            Glide
                .with(this)
                .load(android.net.Uri.parse(imageURL))
                .centerCrop()
                .into(binding.imageView)
        }
        else
            lifecycleScope.launchWhenCreated {
                val itemsList = extensions.getImagesList(dBViewModel.getImagesByType(kinopoiskID!!.toInt(), type!!))
                val pagingSourceFactory = GalleryPagingSource(apiKey, kinopoiskID!!.toInt(), type!!, extensions.getPage(itemsList.size), viewModel, itemsList)
                pagingSourceFactory.netLoadingAllowed = false
                if (type == "All"){
                    pagingSourceFactory.currentTypeIndex = currentTypeIndex!!
                    pagingSourceFactory.currentPage = currentPage!!
                }
                var receivingDBData = true

                viewModel.gallery = Pager(
                    config = PagingConfig(pageSize = 20, prefetchDistance = 0),
                    pagingSourceFactory = { pagingSourceFactory }
                ).flow.cachedIn(lifecycleScope)

                lifecycleScope.launchWhenCreated {
                    viewModel.gallery.collect { images ->
                        adapter.submitData(images)
                    }
                }

                adapter.addOnPagesUpdatedListener {
                    if (receivingDBData){
                        binding.viewPager.setCurrentItem(currentPosition!!, false)
                        receivingDBData = false
                    }
                    if (adapter.snapshot().size > itemsList.size){
                        adapter.snapshot().forEach {
                            dBViewModel.addImage(DBImage(it!!.posterURL!!, type!!, it.filmID!!.toInt()))
                        }
                    }
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        lifecycleScope.launchWhenCreated {
            dBViewModel.deleteImages(dBViewModel.getImagesByType(kinopoiskID!!, "All"))
            _binding = null
        }
    }
}