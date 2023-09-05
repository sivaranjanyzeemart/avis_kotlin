package zeemart.asia.buyers.helper.customviews

import android.content.Context
import android.util.AttributeSet
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import zeemart.asia.buyers.R

/**
 * Created by ParulBhandari on 7/20/2018.
 */
class CustomLoadingViewBlue(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    init {
        init(context)
    }

    private fun init(context: Context) {
        val rootView = inflate(context, R.layout.layout_three_dot_loader, this)
        val imgDot1 = rootView.findViewById<ImageView>(R.id.img_dot_1)
        val imgDot2 = rootView.findViewById<ImageView>(R.id.img_dot_2)
        val imgDot3 = rootView.findViewById<ImageView>(R.id.img_dot_3)
        setAnimationForThreeDots(context, imgDot1, imgDot2, imgDot3)
    }

    companion object {
        fun setAnimationForThreeDots(
            context: Context?,
            imgDot1: ImageView,
            imgDot2: ImageView,
            imgDot3: ImageView
        ) {
            val animGrowDot1 = AnimationUtils.loadAnimation(context, R.anim.grow)
            val animGrowDot2 = AnimationUtils.loadAnimation(context, R.anim.grow)
            val animGrowDot3 = AnimationUtils.loadAnimation(context, R.anim.grow)
            val animShrinkDot1 = AnimationUtils.loadAnimation(context, R.anim.shrink)
            animShrinkDot1.startOffset = 500
            val animShrinkDot2 = AnimationUtils.loadAnimation(context, R.anim.shrink)
            animShrinkDot2.startOffset = 500
            val animShrinkDot3 = AnimationUtils.loadAnimation(context, R.anim.shrink)
            animShrinkDot3.startOffset = 500
            val animSetDot1 = AnimationSet(false)
            animSetDot1.addAnimation(animGrowDot1)
            animSetDot1.addAnimation(animShrinkDot1)
            val animSetDot3 = AnimationSet(false)
            animSetDot3.addAnimation(animGrowDot3)
            animSetDot3.addAnimation(animShrinkDot3)
            val animSetDot2 = AnimationSet(false)
            animSetDot2.addAnimation(animGrowDot2)
            animSetDot2.addAnimation(animShrinkDot2)
            animSetDot2.startOffset = 400
            animSetDot3.startOffset = 800
            imgDot1.startAnimation(animSetDot1)
            imgDot2.startAnimation(animSetDot2)
            imgDot3.startAnimation(animSetDot3)
            animSetDot1.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {
                    imgDot1.visibility = VISIBLE
                }

                override fun onAnimationEnd(animation: Animation) {
                    imgDot1.visibility = INVISIBLE
                }

                override fun onAnimationRepeat(animation: Animation) {}
            })
            animSetDot2.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {
                    imgDot2.visibility = VISIBLE
                }

                override fun onAnimationEnd(animation: Animation) {
                    imgDot2.visibility = INVISIBLE
                }

                override fun onAnimationRepeat(animation: Animation) {}
            })
            animSetDot3.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {
                    imgDot3.visibility = VISIBLE
                }

                override fun onAnimationEnd(animation: Animation) {
                    imgDot1.visibility = GONE
                    imgDot2.visibility = GONE
                    imgDot3.visibility = GONE
                    imgDot1.startAnimation(animSetDot1)
                    imgDot2.startAnimation(animSetDot2)
                    imgDot3.startAnimation(animSetDot3)
                    imgDot3.visibility = INVISIBLE
                }

                override fun onAnimationRepeat(animation: Animation) {}
            })
        }
    }
}