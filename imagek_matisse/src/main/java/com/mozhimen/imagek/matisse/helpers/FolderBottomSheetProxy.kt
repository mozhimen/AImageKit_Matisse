package com.mozhimen.imagek.matisse.helpers

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import com.mozhimen.imagek.matisse.R
import com.mozhimen.imagek.matisse.commons.IFolderBottomSheetListener
import com.mozhimen.imagek.matisse.mos.Album
import com.mozhimen.imagek.matisse.ui.fragments.FolderBottomSheet

class FolderBottomSheetProxy(
    private var _context: Context, private var _folderBottomSheetListener: IFolderBottomSheetListener
) {
    private var _folderCursor: Cursor? = null
    private var _folderList: ArrayList<Album>? = null
    private var _folderBottomSheet: FolderBottomSheet? = null
    private var _lastFolderCheckedPosition = 0

    //////////////////////////////////////////////////////////

    fun createFolderSheetDialog() {
        _folderBottomSheet = FolderBottomSheet.instance(
            _context, _lastFolderCheckedPosition, "Folder"
        )

        _folderBottomSheet?.folderBottomSheetListener = _folderBottomSheetListener
    }

    fun readAlbumFromCursor(): ArrayList<Album>? {
        if (_folderList?.size ?: 0 > 0) return _folderList

        if (_folderCursor == null) return null

        var allFolderCoverPath: Uri? = null
        var allFolderCount = 0L
        if (_folderList == null) {
            _folderList = arrayListOf()
        }

        _folderCursor?.moveToFirst()
        while (_folderCursor!!.moveToNext()) {
            val album = Album.valueOf(_folderCursor!!)
            if (_folderList?.size == 0) {
                allFolderCoverPath = album.getCoverPath()
            }
            _folderList?.add(album)
            allFolderCount += album.getCount()
        }
        _folderList?.add(
            0, Album(allFolderCoverPath, _context.getString(R.string.album_name_all), allFolderCount)
        )
        return _folderList
    }

    fun insetAlbumToFolder(capturePath: Uri) {
        readAlbumFromCursor()

        _folderList?.apply {
            // 全部相册需添加一张
            this[0].addCaptureCount()
            this[0].setCoverPath(capturePath)

            /**
             * 拍照后图片保存在Pictures目录下
             * Pictures为空时，需手动创建
             */
            // TODO 2019/10/28 Leo 查询相册下图片需指定id，无法手动生成
//            val listDCIM: List<Album>? =
//                filter { Environment.DIRECTORY_PICTURES == it.getDisplayName(context) }
//            if (listDCIM == null || listDCIM.isEmpty()) {
//                albumFolderList?.add(Album(Environment.DIRECTORY_PICTURES, 0))
//            }

            // Pictures目录手动添加一张图片
            filter { Environment.DIRECTORY_PICTURES == it.getDisplayName(_context) }.forEach {
                it.addCaptureCount()
                it.setCoverPath(capturePath)
            }
        }
    }

    /**
     * 记录上次选中位置
     * @return true=记录成功   false=记录失败
     */
    fun setLastFolderCheckedPosition(lastPosition: Int): Boolean {
        if (_lastFolderCheckedPosition == lastPosition) return false
        _lastFolderCheckedPosition = lastPosition
        return true
    }

    fun setAlbumFolderCursor(cursor: Cursor) {
        _folderCursor = cursor
        readAlbumFromCursor()
    }

    fun getAlbumFolderList() = _folderList

    fun clearFolderSheetDialog() {
        if (_folderBottomSheet != null && _folderBottomSheet?.adapter != null) {
            _folderCursor = null
            _folderBottomSheet?.adapter?.setListData(null)
        }
    }
}