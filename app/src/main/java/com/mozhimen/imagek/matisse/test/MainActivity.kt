package com.mozhimen.imagek.matisse.test

import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.mozhimen.basick.utilk.android.content.UtilKPackage
import com.mozhimen.imagek.matisse.Glide4Engine
import com.mozhimen.imagek.matisse.Matisse
import com.mozhimen.imagek.matisse.MimeTypeManager
import com.mozhimen.imagek.matisse.SelectionCreator
import com.mozhimen.imagek.matisse.entity.CaptureStrategy
import com.mozhimen.imagek.matisse.test.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    private var selectionCreator: SelectionCreator? = null

    private fun createMatisse() {
        selectionCreator =
                // 绑定Activity/Fragment
            Matisse.from(this)
                // 设置显示类型，单一/混合选择模式
                .choose(MimeTypeManager.ofImage())
                // 外部设置主题样式
                .theme(com.mozhimen.imagek.matisse.R.style.Matisse_Default)
                // 设置选中计数方式
                .countable(false)
                // 设置开启裁剪
                .isCrop(true)
                .isCircleCrop(false)
                // 单一选择下
                .maxSelectable(1)
                // 是否开启内部拍摄
                .capture(false)
                // 拍照设置Strategy
                .captureStrategy(
                    CaptureStrategy(
                        true,
                        "${UtilKPackage.getPackageName()}.provider"
                    )
                )
                // 图片显示压缩比
                .thumbnailScale(0.8f)
                // 强制屏幕方向
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                .imageEngine(Glide4Engine())
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
}