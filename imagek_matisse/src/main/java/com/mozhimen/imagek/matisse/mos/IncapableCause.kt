package com.mozhimen.imagek.matisse.mos

import android.content.Context
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.mozhimen.imagek.matisse.annors.AForm
import com.mozhimen.imagek.matisse.commons.IOnNoticeEventListener
import com.mozhimen.imagek.matisse.widgets.IncapableDialog

class IncapableCause {

    companion object {
        fun handleCause(context: Context, cause: IncapableCause?) {
            if (cause?.onNoticeEventListener != null) {
                cause.onNoticeEventListener?.invoke(context, cause.form, cause.title ?: "", cause.message ?: "")
                return
            }

            when (cause?.form) {
                AForm.DIALOG -> {
                    IncapableDialog.newInstance(cause.title, cause.message)
                        .show(
                            (context as FragmentActivity).supportFragmentManager,
                            IncapableDialog::class.java.name
                        )
                }

                AForm.TOAST -> {
                    Toast.makeText(context, cause.message, Toast.LENGTH_SHORT).show()
                }

                AForm.LOADING -> {
                    // TODO Leo 2019-12-24 complete loading
                }
            }
        }
    }

    var form = AForm.TOAST
    var title: String? = null
    var message: String? = null
    var dismissLoading: Boolean? = null
    var onNoticeEventListener: IOnNoticeEventListener? = null

    ////////////////////////////////////////////////////////////////////////////////////

    constructor(message: String) : this(AForm.TOAST, message)
    constructor(@AForm form: Int, message: String) : this(form, "", message)
    constructor(@AForm form: Int, title: String, message: String) : this(form, title, message, true)
    constructor(@AForm form: Int, title: String, message: String, dismissLoading: Boolean) {
        this.form = form
        this.title = title
        this.message = message
        this.dismissLoading = dismissLoading
        this.onNoticeEventListener = SelectionSpec.getInstance().onNoticeEventListener
    }
}