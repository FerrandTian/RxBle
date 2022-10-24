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