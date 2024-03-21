package com.example.myapplication

import android.view.animation.DecelerateInterpolator
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView

class CustomItemAnimator : DefaultItemAnimator() {

    override fun animateAdd(holder: RecyclerView.ViewHolder?): Boolean {
        holder?.itemView?.translationY = -holder!!.itemView.height.toFloat()
        holder?.itemView?.alpha = 0.0f

        // Animate each item sliding into place from the top and fading in
        holder?.itemView?.animate()
            ?.translationY(0f)
            ?.alpha(1.0f)
            ?.setInterpolator(DecelerateInterpolator())
            ?.setDuration(300) // duration of the animation for each item
            ?.setListener(null)

        return true
    }
}
