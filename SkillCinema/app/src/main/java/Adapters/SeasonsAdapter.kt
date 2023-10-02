package Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import DataModels.Episode
import com.example.skillcinema.databinding.SeasonsRecyclerItemModelBinding
import java.time.LocalDate

class SeasonsAdapter() : ListAdapter<Episode, SeasonRecyclerViewHolder>(
    SeasonsAdapterDiffUtilCallback()
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SeasonRecyclerViewHolder {
        return SeasonRecyclerViewHolder(
            SeasonsRecyclerItemModelBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: SeasonRecyclerViewHolder, position: Int) {
        val item = getItem(position)
        with(holder.binding) {
            val seasonsText = "${item.episodeNumber ?: "Неизвестная"} серия. ${item.nameRu ?: item.nameEn ?: ""}"
            seasonTextView.text = seasonsText

            if(item.releaseDate != null && item.releaseDate != ""){
                val formattedDate = LocalDate.parse("${item.releaseDate}")
                val dateText = "${formattedDate.dayOfMonth} ${getMonth(formattedDate.month.toString())} ${formattedDate.year}"
                dateTextView.text = dateText
            }
        }
    }

    private fun getMonth(month: String): String{
        return when(month){
            "JANUARY" -> "Января"
            "FEBRUARY" -> "Февраля"
            "MARCH" -> "Марта"
            "APRIL" -> "Апреля"
            "MAY" -> "Мая"
            "JUNE" -> "Июня"
            "JULY" -> "Июля"
            "AUGUST" -> "Августа"
            "SEPTEMBER" -> "Сентября"
            "OCTOBER" -> "Октября"
            "NOVEMBER" -> "Ноября"
            else -> "Декабря"
        }
    }
}

class SeasonsAdapterDiffUtilCallback : DiffUtil.ItemCallback<Episode>() {
    override fun areItemsTheSame(oldItem: Episode, newItem: Episode): Boolean =
        oldItem.episodeNumber == newItem.episodeNumber

    override fun areContentsTheSame(oldItem: Episode, newItem: Episode): Boolean =
        oldItem == newItem
}

class SeasonRecyclerViewHolder (val binding: SeasonsRecyclerItemModelBinding) : RecyclerView.ViewHolder(binding.root)