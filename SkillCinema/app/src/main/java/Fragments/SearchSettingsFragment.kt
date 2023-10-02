package Fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.skillcinema.App
import DataModels.CountryID
import com.example.skillcinema.DBViewModel
import DataModels.GenreID
import com.example.skillcinema.HomeViewModel
import com.example.skillcinema.R
import com.example.skillcinema.Repository
import DataModels.SearchSettings
import com.example.skillcinema.databinding.FragmentSearchSettingsBinding
import com.example.skillcinema.utils.Extensions
import com.google.android.material.slider.RangeSlider

private const val KINOPOISK_ID = "kinopoiskID"
private const val COUNTRY_GENRE = "countryGenre"
class SearchSettingsFragment : Fragment() {

    private var _binding: FragmentSearchSettingsBinding? = null
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
    private var settings = SearchSettings(0,0,0,"","",0,0,0,0,false)
    private var allCountriesIDList = listOf<CountryID>()
    private var allGenresIDList = listOf<GenreID>()

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

        _binding = FragmentSearchSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launchWhenStarted {
            settings = dBViewModel.getSearchSettings()
            allCountriesIDList = dBViewModel.getAllCountriesID()
            allGenresIDList = dBViewModel.getAllGenresID()
            showSettings(settings, allCountriesIDList, allGenresIDList)

            binding.radioAll.setOnClickListener {
                settings.type = "ALL"
                binding.radioMiniSeries.isChecked = false
                binding.radioTVShow.isChecked = false
                binding.radioAll.isChecked = true
            }

            binding.radioFilms.setOnClickListener {
                settings.type = "FILM"
                binding.radioMiniSeries.isChecked = false
                binding.radioTVShow.isChecked = false
                binding.radioFilms.isChecked = true
            }

            binding.radioSeries.setOnClickListener {
                settings.type = "TV_SERIES"
                binding.radioMiniSeries.isChecked = false
                binding.radioTVShow.isChecked = false
                binding.radioSeries.isChecked = true
            }

            binding.radioTVShow.setOnClickListener {
                settings.type = "TV_SHOW"
                binding.radioSeries.isChecked = false
                binding.radioFilms.isChecked = false
                binding.radioAll.isChecked = false
                binding.radioTVShow.isChecked = true
            }

            binding.radioMiniSeries.setOnClickListener {
                settings.type = "MINI_SERIES"
                binding.radioSeries.isChecked = false
                binding.radioFilms.isChecked = false
                binding.radioAll.isChecked = false
                binding.radioMiniSeries.isChecked = true
            }

            binding.countryField.setOnClickListener {
                dBViewModel.addSearchSettings(settings)
                binding.countryField.background = ContextCompat.getDrawable(requireActivity(),
                    R.color.recycler_image_foreground
                )
                val bundle = bundleOf(COUNTRY_GENRE to "COUNTRY")
                findNavController().navigate(R.id.action_searchSettingsFragment_to_searchCountryGenreFragment, bundle)
            }

            binding.genreField.setOnClickListener {
                dBViewModel.addSearchSettings(settings)
                binding.genreField.background = ContextCompat.getDrawable(requireActivity(),
                    R.color.recycler_image_foreground
                )
                val bundle = bundleOf(COUNTRY_GENRE to "GENRE")
                findNavController().navigate(R.id.action_searchSettingsFragment_to_searchCountryGenreFragment, bundle)
            }

            binding.yearField.setOnClickListener {
                dBViewModel.addSearchSettings(settings)
                binding.yearField.background = ContextCompat.getDrawable(requireActivity(),
                    R.color.recycler_image_foreground
                )
                findNavController().navigate(R.id.action_searchSettingsFragment_to_searchYearFragment)
            }

            binding.slider.addOnSliderTouchListener(object : RangeSlider.OnSliderTouchListener {
                override fun onStartTrackingTouch(slider: RangeSlider) {
                }

                override fun onStopTrackingTouch(slider: RangeSlider) {
                    settings.ratingFrom = slider.values[0].toInt()
                    settings.ratingTo = slider.values[1].toInt()
                    val range = if (slider.values[0].toInt() == 1 && slider.values[1].toInt() == 10)
                        "Любой"
                    else "От ${slider.values[0].toInt()} до ${slider.values[1].toInt()}"
                    binding.ratingTextView.text = range
                }
            })

            binding.radioDate.setOnClickListener {
                settings.order = "YEAR"
            }

            binding.radioPopularity.setOnClickListener {
                settings.order = "NUM_VOTE"
            }

            binding.radioRating.setOnClickListener {
                settings.order = "RATING"
            }

            binding.watchedIcon.setOnClickListener {
                if (settings.watched){
                    settings.watched = false
                    binding.watchedIcon.setImageResource(R.drawable.ic_not_watched_icon)
                    binding.watchedTextView.text = "Не просмотрено"
                }
                else{
                    settings.watched = true
                    binding.watchedIcon.setImageResource(R.drawable.ic_watched_icon)
                    binding.watchedTextView.text = "Просмотрено"
                }
            }

            binding.buttonBack.setOnClickListener {
                dBViewModel.addSearchSettings(settings)
                findNavController().navigateUp()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dBViewModel.addSearchSettings(settings)
        _binding = null
    }

    private fun showSettings(settings: SearchSettings, allCountriesIDList: List<CountryID>, allGenresIDList: List<GenreID>){
        when(settings.type){
            "ALL" -> binding.radioAll.isChecked = true
            "FILM" -> binding.radioFilms.isChecked = true
            "TV_SERIES" -> binding.radioSeries.isChecked = true
            "TV_SHOW" -> binding.radioTVShow.isChecked = true
            "MINI_SERIES" -> binding.radioMiniSeries.isChecked = true
        }
        allCountriesIDList.forEach { if (it.id == settings.countryID) binding.countryTextView.text = it.country}
        allGenresIDList.forEach { if (it.id == settings.genreID) binding.genreTextView.text = it.genre}
        var range = "С ${settings.yearFrom} по ${settings.yearTo}"
        binding.yearTextView.text = range
        range = if (settings.ratingFrom == 1 && settings.ratingTo == 10)
            "Любой"
        else "От ${settings.ratingFrom} до ${settings.ratingTo}"
        binding.ratingTextView.text = range
        binding.slider.values = mutableListOf(settings.ratingFrom.toFloat(), settings.ratingTo.toFloat())
        when(settings.order){
            "RATING" -> binding.radioRating.isChecked = true
            "NUM_VOTE" -> binding.radioPopularity.isChecked = true
            "YEAR" -> binding.radioDate.isChecked = true
        }
        if (settings.watched){
            binding.watchedIcon.setImageResource(R.drawable.ic_watched_icon)
            binding.watchedTextView.text = "Просмотрено"
        }
        else{
            binding.watchedIcon.setImageResource(R.drawable.ic_not_watched_icon)
            binding.watchedTextView.text = "Не просмотрено"
        }
    }
}
