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
import com.example.skillcinema.App
import com.example.skillcinema.DBViewModel
import com.example.skillcinema.HomeViewModel
import DataModels.Item
import Adapters.ListAdapter
import com.example.skillcinema.R
import com.example.skillcinema.Repository
import com.example.skillcinema.databinding.FragmentFilmographyViewPagerBinding
import com.example.skillcinema.utils.Extensions

private const val KINOPOISK_ID = "kinopoiskID"
private const val WATCHED = "Просмотрено"
class FilmographyViewPagerFragment(val type: String, val tabMap: MutableMap<String, MutableList<MutableMap<String, String>>>) : Fragment() {
    private var _binding: FragmentFilmographyViewPagerBinding? = null
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
    private val filmographyAdapter = ListAdapter{ item -> onItemClick(item)}
    private val extensions = Extensions()
    private val rep = Repository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFilmographyViewPagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.filmographyRecyclerView.adapter = filmographyAdapter

        showData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onItemClick(item: Item) {
        val bundle = bundleOf(KINOPOISK_ID to item.kinopoiskID)
        findNavController().navigate(R.id.action_filmographyFragment_to_filmInfoFragment, bundle)
    }

    private fun showData() {
        val filmsIDList = mutableListOf<Long>()
        val filmsList = mutableListOf<Item>()
        val collectionWatchedIDList = extensions.convertStringToList(rep.getCollection(requireContext(), WATCHED))
        tabMap.forEach{ tabMapItem ->
            if (tabMapItem.key == type){
                tabMapItem.value.forEach {
                    filmsIDList.add(it.values.elementAt(0).toLong())
                }
            }
        }

        lifecycleScope.launchWhenCreated {
            filmsIDList.forEach {
                val dBFilm = dBViewModel.getMaxFilmInfoByID(it)
                filmsList.add(Item(false, null, dBFilm.filmInfo.filmID, dBFilm.filmInfo.filmID, null, dBFilm.filmInfo.nameRu, dBFilm.filmInfo.nameEn, dBFilm.filmInfo.nameOriginal, dBFilm.filmInfo.ratingKinopoisk, dBFilm.filmInfo.ratingImdb, null, null, dBFilm.filmInfo.posterURL, null, null, null, extensions.getCountriesList(dBFilm.country), extensions.getGenresList(dBFilm.genre), null, null, null))
            }
            filmsList.forEach {
                it.watched = collectionWatchedIDList.contains(it.filmID.toString())
            }
            Log.d("filmsID", filmsIDList.toString())
            filmographyAdapter.submitList(filmsList)
        }
    }
}