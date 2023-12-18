package com.mozhimen.imagek.matisse.ui.activities

import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.mozhimen.imagek.matisse.ImageKMatisse
import com.mozhimen.imagek.matisse.R
import com.mozhimen.imagek.matisse.annors.AForm
import com.mozhimen.imagek.matisse.mos.Album
import com.mozhimen.imagek.matisse.cons.Constants
import com.mozhimen.imagek.matisse.mos.IncapableCause
import com.mozhimen.imagek.matisse.mos.Item
import com.mozhimen.imagek.matisse.commons.IAlbum
import com.mozhimen.imagek.matisse.helpers.MediaSelectionProxy
import com.mozhimen.imagek.matisse.ucrop.UCrop
import com.mozhimen.imagek.matisse.bases.BaseActivity
import com.mozhimen.imagek.matisse.helpers.FolderBottomSheetWrapper
import com.mozhimen.imagek.matisse.helpers.AlbumLoadWrapper
import com.mozhimen.imagek.matisse.ui.adapters.MediaAlbumAdapter
import com.mozhimen.imagek.matisse.ui.adapters.FolderItemMediaAdapter
import com.mozhimen.imagek.matisse.ui.fragments.MediaSelectionFragment
import com.mozhimen.imagek.matisse.widgets.CheckRadioView
import com.mozhimen.imagek.matisse.helpers.MediaStoreCompat
import com.mozhimen.imagek.matisse.utils.countOverMaxSize
import com.mozhimen.imagek.matisse.utils.finishIntentFromCrop
import com.mozhimen.imagek.matisse.utils.gotoImageCrop
import com.mozhimen.imagek.matisse.utils.handleIntentFromPreview
import com.mozhimen.imagek.matisse.utils.handlePreviewIntent
import com.mozhimen.imagek.matisse.utils.setOnClickListener
import com.mozhimen.imagek.matisse.utils.setViewVisible

/**
 * desc：入口</br>
 * time: 2019/9/11-14:17</br>
 * author：Leo </br>
 * since V 1.0.0 </br>
 */
class MatisseActivity : BaseActivity(), MediaSelectionFragment.IMediaSelectionProvider,
    MediaAlbumAdapter.CheckStateListener, MediaAlbumAdapter.OnMediaClickListener,
    MediaAlbumAdapter.OnPhotoCapture, View.OnClickListener {

    private var mediaStoreCompat: MediaStoreCompat? = null
    private var originalEnable = false
    private var allAlbum: Album? = null
    private var albumLoadWrapper: AlbumLoadWrapper? = null
    private lateinit var mediaSelectionProxy: MediaSelectionProxy
    private lateinit var folderBottomSheetWrapper: FolderBottomSheetWrapper

    private lateinit var toolbar: ConstraintLayout
    private lateinit var button_apply: TextView
    private lateinit var button_preview: TextView
    private lateinit var button_complete: TextView
    private lateinit var original_layout: LinearLayout
    private lateinit var button_back: TextView
    private lateinit var original: CheckRadioView
    private lateinit var empty_view: View
    private lateinit var container: View

    override fun configActivity() {
        super.configActivity()
        initView()
        selectionSpec?.statusBarFuture?.invoke(this, toolbar)

        if (selectionSpec?.capture == true) {
            mediaStoreCompat = MediaStoreCompat(this)
            if (selectionSpec?.captureStrategy == null)
                throw RuntimeException("Don't forget to set CaptureStrategy.")
            mediaStoreCompat?.setCaptureStrategy(selectionSpec?.captureStrategy!!)
        }
    }

    override fun getResourceLayoutId() = R.layout.activity_matisse

    private fun initView() {
        toolbar = findViewById(R.id.toolbar)
        button_apply = findViewById(R.id.button_apply)
        button_preview = findViewById(R.id.button_preview)
        original_layout = findViewById(R.id.original_layout)
        button_complete = findViewById(R.id.button_complete)
        button_back = findViewById(R.id.button_back)
        original = findViewById(R.id.original)
        empty_view = findViewById(R.id.empty_view)
        container = findViewById(R.id.container)
    }

    override fun setViewData() {
        button_apply.setText(getAttrString(R.attr.BottomBarAlbum_Text, R.string.album_name_all))
        mediaSelectionProxy = MediaSelectionProxy(this).apply { onCreate(savedInstanceState) }
        albumLoadWrapper = AlbumLoadWrapper(this, albumCallback)
        folderBottomSheetWrapper = FolderBottomSheetWrapper(this, albumSheetCallback)
        updateBottomToolbar()
    }

    override fun initListener() {
        setOnClickListener(
            this, button_apply, button_preview,
            original_layout, button_complete, button_back
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mediaSelectionProxy.onSaveInstanceState(outState)
        albumLoadWrapper?.onSaveInstanceState(outState)
        outState.putBoolean(Constants.CHECK_STATE, originalEnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        albumLoadWrapper?.onDestroy()
        selectionSpec?.onCheckedListener = null
        selectionSpec?.onSelectedListener = null
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        super.onBackPressed()
    }

    override fun onSelectUpdate() {
        updateBottomToolbar()
        selectionSpec?.onSelectedListener?.onSelected(
            mediaSelectionProxy.asListOfUri(), mediaSelectionProxy.asListOfString()
        )
    }

    override fun capture() {
        mediaStoreCompat?.dispatchCaptureIntent(this, Constants.REQUEST_CODE_CAPTURE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            Constants.REQUEST_CODE_PREVIEW -> {
                if (resultCode != Activity.RESULT_OK) return
                val cropPath = ImageKMatisse.obtainCropResult(data)

                // 裁剪带回数据，则认为图片经过裁剪流程
                if (cropPath != null) finishIntentFromCrop(activity, cropPath)
                else doActivityResultFromPreview(data)
            }
            Constants.REQUEST_CODE_CAPTURE -> doActivityResultFromCapture()
            Constants.REQUEST_CODE_CROP -> {
                data?.run {
                    val resultUri = UCrop.getOutput(data)
                    finishIntentFromCrop(activity, resultUri)
                }
            }
            Constants.REQUEST_CODE_CROP_ERROR -> {
                data?.run {
                    val cropError = UCrop.getError(data)?.message ?: ""
                    IncapableCause.handleCause(activity, IncapableCause(cropError))
                }
            }
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            button_back -> onBackPressed()
            button_preview -> {
                if (mediaSelectionProxy.count() == 0) {
                    handleCauseTips(getString(R.string.please_select_media_resource))
                    return
                }

                MediaPreviewActivity.instance(
                    activity, mediaSelectionProxy.getDataWithBundle(), originalEnable
                )
            }
            button_complete -> {
                if (mediaSelectionProxy.count() == 0) {
                    handleCauseTips(getString(R.string.please_select_media_resource))
                    return
                }

                val item = mediaSelectionProxy.asList()[0]
                if (selectionSpec?.openCrop() == true && selectionSpec?.isSupportCrop(item) == true) {
                    gotoImageCrop(this, mediaSelectionProxy.asListOfUri() as ArrayList<Uri>)
                    return
                }

                handleIntentFromPreview(activity, originalEnable, mediaSelectionProxy.items())
            }

            original_layout -> {
                val count = countOverMaxSize(mediaSelectionProxy)
                if (count <= 0) {
                    originalEnable = !originalEnable
                    original.setChecked(originalEnable)
                    selectionSpec?.onCheckedListener?.onCheck(originalEnable)
                    return
                }

                handleCauseTips(
                    getString(R.string.error_over_original_count, count, selectionSpec?.originalMaxSize),
                    AForm.DIALOG
                )
            }

            button_apply -> {
                if (allAlbum?.isAll() == true && allAlbum?.isEmpty() == true) {
                    handleCauseTips(getString(R.string.empty_album))
                    return
                }

                folderBottomSheetWrapper.createFolderSheetDialog()
            }
        }
    }

    override fun provideSelectedItemCollection() = mediaSelectionProxy

    override fun onMediaClick(album: Album?, item: Item, adapterPosition: Int) {
        val intent = Intent(this, AlbumPreviewActivity::class.java)
            .putExtra(Constants.EXTRA_ALBUM, album as Parcelable)
            .putExtra(Constants.EXTRA_ITEM, item)
            .putExtra(Constants.EXTRA_DEFAULT_BUNDLE, mediaSelectionProxy.getDataWithBundle())
            .putExtra(Constants.EXTRA_RESULT_ORIGINAL_ENABLE, originalEnable)

        startActivityForResult(intent, Constants.REQUEST_CODE_PREVIEW)
    }

    /**
     * 处理预览的[onActivityResult]
     */
    private fun doActivityResultFromPreview(data: Intent?) {
        data?.apply {

            originalEnable = getBooleanExtra(Constants.EXTRA_RESULT_ORIGINAL_ENABLE, false)
            val isApplyData = getBooleanExtra(Constants.EXTRA_RESULT_APPLY, false)
            handlePreviewIntent(activity, data, originalEnable, isApplyData, mediaSelectionProxy)

            if (!isApplyData) {
                val mediaSelectionFragment = supportFragmentManager.findFragmentByTag(
                    MediaSelectionFragment::class.java.simpleName
                )
                if (mediaSelectionFragment is MediaSelectionFragment) {
                    mediaSelectionFragment.refreshMediaGrid()
                }
                updateBottomToolbar()
            }
        }
    }

    /**
     * 处理拍照的[onActivityResult]
     */
    private fun doActivityResultFromCapture() {
        val capturePathUri = mediaStoreCompat?.getCurrentPhotoUri() ?: return
        val capturePath = mediaStoreCompat?.getCurrentPhotoPath() ?: return
        // 刷新系统相册
        MediaScannerConnection.scanFile(this, arrayOf(capturePath), null, null)
        // 重新获取相册数据
        albumLoadWrapper?.loadAlbumData()
        // 手动插入到相册列表
        folderBottomSheetWrapper.insetAlbumToFolder(capturePathUri)
        // 重新load所有资源
        folderBottomSheetWrapper.getAlbumFolderList()?.apply { onAlbumSelected(this[0]) }

        // Check is Crop first
        if (selectionSpec?.openCrop() == true) {
            gotoImageCrop(this, arrayListOf(capturePathUri))
        }
    }

    private fun updateBottomToolbar() {
        val selectedCount = mediaSelectionProxy.count()
        setCompleteText(selectedCount)

        if (selectionSpec?.originalable == true) {
            setViewVisible(true, original_layout)
            updateOriginalState()
        } else {
            setViewVisible(false, original_layout)
        }
    }

    private fun setCompleteText(selectedCount: Int) {
        if (selectedCount == 0) {
            button_complete.setText(getAttrString(R.attr.Navigation_TextSure, R.string.button_sure))

        } else if (selectedCount == 1 && selectionSpec?.singleSelectionModeEnabled() == true) {
            button_complete.setText(getAttrString(R.attr.Navigation_TextSure, R.string.button_sure))

        } else {
            button_complete.text =
                getString(getAttrString(R.attr.Navigation_TextSure, R.string.button_sure))
                    .plus("(").plus(selectedCount.toString()).plus(")")
        }
    }

    private fun updateOriginalState() {
        original.setChecked(originalEnable)
        if (countOverMaxSize(mediaSelectionProxy) > 0 || originalEnable) {
            handleCauseTips(
                getString(R.string.error_over_original_size, selectionSpec?.originalMaxSize),
                AForm.DIALOG
            )

            original.setChecked(false)
            originalEnable = false
        }
    }

    private fun onAlbumSelected(album: Album) {
        if (album.isAll() && album.isEmpty()) {
            setViewVisible(true, empty_view)
            setViewVisible(false, container)
        } else {
            setViewVisible(false, empty_view)
            setViewVisible(true, container)
            val fragment = MediaSelectionFragment.newInstance(album)
            supportFragmentManager.beginTransaction()
                .replace(container.id, fragment, MediaSelectionFragment::class.java.simpleName)
                .commitAllowingStateLoss()
        }
    }

    private var albumCallback = object : IAlbum {
        override fun onAlbumStart() {
            // do nothing
        }

        override fun onAlbumLoad(cursor: Cursor) {
            folderBottomSheetWrapper.setAlbumFolderCursor(cursor)

            Handler(Looper.getMainLooper()).post {
                if (cursor.moveToFirst()) {
                    allAlbum = Album.valueOf(cursor).apply { onAlbumSelected(this) }
                }
            }
        }

        override fun onAlbumReset() {
            folderBottomSheetWrapper.clearFolderSheetDialog()
        }
    }

    private var albumSheetCallback = object : IFolderBottomSheetListener {
        override fun initData(adapter: FolderItemMediaAdapter) {
            adapter.setListData(folderBottomSheetWrapper.readAlbumFromCursor())
        }

        override fun onItemClick(album: Album, position: Int) {
            if (!folderBottomSheetWrapper.setLastFolderCheckedPosition(position)) return
            albumLoadWrapper?.setStateCurrentSelection(position)

            button_apply.text = album.getDisplayName(activity)
            onAlbumSelected(album)
        }
    }
}
