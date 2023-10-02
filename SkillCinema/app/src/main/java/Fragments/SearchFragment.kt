package Fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.core.os.bundleOf
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.example.skillcinema.App
import com.example.skillcinema.DBViewModel
import DataModels.DynamicSelectionRequestAttributes
import com.example.skillcinema.HomeViewModel
import DataModels.Item
import Adapters.ItemsPagingAdapter
import PagingSources.ItemsPagingSource
import com.example.skillcinema.R
import com.example.skillcinema.Repository
import DataModels.SearchSettings
import com.example.skillcinema.databinding.FragmentSearchBinding
import com.example.skillcinema.utils.Extensions


private const val KINOPOISK_ID = "kinopoiskID"
private const val WATCHED = "Просмотрено"
class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
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
    private val searchAdapter = ItemsPagingAdapter { item -> onItemClick(item) }
    private val rep = Repository()
    private val extensions = Extensions()
    lateinit var searchSettings: SearchSettings
    private var apiKey = ""

    private var kinopoiskID: Long? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            kinopoiskID = it.getLong(KINOPOISK_ID)
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        apiKey = rep.getApiKey(requireActivity())!!
        binding.recyclerView.adapter = searchAdapter
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.buttonSettings.setOnClickListener {
            binding.searchView.setText("")
            findNavController().navigate(R.id.action_searchFragment_to_searchSettingsFragment)
        }

        binding.bottomNav.setOnItemSelectedListener {
            when(it.itemId){
                R.id.home -> findNavController().navigate(R.id.homeFragment)
                R.id.profile -> findNavController().navigate(R.id.profileFragment)
            }
            true
        }
    }

    override fun onResume() {
        super.onResume()
        binding.bottomNav.menu.get(1).isChecked = true
        lifecycleScope.launchWhenCreated {
            searchSettings = dBViewModel.getSearchSettings()
            loadFilms(binding.searchView.text.toString())
            binding.searchView.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    loadFilms(s.toString())
                }

                override fun afterTextChanged(s: Editable?) {
                }
            })
        }

        searchAdapter.addOnPagesUpdatedListener {
            if (searchAdapter.snapshot().isEmpty()){
                binding.recyclerView.visibility = View.INVISIBLE
                binding.textView.visibility = View.VISIBLE
            }
            else{
                binding.recyclerView.visibility = View.VISIBLE
                binding.textView.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onItemClick(item: Item){
        val bundle = bundleOf(KINOPOISK_ID to item.kinopoiskID)
        findNavController().navigate(R.id.action_searchFragment_to_filmInfoFragment, bundle)
    }

    private fun loadFilms(keyword: String){
        viewModel.filteredFilms = Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = {
                ItemsPagingSource(
                    apiKey,
                    DynamicSelectionRequestAttributes(0,0,""),
                    extensions.convertStringToList(rep.getCollection(requireActivity(), WATCHED)),
                    true,
                    searchSettings,
                    keyword,
                    viewModel,
                    dBViewModel,
                    null,
                   ""
                )
            }
        ).flow.cachedIn(lifecycleScope)

        lifecycleScope.launchWhenStarted {
            viewModel.filteredFilms.collect {
                searchAdapter.submitData(it)
            }
        }
    }
}
