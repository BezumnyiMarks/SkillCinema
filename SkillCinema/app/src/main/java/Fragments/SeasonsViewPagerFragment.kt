package Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.skillcinema.R
import DataModels.Season
import Adapters.SeasonsAdapter
import com.example.skillcinema.databinding.FragmentSeasonsViewPagerBinding

class SeasonsViewPagerFragment(private val season: Season) : Fragment() {
    private var _binding: FragmentSeasonsViewPagerBinding? = null
    private val binding get() = _binding!!
    private val seasonsAdapter = SeasonsAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSeasonsViewPagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val seasonsText = "${season.number} сезон," +
                " ${resources.getQuantityString(R.plurals.series, season.episodes.size, season.episodes.size)}"
        binding.seasonTextView.text = seasonsText
        binding.seasonRecyclerView.adapter = seasonsAdapter
        seasonsAdapter.submitList(season.episodes)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}