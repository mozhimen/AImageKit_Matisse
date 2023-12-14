package com.mozhimen.imagek.matisse.commons

import android.database.Cursor

interface IAlbum {
    fun onAlbumStart()
    fun onAlbumLoad(cursor: Cursor)
    fun onAlbumReset()
}