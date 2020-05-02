package com.cazimir.relaxoo

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


abstract class ScrollListenerRecycleView(private val mLinearLayoutManager: LinearLayoutManager) : RecyclerView.OnScrollListener() {
    private var scrollDist = 0
    private var isVisible = true
    private var previousTotal = 0 // The total number of items in the dataset after the last load
    private var loading = true // True if we are still waiting for the last set of data to load.


    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        if (isVisible && scrollDist > MINIMUM) {
            hide()
            scrollDist = 0
            isVisible = false
        } else if (!isVisible && scrollDist < -MINIMUM) {
            show()
            scrollDist = 0
            isVisible = true
        }
        if (isVisible && dy > 0 || !isVisible && dy < 0) {
            scrollDist += dy
        }
        val visibleItemCount: Int = recyclerView.getChildCount()
        val totalItemCount: Int = mLinearLayoutManager.getItemCount()
        val firstVisibleItem: Int = mLinearLayoutManager.findFirstVisibleItemPosition()
        if (loading) {
            if (totalItemCount > previousTotal) {
                loading = false
                previousTotal = totalItemCount
            }
        }
        val visibleThreshold = 5
        if (!loading && totalItemCount - visibleItemCount
                <= firstVisibleItem + visibleThreshold) { // End has been reached
            loading = true
        }
        onScrollFinished()
    }

    abstract fun onScrollFinished()
    abstract fun show()
    abstract fun hide()

    companion object {
        var TAG = ScrollListenerRecycleView::class.java.simpleName
        private const val MINIMUM = 25f
    }
}