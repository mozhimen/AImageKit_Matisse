package com.matisse.ui.view

import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.matisse.R
import com.matisse.databinding.FragmentMediaSelectionBinding
import com.mozhimen.imagek.matisse.entity.Album
import com.matisse.entity.ConstValue
import com.matisse.entity.Item
import com.matisse.internal.entity.SelectionSpec
import com.matisse.model.AlbumCallbacks
import com.matisse.model.AlbumMediaCollection
import com.matisse.model.SelectedItemCollection
import com.matisse.ui.adapter.AlbumMediaAdapter
import com.matisse.utils.MAX_SPAN_COUNT
import com.matisse.utils.spanCount
import com.matisse.widget.MediaGridInset
import kotlin.math.max
import kotlin.math.min

class MediaSelectionFragment : Fragment(), AlbumCallbacks, AlbumMediaAdapter.CheckStateListener,
    AlbumMediaAdapter.OnMediaClickListener {

    private val albumMediaCollection = AlbumMediaCollection()
    private lateinit var adapter: AlbumMediaAdapter
    private lateinit var album: Album
    private lateinit var selectionProvider: SelectionProvider
    private lateinit var checkStateListener: AlbumMediaAdapter.CheckStateListener
    private lateinit var onMediaClickListener: AlbumMediaAdapter.OnMediaClickListener

    companion object {
        fun newInstance(album: Album): MediaSelectionFragment {
            val fragment = MediaSelectionFragment()
            fragment.arguments = Bundle().apply { putParcelable(ConstValue.EXTRA_ALBUM, album) }
            return fragment
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is SelectionProvider) {
            selectionProvider = context
        } else {
            throw IllegalStateException("Context must implement SelectionProvider.")
        }

        if (context is AlbumMediaAdapter.CheckStateListener) checkStateListener = context

        if (context is AlbumMediaAdapter.OnMediaClickListener) onMediaClickListener = context
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
        album = arguments?.getParcelable(ConstValue.EXTRA_ALBUM)!!
        adapter = AlbumMediaAdapter(
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

    override fun onMediaClick(album: Album?, item: Item, adapterPosition: Int) {
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

    interface SelectionProvider {
        fun provideSelectedItemCollection(): SelectedItemCollection
    }
}