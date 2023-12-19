package com.mozhimen.imagek.matisse.helpers

import android.os.Bundle
import com.mozhimen.imagek.matisse.commons.IAlbumLoadListener
import com.mozhimen.imagek.matisse.helpers.loader.AlbumCursorLoaderCallbacks
import com.mozhimen.imagek.matisse.ui.activities.MatisseActivity

class AlbumLoadProxy(
    private var _matisseActivity: MatisseActivity, private var _albumLoadListener: IAlbumLoadListener
) {

    private var _albumCursorLoaderCallbacks: AlbumCursorLoaderCallbacks? = null

    ///////////////////////////////////////////////////////////////

    init {
        _albumCursorLoaderCallbacks = AlbumCursorLoaderCallbacks()
        loadAlbumData()
    }

    ///////////////////////////////////////////////////////////////

    fun loadAlbumData() {
        _albumCursorLoaderCallbacks?.apply {
            onCreate(_matisseActivity, _albumLoadListener)
            _matisseActivity.savedInstanceState?.apply {
                _albumCursorLoaderCallbacks?.onRestoreInstanceState(this)
            }
            loadAlbums()
        }
    }

    fun onSaveInstanceState(outState: Bundle) {
        _albumCursorLoaderCallbacks?.onSaveInstanceState(outState)
    }

    /**
     * 设置当前选中位置，用于数据回收后恢复
     */
    fun setStateCurrentSelection(position: Int) {
        _albumCursorLoaderCallbacks?.setStateCurrentSelection(position)
    }

    fun onDestroy() {
        _albumCursorLoaderCallbacks?.onDestroy()
    }
}