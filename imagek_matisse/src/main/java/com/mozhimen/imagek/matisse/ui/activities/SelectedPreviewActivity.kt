package com.mozhimen.imagek.matisse.ui.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.mozhimen.imagek.matisse.cons.ConstValue
import com.mozhimen.imagek.matisse.mos.Item

/**
 * desc：图片选中预览</br>
 * time: 2019/9/11-14:17</br>
 * author：Leo </br>
 * since V 1.0.0 </br>
 */
class SelectedPreviewActivity : BasePreviewActivity() {

    companion object {
        fun instance(context: Context, bundle: Bundle, mOriginalEnable: Boolean) {
            val intent = Intent(context, SelectedPreviewActivity::class.java)
            intent.putExtra(ConstValue.EXTRA_DEFAULT_BUNDLE, bundle)
                .putExtra(ConstValue.EXTRA_RESULT_ORIGINAL_ENABLE, mOriginalEnable)
            (context as Activity).startActivityForResult(intent, ConstValue.REQUEST_CODE_PREVIEW)
        }
    }

    override fun setViewData() {
        super.setViewData()
        val bundle = intent.getBundleExtra(ConstValue.EXTRA_DEFAULT_BUNDLE)
        val selected = bundle?.getParcelableArrayList<Item>(ConstValue.STATE_SELECTION)
        selected?.apply {
            adapter?.addAll(this)
            adapter?.notifyDataSetChanged()
            check_view?.apply {
                if (spec?.isCountable() == true) {
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