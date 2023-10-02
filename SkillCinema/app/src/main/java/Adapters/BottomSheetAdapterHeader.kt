package Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.skillcinema.databinding.BottomSheetRecyclerHeaderModelBinding

class BottomSheetAdapterHeader: RecyclerView.Adapter<BottomSheetHeaderViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BottomSheetHeaderViewHolder {
        val binding = BottomSheetRecyclerHeaderModelBinding.inflate(LayoutInflater.from(parent.context))
        return  BottomSheetHeaderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BottomSheetHeaderViewHolder, position: Int) {

    }

    override fun getItemCount(): Int = 1

}
class BottomSheetHeaderViewHolder(val binding: BottomSheetRecyclerHeaderModelBinding): RecyclerView.ViewHolder(binding.root)