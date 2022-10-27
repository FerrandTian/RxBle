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

import android.app.Dialog
import android.content.DialogInterface
import android.view.View
import android.widget.CompoundButton
import android.widget.CompoundButton.OnCheckedChangeListener
import android.widget.PopupWindow

/**
 * @author tianfeng
 */
fun <T : View> activate(activated: Boolean, vararg views: T) {
    for (v in views) v.isActivated = activated
}

fun <T : View> clickable(clickable: Boolean, vararg views: T) {
    for (v in views) v.isClickable = clickable
}

fun <T : View> enable(enabled: Boolean, vararg views: T) {
    for (v in views) v.isEnabled = enabled
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
    for (window in windows) if (window != null && window.isShowing) window.dismiss()
}