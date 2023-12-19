package com.mozhimen.imagek.matisse.helpers.loader

import android.content.Context
import android.database.Cursor
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import com.mozhimen.imagek.matisse.commons.IAlbumLoadListener
import com.mozhimen.imagek.matisse.mos.Album
import java.lang.ref.WeakReference

class AlbumMediaCursorLoaderCallbacks : LoaderManager.LoaderCallbacks<Cursor> {

    companion object {
        const val LOADER_ID = 2
        const val ARGS_ALBUM = "args_album"
        const val ARGS_ENABLE_CAPTURE = "args_enable_capture"
    }

    //////////////////////////////////////////////////////////

    private var _contextRef: WeakReference<Context>? = null
    private var _loaderManager: LoaderManager? = null
    private var _albumLoadListener: IAlbumLoadListener? = null

    //////////////////////////////////////////////////////////

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        val content = _contextRef?.get()

        val album = args?.getParcelable<Album>(ARGS_ALBUM)
        return AlbumMediaCursorLoader.newInstance(
            content!!, album!!, album.isAll()
                    && args.getBoolean(ARGS_ENABLE_CAPTURE, false)
        )
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
        if (_contextRef?.get() == null) return

        _albumLoadListener?.onAlbumLoad(data!!)
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        if (_contextRef?.get() == null) return

        _albumLoadListener?.onAlbumReset()
    }

    //////////////////////////////////////////////////////////

    fun onCreate(context: FragmentActivity, callbacks: IAlbumLoadListener) {
        this._contextRef = WeakReference(context)
        _loaderManager = LoaderManager.getInstance(context)
        this._albumLoadListener = callbacks
    }

    fun onDestroy() {
        _loaderManager?.destroyLoader(LOADER_ID)
        if (_albumLoadListener != null) _albumLoadListener = null
    }

    fun load(target: Album) {
        load(target, false)
    }

    fun load(target: Album, enableCapture: Boolean) {
        val args = Bundle()
        args.putParcelable(ARGS_ALBUM, target)
        args.putBoolean(ARGS_ENABLE_CAPTURE, enableCapture)
        _loaderManager?.initLoader(LOADER_ID, args, this)
    }
}