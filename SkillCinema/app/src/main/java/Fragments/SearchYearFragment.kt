package Fragments

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.skillcinema.App
import com.example.skillcinema.DBViewModel
import com.example.skillcinema.Repository
import DataModels.SearchSettings
import Adapters.YearViewPagerAdapter
import com.example.skillcinema.databinding.*
import com.example.skillcinema.utils.Extensions
import java.util.*

class SearchYearFragment : Fragment(){

    private var _binding: FragmentSearchYearBinding? = null
    private val binding get() = _binding!!
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
    lateinit var adapterSince: YearViewPagerAdapter
    lateinit var adapterTo: YearViewPagerAdapter
    private var yearSince = 1895
    private var yearTo = 3000
    private var positionSince = 0
    private var positionTo = 0
    lateinit var settings: SearchSettings

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSearchYearBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launchWhenStarted {
            settings = dBViewModel.getSearchSettings()
            val data = mutableListOf<String>()
            for (i in 1895..Calendar.getInstance().get(Calendar.YEAR))
                data.add(i.toString())
            positionSince = calculateStartPage(Calendar.getInstance().get(Calendar.YEAR)) -1
            positionTo = calculateStartPage(Calendar.getInstance().get(Calendar.YEAR)) - 1
            var itemsList = extensions.getYearsPage(data, positionSince)
            var period = "${itemsList[0]} - ${itemsList[itemsList.lastIndex]}"
            binding.yearPeriodSinceTextView.text = period
            itemsList = extensions.getYearsPage(data, positionTo)
            period = "${itemsList[0]} - ${itemsList[itemsList.lastIndex]}"
            binding.yearPeriodToTextView.text = period
            adapterSince =  YearViewPagerAdapter(data, requireActivity(), settings, yearTo){year -> onSinceClick(year)}
            adapterTo = YearViewPagerAdapter(data, requireActivity(), settings, yearSince){year -> onToClick(year)}
            binding.viewPagerSince.adapter = adapterSince
            binding.viewPagerTo.adapter = adapterTo
           //Log.d("YEARFROM", calculateStartPage(settings.yearFrom).toString())
           //Log.d("YEARFTO", calculateStartPage(settings.yearTo).toString())
            binding.viewPagerSince.currentItem = positionSince
            binding.viewPagerTo.currentItem = positionTo

            binding.viewPagerSince.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback(){
                override fun onPageSelected(position: Int){
                    super.onPageSelected(position)
                    itemsList = extensions.getYearsPage(data, position)
                    period = "${itemsList[0]} - ${itemsList[itemsList.lastIndex]}"
                    binding.yearPeriodSinceTextView.text = period
                    positionSince = position
                }

                override fun onPageScrollStateChanged(state: Int) {
                    super.onPageScrollStateChanged(state)
                    if (state == 1) {
                        yearSince = 1895
                        adapterSince.notifyItemChanged(positionSince)
                    }
                }
            })

            binding.viewPagerTo.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback(){
                override fun onPageSelected(position: Int){
                    super.onPageSelected(position)
                    itemsList = extensions.getYearsPage(data, position)
                    period = "${itemsList[0]} - ${itemsList[itemsList.lastIndex]}"
                    binding.yearPeriodToTextView.text = period
                    positionTo = position
                }

                override fun onPageScrollStateChanged(state: Int) {
                    super.onPageScrollStateChanged(state)
                    if (state == 1){
                        adapterTo.notifyItemChanged(positionTo)
                        yearTo = 3000
                    }
                }
            })

            binding.buttonLessSince.setOnClickListener {
                if (positionSince != 0)
                    positionSince--
                binding.viewPagerSince.setCurrentItem(positionSince, true)
                adapterSince.notifyItemChanged(positionSince)
                yearSince = 1895
            }

            binding.buttonMoreSince.setOnClickListener {
                if (positionSince < adapterSince.itemCount-1)
                    positionSince++
                binding.viewPagerSince.setCurrentItem(positionSince, true)
                adapterSince.notifyItemChanged(positionSince)
                yearSince = 1895
            }

            binding.buttonLessTo.setOnClickListener {
                if (positionTo != 0)
                    positionTo--
                binding.viewPagerTo.setCurrentItem(positionTo, true)
                adapterTo.notifyItemChanged(positionTo)
                yearTo = 3000
            }

            binding.buttonMoreTo.setOnClickListener {
                if (positionTo < adapterTo.itemCount-1)
                    positionTo++
                binding.viewPagerTo.setCurrentItem(positionTo, true)
                adapterTo.notifyItemChanged(positionTo)
                yearTo = 3000
            }
        }

        binding.buttonSave.setOnClickListener {
            if (yearSince > yearTo) Toast.makeText(requireContext(), "Период выбран некорректно", Toast.LENGTH_LONG).show()
            else {
                settings.yearFrom = yearSince
                settings.yearTo = yearTo
                dBViewModel.addSearchSettings(settings)
                findNavController().navigateUp()
            }
        }

        binding.buttonBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onSinceClick(year: String) {
        if (year != "")
            yearSince = year.toInt()
    }

    private fun onToClick(year:String) {
        if (year != "")
            yearTo = year.toInt()
    }

    private fun calculateStartPage(year: Int): Int{
        val page = (year - 1894) / 12
        return if ((year - 1894) % 12 == 0) page else page + 1
    }
}
