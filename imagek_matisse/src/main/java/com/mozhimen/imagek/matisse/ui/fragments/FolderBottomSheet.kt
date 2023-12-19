package com.mozhimen.imagek.matisse.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mozhimen.imagek.matisse.R
import com.mozhimen.imagek.matisse.commons.IFolderBottomSheetListener
import com.mozhimen.imagek.matisse.cons.CImageKMatisse
import com.mozhimen.imagek.matisse.ui.adapters.FolderMediaItemAdapter
import com.mozhimen.imagek.matisse.utils.getScreenHeight

class FolderBottomSheet : BottomSheetDialogFragment() {

    private var kParentView: View? = null
    private lateinit var recyclerView: RecyclerView
    var adapter: FolderMediaItemAdapter? = null
    var folderBottomSheetListener: IFolderBottomSheetListener? = null
    private var currentPosition = 0

    ///////////////////////////////////////////////////////////////////

    companion object {
        fun instance(context: Context, currentPos: Int, tag: String): FolderBottomSheet {
            val folderBottomSheet = FolderBottomSheet()
            val bundle = Bundle()
            bundle.putInt(CImageKMatisse.FOLDER_CHECK_POSITION, currentPos)
            folderBottomSheet.arguments = bundle
            folderBottomSheet.show((context as FragmentActivity).supportFragmentManager, tag)
            return folderBottomSheet
        }
    }

    ///////////////////////////////////////////////////////////////////

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentPosition = arguments?.getInt(CImageKMatisse.FOLDER_CHECK_POSITION, 0) ?: 0
    }

    override fun getContentView(inflater: LayoutInflater, container: ViewGroup): View {
        if (kParentView == null) {
            kParentView = inflater.inflate(R.layout.dialog_bottom_sheet_folder, container, false)
            setDefaultHeight(getScreenHeight(requireContext()) / 2)
            initView()
        } else {
            if (kParentView?.parent != null) {
                val parent = kParentView?.parent as ViewGroup
                parent.removeView(view)
            }
        }
        return kParentView!!
    }

    ///////////////////////////////////////////////////////////////////

    private fun initView() {
        recyclerView = kParentView?.findViewById(R.id.recyclerview)!!
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)
        setRecyclerViewHeight()
        adapter = FolderMediaItemAdapter(requireContext(), currentPosition).apply {
            recyclerView.adapter = this
            folderBottomSheetListener?.initData(this)

            itemClickListener = object : FolderMediaItemAdapter.OnItemClickListener {
                override fun onItemClick(view: View, position: Int) {
                    dismiss()
                    folderBottomSheetListener?.onItemClick(albumList[position], position)
                }
            }
        }
    }

    private fun setRecyclerViewHeight() {
        recyclerView.layoutParams.height = getScreenHeight(requireContext()) / 2
    }
}