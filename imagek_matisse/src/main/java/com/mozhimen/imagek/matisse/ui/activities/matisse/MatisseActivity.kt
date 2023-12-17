package com.mozhimen.imagek.matisse.ui.activities.matisse

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
import com.mozhimen.imagek.matisse.cons.ConstValue
import com.mozhimen.imagek.matisse.mos.IncapableCause
import com.mozhimen.imagek.matisse.mos.Item
import com.mozhimen.imagek.matisse.commons.IAlbum
import com.mozhimen.imagek.matisse.mos.SelectedItemCollection
import com.mozhimen.imagek.matisse.ucrop.UCrop
import com.mozhimen.imagek.matisse.ui.activities.AlbumPreviewActivity
import com.mozhimen.imagek.matisse.bases.BaseActivity
import com.mozhimen.imagek.matisse.ui.activities.SelectedPreviewActivity
import com.mozhimen.imagek.matisse.ui.adapters.AlbumMediaAdapter
import com.mozhimen.imagek.matisse.ui.adapters.FolderItemMediaAdapter
import com.mozhimen.imagek.matisse.ui.fragments.FolderBottomSheet
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
class MatisseActivity : BaseActivity(),
    MediaSelectionFragment.SelectionProvider,
    AlbumMediaAdapter.CheckStateListener, AlbumMediaAdapter.OnMediaClickListener,
    AlbumMediaAdapter.OnPhotoCapture, View.OnClickListener {

    private var mediaStoreCompat: MediaStoreCompat? = null
    private var originalEnable = false
    private var allAlbum: Album? = null
    private var albumLoadHelper: AlbumLoadHelper? = null
    private lateinit var selectedCollection: SelectedItemCollection
    private lateinit var albumFolderSheetHelper: AlbumFolderSheetHelper

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
        spec?.statusBarFuture?.invoke(this, toolbar)

        if (spec?.capture == true) {
            mediaStoreCompat = MediaStoreCompat(this)
            if (spec?.captureStrategy == null)
                throw RuntimeException("Don't forget to set CaptureStrategy.")
            mediaStoreCompat?.setCaptureStrategy(spec?.captureStrategy!!)
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
        button_apply.setText(getAttrString(R.attr.Media_Album_text, R.string.album_name_all))
        selectedCollection = SelectedItemCollection(this).apply { onCreate(instanceState) }
        albumLoadHelper = AlbumLoadHelper(this, albumCallback)
        albumFolderSheetHelper = AlbumFolderSheetHelper(this, albumSheetCallback)
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
        selectedCollection.onSaveInstanceState(outState)
        albumLoadHelper?.onSaveInstanceState(outState)
        outState.putBoolean(ConstValue.CHECK_STATE, originalEnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        albumLoadHelper?.onDestroy()
        spec?.onCheckedListener = null
        spec?.onSelectedListener = null
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        super.onBackPressed()
    }

    override fun onSelectUpdate() {
        updateBottomToolbar()
        spec?.onSelectedListener?.onSelected(
            selectedCollection.asListOfUri(), selectedCollection.asListOfString()
        )
    }

    override fun capture() {
        mediaStoreCompat?.dispatchCaptureIntent(this, ConstValue.REQUEST_CODE_CAPTURE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            ConstValue.REQUEST_CODE_PREVIEW -> {
                if (resultCode != Activity.RESULT_OK) return
                val cropPath = ImageKMatisse.obtainCropResult(data)

                // 裁剪带回数据，则认为图片经过裁剪流程
                if (cropPath != null) finishIntentFromCrop(activity, cropPath)
                else doActivityResultFromPreview(data)
            }
            ConstValue.REQUEST_CODE_CAPTURE -> doActivityResultFromCapture()
            ConstValue.REQUEST_CODE_CROP -> {
                data?.run {
                    val resultUri = UCrop.getOutput(data)
                    finishIntentFromCrop(activity, resultUri)
                }
            }
            ConstValue.REQUEST_CODE_CROP_ERROR -> {
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
                if (selectedCollection.count() == 0) {
                    handleCauseTips(getString(R.string.please_select_media_resource))
                    return
                }

                SelectedPreviewActivity.instance(
                    activity, selectedCollection.getDataWithBundle(), originalEnable
                )
            }
            button_complete -> {
                if (selectedCollection.count() == 0) {
                    handleCauseTips(getString(R.string.please_select_media_resource))
                    return
                }

                val item = selectedCollection.asList()[0]
                if (spec?.openCrop() == true && spec?.isSupportCrop(item) == true) {
                    gotoImageCrop(this, selectedCollection.asListOfUri() as ArrayList<Uri>)
                    return
                }

                handleIntentFromPreview(activity, originalEnable, selectedCollection.items())
            }

            original_layout -> {
                val count = countOverMaxSize(selectedCollection)
                if (count <= 0) {
                    originalEnable = !originalEnable
                    original.setChecked(originalEnable)
                    spec?.onCheckedListener?.onCheck(originalEnable)
                    return
                }

                handleCauseTips(
                    getString(R.string.error_over_original_count, count, spec?.originalMaxSize),
                    AForm.DIALOG
                )
            }

            button_apply -> {
                if (allAlbum?.isAll() == true && allAlbum?.isEmpty() == true) {
                    handleCauseTips(getString(R.string.empty_album))
                    return
                }

                albumFolderSheetHelper.createFolderSheetDialog()
            }
        }
    }

    override fun provideSelectedItemCollection() = selectedCollection

    override fun onMediaClick(album: Album?, item: Item, adapterPosition: Int) {
        val intent = Intent(this, AlbumPreviewActivity::class.java)
            .putExtra(ConstValue.EXTRA_ALBUM, album as Parcelable)
            .putExtra(ConstValue.EXTRA_ITEM, item)
            .putExtra(ConstValue.EXTRA_DEFAULT_BUNDLE, selectedCollection.getDataWithBundle())
            .putExtra(ConstValue.EXTRA_RESULT_ORIGINAL_ENABLE, originalEnable)

        startActivityForResult(intent, ConstValue.REQUEST_CODE_PREVIEW)
    }

    /**
     * 处理预览的[onActivityResult]
     */
    private fun doActivityResultFromPreview(data: Intent?) {
        data?.apply {

            originalEnable = getBooleanExtra(ConstValue.EXTRA_RESULT_ORIGINAL_ENABLE, false)
            val isApplyData = getBooleanExtra(ConstValue.EXTRA_RESULT_APPLY, false)
            handlePreviewIntent(activity, data, originalEnable, isApplyData, selectedCollection)

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
        albumLoadHelper?.loadAlbumData()
        // 手动插入到相册列表
        albumFolderSheetHelper.insetAlbumToFolder(capturePathUri)
        // 重新load所有资源
        albumFolderSheetHelper.getAlbumFolderList()?.apply { onAlbumSelected(this[0]) }

        // Check is Crop first
        if (spec?.openCrop() == true) {
            gotoImageCrop(this, arrayListOf(capturePathUri))
        }
    }

    private fun updateBottomToolbar() {
        val selectedCount = selectedCollection.count()
        setCompleteText(selectedCount)

        if (spec?.originalable == true) {
            setViewVisible(true, original_layout)
            updateOriginalState()
        } else {
            setViewVisible(false, original_layout)
        }
    }

    private fun setCompleteText(selectedCount: Int) {
        if (selectedCount == 0) {
            button_complete.setText(getAttrString(R.attr.Media_Sure_text, R.string.button_sure))

        } else if (selectedCount == 1 && spec?.singleSelectionModeEnabled() == true) {
            button_complete.setText(getAttrString(R.attr.Media_Sure_text, R.string.button_sure))

        } else {
            button_complete.text =
                getString(getAttrString(R.attr.Media_Sure_text, R.string.button_sure))
                    .plus("(").plus(selectedCount.toString()).plus(")")
        }
    }

    private fun updateOriginalState() {
        original.setChecked(originalEnable)
        if (countOverMaxSize(selectedCollection) > 0 || originalEnable) {
            handleCauseTips(
                getString(R.string.error_over_original_size, spec?.originalMaxSize),
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
            albumFolderSheetHelper.setAlbumFolderCursor(cursor)

            Handler(Looper.getMainLooper()).post {
                if (cursor.moveToFirst()) {
                    allAlbum = Album.valueOf(cursor).apply { onAlbumSelected(this) }
                }
            }
        }

        override fun onAlbumReset() {
            albumFolderSheetHelper.clearFolderSheetDialog()
        }
    }

    private var albumSheetCallback = object : FolderBottomSheet.BottomSheetCallback {
        override fun initData(adapter: FolderItemMediaAdapter) {
            adapter.setListData(albumFolderSheetHelper.readAlbumFromCursor())
        }

        override fun onItemClick(album: Album, position: Int) {
            if (!albumFolderSheetHelper.setLastFolderCheckedPosition(position)) return
            albumLoadHelper?.setStateCurrentSelection(position)

            button_apply.text = album.getDisplayName(activity)
            onAlbumSelected(album)
        }
    }
}
