package Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.skillcinema.databinding.BottomSheetRecyclerAddModelBinding

class BottomSheetAdapterAdd(private val onClick:() -> Unit): RecyclerView.Adapter<BottomSheetAddViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BottomSheetAddViewHolder {
        val binding = BottomSheetRecyclerAddModelBinding.inflate(LayoutInflater.from(parent.context))
        return  BottomSheetAddViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BottomSheetAddViewHolder, position: Int) {
        holder.binding.root.setOnClickListener {
            onClick()
        }
    }

    override fun getItemCount(): Int = 1

}
class BottomSheetAddViewHolder(val binding: BottomSheetRecyclerAddModelBinding): RecyclerView.ViewHolder(binding.root)