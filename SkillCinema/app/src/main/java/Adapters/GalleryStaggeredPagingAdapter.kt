package Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import DataModels.Item
import com.example.skillcinema.databinding.GalleryStaggeredItemModelBinding

class GalleryStaggeredPagingAdapter(private val onClick: (Item) -> Unit) : PagingDataAdapter<Item, GalleryStaggeredRecyclerViewHolder>(
    GalleryPagingAdapterDiffUtilCallback()
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryStaggeredRecyclerViewHolder {
        return GalleryStaggeredRecyclerViewHolder(
            GalleryStaggeredItemModelBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: GalleryStaggeredRecyclerViewHolder, position: Int) {
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

class GalleryStaggeredRecyclerViewHolder (val binding: GalleryStaggeredItemModelBinding) : RecyclerView.ViewHolder(binding.root)