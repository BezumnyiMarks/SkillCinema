package Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.get
import androidx.navigation.fragment.findNavController
import DataModels.Item
import com.example.skillcinema.R
import Adapters.ViewPagerAdapter
import com.example.skillcinema.databinding.FragmentFilmographyBinding
import com.example.skillcinema.utils.Extensions
import com.google.android.material.tabs.TabLayoutMediator

private const val KINOPOISK_ID = "kinopoiskID"
private const val STAFF_FILMS_ID = "staffFilmsID"
private const val PROFESSION_KEYS = "profesionKeys"
private const val SEX = "sex"
private const val NAME = "name"
class FilmographyFragment : Fragment() {
    private var _binding: FragmentFilmographyBinding? = null
    private val binding get() = _binding!!
    private val extensions = Extensions()

    private var kinopoiskID: Long? = null
    private var staffFilmsID: String? = null
    private var profesionKeys: String? = null
    private var sex: String? = null
    private var name: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            kinopoiskID = it.getLong(KINOPOISK_ID)
            staffFilmsID = it.getString(STAFF_FILMS_ID)
            profesionKeys = it.getString(PROFESSION_KEYS)
            sex = it.getString(SEX)
            name = it.getString(NAME)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFilmographyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showData()

        binding.buttonBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.bottomNav.setOnItemSelectedListener {
            when(it.itemId){
                R.id.home -> findNavController().navigate(R.id.homeFragment)
                R.id.search -> findNavController().navigate(R.id.searchFragment)
                R.id.profile -> findNavController().navigate(R.id.profileFragment)
            }
            true
        }

        binding.bottomNav.menu.get(0).isChecked = false
        binding.bottomNav.menu.get(1).isChecked = false
        binding.bottomNav.menu.get(2).isChecked = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onImageClick(item: Item) {

    }

    private fun showData() {
        val professionKeysList = extensions.convertStringToList(profesionKeys)
        val filmsIDList = extensions.convertStringToList(staffFilmsID)
        val filmsMapList = mutableListOf<MutableMap<String, String>>()
        val tabMap = mutableMapOf<String, MutableList<MutableMap<String, String>>>()

        for (i in filmsIDList.indices){
            filmsMapList.add(mutableMapOf(professionKeysList[i] to filmsIDList[i]))
        }
        for (i in filmsMapList.indices){
            val currentMapList = mutableListOf<MutableMap<String, String>>()
            when(filmsMapList[i].keys.elementAt(0)){
                "ACTOR" -> {
                        tabMap.values.forEach { values ->
                            values.forEach {
                                if (it.keys.elementAt(0) == "ACTOR")
                                    currentMapList.add(it)
                            }
                        }
                        currentMapList.add(filmsMapList[i])
                    if (sex == "MALE"){
                        tabMap["Актёр"] = currentMapList
                    }
                    else tabMap["Актёрка"] = currentMapList
                }
                "HIMSELF" -> {
                    tabMap.values.forEach { values ->
                        values.forEach {
                            if (it.keys.elementAt(0) == "HIMSELF")
                                currentMapList.add(it)
                        }
                    }
                    currentMapList.add(filmsMapList[i])
                    tabMap["Актёр играет себя"] = currentMapList
                }
                "HERSELF" -> {
                    tabMap.values.forEach { values ->
                        values.forEach {
                            if (it.keys.elementAt(0) == "HERSELF")
                                currentMapList.add(it)
                        }
                    }
                    currentMapList.add(filmsMapList[i])
                    tabMap["Актёрка играет себя"] = currentMapList
                }
                "WRITER" -> {
                    tabMap.values.forEach { values ->
                        values.forEach {
                            if (it.keys.elementAt(0) == "WRITER")
                                currentMapList.add(it)
                        }
                    }
                    currentMapList.add(filmsMapList[i])
                    if (sex == "MALE"){
                        tabMap["Сценарист"] = currentMapList
                    }
                    else tabMap["Сценарка"] = currentMapList
                }
                "DIRECTOR" -> {
                    tabMap.values.forEach { values ->
                        values.forEach {
                            if (it.keys.elementAt(0) == "DIRECTOR")
                                currentMapList.add(it)
                        }
                    }
                    currentMapList.add(filmsMapList[i])
                    if (sex == "MALE"){
                        tabMap["Режиссёр"] = currentMapList
                    }
                    else tabMap["Режиссёрка"] = currentMapList
                }
                "PRODUCER" -> {
                    tabMap.values.forEach { values ->
                        values.forEach {
                            if (it.keys.elementAt(0) == "PRODUCER")
                                currentMapList.add(it)
                        }
                    }
                    currentMapList.add(filmsMapList[i])
                    if (sex == "MALE"){
                        tabMap["Продюсер"] = currentMapList
                    }
                    else tabMap["Продюсерка"] = currentMapList
                }
                "EDITOR" -> {
                    tabMap.values.forEach { values ->
                        values.forEach {
                            if (it.keys.elementAt(0) == "EDITOR")
                                currentMapList.add(it)
                        }
                    }
                    currentMapList.add(filmsMapList[i])
                    if (sex == "MALE"){
                        tabMap["Редактор"] = currentMapList
                    }
                    else tabMap["Редакторка"] = currentMapList
                }
                "COMPOSER" -> {
                    tabMap.values.forEach { values ->
                        values.forEach {
                            if (it.keys.elementAt(0) == "COMPOSER")
                                currentMapList.add(it)
                        }
                    }
                    currentMapList.add(filmsMapList[i])
                    if (sex == "MALE"){
                        tabMap["Композитор"] = currentMapList
                    }
                    else tabMap["Композиторка"] = currentMapList
                }
                "VOICE_MALE" -> {
                    tabMap.values.forEach { values ->
                        values.forEach {
                            if (it.keys.elementAt(0) == "VOICE_MALE")
                                currentMapList.add(it)
                        }
                    }
                    currentMapList.add(filmsMapList[i])
                    tabMap["Актёр дубляжа"] = currentMapList
                }
                "VOICE_FEMALE" -> {
                    tabMap.values.forEach { values ->
                        values.forEach {
                            if (it.keys.elementAt(0) == "VOICE_FEMALE")
                                currentMapList.add(it)
                        }
                    }
                    currentMapList.add(filmsMapList[i])
                    tabMap["Актёрка дубляжа"] = currentMapList
                }
                "HRONO_TITR_MALE" -> {
                    tabMap.values.forEach { values ->
                        values.forEach {
                            if (it.keys.elementAt(0) == "HRONO_TITR_MALE")
                                currentMapList.add(it)
                        }
                    }
                    currentMapList.add(filmsMapList[i])
                    tabMap["Второстепенная роль"] = currentMapList
                }
                "HRONO_TITR_FEMALE" -> {
                    tabMap.values.forEach { values ->
                        values.forEach {
                            if (it.keys.elementAt(0) == "HRONO_TITR_FEMALE")
                                currentMapList.add(it)
                        }
                    }
                    currentMapList.add(filmsMapList[i])
                    tabMap["Второстепенная роль"] = currentMapList
                }
                else -> {
                    tabMap.values.forEach { values ->
                        values.forEach {
                            if (it.keys.elementAt(0) == filmsMapList[i].keys.elementAt(0))
                                currentMapList.add(it)
                        }
                    }
                    currentMapList.add(filmsMapList[i])
                    if (sex == "MALE"){
                        tabMap[filmsMapList[i].keys.elementAt(0)] = currentMapList
                    }
                    else tabMap[filmsMapList[i].keys.elementAt(0)] = currentMapList
                }
            }
        }

        binding.textViewName.text = name

        binding.viewPager.adapter = ViewPagerAdapter(this, null, null, tabMap, null)
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabMap.keys.elementAt(position)
        }.attach()

        for (i in tabMap.keys.indices){
            val item = LayoutInflater.from(requireContext()).inflate(R.layout.tablayout_item, null) as ConstraintLayout
            binding.tabLayout.getTabAt(i)?.customView = item
        }
    }
}
