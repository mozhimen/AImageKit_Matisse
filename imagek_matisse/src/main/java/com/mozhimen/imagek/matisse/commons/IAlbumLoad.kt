package com.mozhimen.imagek.matisse.commons

import android.database.Cursor

interface IAlbumLoad {

    /**
     * 相册查询完成回调
     */
    fun onAlbumLoad(cursor: Cursor)

    /**
     *
     */
    fun onAlbumReset()
}