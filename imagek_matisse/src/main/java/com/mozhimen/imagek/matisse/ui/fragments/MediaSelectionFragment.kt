package com.mozhimen.imagek.matisse.ui.fragments

import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.mozhimen.imagek.matisse.R
import com.mozhimen.imagek.matisse.databinding.FragmentMediaSelectionBinding
import com.mozhimen.imagek.matisse.mos.Album
import com.mozhimen.imagek.matisse.cons.CImageKMatisse
import com.mozhimen.imagek.matisse.mos.MediaItem
import com.mozhimen.imagek.matisse.mos.SelectionSpec
import com.mozhimen.imagek.matisse.commons.IAlbumLoadListener
import com.mozhimen.imagek.matisse.helpers.loader.AlbumMediaCursorLoaderCallbacks
import com.mozhimen.imagek.matisse.helpers.MediaSelectionProxy
import com.mozhimen.imagek.matisse.ui.adapters.MediaAlbumAdapter
import com.mozhimen.imagek.matisse.utils.MAX_SPAN_COUNT
import com.mozhimen.imagek.matisse.utils.spanCount
import com.mozhimen.imagek.matisse.widgets.MediaGridInset
import kotlin.math.max
import kotlin.math.min

class MediaSelectionFragment : Fragment(), IAlbumLoadListener, MediaAlbumAdapter.CheckStateListener, MediaAlbumAdapter.OnMediaClickListener {

    private val albumMediaCollection = AlbumMediaCursorLoaderCallbacks()
    private lateinit var adapter: MediaAlbumAdapter
    private lateinit var album: Album
    private lateinit var selectionProvider: IMediaSelectionProvider
    private lateinit var checkStateListener: MediaAlbumAdapter.CheckStateListener
    private lateinit var onMediaClickListener: MediaAlbumAdapter.OnMediaClickListener

    companion object {
        fun newInstance(album: Album): MediaSelectionFragment {
            val fragment = MediaSelectionFragment()
            fragment.arguments = Bundle().apply { putParcelable(CImageKMatisse.EXTRA_ALBUM, album) }
            return fragment
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is IMediaSelectionProvider) {
            selectionProvider = context
        } else {
            throw IllegalStateException("Context must implement SelectionProvider.")
        }

        if (context is MediaAlbumAdapter.CheckStateListener) checkStateListener = context

        if (context is MediaAlbumAdapter.OnMediaClickListener) onMediaClickListener = context
    }

    private var mBinding: FragmentMediaSelectionBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val binding = FragmentMediaSelectionBinding.inflate(inflater, container, false)
        mBinding = binding
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val recyclerview = mBinding?.recyclerview
        recyclerview ?: return
        album = arguments?.getParcelable(CImageKMatisse.EXTRA_ALBUM)!!
        adapter = MediaAlbumAdapter(
            requireContext(), selectionProvider.provideSelectedItemCollection(), recyclerview
        )
        adapter.checkStateListener = this
        adapter.onMediaClickListener = this
        recyclerview.setHasFixedSize(true)

        val selectionSpec = SelectionSpec.getInstance()
        val spanCount = if (selectionSpec.gridExpectedSize > 0) {
            spanCount(requireContext(), selectionSpec.gridExpectedSize)
        } else {
            max(min(selectionSpec.spanCount, MAX_SPAN_COUNT), 1)
        }

        recyclerview.layoutManager = GridLayoutManager(requireContext(), spanCount)
        val spacing = resources.getDimensionPixelSize(R.dimen.media_grid_spacing)
        recyclerview.addItemDecoration(MediaGridInset(spanCount, spacing, false))
        recyclerview.itemAnimator?.changeDuration = 0
        recyclerview.adapter = adapter
        albumMediaCollection.onCreate(requireActivity(), this)
        albumMediaCollection.load(album, selectionSpec.capture)
    }

    fun refreshMediaGrid() {
        adapter.notifyDataSetChanged()
    }

    override fun onMediaClick(album: Album?, item: MediaItem, adapterPosition: Int) {
        onMediaClickListener.onMediaClick(this.album, item, adapterPosition)
    }

    override fun onSelectUpdate() {
        checkStateListener.onSelectUpdate()
    }

    override fun onAlbumStart() {
        // do nothing
    }

    override fun onAlbumLoad(cursor: Cursor) {
        adapter.swapCursor(cursor)
    }

    override fun onAlbumReset() {
        adapter.swapCursor(null)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        albumMediaCollection.onDestroy()
    }

    interface IMediaSelectionProvider {
        fun provideSelectedItemCollection(): MediaSelectionProxy
    }
}