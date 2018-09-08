package me.bakumon.moneykeeper.utill

import android.view.View
import android.view.animation.AnimationUtils
import me.bakumon.moneykeeper.App
import me.bakumon.moneykeeper.R

/**
 * @author Bakumon https://bakumon.me
 */
object ViewUtil {
    fun startShake(view: View) {
        val animation = AnimationUtils.loadAnimation(App.instance, R.anim.shake)
        view.startAnimation(animation)
    }
}