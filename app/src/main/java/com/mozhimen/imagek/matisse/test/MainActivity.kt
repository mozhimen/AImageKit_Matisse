package com.mozhimen.imagek.matisse.test

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import com.mozhimen.basick.elemk.androidx.appcompat.bases.BaseActivityVB
import com.mozhimen.basick.elemk.commons.I_Listener
import com.mozhimen.basick.utilk.android.content.UtilKPackage
import com.mozhimen.imagek.matisse.helpers.Glide4Engine
import com.mozhimen.imagek.matisse.ImageKMatisse
import com.mozhimen.imagek.matisse.helpers.MimeTypeManager
import com.mozhimen.imagek.matisse.helpers.SelectionCreator
import com.mozhimen.imagek.matisse.mos.CaptureStrategy
import com.mozhimen.imagek.matisse.cons.Constants
import com.mozhimen.imagek.matisse.test.databinding.ActivityMainBinding
import com.mozhimen.manifestk.xxpermission.XXPermissionUtil

class MainActivity : BaseActivityVB<ActivityMainBinding>() {
    private var _selectionCreator: SelectionCreator? = null

    override fun initData(savedInstanceState: Bundle?) {
        startPermissionReadWrite(this) {
            super.initData(savedInstanceState)
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        createMatisse()
        vb.mainBtnSelect.setOnClickListener {
            _selectionCreator?.forResult(Constants.REQUEST_CODE_CHOOSE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return
        android.util.Log.d(TAG, "onActivityResult: requestCode $requestCode, resultCode $resultCode")
    }

    private fun createMatisse() {
        _selectionCreator =
            ImageKMatisse.from(this)
                .choose(MimeTypeManager.ofImage())
                .theme(com.mozhimen.imagek.matisse.R.style.Matisse_Default)
                .countable(false)
                .maxSelectable(1)
                .capture(false)
                .captureStrategy(CaptureStrategy(true, "${UtilKPackage.getPackageName()}.provider"))
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                .spanCount(3)
                .thumbnailScale(0.8f)
                .imageEngine(Glide4Engine())
                .isCrop(true)
                .isCircleCrop(true)
                .setStatusBarFuture { params, view ->
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