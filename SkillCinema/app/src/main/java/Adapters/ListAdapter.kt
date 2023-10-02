package Adapters

import DataModels.Item
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.skillcinema.databinding.HomeRecyclerItemModelBinding
import com.example.skillcinema.utils.Extensions

class ListAdapter(private val onClick: (Item) -> Unit) : ListAdapter<Item, RecyclerViewHolder>(
    ListAdapterDiffUtilCallback()
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
        val rating = item?.ratingKinopoisk ?: ""
        val genresSet = item.genres?.let { extensions.getGenreSet(it) }
        with(holder.binding) {
            name.text = item?.nameRu ?: ""
            genre.text = genresSet
            if (rating == "")
                this.rating.visibility = View.GONE
            else{
                this.rating.visibility = View.VISIBLE
                this.rating.text = rating.toString()
            }
            if (item!!.posterURL != null) {
                Glide
                    .with(recyclerImageView.context)
                    .load(android.net.Uri.parse(item.posterURL))
                    .centerCrop()
                    .into(recyclerImageView)
            }
        }
        holder.binding.root.setOnClickListener {
            onClick(item)
        }
    }
}

class ListAdapterDiffUtilCallback : DiffUtil.ItemCallback<Item>() {
    override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean =
        oldItem.kinopoiskID == newItem.kinopoiskID

    override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean =
        oldItem == newItem
}

class RecyclerViewHolder (val binding: HomeRecyclerItemModelBinding) : RecyclerView.ViewHolder(binding.root)
