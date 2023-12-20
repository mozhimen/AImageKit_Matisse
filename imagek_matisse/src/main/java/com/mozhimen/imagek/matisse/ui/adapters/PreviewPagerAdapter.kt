package com.mozhimen.imagek.matisse.ui.adapters

import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.mozhimen.imagek.matisse.mos.MediaItem
import com.mozhimen.imagek.matisse.ui.fragments.MediaPicturePreviewFragment

/**
 * Created by liubo on 2018/9/6.
 */
class PreviewPagerAdapter(manager: FragmentManager, listener: OnPrimaryItemSetListener?) :
    FragmentStatePagerAdapter(manager) {

    var items: ArrayList<MediaItem> = ArrayList()
    var kListener: OnPrimaryItemSetListener? = null

    init {
        this.kListener = listener
    }

    override fun getCount() = items.size

    override fun getItem(position: Int) = MediaPicturePreviewFragment.newInstance(items[position])

    override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
        super.setPrimaryItem(container, position, `object`)
        kListener?.onPrimaryItemSet(position)
    }

    fun getMediaItem(position: Int): MediaItem? {
        if (count > position) {
            return items[position]
        }

        return null
    }

    fun addAll(items: List<MediaItem>) {
        this.items.addAll(items)
    }

    interface OnPrimaryItemSetListener {
        fun onPrimaryItemSet(position: Int)
    }
}