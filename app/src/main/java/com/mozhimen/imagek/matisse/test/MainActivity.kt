package com.mozhimen.imagek.matisse.test

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import com.mozhimen.basick.elemk.androidx.appcompat.bases.databinding.BaseActivityVB
import com.mozhimen.basick.elemk.commons.I_Listener
import com.mozhimen.basick.utilk.android.content.UtilKPackage
import com.mozhimen.imagek.matisse.impls.GlideImageEngine
import com.mozhimen.imagek.matisse.ImageKMatisse
import com.mozhimen.imagek.matisse.helpers.MediaMimeTypeHelper
import com.mozhimen.basick.elemk.android.provider.MediaStoreCaptureProxy
import com.mozhimen.basick.utilk.kotlin.collections.ifNotEmpty
import com.mozhimen.imagek.matisse.ImageKMatisseSelectionBuilder
import com.mozhimen.imagek.matisse.cons.CImageKMatisse
import com.mozhimen.imagek.matisse.test.databinding.ActivityMainBinding
import com.mozhimen.basick.utilk.android.net.uri2strFilePathName
import com.mozhimen.manifestk.xxpermissions.XXPermissionsCheckUtil
import com.mozhimen.manifestk.xxpermissions.XXPermissionsNavHostUtil
import com.mozhimen.manifestk.xxpermissions.XXPermissionsRequestUtil
import com.mozhimen.uicorek.adaptk.systembar.cons.CPropertyOr
import com.mozhimen.uicorek.adaptk.systembar.initAdaptKSystemBar

class MainActivity : BaseActivityVB<ActivityMainBinding>() {
    private var _selectionBuilder: ImageKMatisseSelectionBuilder? = null
    private var _imagePathName:String? = ""
    override fun initData(savedInstanceState: Bundle?) {
        startPermissionReadWrite(this) {
            super.initData(savedInstanceState)
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        createMatisse()
        vb.mainBtnSelect.setOnClickListener {
            _selectionBuilder?.forResult(CImageKMatisse.REQUEST_CODE_CHOOSE)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return
        android.util.Log.d(TAG, "onActivityResult: requestCode $requestCode, resultCode $resultCode")
        when (requestCode) {
            CImageKMatisse.REQUEST_CODE_CHOOSE -> doActivityResultForChoose(data)
//            ImageKMatisseCons.REQUEST_CODE_CAPTURE -> doActivityResultForCapture()
//            ImageKMatisseCons.REQUEST_CODE_CROP -> doActivityResultForCrop(data)
        }
    }

    private fun doActivityResultForChoose(data: Intent?) {
        if (data == null) return
        // 获取uri返回值  裁剪结果不返回uri
        val uriList = ImageKMatisse.obtainResult(data)
        uriList?.ifNotEmpty {
            _imagePathName = it[0].uri2strFilePathName()
            if (!_imagePathName.isNullOrEmpty()) {
                com.mozhimen.basick.imagek.glide.ImageKGlide.loadImageCircleGlide(vb.mainImg,_imagePathName, com.mozhimen.uicorek.R.color.cok_white,com.mozhimen.uicorek.R.color.cok_white)
            }
        }
    }

    private fun createMatisse() {
        _selectionBuilder =
            ImageKMatisse.from(this)
                .select(MediaMimeTypeHelper.ofImage())
                .setThemeRes(com.mozhimen.imagek.matisse.R.style.ImageKMatisse_Default)
                .setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                .setCountable(false)
                .setMaxSelectable(1)
                .setMediaCaptureEnable(false)
                .setMediaCaptureStrategy(MediaStoreCaptureProxy.CaptureStrategy(true, "${UtilKPackage.getPackageName()}.provider"))
                .setOnLoadStatusBarListener { activity ->
                    activity.initAdaptKSystemBar(CPropertyOr.NORMAL or CPropertyOr.THEME_DARK or CPropertyOr.THEME_CUSTOM)
                }
                .setImageThumbnailScale(0.8f)
                .setImageEngine(GlideImageEngine())
                .setImageCropEnable(true)
                .setImageCropFrameIsCircle(true)
                .setImageCropFrameRectVisible(false)
                .setImageCropFrameCanDrag(true)
                .setImageCropFrameCanAutoSize(false)
                .setGridSpanCount(3)

/*                .setStatusBarFuture { params, view ->
                    // 外部设置状态栏
                    ImmersionBar.with(params)?.run {
                        statusBarDarkFont(true)
                        view?.apply { titleBar(this) }
                        init()
                    }

                    // 外部可隐藏Matisse界面中的标题栏
                    // view?.visibility = if (isDarkStatus) View.VISIBLE else View.GONE
                }*/

    }

    @SuppressLint("MissingPermission")
    private fun startPermissionReadWrite(context: Context, allGrant: I_Listener? = null) {
        if (XXPermissionsCheckUtil.hasReadWritePermission(context)) {
            allGrant?.invoke()
        } else {
            XXPermissionsRequestUtil.requestReadWritePermission(context,
                onGranted = {
                    allGrant?.invoke()
                },
                onDenied = {
                    XXPermissionsNavHostUtil.startSettingManageStorage(context)
                }
            )
        }
    }
}