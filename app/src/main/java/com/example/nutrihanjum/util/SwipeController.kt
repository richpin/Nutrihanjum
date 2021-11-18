package com.example.nutrihanjum.util

import android.graphics.Canvas
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.RecyclerView
import com.example.nutrihanjum.community.CommentRecyclerViewAdapter
import kotlin.math.max
import kotlin.math.min

class SwipeController : Callback() {
    private var currentPosition: Int? = null
    private var previousPosition: Int? = null
    private var currentDx = 0f
    private var clamp = 0f

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        return makeMovementFlags(0, LEFT)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        currentDx = 0f
        previousPosition = viewHolder.bindingAdapterPosition
        getDefaultUIUtil().clearView(getView(viewHolder))
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        viewHolder?.let {
            currentPosition = viewHolder.bindingAdapterPosition
            getDefaultUIUtil().onSelected(getView(it))
        }
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        if (actionState == ACTION_STATE_SWIPE) {
            val view = getView(viewHolder)
            val isClamped = getTag(viewHolder)
            val x = ClampViewPositionHorizontal(view, dX, isClamped, isCurrentlyActive)

            currentDx = x
            getDefaultUIUtil().onDraw(c, recyclerView, view, x, dY, actionState, isCurrentlyActive)
        }
    }

    private fun ClampViewPositionHorizontal(
        view: View,
        dX: Float,
        isClamped: Boolean,
        isCurrentlyActive: Boolean
    ) : Float {
        val min: Float = -250f
        val max = 0f

        val x = if(isClamped) {
            if(isCurrentlyActive) dX - clamp else -clamp
        } else dX

        return min(max(min, x), max)
    }

    private fun getView(viewHolder: RecyclerView.ViewHolder): View {
        return (viewHolder as CommentRecyclerViewAdapter.ViewHolder).comment_swipe_view
    }

    override fun getSwipeEscapeVelocity(defaultValue: Float): Float {
        return defaultValue * 10
    }

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        val isClamped = getTag(viewHolder)
        setTag(viewHolder, !isClamped && currentDx <= -clamp)
        return 2f
    }

    private fun setTag(viewHolder: RecyclerView.ViewHolder, isClamped: Boolean) {
        viewHolder.itemView.tag = isClamped
    }

    private fun getTag(viewHolder: RecyclerView.ViewHolder) : Boolean {
        return viewHolder.itemView.tag as? Boolean ?: false
    }

    fun setClamp(clamp: Float) {
        this.clamp = clamp
    }

    fun removePreviousClamp(recyclerView: RecyclerView) {
        if (currentPosition == previousPosition)
            return
        previousPosition?.let {
            val viewHolder = recyclerView.findViewHolderForAdapterPosition(it) ?: return
            getView(viewHolder).translationX = 0f
            setTag(viewHolder, false)
            previousPosition = null
        }
    }

}