package com.webengage.notification_inbox_plugin.view

import android.util.Log
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.webengage.notification_inbox_plugin.utils.WENotificationInboxConstants


class SwipeToDeleteCallback(deleteCallback:WebEngageRecyclerViewAdapter.WEInboxItemDelete) : ItemTouchHelper.Callback() {
    private val mDeleteCallback = deleteCallback

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val swipeFlags = ItemTouchHelper.START
        return makeMovementFlags(0, swipeFlags)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        Log.d(WENotificationInboxConstants.TAG, "Swiped position - $position")
        mDeleteCallback.onItemDelete(null, position)
    }

}
