package com.renzobiz.simpletodo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.view.View;

abstract class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback{

    private final Drawable deleteIcon;
    private final int intrinsicWidth;
    private final int intrinsicHeight;
    private final ColorDrawable background;
    private final int backgroundColor;
    private final Paint clearPaint;

    public SwipeToDeleteCallback(Context context){
        super(0,ItemTouchHelper.LEFT);
        deleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_swipe_delete);
        intrinsicWidth = deleteIcon.getIntrinsicWidth();
        intrinsicHeight = deleteIcon.getIntrinsicHeight();
        background = new ColorDrawable();
        backgroundColor = Color.parseColor("#f44336");
        clearPaint = new Paint();
        clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        if(viewHolder.getAdapterPosition() == 10){
            return 0;
        }
        return super.getMovementFlags(recyclerView, viewHolder);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
        return false;
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        final View itemView = viewHolder.itemView;
        final int itemHeight = itemView.getBottom() - itemView.getTop();
        final boolean isCanceled = dX == 0f && !isCurrentlyActive;

        if(isCanceled){
            clearCanvas(c, itemView.getRight() + dX,(float) itemView.getTop(), (float)itemView.getRight(), (float)itemView.getBottom());
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            return;
        }

        //draw the red delete background
        background.setColor(backgroundColor);
        background.setBounds(itemView.getRight() + (int)dX, itemView.getTop(), itemView.getRight(),itemView.getBottom());
        background.draw(c);

        //calculate position of delete icon
        final int deleteIconTop = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
        final int deleteIconMargin = (itemHeight - intrinsicHeight) / 2;
        final int deleteIconLeft = itemView.getRight() - deleteIconMargin - intrinsicWidth;
        final int deleteIconRight = itemView.getRight() - deleteIconMargin;
        final int deleteIconBottom = deleteIconTop + intrinsicHeight;

        //draw the delete icon
        deleteIcon.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom);
        deleteIcon.draw(c);

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    private void clearCanvas(Canvas c, float left, float top, float right, float bottom){
        c.drawRect(left,top,right,bottom,clearPaint);
    }

}
