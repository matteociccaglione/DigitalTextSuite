package it.trentabitplus.digitaltextsuite.decorator

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class LinearSpacingDecorator(val spacing: Int,val spanCount: Int): RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val column = position % spanCount
        if(position < spanCount){
            outRect.top = spacing
        }
        view.layoutParams.width = parent.width
        outRect.left = spacing - column * spacing / spanCount
        outRect.right = (column + 1) * spacing / spanCount
        outRect.bottom = spacing
    }
}