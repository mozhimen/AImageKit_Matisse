package com.mozhimen.imagek.matisse.test

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.mozhimen.basick.elemk.androidx.appcompat.bases.BaseActivityVB
import com.mozhimen.basick.elemk.commons.I_Listener
import com.mozhimen.basick.utilk.android.content.UtilKPackage
import com.mozhimen.imagek.matisse.impls.Glide4ImageEngine
import com.mozhimen.imagek.matisse.ImageKMatisse
import com.mozhimen.imagek.matisse.helpers.MimeTypeManager
import com.mozhimen.imagek.matisse.helpers.SelectionBuilder
import com.mozhimen.imagek.matisse.cons.Constants
import com.mozhimen.basick.elemk.android.provider.MediaStoreCaptureProxy
import com.mozhimen.basick.utilk.kotlin.collections.ifNotEmpty
import com.mozhimen.imagek.matisse.cons.ImageKMatisseCons
import com.mozhimen.imagek.matisse.test.databinding.ActivityMainBinding
import com.mozhimen.manifestk.xxpermission.XXPermissionUtil

class MainActivity : BaseActivityVB<ActivityMainBinding>() {
    private var _selectionBuilder: SelectionBuilder? = null

    override fun initData(savedInstanceState: Bundle?) {
        startPermissionReadWrite(this) {
            super.initData(savedInstanceState)
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        createMatisse()
        vb.mainBtnSelect.setOnClickListener {
            _selectionBuilder?.forResult(Constants.REQUEST_CODE_CHOOSE)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return
        android.util.Log.d(TAG, "onActivityResult: requestCode $requestCode, resultCode $resultCode")
        when (requestCode) {
            ImageKMatisseCons.REQUEST_CODE_CHOOSE -> doActivityResultForChoose(data)
//            ImageKMatisseCons.REQUEST_CODE_CAPTURE -> doActivityResultForCapture()
//            ImageKMatisseCons.REQUEST_CODE_CROP -> doActivityResultForCrop(data)
        }
    }

    private fun doActivityResultForChoose(data: Intent?) {
        if (data == null) return
        // 获取uri返回值  裁剪结果不返回uri
        val uriList = ImageKMatisse.obtainResult(data)
        uriList?.ifNotEmpty {
            val selectedPhotoPath =
                UriHelper.uriToFile(this@UserInformationEditingActivity, it[0])?.absolutePath
            if (!selectedPhotoPath.isNullOrEmpty()) {
                mLastSelectedPhotoPath = selectedPhotoPath
                GlideApp
                    .with(this@UserInformationEditingActivity)
                    .load(selectedPhotoPath)
                    .apply(RequestOptions.bitmapTransform(CircleCrop()))
                    .into(mBinding.userHeadImg)
            }
        }
    }

    private fun createMatisse() {
        _selectionBuilder =
            ImageKMatisse.from(this)
                .select(MimeTypeManager.ofImage())
                .setThemeRes(com.mozhimen.imagek.matisse.R.style.Matisse_Default)
                .setCountable(false)
                .setMaxSelectable(1)
                .setIsCapture(false)
                .setCaptureStrategy(MediaStoreCaptureProxy.CaptureStrategy(true, "${UtilKPackage.getPackageName()}.provider"))
                .setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                .setSpanCount(3)
                .setThumbnailScale(0.8f)
                .setImageEngine(Glide4ImageEngine())
                .setIsCrop(true)
                .setIsCircleCrop(true)
                .setOnLoadStatusBarListener { activity, view ->
//                    params.initAdaptKSystemBar(CProperty.IMMERSED_HARD_STICKY)
                }
//                .setStatusBarFuture { params, view ->
//                    // 外部设置状态栏
//                    ImmersionBar.with(params)?.run {
//                        statusBarDarkFont(true)
//                        view?.apply { titleBar(this) }
//                        init()
//                    }
//
//                    // 外部可隐藏Matisse界面中的标题栏
//                    // view?.visibility = if (isDarkStatus) View.VISIBLE else View.GONE
//                }

    }

    private fun startPermissionReadWrite(context: Context, allGrant: I_Listener? = null) {
        if (XXPermissionUtil.hasReadWritePermission(context)) {
            allGrant?.invoke()
        } else {
            XXPermissionUtil.requestReadWritePermission(context,
                onGranted = {
                    allGrant?.invoke()
                },
                onDenied = {
                    XXPermissionUtil.startSettingManageStorage(context)
                }
            )
        }
    }
}