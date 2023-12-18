package com.mozhimen.imagek.matisse.ui.activities

import android.database.Cursor
import com.mozhimen.imagek.matisse.bases.BasePreviewActivity
import com.mozhimen.imagek.matisse.mos.Album
import com.mozhimen.imagek.matisse.cons.Constants
import com.mozhimen.imagek.matisse.mos.MediaItem
import com.mozhimen.imagek.matisse.commons.IAlbumListener
import com.mozhimen.imagek.matisse.mos.AlbumMediaCollection
import com.mozhimen.imagek.matisse.ui.adapters.PreviewPagerAdapter

/**
 * Created by liubo on 2018/9/11.
 */
class AlbumPreviewActivity : BasePreviewActivity(), IAlbumListener {

    private var collection = AlbumMediaCollection()
    private var isAlreadySetPosition = false

    override fun setViewData() {
        super.setViewData()
        collection.onCreate(this, this)
        val album = intent.getParcelableExtra<Album>(Constants.EXTRA_ALBUM) ?: return
        collection.load(album)
        val item = intent.getParcelableExtra<MediaItem>(Constants.EXTRA_ITEM)
        check_view?.apply {
            if (selectionSpec?.isCountable() == true) {
                setCheckedNum(selectedCollection.checkedNumOf(item))
            } else {
                setChecked(selectedCollection.isSelected(item))
            }
        }
        updateSize(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        collection.onDestroy()
    }

    override fun onAlbumLoad(cursor: Cursor) {
        val items = ArrayList<MediaItem>()
        while (cursor.moveToNext()) {
            MediaItem.valueOf(cursor)?.run { items.add(this) }
        }

        if (items.isEmpty()) return
        val adapter = pager?.adapter as PreviewPagerAdapter
        adapter.addAll(items)
        adapter.notifyDataSetChanged()
        if (!isAlreadySetPosition) {
            isAlreadySetPosition = true
            val selected = intent.getParcelableExtra<MediaItem>(Constants.EXTRA_ITEM) ?: return
            val selectedIndex = items.indexOf(selected)
            pager?.setCurrentItem(selectedIndex, false)
            previousPos = selectedIndex
        }
    }

    override fun onAlbumReset() {
    }

    override fun onAlbumStart() {
    }
}