package com.mozhimen.imagek.matisse.bases

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mozhimen.imagek.matisse.R
import com.mozhimen.imagek.matisse.annors.AForm
import com.mozhimen.imagek.matisse.mos.IncapableCause
import com.mozhimen.imagek.matisse.mos.SelectionSpec
import com.mozhimen.imagek.matisse.utils.handleCause
import com.mozhimen.imagek.matisse.utils.obtainAttrString

abstract class BaseActivity : AppCompatActivity() {

    lateinit var activity: Activity
    var selectionSpec: SelectionSpec? = null
    var savedInstanceState: Bundle? = null

    //////////////////////////////////////////////////////////////////////////////////

    override fun onCreate(savedInstanceState: Bundle?) {
        selectionSpec = SelectionSpec.getInstance()
        setTheme(selectionSpec?.themeId ?: R.style.Matisse_Default)
        super.onCreate(savedInstanceState)
        if (safeCancelActivity()) return
        activity = this
        setContentView(getResourceLayoutId())
        configActivity()
        configSaveInstanceState(savedInstanceState)
        setViewData()
        initListener()
    }

    //////////////////////////////////////////////////////////////////////////////////

    abstract fun setViewData()

    abstract fun initListener()

    abstract fun getResourceLayoutId(): Int

    //////////////////////////////////////////////////////////////////////////////////

    /**
     * 处理状态栏(状态栏颜色、状态栏字体颜色、是否隐藏等操作)
     *
     * 空实现，供外部重写
     */
    open fun configActivity() {
        if (selectionSpec?.needOrientationRestriction() == true) {
            requestedOrientation = selectionSpec?.orientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    //////////////////////////////////////////////////////////////////////////////////

    /**
     * 获取主题配置中的属性值
     * @param attr 主题配置属性key
     * @param defaultRes 默认值
     */
    fun getAttrString(attr: Int, defaultRes: Int): Int =
        obtainAttrString(this, attr, defaultRes)

    /**
     * 抽离提示方法
     */
    fun handleCauseTips(message: String = "", @AForm form: Int = AForm.TOAST, title: String = "", dismissLoading: Boolean = true) {
        handleCause(activity, IncapableCause(form, title, message, dismissLoading))
    }

    //////////////////////////////////////////////////////////////////////////////////

    private fun safeCancelActivity(): Boolean {
        if (selectionSpec?.hasInited == false) {
            setResult(Activity.RESULT_CANCELED)
            finish()
            return true
        }

        return false
    }

    private fun configSaveInstanceState(savedInstanceState: Bundle?) {
        this.savedInstanceState = savedInstanceState
    }
}