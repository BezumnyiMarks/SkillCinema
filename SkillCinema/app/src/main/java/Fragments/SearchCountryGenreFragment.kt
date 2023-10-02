package Fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.skillcinema.App
import Adapters.CountryGenreAdapter
import DataModels.CountryID
import com.example.skillcinema.DBViewModel
import DataModels.GenreID
import com.example.skillcinema.HomeViewModel
import DataModels.SearchSettings
import com.example.skillcinema.databinding.FragmentSearchCountryGenreBinding
import java.util.*

private const val COUNTRY_GENRE = "countryGenre"
class SearchCountryGenreFragment : Fragment() {

    private var _binding: FragmentSearchCountryGenreBinding? = null
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
    lateinit var allCountriesID: List<CountryID>
    lateinit var allGenresID: List<GenreID>
    lateinit var settings: SearchSettings
    private var data = mutableListOf<String>()
    lateinit var adapter: CountryGenreAdapter

    private var countryGenre: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            countryGenre = it.getString(COUNTRY_GENRE)
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSearchCountryGenreBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.headerTextView.text = if (countryGenre == "COUNTRY") "Страна" else "Жанр"
        binding.searchView.hint = if (countryGenre == "COUNTRY") "Выберите страну" else "Выберите жанр"
        lifecycleScope.launchWhenStarted {
            settings = dBViewModel.getSearchSettings()
            if (countryGenre == "COUNTRY") {
                allCountriesID = dBViewModel.getAllCountriesID()
                allCountriesID.forEach { data.add(it.country) }
            } else {
                allGenresID = dBViewModel.getAllGenresID()
                allGenresID.forEach { data.add(it.genre) }
            }
            adapter = CountryGenreAdapter(data, requireActivity()){item -> onItemClick(item)}
            binding.recyclerView.adapter = adapter
        }

        binding.searchView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val filteredData = mutableListOf<String>()
                data.forEach {
                    if (it.lowercase(Locale.getDefault()).contains(s.toString().lowercase(Locale.getDefault())))
                        filteredData.add(it)
                }
                Log.d("", filteredData.toString())
                adapter.setData(filteredData)
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        binding.buttonBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onItemClick(item: String) {
        if (countryGenre == "COUNTRY"){
            allCountriesID.forEach {
                if (it.country == item){
                    settings.countryID = it.id
                    dBViewModel.addSearchSettings(settings)
                }
            }
        }
        else{
            allGenresID.forEach {
                if (it.genre == item){
                    settings.genreID = it.id
                    dBViewModel.addSearchSettings(settings)
                }
            }
        }
        findNavController().navigateUp()
    }
}
