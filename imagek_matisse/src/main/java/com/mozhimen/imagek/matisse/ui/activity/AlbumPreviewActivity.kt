package com.mozhimen.imagek.matisse.ui.activity

import android.database.Cursor
import com.mozhimen.imagek.matisse.entity.Album
import com.matisse.entity.ConstValue
import com.matisse.entity.Item
import com.matisse.model.AlbumCallbacks
import com.matisse.model.AlbumMediaCollection
import com.matisse.ui.activity.BasePreviewActivity
import com.matisse.ui.adapter.PreviewPagerAdapter

/**
 * Created by liubo on 2018/9/11.
 */
class AlbumPreviewActivity : BasePreviewActivity(), AlbumCallbacks {

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