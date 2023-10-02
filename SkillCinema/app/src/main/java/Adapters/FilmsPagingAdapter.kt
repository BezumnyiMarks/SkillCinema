package Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.Glide
import DataModels.Film
import com.example.skillcinema.databinding.HomeRecyclerItemModelBinding
import com.example.skillcinema.utils.Extensions

class FilmsPagingAdapter(private val onClick: (Film) -> Unit) : PagingDataAdapter<Film, RecyclerViewHolder>(
    FilmsPagingAdapterDiffUtilCallback()
) {
    private val extensions = Extensions()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        return RecyclerViewHolder(
            HomeRecyclerItemModelBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            if (item.watched == true){
                with(holder.binding){
                    watchedIcon.visibility = View.VISIBLE
                    foreground.visibility = View.VISIBLE
                }
            }
            else{
                with(holder.binding){
                    watchedIcon.visibility = View.GONE
                    foreground.visibility = View.GONE
                }
            }
        }
        val genresSet = item?.let { extensions.getGenreSet(it.genres!!) }
        with(holder.binding) {
            name.text = item?.nameRu ?: item?.nameEn ?: ""
            genre.text = genresSet
            if (item != null && item.rating != null) {
                rating.text = if(item.rating[item.rating.lastIndex] == '%') convertRatingValue(item.rating)
                else item.rating
            }else rating.visibility = View.GONE
            item?.let {
                Glide
                    .with(recyclerImageView.context)
                    .load(android.net.Uri.parse(it.posterURL))
                    .centerCrop()
                    .into(recyclerImageView)
            }
        }
        holder.binding.root.setOnClickListener {
            if (item != null) {
                onClick(item)
            }
        }
    }

    private fun convertRatingValue(rating: String): String{
        var normalRatingValueMadeBySmartHumanByConvertingShittyValueTakenFromCrappyKinopoiskApi = ""
        if (rating != "100.0%" && rating != "100.00%" && rating != "100%") {
            var ratingValueDouble = (rating[0].toString() + "." + rating[1].toString()).toDouble()
            var decimalPartStr = ""
            var decimalPart = 0
            var i = 0
            while (rating[i] != '.')
                i++
            i += 1
            while (rating[i] != '%') {
                decimalPartStr += rating[i].toString()
                i++
            }
            decimalPart = decimalPartStr[0].toString().toInt()

            if (decimalPartStr.length == 2)
            if (decimalPartStr[1].toString().toInt() >= 5)
                decimalPart += 1

            if (decimalPart >= 5)
                ratingValueDouble += 0.1F

            for (i in ratingValueDouble.toString().indices) {
                normalRatingValueMadeBySmartHumanByConvertingShittyValueTakenFromCrappyKinopoiskApi += ratingValueDouble.toString()[i].toString()
                if (ratingValueDouble.toString()[i] == '.') {
                    normalRatingValueMadeBySmartHumanByConvertingShittyValueTakenFromCrappyKinopoiskApi += ratingValueDouble.toString()[i + 1].toString()
                    break
                }
            }
        }else normalRatingValueMadeBySmartHumanByConvertingShittyValueTakenFromCrappyKinopoiskApi = "10.0"
        return normalRatingValueMadeBySmartHumanByConvertingShittyValueTakenFromCrappyKinopoiskApi
    }
}

class FilmsPagingAdapterDiffUtilCallback : DiffUtil.ItemCallback<Film>() {
    override fun areItemsTheSame(oldItem: Film, newItem: Film): Boolean =
        oldItem.filmID == newItem.filmID

    override fun areContentsTheSame(oldItem: Film, newItem: Film): Boolean =
        oldItem == newItem
}
