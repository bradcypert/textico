package com.bradcypert.textico.itemtouch.callbacks;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import com.bradcypert.textico.adapters.SearchAndRemove;

public class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {

    private Drawable background = new ColorDrawable();
    private int backgroundColor = Color.parseColor("#f44336");
    private RecyclerView recyclerView;

    public SwipeToDeleteCallback(RecyclerView recyclerView) {
        super(0, ItemTouchHelper.LEFT);
        this.recyclerView = recyclerView;
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        // Row is swiped from recycler view
        // remove it from adapter
        ((SearchAndRemove) this.recyclerView.getAdapter()).removeItem(viewHolder.getAdapterPosition());
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        // view the background view
        View itemView = viewHolder.itemView;
        int itemHeight = itemView.getBottom() - itemView.getTop();

        // Draw the red delete background
        this.background.setColorFilter(this.backgroundColor, PorterDuff.Mode.SRC);
        background.setBounds(
                itemView.getRight() + (int) dX,
                itemView.getTop(),
                itemView.getRight(),
                itemView.getBottom()
        );
        background.draw(c);

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }
}