package it.trentabitplus.digitaltextsuite.animation

import android.content.Context
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import it.trentabitplus.digitaltextsuite.R

//View must be the target of first animation
class ZoomAnimationListener(val view: View, val context: Context): Animation.AnimationListener {
    override fun onAnimationStart(animation: Animation?) {

    }

    override fun onAnimationEnd(animation: Animation?) {
        //Start zoom in animation on target view
        val zoomIn = AnimationUtils.loadAnimation(context, R.anim.zoom_in)
        view.startAnimation(zoomIn)
    }

    override fun onAnimationRepeat(animation: Animation?) {

    }
}