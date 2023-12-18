package com.mozhimen.imagek.matisse.helpers

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo.*
import android.view.View
import androidx.annotation.StyleRes
import com.mozhimen.imagek.matisse.ImageKMatisse
import com.mozhimen.imagek.matisse.annors.AScreenOrientation
import com.mozhimen.imagek.matisse.commons.IImageEngine
import com.mozhimen.imagek.matisse.mos.CaptureStrategy
import com.mozhimen.imagek.matisse.bases.BaseMediaFilter
import com.mozhimen.imagek.matisse.mos.SelectionSpec
import com.mozhimen.imagek.matisse.commons.IOnCheckedListener
import com.mozhimen.imagek.matisse.commons.IOnSelectedListener
import com.mozhimen.imagek.matisse.cons.EMimeType
import com.mozhimen.imagek.matisse.bases.BaseActivity
import com.mozhimen.imagek.matisse.commons.IOnNoticeEventListener
import com.mozhimen.imagek.matisse.ui.activities.MatisseActivity
import java.io.File

/**
 * Fluent API for building media select specification.
 * Constructs a new specification builder on the context.
 *
 * @param _imageKMatisse   a requester context wrapper.
 * @param mimeTypes MIME type set to select.
 */
class SelectionCreator(
    private val _imageKMatisse: ImageKMatisse, mimeTypes: Set<EMimeType>, mediaTypeExclusive: Boolean
) {
    private val _selectionSpec: SelectionSpec = SelectionSpec.getCleanInstance()

    /////////////////////////////////////////////////////////////////////////////////

    init {
        _selectionSpec.run {
            this.mimeTypeSet = mimeTypes
            this.mediaTypeExclusive = mediaTypeExclusive
            this.orientation = SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    /////////////////////////////////////////////////////////////////////////////////

    /**
     * Theme for media selecting Activity.
     *  外部设置主题样式
     * There are two built-in themes:
     * you can define a custom theme derived from the above ones or other themes.
     *
     * @param themeRes theme resource id. Default value is R.style.Matisse_Zhihu.
     * @return [SelectionCreator] for fluent API.
     */
    fun setThemeRes(@StyleRes themeRes: Int) = this.apply { _selectionSpec.themeRes = themeRes }

    /**
     * Show a auto-increased number or a check mark when user select media.
     * 设置选中计数方式
     * @param countable true for a auto-increased number from 1, false for a check mark. Default
     * 对于从1开始自动增加的数字为True，对于复选标记为false。默认的
     * value is false.
     * @return [SelectionCreator] for fluent API.
     */
    fun setCountable(countable: Boolean) = this.apply { _selectionSpec.countable = countable }

    /**
     * 单一选择下
     * Maximum selectable count.
     * mediaTypeExclusive true
     *      use maxSelectable
     * mediaTypeExclusive false
     *      use maxImageSelectable and maxVideoSelectable
     * @param maxSelectable Maximum selectable count. Default value is 1.
     * @return [SelectionCreator] for fluent API.
     */
    fun setMaxSelectable(maxSelectable: Int) = this.apply {
        if (!_selectionSpec.mediaTypeExclusive) return this
        require(maxSelectable >= 1) { "maxSelectable must be greater than or equal to one" }
        check(!(_selectionSpec.maxImageSelectable > 0 || _selectionSpec.maxVideoSelectable > 0)) {
            "already set maxImageSelectable and maxVideoSelectable"
        }
        _selectionSpec.maxSelectable = maxSelectable
    }

    /**
     * Only useful when [SelectionSpec.mediaTypeExclusive] set true and you want to set different maximum
     * selectable files for image and video media types.
     *
     * @param maxImageSelectable Maximum selectable count for image.
     * @param maxVideoSelectable Maximum selectable count for video.
     * @return
     */
    fun setMaxSelectablePerMediaType(maxImageSelectable: Int, maxVideoSelectable: Int) = this.apply {
        if (_selectionSpec.mediaTypeExclusive) return this
        require(!(maxImageSelectable < 1 || maxVideoSelectable < 1)) {
            "mediaTypeExclusive must be false and max selectable must be greater than or equal to one"
        }
        _selectionSpec.maxSelectable = -1
        _selectionSpec.maxImageSelectable = maxImageSelectable
        _selectionSpec.maxVideoSelectable = maxVideoSelectable
    }

    /**
     * Add filter to filter each selecting item.
     *
     * @param mediaFilter [BaseMediaFilter]
     * @return [SelectionCreator] for fluent API.
     */
    fun addMediaFilter(mediaFilter: BaseMediaFilter) = apply {
        if (_selectionSpec.mediaFilters == null) _selectionSpec.mediaFilters = mutableListOf()
        _selectionSpec.mediaFilters?.add(mediaFilter)
    }

    /////////////////////////////////////////////////////////////////////////////////

    /**
     * Determines whether the photo capturing is enabled or not on the media grid view.
     * If this value is set true, photo capturing entry will appear only on All Media's page.
     * 是否开启内部拍摄
     * @param enable Whether to enable capturing or not. Default value is false;
     * @return [SelectionCreator] for fluent API.
     */
    fun setIsCapture(enable: Boolean) = this.apply { _selectionSpec.capture = enable }

    /**
     * Show a original photo check options.Let users decide whether use original photo after select
     * 显示原始照片检查选项。让用户选择后决定是否使用原图
     * @param enable Whether to enable original photo or not
     * @return [SelectionCreator] for fluent API.
     */
    fun setOriginalCheckEnable(enable: Boolean) = this.apply { _selectionSpec.originalable = enable }

    /**
     * Maximum original size,the unit is MB. Only useful when {link@originalEnable} set true
     * 最大原始大小，单位为MB。仅当flink@originalEnable)设置为true时有效
     * @param size Maximum original size. Default value is Integer.MAX_VALUE
     * @return [SelectionCreator] for fluent API.
     */
    fun setOriginalMaxSize(size: Int) = this.apply { _selectionSpec.originalMaxSize = size }

    /**
     * 为保存照片的位置提供捕获策略，包括内部和外部存储，以及[androidx.core.content.FileProvider]的权限。
     * Capture strategy provided for the location to save photos including internal and external
     * storage and also a authority for [androidx.core.content.FileProvider].
     * 拍照设置Strategy
     * @param captureStrategy [CaptureStrategy], needed only when capturing is enabled.
     * @return [SelectionCreator] for fluent API.
     */
    fun setCaptureStrategy(captureStrategy: CaptureStrategy) = this.apply {
        _selectionSpec.captureStrategy = captureStrategy
    }

    /////////////////////////////////////////////////////////////////////////////////

    /**
     * Set the desired orientation of this activity.
     * 设置此活动所需的方向。
     * 强制屏幕方向
     * @param orientation An orientation constant as used in [AScreenOrientation].
     * Default value is [android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT].
     * @return [SelectionCreator] for fluent API.
     * @see Activity.setRequestedOrientation
     */
    fun setOrientation(@AScreenOrientation orientation: Int) = this.apply {
        _selectionSpec.orientation = orientation
    }

    /**
     * Set a fixed span count for the media grid. Same for different screen orientations.
     * This will be ignored when [.gridExpectedSize] is set.
     * [get gridExpectedSize first]
     * 为媒体网格设置一个固定的跨度计数。对于不同的屏幕方向也是一样。
     * @param spanCount Requested span count.
     * @return [SelectionCreator] for fluent API.
     */
    fun setSpanCount(spanCount: Int) = this.apply {
        if (_selectionSpec.gridExpectedSize > 0) return this
        _selectionSpec.spanCount = spanCount
    }

    /**
     * Set expected size for media grid to adapt to different screen sizes. This won't necessarily
     * be applied cause the media grid should fill the view container. The measured media grid's
     * size will be as close to this value as possible.
     * 设置媒体网格的预期大小，以适应不同的屏幕尺寸。这并不一定会被应用，因为媒体网格应该填充视图容器。被测量的媒体网格的大小将尽可能接近这个值。
     *
     * @param sizePx Expected media grid size in pixel.
     * @return [SelectionCreator] for fluent API.
     */
    fun setExpectedGridSize(sizePx: Int) = this.apply { _selectionSpec.gridExpectedSize = sizePx }

    /**
     * Photo thumbnail's scale compared to the View's size. It should be a float value in (0.0,1.0].
     * 图片显示压缩比
     * @param scale Thumbnail's scale in (0.0, 1.0]. Default value is 0.5.
     * @return [SelectionCreator] for fluent API.
     */
    fun setThumbnailScale(scale: Float) = this.apply {
        require(!(scale <= 0f || scale > 1f)) { "Thumbnail scale must be between (0.0, 1.0]" }
        _selectionSpec.thumbnailScale = scale
    }

    /**
     * Provide an image engine.
     * There are two built-in image engines:
     * And you can implement your own image engine.
     * 提供一个图像引擎。有两个内置的图像引擎:你可以实现你自己的图像引擎。
     * @param imageEngine [IImageEngine]
     * @return [SelectionCreator] for fluent API.
     */
    fun setImageEngine(imageEngine: IImageEngine) = this.apply {
        _selectionSpec.imageEngine = imageEngine
        _selectionSpec.imageEngine?.init(_imageKMatisse.activity?.applicationContext!!)
    }

    /**
     * 设置开启裁剪
     * Whether to support crop
     * If this value is set true, it will support function crop.
     * @param crop Whether to support crop or not. Default value is false;
     * @return [SelectionCreator] for fluent API.
     */
    fun setIsCrop(crop: Boolean) = this.apply { _selectionSpec.isCrop = crop }

    /**
     * isCircleCrop
     * default is RECTANGLE CROP
     */
    fun setIsCircleCrop(isCircle: Boolean) = this.apply {
        _selectionSpec.isCircleCrop = isCircle
    }

    /**
     * provide file to save image after crop
     */
    fun setCropCacheFolder(cropCacheFolder: File) = this.apply {
        _selectionSpec.cropCacheFolder = cropCacheFolder
    }

    /**
     * Set listener for callback immediately when user select or unselect something.
     *
     * It's a redundant API with [ImageKMatisse.obtainResult],
     * we only suggest you to use this API when you need to do something immediately.
     *
     * @param listener [IOnSelectedListener]
     * @return [SelectionCreator] for fluent API.
     */
    fun setOnSelectedListener(listener: IOnSelectedListener?) = this.apply {
        _selectionSpec.onSelectedListener = listener
    }

    /**
     * Set listener for callback immediately when user check or uncheck original.
     *
     * @param listener [IOnSelectedListener]
     * @return [SelectionCreator] for fluent API.
     */
    fun setOnCheckedListener(listener: IOnCheckedListener?) = this.apply {
        _selectionSpec.onCheckedListener = listener
    }

    /**
     * set notice type for matisse
     */
    fun setOnNoticeEventListener(
        listener: IOnNoticeEventListener?
    ) = this.apply {
        _selectionSpec.onNoticeEventListener = listener
    }

    /**
     * set Status Bar
     */
    fun setOnLoadStatusBarListener(statusBarFunction: ((params: BaseActivity, view: View?) -> Unit)?) =
        this.apply {
            _selectionSpec.onLoadStatusBarListener = statusBarFunction
        }

    /**
     * set last choose pictures ids
     * id is cursor id. not support crop picture
     * 预选中上次带回的图片
     * 注：暂时无法保持预选中图片的顺序
     */
    fun setLastChoosePicturesIdOrUri(list: ArrayList<String>?) = this.apply {
        _selectionSpec.lastChoosePictureIdsOrUris = list
    }

    /**
     * Start to select media and wait for result.
     *
     * @param requestCode Identity of the request Activity or Fragment.
     */
    fun forResult(requestCode: Int) {
        val activity = _imageKMatisse.activity ?: return

        val intent = Intent(activity, MatisseActivity::class.java)

        val fragment = _imageKMatisse.fragment
        if (fragment != null) {
            fragment.startActivityForResult(intent, requestCode)
        } else {
            activity.startActivityForResult(intent, requestCode)
        }
    }
}
