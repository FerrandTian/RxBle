/*
 * Copyright (c) 2022-present, TianFeng.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package tt.tt.widget

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.recyclerview.widget.RecyclerView

/**
 * 列表Item间隙
 *
 * @author tianfeng
 */
class TTSpacingItemDecoration(
    val spacing: Int,
    @RecyclerView.Orientation val orientation: Int = RecyclerView.VERTICAL,
    val includeEdge: Boolean = false
) : ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        val position = parent.getChildAdapterPosition(view)
        if (orientation == RecyclerView.VERTICAL) {
            if (includeEdge) {
                if (position == 0) outRect.top = spacing
                outRect.left = spacing
                outRect.right = spacing
                outRect.bottom = spacing
            } else {
                if (position != 0) outRect.top = spacing
            }
        } else {
            if (includeEdge) {
                if (position == 0) outRect.left = spacing
                outRect.top = spacing
                outRect.right = spacing
                outRect.bottom = spacing
            } else {
                if (position != 0) outRect.left = spacing
            }
        }
    }
}