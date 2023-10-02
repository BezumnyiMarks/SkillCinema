package Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import DataModels.BottomSheetItemDataModel
import com.example.skillcinema.databinding.BottomSheetRecyclerItemModelBinding

class BottomSheetAdapterItem(private val onClick: (BottomSheetItemDataModel) -> Unit) :
    ListAdapter<BottomSheetItemDataModel, BottomSheetRecyclerViewHolder>(
        BottomSheetAdapterItemDiffUtilCallback()
    ) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BottomSheetRecyclerViewHolder {
        return BottomSheetRecyclerViewHolder(
            BottomSheetRecyclerItemModelBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: BottomSheetRecyclerViewHolder, position: Int) {
        val item = getItem(position)
        with(holder.binding){
            collectionName.text = item.collectionName
            filmsAmount.text = item.filmsNumber.toString()
            checkbox.isChecked = item.includesCurrentFilm
        }
        holder.binding.checkbox.setOnClickListener {
            onClick(item)
            var filmsAmount = holder.binding.filmsAmount.text.toString().toInt()
            if (!holder.binding.checkbox.isChecked){
                filmsAmount--
                holder.binding.filmsAmount.text = filmsAmount.toString()
            }
            else{
                filmsAmount++
                holder.binding.filmsAmount.text = filmsAmount.toString()
            }
        }
    }
}

class BottomSheetAdapterItemDiffUtilCallback : DiffUtil.ItemCallback<BottomSheetItemDataModel>() {
    override fun areItemsTheSame(oldItem: BottomSheetItemDataModel, newItem: BottomSheetItemDataModel): Boolean =
        oldItem.collectionName == newItem.collectionName

    override fun areContentsTheSame(oldItem: BottomSheetItemDataModel, newItem: BottomSheetItemDataModel): Boolean =
        oldItem == newItem
}

class BottomSheetRecyclerViewHolder (val binding: BottomSheetRecyclerItemModelBinding) : RecyclerView.ViewHolder(binding.root)
