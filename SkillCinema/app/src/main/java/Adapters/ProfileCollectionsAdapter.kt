package Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import DataModels.ProfileCollection
import com.example.skillcinema.R
import com.example.skillcinema.databinding.CollectionRecyclerItemModelBinding

private const val FAVOURITES = "Любимое"
private const val BOOKMARK = "Хочу посмотреть"
class ProfileCollectionsAdapter(private val onDeleteClick: (ProfileCollection) -> Unit, private val onRootClick: (ProfileCollection) -> Unit) :
    ListAdapter<ProfileCollection, ProfileCollectionsViewHolder>(
        ProfileCollectionsAdapterDiffUtilCallback()
    ) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileCollectionsViewHolder {
        return ProfileCollectionsViewHolder(
            CollectionRecyclerItemModelBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ProfileCollectionsViewHolder, position: Int) {
        val item = getItem(position)

        holder.binding.collectionName.text = item.collectionName
        holder.binding.filmsAmount.text = item.filmsAmount.toString()
        when (item.collectionName){
            FAVOURITES -> {
                holder.binding.imageView.setImageResource(R.drawable.ic_favourite_border_icon)
                holder.binding.buttonDelete.setImageResource(R.drawable.ic_remove_icon)
                if (item.filmsAmount != 0)
                    holder.binding.buttonDelete.visibility = View.VISIBLE
                else holder.binding.buttonDelete.visibility = View.GONE
            }
            BOOKMARK -> {
                holder.binding.imageView.setImageResource(R.drawable.ic_bookmark_border_icon)
                holder.binding.buttonDelete.setImageResource(R.drawable.ic_remove_icon)
                if (item.filmsAmount != 0)
                    holder.binding.buttonDelete.visibility = View.VISIBLE
                else holder.binding.buttonDelete.visibility = View.GONE
            }
            else -> holder.binding.imageView.setImageResource(R.drawable.ic_profile_icon)
        }

        holder.binding.buttonDelete.setOnClickListener {
            onDeleteClick(item)
        }

        holder.binding.root.setOnClickListener {
            onRootClick(item)
        }
    }
}

class ProfileCollectionsAdapterDiffUtilCallback : DiffUtil.ItemCallback<ProfileCollection>() {
    override fun areItemsTheSame(oldItem: ProfileCollection, newItem: ProfileCollection): Boolean =
        oldItem.collectionName == newItem.collectionName

    override fun areContentsTheSame(oldItem: ProfileCollection, newItem: ProfileCollection): Boolean =
        oldItem == newItem
}

class ProfileCollectionsViewHolder (val binding: CollectionRecyclerItemModelBinding) : RecyclerView.ViewHolder(binding.root)
