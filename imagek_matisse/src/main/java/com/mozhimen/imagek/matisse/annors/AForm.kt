package com.mozhimen.imagek.matisse.annors

import androidx.annotation.IntDef

/**
 * @ClassName AForm
 * @Description TODO
 * @Author Mozhimen & Kolin Zhao
 * @Date 2023/12/14
 * @Version 1.0
 */
@Retention(AnnotationRetention.SOURCE)
@IntDef(AForm.TOAST, AForm.DIALOG, AForm.LOADING, AForm.NONE)
annotation class AForm {
    companion object {
        const val TOAST = 0x0001
        const val DIALOG = 0x0002
        const val LOADING = 0x0003
        const val NONE = 0x0004
    }
}