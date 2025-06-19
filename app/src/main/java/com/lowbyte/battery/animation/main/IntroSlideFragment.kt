package com.lowbyte.battery.animation.main

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.lowbyte.battery.animation.R

class IntroSlideFragment : Fragment(R.layout.fragment_intro_slide) {
    companion object {
        fun newInstance(title: String, desc: String, imageResId: Int): IntroSlideFragment {
            val fragment = IntroSlideFragment()
            val args = Bundle()
            args.putString("title", title)
            args.putString("desc", desc)
            args.putInt("image", imageResId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val title = arguments?.getString("title")
        val desc = arguments?.getString("desc")
        val image = arguments?.getInt("image")

        view.findViewById<TextView>(R.id.textTitle).text = title
        view.findViewById<TextView>(R.id.textDescription).text = desc
        view.findViewById<ImageView>(R.id.imageView).setImageResource(image ?: 0)
    }
}