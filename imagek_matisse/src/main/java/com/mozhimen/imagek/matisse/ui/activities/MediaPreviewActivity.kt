package com.mozhimen.imagek.matisse.ui.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.mozhimen.imagek.matisse.bases.BasePreviewActivity
import com.mozhimen.imagek.matisse.cons.Constants
import com.mozhimen.imagek.matisse.mos.MediaItem

/**
 * desc：图片选中预览</br>
 * time: 2019/9/11-14:17</br>
 * author：Leo </br>
 * since V 1.0.0 </br>
 */
class MediaPreviewActivity : BasePreviewActivity() {

    companion object {
        fun instance(context: Context, bundle: Bundle, mOriginalEnable: Boolean) {
            val intent = Intent(context, MediaPreviewActivity::class.java)
            intent.putExtra(Constants.EXTRA_DEFAULT_BUNDLE, bundle).putExtra(Constants.EXTRA_RESULT_ORIGINAL_ENABLE, mOriginalEnable)
            (context as Activity).startActivityForResult(intent, Constants.REQUEST_CODE_PREVIEW)
        }
    }

    override fun setViewData() {
        super.setViewData()
        val bundle = intent.getBundleExtra(Constants.EXTRA_DEFAULT_BUNDLE)
        val selected = bundle?.getParcelableArrayList<MediaItem>(Constants.STATE_SELECTION)
        selected?.apply {
            adapter?.addAll(this)
            adapter?.notifyDataSetChanged()
            check_view?.apply {
                if (selectionSpec?.isCountable() == true) {
                    setCheckedNum(1)
                } else {
                    setChecked(true)
                }
            }
            previousPos = 0
            updateSize(this[0])
        }
    }
}