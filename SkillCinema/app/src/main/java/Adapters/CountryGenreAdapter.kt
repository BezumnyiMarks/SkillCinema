package Adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.skillcinema.R
import com.example.skillcinema.databinding.CountryGenreRecyclerItemModelBinding
import com.example.skillcinema.utils.Extensions

class CountryGenreAdapter(
     _data: List<String>,
     _context: Context,
     private val onClick: (String) -> Unit
): RecyclerView.Adapter<CountryGenreViewHolder>(){
    private var data = _data
    private val  context = _context
    private val extensions = Extensions()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountryGenreViewHolder {
        return CountryGenreViewHolder(
            CountryGenreRecyclerItemModelBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: CountryGenreViewHolder, position: Int) {
        val item = data[position]
        holder.binding.countryGenreTextView.text = item
        holder.binding.countryGenreTextView.setOnClickListener {
            holder.binding.root.background = ContextCompat.getDrawable(context,
                R.color.recycler_image_foreground
            )
            onClick(item)
        }
    }

    override fun getItemCount(): Int = data.size

    @SuppressLint("NotifyDataSetChanged")
    fun setData(data: List<String>){
        this.data = data
        notifyDataSetChanged()
    }
}

class CountryGenreViewHolder (val binding: CountryGenreRecyclerItemModelBinding) : RecyclerView.ViewHolder(binding.root)