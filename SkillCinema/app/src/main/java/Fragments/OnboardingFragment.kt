package Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.skillcinema.App
import DataModels.CountryID
import com.example.skillcinema.DBViewModel
import DataModels.GenreID
import com.example.skillcinema.HomeViewModel
import Adapters.OnboardingAdapter
import com.example.skillcinema.R
import com.example.skillcinema.Repository
import DataModels.SearchSettings
import com.example.skillcinema.databinding.FragmentOnboardingBinding
import kotlinx.coroutines.delay

private const val FAVOURITES = "Любимое"
private const val BOOKMARK = "Хочу посмотреть"
class OnboardingFragment : Fragment() {

    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!
    private val adapter = OnboardingAdapter()
    private val rep = Repository()
    private val viewModel: HomeViewModel by viewModels()
    private val dBViewModel: DBViewModel by viewModels {
        object : ViewModelProvider.Factory{
            override fun <T : ViewModel> create(modelClass: Class<T>): T{
                val filmInfoDao = (requireActivity().application as App).db.filmInfoDao()
                return DBViewModel(filmInfoDao) as T
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setDefaultSettings()

        binding.buttonSkip.setOnClickListener {
            rep.saveFirstLoadCheck(requireActivity())
            findNavController().navigate(R.id.action_onboardingFragment_to_registrationFragment)
        }

        binding.viewPager.adapter = adapter
        binding.viewPager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int){
                super.onPageSelected(position)
                when(position){
                    0 -> {
                        binding.circle1.setBackgroundResource(R.drawable.onboarding_active_background)
                        binding.circle2.setBackgroundResource(R.drawable.onboarding_inactive_background)
                        binding.circle3.setBackgroundResource(R.drawable.onboarding_inactive_background)
                    }
                    1 -> {
                        binding.circle1.setBackgroundResource(R.drawable.onboarding_inactive_background)
                        binding.circle2.setBackgroundResource(R.drawable.onboarding_active_background)
                        binding.circle3.setBackgroundResource(R.drawable.onboarding_inactive_background)
                    }
                    2 -> {
                        binding.circle1.setBackgroundResource(R.drawable.onboarding_inactive_background)
                        binding.circle2.setBackgroundResource(R.drawable.onboarding_inactive_background)
                        binding.circle3.setBackgroundResource(R.drawable.onboarding_active_background)
                    }
                    3 -> {
                        binding.circle1.visibility = View.INVISIBLE
                        binding.circle2.visibility = View.INVISIBLE
                        binding.circle3.visibility = View.INVISIBLE
                        binding.buttonSkip.visibility = View.INVISIBLE
                        lifecycleScope.launchWhenCreated {
                            rep.saveFirstLoadCheck(requireActivity())
                            delay(2000)
                            findNavController().navigate(R.id.action_onboardingFragment_to_registrationFragment)
                        }
                    }
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setDefaultSettings(){
        rep.saveCollectionsNames(requireActivity(), "$FAVOURITES,$BOOKMARK")
        rep.saveCollection(requireContext(), "", FAVOURITES)
        rep.saveCollection(requireContext(), "", BOOKMARK)

        dBViewModel.addSearchSettings(SearchSettings(1, 33,13,"RATING", "FILM", 1, 10, 1917, 1991, false))
    }
}