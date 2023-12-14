package com.mozhimen.imagek.matisse.ui.activities

import android.database.Cursor
import com.mozhimen.imagek.matisse.mos.Album
import com.mozhimen.imagek.matisse.cons.ConstValue
import com.mozhimen.imagek.matisse.mos.Item
import com.mozhimen.imagek.matisse.commons.IAlbum
import com.mozhimen.imagek.matisse.mos.AlbumMediaCollection
import com.mozhimen.imagek.matisse.ui.adapters.PreviewPagerAdapter

/**
 * Created by liubo on 2018/9/11.
 */
class AlbumPreviewActivity : BasePreviewActivity(), IAlbum {

    private var collection = AlbumMediaCollection()
    private var isAlreadySetPosition = false

    override fun setViewData() {
        super.setViewData()
        collection.onCreate(this, this)
        val album = intent.getParcelableExtra<Album>(ConstValue.EXTRA_ALBUM) ?: return
        collection.load(album)
        val item = intent.getParcelableExtra<Item>(ConstValue.EXTRA_ITEM)
        check_view?.apply {
            if (spec?.isCountable() == true) {
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
        val items = ArrayList<Item>()
        while (cursor.moveToNext()) {
            Item.valueOf(cursor)?.run { items.add(this) }
        }

        if (items.isEmpty()) return
        val adapter = pager?.adapter as PreviewPagerAdapter
        adapter.addAll(items)
        adapter.notifyDataSetChanged()
        if (!isAlreadySetPosition) {
            isAlreadySetPosition = true
            val selected = intent.getParcelableExtra<Item>(ConstValue.EXTRA_ITEM) ?: return
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