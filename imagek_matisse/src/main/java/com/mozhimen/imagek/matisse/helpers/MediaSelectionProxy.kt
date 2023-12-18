package com.mozhimen.imagek.matisse.helpers

import android.content.Context
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import com.mozhimen.imagek.matisse.R
import com.mozhimen.imagek.matisse.cons.Constants.STATE_COLLECTION_TYPE
import com.mozhimen.imagek.matisse.cons.Constants.STATE_SELECTION
import com.mozhimen.imagek.matisse.mos.IncapableCause
import com.mozhimen.imagek.matisse.mos.MediaItem
import com.mozhimen.imagek.matisse.mos.SelectionSpec
import com.mozhimen.imagek.matisse.utils.PhotoMetadataUtils
import com.mozhimen.imagek.matisse.utils.getPath
import com.mozhimen.imagek.matisse.widgets.CheckView
import java.util.*
import kotlin.collections.ArrayList

class MediaSelectionProxy(private var _context: Context) {

    companion object {
        /**
         * Empty collection
         */
        const val COLLECTION_UNDEFINED = 0x00

        /**
         * Collection only with images
         */
        const val COLLECTION_IMAGE = 0x01

        /**
         * Collection only with videos
         */
        const val COLLECTION_VIDEO = 0x02

        /**
         * Collection with images and videos.
         */
        const val COLLECTION_MIXED = COLLECTION_IMAGE or COLLECTION_VIDEO
    }

    /////////////////////////////////////////////////////////////////

    private lateinit var items: LinkedHashSet<MediaItem>
    private var imageItems: LinkedHashSet<MediaItem>? = null
    private var videoItems: LinkedHashSet<MediaItem>? = null
    private var collectionType = COLLECTION_UNDEFINED
    private val spec: SelectionSpec = SelectionSpec.getInstance()

    /////////////////////////////////////////////////////////////////

    fun onCreate(bundle: Bundle?) {
        if (bundle == null) {
            items = linkedSetOf()
        } else {
            val saved = bundle.getParcelableArrayList<MediaItem>(STATE_SELECTION)
            items = LinkedHashSet(saved!!)
            initImageOrVideoItems()
            collectionType = bundle.getInt(STATE_COLLECTION_TYPE, COLLECTION_UNDEFINED)
        }
    }

    fun onSaveInstanceState(outState: Bundle?) {
        outState?.putParcelableArrayList(STATE_SELECTION, ArrayList(items))
        outState?.putInt(STATE_COLLECTION_TYPE, collectionType)
    }

    fun getDataWithBundle() = Bundle().run {
        putParcelableArrayList(STATE_SELECTION, ArrayList(items))
        putInt(STATE_COLLECTION_TYPE, collectionType)
        this
    }

    fun setDefaultSelection(uris: List<MediaItem>) {
        items.addAll(uris)
    }

    fun overwrite(items: ArrayList<MediaItem>, collectionType: Int) {
        this.collectionType = if (items.size == 0) COLLECTION_UNDEFINED else collectionType

        this.items.clear()
        this.items.addAll(items)
    }

    fun asList() = ArrayList(items)

    fun asListOfUri(): List<Uri> {
        val uris = arrayListOf<Uri>()
        for (item in items) {
            uris.add(item.getContentUri())
        }
        return uris
    }

    fun asListOfString(): List<String> {
        val paths = ArrayList<String>()
        items.forEach {
            val path = getPath(_context, it.getContentUri())
            if (path != null) paths.add(path)
        }

        return paths
    }

    fun isAcceptable(item: MediaItem?): IncapableCause? {
        if (maxSelectableReached(item)) {
            val maxSelectable = currentMaxSelectable(item)
            val maxSelectableTips = currentMaxSelectableTips(item)

            val cause = try {
                _context.getString(maxSelectableTips, maxSelectable)
            } catch (e: Resources.NotFoundException) {
                _context.getString(maxSelectableTips, maxSelectable)
            } catch (e: NoClassDefFoundError) {
                _context.getString(maxSelectableTips, maxSelectable)
            }

            return IncapableCause(cause)
        } else if (typeConflict(item)) {
            return IncapableCause(_context.getString(R.string.error_type_conflict))
        }

        return PhotoMetadataUtils.isAcceptable(_context, item)
    }

    fun maxSelectableReached(item: MediaItem?): Boolean {
        if (!spec.isMediaTypeExclusive()) {
            if (item?.isImage() == true) {
                return spec.maxImageSelectable == imageItems?.size
            } else if (item?.isVideo() == true) {
                return spec.maxVideoSelectable == videoItems?.size
            }
        }
        return spec.maxSelectable == items.size
    }

    fun getCollectionType() = collectionType

    fun isEmpty() = items.isEmpty()

    fun isSelected(item: MediaItem?) = items.contains(item)

    fun count() = items.size

    fun items() = items.toList()

    /**
     * 注：
     * 此处取的是item在选中集合中的序号，
     * 所以不需区分混合选择或单独选择
     */
    fun checkedNumOf(item: MediaItem?): Int {
        val index = ArrayList(items).indexOf(item)
        return if (index == -1) CheckView.UNCHECKED else index + 1
    }

    fun add(item: MediaItem?): Boolean {
        if (typeConflict(item)) {
            throw IllegalArgumentException("Can't select images and videos at the same time.")
        }
        if (item == null) return false

        val added = items.add(item)
        addImageOrVideoItem(item)
        if (added) {
            when (collectionType) {
                COLLECTION_UNDEFINED -> {
                    if (item.isImage()) {
                        collectionType = COLLECTION_IMAGE
                    } else if (item.isVideo()) {
                        collectionType = COLLECTION_VIDEO
                    }
                }

                COLLECTION_IMAGE, COLLECTION_VIDEO -> {
                    if ((item.isImage() && collectionType == COLLECTION_VIDEO)
                        || item.isVideo() && collectionType == COLLECTION_IMAGE
                    ) {
                        collectionType = COLLECTION_MIXED
                    }
                }
            }
        }

        return added
    }

    fun remove(item: MediaItem?): Boolean {
        if (item == null) return false
        val removed = items.remove(item)
        removeImageOrVideoItem(item)
        if (removed) resetType()
        return removed
    }

    fun removeAll() {
        items.clear()
        imageItems?.clear()
        videoItems?.clear()
        resetType()
    }

    /////////////////////////////////////////////////////////////////

    /**
     * 根据混合选择模式，初始化图片与视频集合
     */
    private fun initImageOrVideoItems() {
        if (spec.isMediaTypeExclusive()) return
        items.forEach {
            addImageOrVideoItem(it)
        }
    }

    private fun resetType() {
        if (items.size == 0) {
            collectionType = COLLECTION_UNDEFINED
        } else {
            if (collectionType == COLLECTION_MIXED) refineCollectionType()
        }
    }

    private fun currentMaxSelectableTips(item: MediaItem?): Int {
        if (!spec.isMediaTypeExclusive()) {
            if (item?.isImage() == true) {
                return R.string.error_over_count_of_image
            } else if (item?.isVideo() == true) {
                return R.string.error_over_count_of_video
            }
        }

        return R.string.error_over_count
    }

    // depends
    private fun currentMaxSelectable(item: MediaItem?): Int {
        if (!spec.isMediaTypeExclusive()) {
            if (item?.isImage() == true) {
                return spec.maxImageSelectable
            } else if (item?.isVideo() == true) {
                return spec.maxVideoSelectable
            }
        }

        return spec.maxSelectable
    }

    /**
     * 根据item集合数据设置collectionType
     */
    private fun refineCollectionType() {
        val hasImage = imageItems != null && imageItems?.size ?: 0 > 0
        val hasVideo = videoItems != null && videoItems?.size ?: 0 > 0

        collectionType = if (hasImage && hasVideo) {
            COLLECTION_MIXED
        } else if (hasImage) {
            COLLECTION_IMAGE
        } else if (hasVideo) {
            COLLECTION_VIDEO
        } else {
            COLLECTION_UNDEFINED
        }
    }

    /**
     * Determine whether there will be conflict media types. A user can only select images and videos at the same time
     * while [SelectionSpec.mediaTypeExclusive] is set to false.
     */
    private fun typeConflict(item: MediaItem?) =
        spec.isMediaTypeExclusive()
                && ((item?.isImage() == true && (collectionType == COLLECTION_VIDEO || collectionType == COLLECTION_MIXED))
                || (item?.isVideo() == true && (collectionType == COLLECTION_IMAGE || collectionType == COLLECTION_MIXED)))

    private fun addImageOrVideoItem(item: MediaItem) {
        if (item.isImage()) {
            if (imageItems == null)
                imageItems = linkedSetOf()

            imageItems?.add(item)
        } else if (item.isVideo()) {
            if (videoItems == null)
                videoItems = linkedSetOf()

            videoItems?.add(item)
        }
    }

    private fun removeImageOrVideoItem(item: MediaItem) {
        if (item.isImage()) {
            imageItems?.remove(item)
        } else if (item.isVideo()) {
            videoItems?.remove(item)
        }
    }
}