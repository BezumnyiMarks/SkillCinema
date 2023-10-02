package Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import DataModels.Item
import com.example.skillcinema.databinding.PictureViewPagerItemModelBinding

class PicturePagingAdapter(private val onClick: (Item) -> Unit) : PagingDataAdapter<Item, PictureRecyclerViewHolder>(
    GalleryPagingAdapterDiffUtilCallback()
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PictureRecyclerViewHolder {
        return PictureRecyclerViewHolder(
            PictureViewPagerItemModelBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: PictureRecyclerViewHolder, position: Int) {
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

class PictureRecyclerViewHolder (val binding: PictureViewPagerItemModelBinding) : RecyclerView.ViewHolder(binding.root)