package com.example.skillcinema

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ScrollView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.skillcinema.databinding.HomeRecyclersContainerBinding

class HomeRecyclersContainer
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int=0
): ConstraintLayout(context,attrs,defStyleAttr){

    val binding = HomeRecyclersContainerBinding.inflate(LayoutInflater.from(context))

    init{
        addView(binding.root)
    }
}