package tk.limt.demo

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.recyclerview.widget.RecyclerView

/**
 * 列表Item间隙
 *
 * @author tianfeng
 */
class SpacingItemDecoration(private val spacing: Int) : ItemDecoration() {
    @RecyclerView.Orientation
    private var orientation = RecyclerView.VERTICAL
    private var includeEdge = false
    fun orientation(@RecyclerView.Orientation orientation: Int): SpacingItemDecoration {
        this.orientation = orientation
        return this
    }

    fun includeEdge(includeEdge: Boolean): SpacingItemDecoration {
        this.includeEdge = includeEdge
        return this
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        val position = parent.getChildAdapterPosition(view)
        if (orientation == RecyclerView.VERTICAL) {
            if (includeEdge) {
                if (position == 0) {
                    outRect.top = spacing
                }
                outRect.left = spacing
                outRect.right = spacing
                outRect.bottom = spacing
            } else {
                if (position != 0) {
                    outRect.top = spacing
                }
            }
        } else {
            if (includeEdge) {
                if (position == 0) {
                    outRect.left = spacing
                }
                outRect.top = spacing
                outRect.right = spacing
                outRect.bottom = spacing
            } else {
                if (position != 0) {
                    outRect.left = spacing
                }
            }
        }
    }
}