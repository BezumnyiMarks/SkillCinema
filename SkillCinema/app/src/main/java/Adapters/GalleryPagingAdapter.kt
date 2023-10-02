package Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import DataModels.Item
import com.example.skillcinema.databinding.GalleryRecyclerItemModelBinding

class GalleryPagingAdapter(private val onClick: (Item) -> Unit) : PagingDataAdapter<Item, GalleryRecyclerViewHolder>(
    GalleryPagingAdapterDiffUtilCallback()
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryRecyclerViewHolder {
        return GalleryRecyclerViewHolder(
            GalleryRecyclerItemModelBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: GalleryRecyclerViewHolder, position: Int) {
        val item = getItem(position)
        with(holder.binding) {
            item?.let {
                Glide
                    .with(imageView.context)
                    .load(android.net.Uri.parse(it.imageURL))
                    .centerCrop()
                    .into(imageView)
            }
        }
        holder.binding.root.setOnClickListener {
            if (item != null) {
                onClick(item)
            }
        }
    }
}

class GalleryPagingAdapterDiffUtilCallback : DiffUtil.ItemCallback<Item>() {
    override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean =
        oldItem.imageURL == newItem.imageURL

    override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean =
        oldItem == newItem
}

class GalleryRecyclerViewHolder (val binding: GalleryRecyclerItemModelBinding) : RecyclerView.ViewHolder(binding.root)