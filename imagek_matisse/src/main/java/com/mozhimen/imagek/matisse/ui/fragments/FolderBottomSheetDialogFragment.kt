package com.mozhimen.imagek.matisse.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mozhimen.basick.utilk.android.view.UtilKScreen
import com.mozhimen.imagek.matisse.R
import com.mozhimen.imagek.matisse.bases.BaseBottomSheetDialogFragment
import com.mozhimen.imagek.matisse.commons.IFolderBottomSheetListener
import com.mozhimen.imagek.matisse.cons.CImageKMatisse
import com.mozhimen.imagek.matisse.ui.adapters.FolderMediaItemAdapter

class FolderBottomSheetDialogFragment : BaseBottomSheetDialogFragment() {
    companion object {
        fun newInstance(currentPos: Int): FolderBottomSheetDialogFragment {
            val folderBottomSheetDialogFragment = FolderBottomSheetDialogFragment()
            folderBottomSheetDialogFragment.arguments = Bundle().apply {
                putInt(CImageKMatisse.FOLDER_CHECK_POSITION, currentPos)
            }
            return folderBottomSheetDialogFragment
        }
    }

    ///////////////////////////////////////////////////////////////////

    private var _kParentView: View? = null
    private var _currentPosition = 0
    private lateinit var _recyclerView: RecyclerView

    var folderMediaItemAdapter: FolderMediaItemAdapter? = null
    var folderBottomSheetListener: IFolderBottomSheetListener? = null

    ///////////////////////////////////////////////////////////////////

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _currentPosition = arguments?.getInt(CImageKMatisse.FOLDER_CHECK_POSITION, 0) ?: 0
    }

    override fun getContentView(inflater: LayoutInflater, container: ViewGroup): View {
        if (_kParentView == null) {
            _kParentView = inflater.inflate(R.layout.fragment_dialog_bottom_sheet_folder, container, false)
            setDefaultHeight(UtilKScreen.getHeightOfDefaultDisplay() / 2)
            initView()
        } else {
            if (_kParentView?.parent != null) {
                val parent = _kParentView?.parent as ViewGroup
                parent.removeView(view)
            }
        }
        return _kParentView!!
    }

    ///////////////////////////////////////////////////////////////////

    private fun initView() {
        _recyclerView = _kParentView?.findViewById(R.id.recyclerview)!!
        _recyclerView.layoutManager = LinearLayoutManager(context)
        _recyclerView.setHasFixedSize(true)
        _recyclerView.layoutParams.height = UtilKScreen.getHeightOfDefaultDisplay() / 2
        folderMediaItemAdapter = FolderMediaItemAdapter(requireContext(), _currentPosition).apply {
            _recyclerView.adapter = this
            folderBottomSheetListener?.initData(this)

            itemClickListener = object : FolderMediaItemAdapter.OnItemClickListener {
                override fun onItemClick(view: View, position: Int) {
                    dismiss()
                    if (albumList.isNotEmpty()){
                        Log.d(TAG, "onItemClick: albumList size ${albumList.size}")
                    }
                    folderBottomSheetListener?.onItemClick(albumList[position], position)
                }
            }
        }
    }
}