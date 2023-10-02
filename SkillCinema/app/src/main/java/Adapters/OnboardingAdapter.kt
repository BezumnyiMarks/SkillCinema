package Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import DataModels.OnboardingAdapterItems
import com.example.skillcinema.R
import com.example.skillcinema.databinding.OnboardingViewPagerModelBinding

class OnboardingAdapter : RecyclerView.Adapter<OnboardingViewHolder>(){
    private var data = listOf<OnboardingAdapterItems>(
        OnboardingAdapterItems(R.drawable.onboarding1, "Узнавай о премьерах"),
        OnboardingAdapterItems(R.drawable.onboarding2, "Создавай коллекции"),
        OnboardingAdapterItems(R.drawable.onboarding3, "Делись с друзьями"),
        OnboardingAdapterItems(R.drawable.onboarding4, "Разлагайся перед телеком")
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
        return OnboardingViewHolder(
            OnboardingViewPagerModelBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
        val item = data[position]
        with(holder.binding) {
            imageView.setImageResource(item.image)
            textView.text = item.text
        }
    }

    override fun getItemCount(): Int = data.size
}

class OnboardingViewHolder (val binding: OnboardingViewPagerModelBinding) : RecyclerView.ViewHolder(binding.root)