package Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import DataModels.Staff
import com.example.skillcinema.databinding.StaffRecyclerItemModelBinding

class StaffAdapter(private val onClick: (Staff) -> Unit) : ListAdapter<Staff, StaffRecyclerViewHolder>(
    StaffAdapterDiffUtilCallback()
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StaffRecyclerViewHolder {
        return StaffRecyclerViewHolder(
            StaffRecyclerItemModelBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: StaffRecyclerViewHolder, position: Int) {
        val item = getItem(position)
        with(holder.binding) {
            name.text = item?.nameRu ?: item.nameEn ?: ""
            role.text = item?.description ?: ""
            item.let {
                com.bumptech.glide.Glide
                    .with(imageView.context)
                    .load(android.net.Uri.parse(it?.posterURL))
                    .centerCrop()
                    .into(imageView)
            }
        }
        holder.binding.root.setOnClickListener {
            onClick(item)
        }
    }
}

class StaffAdapterDiffUtilCallback : DiffUtil.ItemCallback<Staff>() {
    override fun areItemsTheSame(oldItem: Staff, newItem: Staff): Boolean =
        oldItem.staffID == newItem.staffID

    override fun areContentsTheSame(oldItem: Staff, newItem: Staff): Boolean =
        oldItem == newItem
}

class StaffRecyclerViewHolder (val binding: StaffRecyclerItemModelBinding) : RecyclerView.ViewHolder(binding.root)