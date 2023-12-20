package com.mozhimen.imagek.matisse.ui.adapters

import android.content.Context
import android.database.Cursor
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mozhimen.imagek.matisse.R
import com.mozhimen.imagek.matisse.mos.Album
import com.mozhimen.imagek.matisse.mos.MediaItem
import com.mozhimen.imagek.matisse.mos.SelectionSpec
import com.mozhimen.imagek.matisse.helpers.MediaSelectionProxy
import com.mozhimen.imagek.matisse.utils.handleCause
import com.mozhimen.imagek.matisse.utils.setTextDrawable
import com.mozhimen.imagek.matisse.widgets.CheckView
import com.mozhimen.imagek.matisse.widgets.MediaGrid

class MediaAlbumAdapter(
    private var context: Context, private var selectedCollection: MediaSelectionProxy,
    private var recyclerView: RecyclerView
) : RecyclerViewCursorAdapter<RecyclerView.ViewHolder>(null), MediaGrid.IOnMediaGridClickListener {

    companion object {
        const val VIEW_TYPE_CAPTURE = 0X01
        const val VIEW_TYPE_MEDIA = 0X02
    }

    ////////////////////////////////////////////////////////////////////////////

    private var placeholder: Drawable? = null
    private var selectionSpec: SelectionSpec = SelectionSpec.getInstance()
    var checkStateListener: CheckStateListener? = null
    var onMediaClickListener: OnMediaClickListener? = null
    private var imageResize = 0
    private var layoutInflater: LayoutInflater

    ////////////////////////////////////////////////////////////////////////////

    init {
        val ta = context.theme.obtainStyledAttributes(intArrayOf(R.attr.ItemImage_ResPlaceholder))
        placeholder = ta.getDrawable(0)
        ta.recycle()

        layoutInflater = LayoutInflater.from(context)
    }

    ////////////////////////////////////////////////////////////////////////////

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_CAPTURE -> {
                val v = layoutInflater.inflate(R.layout.item_media_capture_photo, parent, false)
                CaptureViewHolder(v).run {
                    itemView.setOnClickListener {
                        if (it.context is OnPhotoCapture) (it.context as OnPhotoCapture).capture()
                    }
                    this
                }
            }
            else -> {
                val v = layoutInflater.inflate(R.layout.item_media_grid, parent, false)
                MediaViewHolder(v)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, cursor: Cursor, position: Int) {
        holder.apply {
            when (this) {
                is CaptureViewHolder ->
                    setTextDrawable(itemView.context, hint, R.attr.ItemPhoto_TextColor)
                is MediaViewHolder -> {
                    val item = MediaItem.valueOf(cursor, position)
                    mediaGrid.preBindMedia(
                        MediaGrid.PreBindInfo(
                            getImageResize(mediaGrid.context), placeholder,
                            selectionSpec.isCountable(), holder
                        )
                    )
                    item?.let {
                        mediaGrid.bindMedia(it)
                        mediaGrid.listener = this@MediaAlbumAdapter
                        setCheckStatus(it, mediaGrid)
                    }
                }
            }
        }
    }

    override fun getItemViewType(position: Int, cursor: Cursor) =
        if (MediaItem.valueOf(cursor)?.isCapture() == true) VIEW_TYPE_CAPTURE else VIEW_TYPE_MEDIA

    override fun onThumbnailClicked(thumbnail: ImageView, item: MediaItem, holder: RecyclerView.ViewHolder) {
        onMediaClickListener?.onMediaClick(null, item, holder.adapterPosition)
    }

    /**
     * 单选：
     *     a.选中：刷新当前项与上次选择项
     *     b.取消选中：刷新当前项与上次选择项
     *
     * 多选：
     *      1. 按序号计数
     *          a.选中：仅刷新选中的item
     *          b.取消选中：
     *              取消最后一位：仅刷新当前操作的item
     *              取消非最后一位：刷新所有选中的item
     *      2. 无序号计数
     *          a.选中：仅刷新选中的item
     *          b.取消选中：仅刷新选中的item
     */
    override fun onCheckViewClicked(checkView: CheckView, item: MediaItem, holder: RecyclerView.ViewHolder) {
        if (selectionSpec.isSingleChoose()) {
            notifySingleChooseData(item)
        } else {
            notifyMultiChooseData(item)
        }
    }

    ////////////////////////////////////////////////////////////////////////////

    private fun getImageResize(context: Context): Int {
        if (imageResize != 0) return imageResize

        val layoutManager = recyclerView.layoutManager as GridLayoutManager
        val spanCount = layoutManager.spanCount
        val screenWidth = context.resources.displayMetrics.widthPixels
        val availableWidth = screenWidth - context.resources.getDimensionPixelSize(
            R.dimen.spacing_media_grid
        ) * (spanCount - 1)

        imageResize = availableWidth / spanCount
        imageResize = (imageResize * selectionSpec.thumbnailScale).toInt()
        return imageResize
    }

    /**
     * 初始化选择框选中状态
     */
    private fun setCheckStatus(item: MediaItem, mediaGrid: MediaGrid) {
        // 初始化时 添加上次选中的图片
        setLastChooseItems(item)
        if (selectionSpec.isCountable()) {
            val checkedNum = selectedCollection.checkedNumOf(item)

            if (checkedNum > 0) {
                mediaGrid.setCheckedNum(checkedNum)
            } else {
                mediaGrid.setCheckedNum(
                    if (selectedCollection.maxSelectableReached(item)) CheckView.UNCHECKED else checkedNum
                )
            }
        } else {
            mediaGrid.setChecked(selectedCollection.isSelected(item))
        }
    }

    /**
     * 单选刷新数据
     */
    private fun notifySingleChooseData(item: MediaItem) {
        if (selectedCollection.isSelected(item)) {
            selectedCollection.remove(item)
            notifyItemChanged(item.positionInList)
        } else {
            notifyLastItem()
            if (!addItem(item)) return
            notifyItemChanged(item.positionInList)
        }
        notifyCheckStateChanged()
    }

    private fun notifyLastItem() {
        val itemLists = selectedCollection.asList()
        if (itemLists.size > 0) {
            selectedCollection.remove(itemLists[0])
            notifyItemChanged(itemLists[0].positionInList)
        }
    }

    /**
     * 多选刷新数据
     *      1. 按序号计数
     *          a.选中：仅刷新选中的item
     *          b.取消选中：
     *              取消最后一位：仅刷新当前操作的item
     *              取消非最后一位：刷新所有选中的item
     *      2. 无序号计数
     *          a.选中：仅刷新选中的item
     *          b.取消选中：仅刷新选中的item
     */
    private fun notifyMultiChooseData(item: MediaItem) {
        if (selectionSpec.isCountable()) {
            if (notifyMultiCountableItem(item)) return
        } else {
            if (selectedCollection.isSelected(item)) {
                selectedCollection.remove(item)
            } else {
                if (!addItem(item)) return
            }

            notifyItemChanged(item.positionInList)
        }

        notifyCheckStateChanged()
    }

    /**
     * @return 是否拦截 true=拦截  false=不拦截
     */
    private fun notifyMultiCountableItem(item: MediaItem): Boolean {
        val checkedNum = selectedCollection.checkedNumOf(item)
        if (checkedNum == CheckView.UNCHECKED) {
            if (!addItem(item)) return true
            notifyItemChanged(item.positionInList)
        } else {
            selectedCollection.remove(item)
            // 取消选中中间序号时，刷新所有选中item
            if (checkedNum != selectedCollection.count() + 1) {
                selectedCollection.asList().forEach {
                    notifyItemChanged(it.positionInList)
                }
            }
            notifyItemChanged(item.positionInList)
        }
        return false
    }

    private fun notifyCheckStateChanged() {
        checkStateListener?.onSelectUpdate()
    }

    private fun addItem(item: MediaItem): Boolean {
        if (!assertAddSelection(context, item)) return false
        selectedCollection.add(item)
        return true
    }

    private fun assertAddSelection(context: Context, item: MediaItem): Boolean {
        val cause = selectedCollection.isAcceptable(item)
        handleCause(context, cause)
        return cause == null
    }

    /**
     * 初始化外部传入上次选中的图片
     */
    private fun setLastChooseItems(item: MediaItem) {
        if (selectionSpec.lastChoosePictureIdsOrUris == null) return

        selectionSpec.lastChoosePictureIdsOrUris?.forEachIndexed { index, s ->
            if (s == item.id.toString() || s == item.getContentUri().toString()) {
                selectedCollection.add(item)
                selectionSpec.lastChoosePictureIdsOrUris!![index] = ""
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    
    interface CheckStateListener {
        fun onSelectUpdate()
    }

    interface OnMediaClickListener {
        fun onMediaClick(album: Album?, item: MediaItem, adapterPosition: Int)
    }

    interface OnPhotoCapture {
        fun capture()
    }

    class MediaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mediaGrid: MediaGrid = itemView as MediaGrid
    }

    class CaptureViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var hint: TextView = itemView.findViewById(R.id.hint)
    }
}