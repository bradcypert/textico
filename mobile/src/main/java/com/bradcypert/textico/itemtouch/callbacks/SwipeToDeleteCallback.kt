package com.bradcypert.textico.itemtouch.callbacks

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import com.bradcypert.textico.adapters.SearchAndRemove

open class SwipeToDeleteCallback(private val recyclerView: RecyclerView) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

    private val background = ColorDrawable()
    private val backgroundColor = Color.parseColor("#f44336")

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        // Row is swiped from recycler view
        // remove it from adapter
        (this.recyclerView.adapter as SearchAndRemove).removeItem(viewHolder.adapterPosition)
    }

    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        // view the background view
        val itemView = viewHolder.itemView

        // Draw the red delete background
        this.background.setColorFilter(this.backgroundColor, PorterDuff.Mode.SRC)
        background.setBounds(
                itemView.right + dX.toInt(),
                itemView.top,
                itemView.right,
                itemView.bottom
        )
        background.draw(c)

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }
}