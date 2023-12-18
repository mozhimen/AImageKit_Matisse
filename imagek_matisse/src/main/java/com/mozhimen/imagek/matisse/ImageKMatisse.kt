package com.mozhimen.imagek.matisse

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.fragment.app.Fragment
import com.mozhimen.imagek.matisse.cons.Constants
import com.mozhimen.imagek.matisse.cons.EMimeType
import com.mozhimen.imagek.matisse.helpers.SelectionCreator
import java.lang.ref.WeakReference

/**
 * Entry for Matisse's media selection.
 */
class ImageKMatisse {

    companion object {

        /**
         * Start Matisse from an Activity.
         * This Activity's [Activity.onActivityResult] will be called when user
         * finishes selecting.
         * 绑定Activity/Fragment
         * @param activity Activity instance.
         * @return Matisse instance.
         */
        fun from(activity: Activity?): ImageKMatisse {
            return ImageKMatisse(activity)
        }

        /**
         * Start Matisse from a Fragment.
         *
         * This Fragment's [Fragment.onActivityResult] will be called when user
         * finishes selecting.
         *
         * @param fragment Fragment instance.
         * @return Matisse instance.
         */
        fun from(fragment: Fragment): ImageKMatisse {
            return ImageKMatisse(fragment)
        }

        /**
         * Obtain user selected media' [Uri] list in the starting Activity or Fragment.
         *
         * @param data Intent passed by [Activity.onActivityResult] or
         * [Fragment.onActivityResult].
         * @return User selected media' [Uri] list.
         */
        fun obtainResult(data: Intent): List<Uri>? {
            return data.getParcelableArrayListExtra(Constants.EXTRA_RESULT_SELECTION)
        }

        /**
         * Obtain user selected media path id list in the starting Activity or Fragment.
         *
         * @param data Intent passed by [Activity.onActivityResult] or
         * [Fragment.onActivityResult].
         * @return User selected media path id list.
         */
        fun obtainPathIdResult(data: Intent): List<String>? {
            return data.getStringArrayListExtra(Constants.EXTRA_RESULT_SELECTION_ID)
        }

        /**
         * 直接获取裁剪结果
         */
        fun obtainCropResult(data: Intent?): Uri? {
            return data?.getParcelableExtra(Constants.EXTRA_RESULT_CROP_BACK_BUNDLE)
        }

        /**
         * Obtain state whether user decide to use selected media in original
         *
         * @param data Intent passed by [Activity.onActivityResult] or
         * [Fragment.onActivityResult].
         * @return Whether use original photo
         */
        fun obtainOriginalState(data: Intent) =
            data.getBooleanExtra(Constants.EXTRA_RESULT_ORIGINAL_ENABLE, false)
    }

    ////////////////////////////////////////////////////////////////////

    private constructor(fragment: Fragment) : this(fragment.activity, fragment)

    constructor(activity: Activity?, fragment: Fragment? = null) {
        _fragmentRef = WeakReference<Fragment>(fragment)
        _activityRef = WeakReference<Activity>(activity)
    }

    ////////////////////////////////////////////////////////////////////

    private val _activityRef: WeakReference<Activity>?
    private val _fragmentRef: WeakReference<Fragment>?

    internal val activity: Activity?
        get() = _activityRef?.get()

    internal val fragment: Fragment?
        get() = _fragmentRef?.get()

    ////////////////////////////////////////////////////////////////////

    /**
     * MIME types the selection constrains on.
     * Types not included in the set will still be shown in the grid but can't be chosen.
     * 设置显示类型，单一/混合选择模式 选择约束的MIME类型。未包含在集合中的类型仍将显示在网格中，但不能被选择。
     *
     * @param mimeTypes MIME types set user can choose from.
     * @return [SelectionCreator] to build select specifications.
     * @see EMimeType
     *
     * @see SelectionCreator
     */
    fun choose(mimeTypes: Set<EMimeType>): SelectionCreator {
        return choose(mimeTypes, true)
    }

    /**
     * MIME types the selection constrains on.
     * Types not included in the set will still be shown in the grid but can't be chosen.
     * 选择约束的MIME类型。未包含在集合中的类型仍将显示在网格中，但不能被选择。
     *
     * @param mimeTypes          MIME types set user can choose from.
     * @param mediaTypeExclusive Whether can choose images and videos at the same time during one single choosing
     * process. true corresponds to not being able to choose images and videos at the same
     * time, and false corresponds to being able to do this.
     * @return [SelectionCreator] to build select specifications.
     * @see EMimeType
     *
     * @see SelectionCreator
     */
    fun choose(mimeTypes: Set<EMimeType>, mediaTypeExclusive: Boolean) =
        SelectionCreator(this, mimeTypes, mediaTypeExclusive)
}
