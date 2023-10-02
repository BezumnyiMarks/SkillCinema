package Fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.os.bundleOf
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.skillcinema.App
import com.example.skillcinema.DBViewModel
import DataModels.Item
import Adapters.ListAdapter
import DataModels.MaxFilmInfo
import DataModels.ProfileCollection
import Adapters.ProfileCollectionsAdapter
import com.example.skillcinema.R
import com.example.skillcinema.Repository
import com.example.skillcinema.databinding.CentralSheetBinding
import com.example.skillcinema.databinding.DeleteCollectionDialogBinding
import com.example.skillcinema.databinding.FragmentProfileBinding
import com.example.skillcinema.utils.Extensions

private const val KINOPOISK_ID = "kinopoiskID"
private const val WATCHED = "Просмотрено"
private const val INTERESTING = "Было интересно"
private const val FAVOURITES = "Любимое"
private const val BOOKMARK = "Хочу посмотреть"
private const val COLLECTION_NAME = "COLLECTION_NAME"
class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val dBViewModel: DBViewModel by viewModels {
        object : ViewModelProvider.Factory{
            override fun <T : ViewModel> create(modelClass: Class<T>): T{
                val filmInfoDao = (requireActivity().application as App).db.filmInfoDao()
                return DBViewModel(filmInfoDao) as T
            }
        }
    }
    private val watchedFilmsAdapter = ListAdapter { item -> onWatchedFilmClick(item) }
    private val interestingFilmsAdapter = ListAdapter { item -> onInterestingFilmClick(item) }
    private val collectionsAdapter = ProfileCollectionsAdapter ({ item -> onButtonDeleteCollectionClick(item)}, {item -> onCollectionClick(item)})

    private val rep = Repository()
    private val extensions = Extensions()
    private val collectionsData = mutableListOf<ProfileCollection>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.watchedFilmsRecyclerView.adapter = watchedFilmsAdapter
        binding.interestingFilmsRecyclerView.adapter = interestingFilmsAdapter
        binding.collectionsRecyclerView.adapter = collectionsAdapter

        showFilms(WATCHED)
        showFilms(INTERESTING)
        showCollections()
        addCollection()
        showAllFilms()

        binding.buttonCleanWatched.setOnClickListener {
            cleanCollection(WATCHED)
        }

        binding.buttonCleanInteresting.setOnClickListener {
            cleanCollection(INTERESTING)
        }

        binding.bottomNav.setOnItemSelectedListener {
            when(it.itemId){
                R.id.home -> findNavController().navigate(R.id.homeFragment)
                R.id.search -> findNavController().navigate(R.id.searchFragment)
            }
            true
        }
    }

    override fun onResume() {
        super.onResume()
        if (rep.getCollection(requireActivity(), WATCHED) != "")
            binding.buttonCleanWatched.visibility = View.VISIBLE
        else binding.buttonCleanWatched.visibility = View.GONE

        if (rep.getCollection(requireActivity(), INTERESTING) != "")
            binding.buttonCleanInteresting.visibility = View.VISIBLE
        else binding.buttonCleanInteresting.visibility = View.GONE

        binding.bottomNav.menu.get(2).isChecked = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onWatchedFilmClick(item: Item) {
        val bundle = bundleOf(KINOPOISK_ID to item.kinopoiskID)
        findNavController().navigate(R.id.action_profileFragment_to_filmInfoFragment, bundle)
    }

    private fun onInterestingFilmClick(item: Item) {
        val bundle = bundleOf(KINOPOISK_ID to item.kinopoiskID)
        findNavController().navigate(R.id.action_profileFragment_to_filmInfoFragment, bundle)
    }

    private fun onCollectionClick(item: ProfileCollection){
        val bundle = bundleOf(COLLECTION_NAME to item.collectionName)
        findNavController().navigate(R.id.action_profileFragment_to_allItemsFragment, bundle)
    }

    private fun showAllFilms(){
        binding.buttonAllWatched.setOnClickListener {
            val bundle = bundleOf(COLLECTION_NAME to WATCHED)
            findNavController().navigate(R.id.action_profileFragment_to_allItemsFragment, bundle)
        }

        binding.buttonAllInteresting.setOnClickListener {
            val bundle = bundleOf(COLLECTION_NAME to INTERESTING)
            findNavController().navigate(R.id.action_profileFragment_to_allItemsFragment, bundle)
        }
    }

    private fun onButtonDeleteCollectionClick(item: ProfileCollection){
        val deleteDialog = Dialog(requireActivity())
        val deleteBinding = DeleteCollectionDialogBinding.inflate(layoutInflater)
        deleteDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        deleteDialog.setContentView(deleteBinding.root)
        deleteDialog.show()
        deleteDialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        deleteDialog.window?.setGravity(Gravity.CENTER)
        deleteDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        if (item.collectionName == FAVOURITES || item.collectionName == BOOKMARK)
            deleteBinding.textView.text = "Изволите опорожнить коллекцию?"

        deleteBinding.buttonPositive.setOnClickListener {
            if (item.collectionName == FAVOURITES || item.collectionName == BOOKMARK)
                rep.saveCollection(requireActivity(), "", item.collectionName)
            else{
                rep.deleteCollection(requireActivity(), item.collectionName)
                val collectionsNamesList = extensions.convertStringToList(rep.getCollectionsNames(requireActivity())).toMutableList()
                collectionsNamesList.remove(item.collectionName)
                rep.saveCollectionsNames(requireActivity(), extensions.convertListToString(collectionsNamesList))
            }
            showCollections()
            deleteDialog.hide()
        }

        deleteBinding.buttonNegative.setOnClickListener {
            deleteDialog.hide()
        }
    }

    private fun cleanCollection(collectionName: String){
        val deleteDialog = Dialog(requireActivity())
        val deleteBinding = DeleteCollectionDialogBinding.inflate(layoutInflater)
        deleteDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        deleteDialog.setContentView(deleteBinding.root)
        deleteDialog.show()
        deleteDialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        deleteDialog.window?.setGravity(Gravity.CENTER)
        deleteDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        deleteBinding.textView.text = "Изволите опорожнить коллекцию?"

        deleteBinding.buttonPositive.setOnClickListener {
            rep.saveCollection(requireActivity(), "", collectionName)
            showFilms(collectionName)
            if (collectionName == WATCHED)
                binding.buttonCleanWatched.visibility = View.GONE
            else
                binding.buttonCleanInteresting.visibility = View.GONE

            deleteDialog.hide()
        }

        deleteBinding.buttonNegative.setOnClickListener {
            deleteDialog.hide()
        }
    }

    private fun showFilms(collectionName: String){
        var watched = false
        if (collectionName == WATCHED)
            watched = true

        lifecycleScope.launchWhenCreated {
            val filmsIDList = extensions.convertStringToList(rep.getCollection(requireActivity(), collectionName))
            val dBFilmsList = mutableListOf<MaxFilmInfo>()
            val filmsList = mutableListOf<Item>()

            filmsIDList.forEach {
                dBFilmsList.add(dBViewModel.getMaxFilmInfoByID(it.toLong()))
            }

            dBFilmsList.forEach {
                val countries = extensions.getCountriesList(it.country)
                val genres = extensions.getGenresList(it.genre)
                filmsList.add(Item(watched, null, it.filmInfo.filmID, null, null, it.filmInfo.nameRu, it.filmInfo.nameEn, it.filmInfo.nameOriginal, it.filmInfo.ratingKinopoisk, it.filmInfo.ratingImdb, null,null, it.filmInfo.posterURL, null,null, null, countries, genres, null, null, null))
            }

            filmsList.reverse()
            if (collectionName == WATCHED){
                if (filmsList.size != 0){
                    binding.buttonAllWatched.text = filmsList.size.toString()
                    binding.buttonAllWatched.visibility = View.VISIBLE
                }
                else binding.buttonAllWatched.visibility = View.GONE
                watchedFilmsAdapter.submitList(filmsList)
            }
            else {
                if (filmsList.size != 0){
                    binding.buttonAllInteresting.text = filmsList.size.toString()
                    binding.buttonAllInteresting.visibility = View.VISIBLE
                }
                else binding.buttonAllInteresting.visibility = View.GONE
                interestingFilmsAdapter.submitList(filmsList)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showCollections(){
        val collectionsNamesList = extensions.convertStringToList(rep.getCollectionsNames(requireActivity()))
        val collectionsFilmsAmountList = mutableListOf<Int>()

        collectionsNamesList.forEach { collectionName ->
            collectionsFilmsAmountList.add(extensions.convertStringToList(rep.getCollection(requireActivity(),collectionName)).size)
        }

        collectionsData.clear()
        for (i in collectionsNamesList.indices){
            collectionsData.add(ProfileCollection(collectionsNamesList[i], collectionsFilmsAmountList[i]))
        }

        collectionsAdapter.submitList(collectionsData)
        collectionsAdapter.notifyDataSetChanged()
    }

    private fun addCollection(){
        binding.addCollection.setOnClickListener {
            val addDialog = Dialog(requireActivity())
            val addBinding = CentralSheetBinding.inflate(layoutInflater)
            addDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            addDialog.setContentView(addBinding.root)
            addDialog.show()
            addDialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            addDialog.window?.setGravity(Gravity.CENTER)
            addDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            addBinding.buttonSave.setOnClickListener {
                val collectionsNamesList = extensions.convertStringToList(rep.getCollectionsNames(requireContext())).toMutableList()

                if (!collectionsNamesList.contains(addBinding.editText.text.toString()) && addBinding.editText.text.toString() != ""){
                    collectionsNamesList.add(addBinding.editText.text.toString())
                    rep.saveCollection(requireActivity(), "", addBinding.editText.text.toString())
                    rep.saveCollectionsNames(requireActivity(), extensions.convertListToString(collectionsNamesList))
                    showCollections()
                }
                addDialog.hide()
            }

            addBinding.buttonDismiss.setOnClickListener {
                addDialog.hide()
            }
        }
    }
}