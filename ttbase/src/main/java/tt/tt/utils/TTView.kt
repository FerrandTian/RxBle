/*
 * Copyright (C) 2022 TianFeng
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:JvmName("TTView")

package tt.tt.utils

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.os.Build
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Checkable
import android.widget.CompoundButton
import android.widget.CompoundButton.OnCheckedChangeListener
import android.widget.PopupMenu
import android.widget.PopupWindow

fun requestFullscreen(activity: Activity) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        val window = activity.window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = Color.TRANSPARENT
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
    }
}

fun setStatusBarColor(activity: Activity, dark: Boolean) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val decorView = activity.window.decorView
        var vis = decorView.systemUiVisibility
        vis =
            if (dark) vis or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR else vis and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        decorView.systemUiVisibility = vis
    }
}

fun setEmptyView(root: ViewGroup, emptyView: View, show: Boolean) {
    if (show) {
        emptyView.visibility = View.VISIBLE
        val view = root.getChildAt(0)
        if (view != emptyView) {
            root.addView(emptyView, 0)
            if (!emptyView.isLaidOut) if (root.isLaidOut) requestLayout(
                root, emptyView
            ) else root.addOnLayoutChangeListener(object :
                View.OnLayoutChangeListener {
                override fun onLayoutChange(
                    v: View, left: Int, top: Int, right: Int, bottom: Int,
                    oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int,
                ) {
                    v.removeOnLayoutChangeListener(this)
                    requestLayout(v as ViewGroup, v.getChildAt(0))
                }
            })
        }
        for (i in 1 until root.childCount) root.getChildAt(i).visibility = View.GONE
    } else {
        val view = root.getChildAt(0)
        if (view == emptyView) root.removeViewAt(0)
        for (i in 0 until root.childCount) root.getChildAt(i).visibility = View.VISIBLE
    }
    root.requestLayout()
}

fun requestLayout(parent: ViewGroup, child: View) {
    child.measure(
        View.MeasureSpec.makeMeasureSpec(
            parent.measuredWidth - parent.paddingLeft - parent.paddingRight,
            View.MeasureSpec.EXACTLY
        ), View.MeasureSpec.makeMeasureSpec(
            parent.measuredHeight - parent.paddingTop - parent.paddingBottom,
            View.MeasureSpec.EXACTLY
        )
    )
    val childLeft = parent.paddingLeft
    val childTop = parent.paddingTop
    val childWidth = parent.measuredWidth - parent.paddingLeft - parent.paddingRight
    val childHeight = parent.measuredHeight - parent.paddingTop - parent.paddingBottom
    child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight)
}

fun <T : View> activate(activated: Boolean, vararg views: T) {
    for (v in views) v.isActivated = activated
}

fun <T : View> clickable(clickable: Boolean, vararg views: T) {
    for (v in views) v.isClickable = clickable
}

fun <T : View> enable(enabled: Boolean, vararg views: T) {
    for (v in views) v.isEnabled = enabled
}

fun <T : MenuItem> check(checked: Boolean, vararg views: T) {
    for (v in views) v.isChecked = checked
}

fun <T : Checkable> check(checked: Boolean, vararg views: T) {
    for (v in views) v.isChecked = checked
}

fun <T : CompoundButton> checkListener(l: OnCheckedChangeListener?, vararg views: T) {
    for (v in views) v.setOnCheckedChangeListener(l)
}

fun <T : View> clickListener(l: View.OnClickListener?, vararg views: T) {
    for (v in views) v.setOnClickListener(l)
}

fun <T : View> longClickListener(l: View.OnLongClickListener?, vararg views: T) {
    for (v in views) v.setOnLongClickListener(l)
}

fun <T : View> select(selected: Boolean, vararg views: T) {
    for (v in views) v.isSelected = selected
}

fun <T : View> visible(vararg views: T) {
    for (v in views) v.visibility = View.VISIBLE
}

fun <T : View> invisible(vararg views: T) {
    invisible(false, *views)
}

fun <T : View> invisible(visible: Boolean, vararg views: T) {
    for (v in views) v.visibility = if (visible) View.VISIBLE else View.INVISIBLE
}

fun <T : View> gone(vararg views: T) {
    gone(false, *views)
}

fun <T : View> gone(visible: Boolean, vararg views: T) {
    for (v in views) v.visibility = if (visible) View.VISIBLE else View.GONE
}

fun isShowing(view: Any?): Boolean = when (view) {
    is View -> view.isShown
    is Dialog -> view.isShowing
    is PopupWindow -> view.isShowing
    else -> false
}

fun <T : DialogInterface> dismiss(vararg dialogs: T?) {
    for (dialog in dialogs) dialog?.dismiss()
}

fun <T : DialogInterface> cancel(vararg dialogs: T?) {
    for (dialog in dialogs) dialog?.cancel()
}

fun <T : PopupWindow> dismiss(vararg windows: T?) {
    for (window in windows) window?.dismiss()
}

fun <T : PopupMenu> dismiss(vararg menus: T?) {
    for (menu in menus) menu?.dismiss()
}