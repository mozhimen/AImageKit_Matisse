package com.mozhimen.imagek.matisse.mos

import android.content.pm.ActivityInfo
import androidx.annotation.StyleRes
import com.mozhimen.imagek.matisse.cons.EMimeType
import com.mozhimen.imagek.matisse.helpers.MediaMimeTypeHelper
import com.mozhimen.imagek.matisse.R
import com.mozhimen.imagek.matisse.commons.IImageEngine
import com.mozhimen.imagek.matisse.bases.BaseMediaFilter
import com.mozhimen.imagek.matisse.commons.INoticeEventListener
import com.mozhimen.imagek.matisse.commons.ILoadStatusBarListener
import com.mozhimen.imagek.matisse.commons.IMediaCheckedListener
import com.mozhimen.imagek.matisse.commons.IMediaSelectedListener
import java.io.File
import com.mozhimen.basick.elemk.android.provider.MediaStoreCaptureProxy.CaptureStrategy
import com.mozhimen.imagek.matisse.commons.ILoadToolBarListener

/**
 * Describe : Builder to get config values
 * Created by Leo on 2018/8/29 on 14:54.
 */
class Selection {
    var mimeTypeSet: Set<EMimeType>? = null
    var mediaTypeExclusive = false                      // 设置单种/多种媒体资源选择 默认支持多种
    var mediaFilters: MutableList<BaseMediaFilter>? = null
    var maxSelectable = 1
    var maxImageSelectable = 0
    var maxVideoSelectable = 0
    var thumbnailScale = 0.5f
    var countable = false
    var capture = false
    var gridExpectedSize = 0
    var spanCount = 3
    var captureStrategy: CaptureStrategy? = null

    @StyleRes
    var themeRes = R.style.ImageKMatisse_Default
    var orientation = 0
    var originalable = false
    var originalMaxSize = 0
    var imageEngine: IImageEngine? = null
    var onSelectedListener: IMediaSelectedListener? = null
    var onCheckedListener: IMediaCheckedListener? = null
    var isCrop = false                              // 裁剪
    var isCircleCrop = false                        // 裁剪框的形状
    var cropCacheFolder: File? = null               // 裁剪后文件保存路径
    var hasInited = false                           // 是否初始化完成
    var onNoticeEventListener: INoticeEventListener? = null// 库内提示具体回调
    var onLoadStatusBarListener: ILoadStatusBarListener? = null//
    var onLoadToolbarListener: ILoadToolBarListener? = null
    var lastChoosePictureIdsOrUris: ArrayList<String>? = null   // 上次选中的图片Id

    class InstanceHolder {
        companion object {
            val INSTANCE: Selection = Selection()
        }
    }

    companion object {
        fun getInstance() = InstanceHolder.INSTANCE
        fun getCleanInstance(): Selection {
            val selectionSpec = getInstance()
            selectionSpec.reset()
            return selectionSpec
        }
    }

    fun reset() {
        mimeTypeSet = null
        mediaTypeExclusive = false
        themeRes = R.style.ImageKMatisse_Default
        orientation = 0
        countable = false
        maxSelectable = 1
        maxImageSelectable = 0
        maxVideoSelectable = 0
        mediaFilters = null
        capture = false
        captureStrategy = null
        spanCount = 3
        gridExpectedSize = 0
        thumbnailScale = 0.5f
        imageEngine = null
        hasInited = true
        isCrop = false// crop
        isCircleCrop = false
        originalable = false// return original setting
        originalMaxSize = Integer.MAX_VALUE
        onNoticeEventListener = null
        onLoadStatusBarListener = null
        onLoadToolbarListener = null
        lastChoosePictureIdsOrUris = null
    }

    // 是否可计数
    fun isCountable() = countable && !isSingleChoose()

    // 是否可单选
    fun isSingleChoose() =
        maxSelectable == 1 || (maxImageSelectable == 1 && maxVideoSelectable == 1)

    // 是否可裁剪
    fun openCrop() = isCrop && isSingleChoose()

    fun isSupportCrop(item: Media?) = item != null && item.isImage() && !item.isGif()

    // 是否单一资源选择方式
    fun isMediaTypeExclusive() =
        mediaTypeExclusive && (maxImageSelectable + maxVideoSelectable == 0)

    fun onlyShowImages() =
        if (mimeTypeSet != null) MediaMimeTypeHelper.ofImage().containsAll(mimeTypeSet!!) else false

    fun onlyShowVideos() =
        if (mimeTypeSet != null) MediaMimeTypeHelper.ofVideo().containsAll(mimeTypeSet!!) else false

    fun singleSelectionModeEnabled() = !countable && isSingleChoose()

    fun needOrientationRestriction() = orientation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
}