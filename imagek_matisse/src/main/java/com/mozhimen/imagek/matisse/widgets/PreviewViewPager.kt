package com.mozhimen.imagek.matisse.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.viewpager.widget.ViewPager
import it.sephiroth.android.library.imagezoom.ImageViewTouch

/**
 * Created by liubo on 2018/9/10.
 */
class PreviewViewPager(context: Context, attributes: AttributeSet) : ViewPager(context, attributes) {

    override fun canScroll(v: View?, checkV: Boolean, dx: Int, x: Int, y: Int): Boolean {
        if (v is ImageViewTouch) {
            return v.canScroll(dx) || super.canScroll(v, checkV, dx, x, y)
        }
        return super.canScroll(v, checkV, dx, x, y)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return try {
            super.onInterceptTouchEvent(ev)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}