package Adapters

import Fragments.FilmographyViewPagerFragment
import Fragments.GalleryViewPagerFragment
import Fragments.SeasonsViewPagerFragment
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import DataModels.Series

class ViewPagerAdapter(
    fragment: Fragment,
    private val tabTypes: MutableList<String>?,
    private val kinopoiskID: Long?,
    private val tabMap: MutableMap<String, MutableList<MutableMap<String, String>>>?,
    private val series: Series?,
    ) : FragmentStateAdapter(fragment){

    override fun getItemCount(): Int = tabTypes?.size ?: tabMap?.size ?: series!!.total!!.toInt()

    override fun createFragment(position: Int): Fragment {
        return if (kinopoiskID != null && tabTypes != null)
            GalleryViewPagerFragment(kinopoiskID, tabTypes[position])
        else if(series != null)
            SeasonsViewPagerFragment(series.items[position])
        else FilmographyViewPagerFragment(tabMap!!.keys.elementAt(position), tabMap)
    }
}