package com.comismar.informes.view.ui;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import androidx.appcompat.widget.AppCompatImageView;

public class ZoomableImageView extends AppCompatImageView {

    private static final float MIN_SCALE = 1.0f;
    private static final float MAX_SCALE = 4.0f;

    private final Matrix matrix = new Matrix();
    private float scale = 1.0f;
    private float lastX;
    private float lastY;
    private boolean isDragging;

    private final ScaleGestureDetector scaleDetector;
    private final GestureDetector gestureDetector;

    public ZoomableImageView(Context context) {
        this(context, null);
    }

    public ZoomableImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ZoomableImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setScaleType(ScaleType.MATRIX);

        scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                resetZoom();
                return true;
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        scaleDetector.onTouchEvent(event);

        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {
            lastX = event.getX();
            lastY = event.getY();
            isDragging = true;
        } else if (action == MotionEvent.ACTION_MOVE && !scaleDetector.isInProgress() && isDragging) {
            float dx = event.getX() - lastX;
            float dy = event.getY() - lastY;
            matrix.postTranslate(dx, dy);
            constrainMatrix();
            setImageMatrix(matrix);
            lastX = event.getX();
            lastY = event.getY();
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            isDragging = false;
        }

        return true;
    }

    private void resetZoom() {
        scale = 1.0f;
        matrix.reset();
        constrainMatrix();
        setImageMatrix(matrix);
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactor = detector.getScaleFactor();
            float newScale = scale * scaleFactor;
            if (newScale < MIN_SCALE) {
                scaleFactor = MIN_SCALE / scale;
                scale = MIN_SCALE;
            } else if (newScale > MAX_SCALE) {
                scaleFactor = MAX_SCALE / scale;
                scale = MAX_SCALE;
            } else {
                scale = newScale;
            }
            matrix.postScale(scaleFactor, scaleFactor, detector.getFocusX(), detector.getFocusY());
            constrainMatrix();
            setImageMatrix(matrix);
            return true;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        constrainMatrix();
        setImageMatrix(matrix);
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        post(() -> {
            constrainMatrix();
            setImageMatrix(matrix);
        });
    }

    private void constrainMatrix() {
        Drawable drawable = getDrawable();
        if (drawable == null) return;

        RectF rect = new RectF(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        matrix.mapRect(rect);

        float viewWidth = getWidth();
        float viewHeight = getHeight();
        if (viewWidth == 0 || viewHeight == 0) return;

        float leftBound = getPaddingLeft();
        float topBound = getPaddingTop();
        float rightBound = viewWidth - getPaddingRight();
        float bottomBound = viewHeight - getPaddingBottom();
        float availableWidth = rightBound - leftBound;
        float availableHeight = bottomBound - topBound;

        float dx = 0;
        float dy = 0;

        if (rect.width() <= availableWidth) {
            dx = leftBound + (availableWidth - rect.width()) / 2f - rect.left;
        } else {
            if (rect.left > leftBound) dx = leftBound - rect.left;
            if (rect.right < rightBound) dx = rightBound - rect.right;
        }

        if (rect.height() <= availableHeight) {
            dy = topBound + (availableHeight - rect.height()) / 2f - rect.top;
        } else {
            if (rect.top > topBound) dy = topBound - rect.top;
            if (rect.bottom < bottomBound) dy = bottomBound - rect.bottom;
        }

        if (dx != 0 || dy != 0) {
            matrix.postTranslate(dx, dy);
        }
    }
}
