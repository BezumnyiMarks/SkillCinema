package Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.skillcinema.R
import DataModels.SearchSettings
import com.example.skillcinema.databinding.YearViewPagerModelBinding
import com.example.skillcinema.utils.Extensions

class YearViewPagerAdapter(
    _data: MutableList<String>,
    _context: Context,
    _settings: SearchSettings,
    _oppositeYear: Int,
    private val onClick: (String) -> Unit
): RecyclerView.Adapter<YearViewPagerViewHolder>(){
    private var data = _data
    private val context = _context
    private val extensions = Extensions()
    private val settings = _settings
    private val oppositeYear = _oppositeYear

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): YearViewPagerViewHolder {
        return YearViewPagerViewHolder(
            YearViewPagerModelBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: YearViewPagerViewHolder, position: Int) {
        val yearsPage = extensions.getYearsPage(data, position)
        val viewsList = listOf(
            holder.binding.yearTextView1, holder.binding.yearTextView2, holder.binding.yearTextView3,
            holder.binding.yearTextView4, holder.binding.yearTextView5, holder.binding.yearTextView6,
            holder.binding.yearTextView7, holder.binding.yearTextView8, holder.binding.yearTextView9,
            holder.binding.yearTextView10, holder.binding.yearTextView11, holder.binding.yearTextView12
        )
        viewsList.forEachIndexed { index, textView ->
            var yearString = ""
            if (index < yearsPage.size)
                yearString = yearsPage[index]

            if (yearString != ""){
                textView.text = yearString
                textView.setTextColor(ContextCompat.getColor(context, R.color.text_black))
                textView.background = null

             // if (adapterID == "Since" && yearString.toInt() > oppositeYear && oppositeYear != 0){
             //     textView.setTextColor(ContextCompat.getColor(context, R.color.recycler_image_foreground))
             //     textView.isCursorVisible = false
             // }

             // if (adapterID == "To" && yearString.toInt() < oppositeYear && oppositeYear != 0){
             //     textView.setTextColor(ContextCompat.getColor(context, R.color.recycler_image_foreground))
             //     textView.isCursorVisible = false
             // }
                //extensions.setYearsPagerViews(yearString, yearsPage, textView, viewsList, context)
            }
            else textView.setTextColor(ContextCompat.getColor(context, R.color.transparent))

            textView.setOnClickListener {
                onClick(yearString)
                extensions.setYearsPagerViews(yearString, yearsPage, textView, viewsList, context)
            }

           // if (yearString == settings.yearFrom.toString() && adapterID == "Since")
           //     extensions.setYearsPagerViews(settings.yearFrom.toString(), yearsPage, textView, viewsList, context)
           // if (yearString == settings.ratingTo.toString() && adapterID == "To")
           //     extensions.setYearsPagerViews(settings.yearTo.toString(), yearsPage, textView, viewsList, context)
        }
    }

    override fun getItemCount(): Int{
        return if (data.size % 12 == 0)
            data.size/12
        else data.size/12 + 1
    }
}

class YearViewPagerViewHolder (val binding: YearViewPagerModelBinding) : RecyclerView.ViewHolder(binding.root)