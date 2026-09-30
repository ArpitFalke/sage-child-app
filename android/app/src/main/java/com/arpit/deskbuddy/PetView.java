package com.arpit.deskbuddy;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

public class PetView extends View {
    public interface Listener {
        void onPetDown(MotionEvent event);
        void onPetMove(MotionEvent event);
        void onPetUp(MotionEvent event);
        void onOutsideTap(float rawX, float rawY);
        void onLongPress();
    }

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final RectF dst = new RectF();
    private Bitmap bitmap;
    private boolean facingLeft;
    private final Listener listener;

    public PetView(android.content.Context context, Listener listener) {
        super(context);
        this.listener = listener;
        setLayerType(View.LAYER_TYPE_HARDWARE, null);
    }

    public void setFrame(Bitmap bitmap, boolean facingLeft) {
        this.bitmap = bitmap;
        this.facingLeft = facingLeft;
        invalidate();
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (bitmap == null) return;
        float left = (getWidth() - bitmap.getWidth()) / 2f;
        float top = (getHeight() - bitmap.getHeight()) / 2f;
        dst.set(left, top, left + bitmap.getWidth(), top + bitmap.getHeight());
        if (facingLeft) {
            canvas.save();
            canvas.scale(-1f, 1f, getWidth() / 2f, getHeight() / 2f);
            canvas.drawBitmap(bitmap, null, dst, paint);
            canvas.restore();
        } else {
            canvas.drawBitmap(bitmap, null, dst, paint);
        }
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                listener.onPetDown(event); return true;
            case MotionEvent.ACTION_MOVE:
                listener.onPetMove(event); return true;
            case MotionEvent.ACTION_UP:
                listener.onPetUp(event); return true;
            case MotionEvent.ACTION_CANCEL:
                listener.onPetUp(event); return true;
            case MotionEvent.ACTION_OUTSIDE:
                listener.onOutsideTap(event.getRawX(), event.getRawY()); return true;
            default:
                return true;
        }
    }
}
