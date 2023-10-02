package Fragments

import DataModels.CountryID
import DataModels.GenreID
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.skillcinema.App
import com.example.skillcinema.DBViewModel
import com.example.skillcinema.HomeViewModel
import com.example.skillcinema.R
import com.example.skillcinema.Repository
import com.example.skillcinema.databinding.FragmentRegistrationBinding
import kotlinx.coroutines.launch


class RegistrationFragment : Fragment() {

    private var _binding: FragmentRegistrationBinding? = null
    private val binding get() = _binding!!
    private val rep = Repository()
    private val viewModel: HomeViewModel by viewModels()
    private val dBViewModel: DBViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T{
                val filmInfoDao = (requireActivity().application as App).db.filmInfoDao()
                return DBViewModel(filmInfoDao) as T
            }
        }
    }
    private var load = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentRegistrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        textViewWork()
        editTextWork()
        buttonsWork()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun textViewWork(){
        binding.textView.setAnimationDuration(300)

        binding.textView.setOnClickListener {
            if (binding.textView.isExpanded)
                binding.textView.collapse()
            else{
                binding.editText.clearFocus()
                binding.textView.expand()
            }
        }
    }

    private fun editTextWork(){
        binding.editText.setOnFocusChangeListener { view, b ->
            if (b)
                binding.textView.collapse()
            else binding.textView.expand()
        }

        binding.editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null){
                    if (s.length == 36){
                        val list = s.toString().split("-")
                        if (list[0].length == 8 && list[1].length == 4 && list[2].length == 4 && list[3].length == 4 && list[4].length == 12)
                            loadAllCountryGenreID(s.toString())
                    }
                    else if (s.toString() != "") {
                        binding.editText.setText("")
                        binding.buttonSave.visibility = View.GONE
                    }
                }
                else binding.buttonSave.visibility = View.GONE
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
    }

    private fun buttonsWork(){
        binding.buttonGetKey.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://kinopoiskapiunofficial.tech"))
            startActivity(browserIntent)
        }

        binding.buttonSave.setOnClickListener {
            rep.saveApiKey(requireActivity(), binding.editText.text.toString())
            findNavController().navigate(R.id.action_registrationFragment_to_homeFragment)
        }

        lifecycleScope.launch {
            viewModel.countryGenreIDSaving.collect{
                if (!it && load)
                    binding.buttonSave.visibility = View.VISIBLE
                else binding.buttonSave.visibility = View.GONE
            }
        }
    }

    private fun loadAllCountryGenreID(apikey: String){
        viewModel.loadCountryGenreID(apikey)
        load = true
        lifecycleScope.launchWhenStarted {
            viewModel.idLoading.collect{ idLoading ->
                if (!idLoading){
                    viewModel.countryGenreID.collect{ countryGenreID->
                        countryGenreID.countries.forEach {
                            if (it.country != "" && it.id != null)
                                dBViewModel.addCountryID(CountryID(it.id.toInt(), it.country))
                        }

                        countryGenreID.genres.forEach {
                            if (it.genre != "" && it.id != null)
                                dBViewModel.addGenreID(GenreID(it.id.toInt(), it.genre))
                        }

                        lifecycleScope.launch {
                            dBViewModel.allCountriesIDFlow.collect{ countriesList ->
                                val countriesIDList = mutableListOf<Int>()
                                countriesList.forEach {
                                    countriesIDList.add(it.id)
                                }
                                if (countriesIDList.contains(countryGenreID.countries[countryGenreID.countries.lastIndex].id!!.toInt()))
                                    viewModel._countryIDSaving.value = false
                            }
                        }

                        lifecycleScope.launch {
                            dBViewModel.allGenresIDFlow.collect{ genresList ->
                                val genresIDList = mutableListOf<Int>()
                                genresList.forEach {
                                    genresIDList.add(it.id)
                                }
                                if (genresIDList.contains(countryGenreID.genres[countryGenreID.genres.lastIndex].id!!.toInt()))
                                    viewModel._genreIDSaving.value = false
                            }
                        }
                    }
                }
            }
        }
    }
}