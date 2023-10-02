package Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.Glide
import DataModels.Item
import com.example.skillcinema.databinding.HomeRecyclerItemModelBinding
import com.example.skillcinema.utils.Extensions

class ItemsPagingAdapter(private val onClick: (Item) -> Unit) : PagingDataAdapter<Item, RecyclerViewHolder>(
    ItemsPagingAdapterDiffUtilCallback()
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
        val genresSet = item?.let { it.genres?.let { genre -> extensions.getGenreSet(genre) } }
        val rating = item?.ratingKinopoisk ?: ""
        with(holder.binding) {
            name.text = item?.nameRu ?: item?.nameEn ?: item?.nameOriginal ?: ""
            genre.text = genresSet
            if (rating == "")
                this.rating.visibility = View.GONE
            else this.rating.text = rating.toString()
            if (item!!.posterURL != null) {
                Glide
                    .with(recyclerImageView.context)
                    .load(android.net.Uri.parse(item.posterURL))
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
}

class ItemsPagingAdapterDiffUtilCallback : DiffUtil.ItemCallback<Item>() {
    override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean =
        oldItem.kinopoiskID == newItem.kinopoiskID

    override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean =
        oldItem == newItem
}