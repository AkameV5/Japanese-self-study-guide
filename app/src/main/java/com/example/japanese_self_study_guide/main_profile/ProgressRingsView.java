package com.example.japanese_self_study_guide.main_profile;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.MotionEvent;

import androidx.annotation.Nullable;



public class ProgressRingsView extends View {

    private float[] progress = new float[6]; // % [0..100]

    private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float ringStroke;
    private float startRadius;

    private final int[] colors = {
            0xFFFF80AB, // hiragana
            0xFFFFB74D, // katakana
            0xFF4FC3F7, // kanji
            0xFF81C784, // grammar
            0xFFBA68C8, // texts
            0xFFFF8A65  // audio
    };

    private final int[] bgColors = {
            0x30FF80AB, // hiragana
            0x30FFB74D, // katakana
            0x304FC3F7, // kanji
            0x3081C784, // grammar
            0x30BA68C8, // texts
            0x30FF8A65  // audio
    };


    public ProgressRingsView(Context c, @Nullable AttributeSet attrs) {
        super(c, attrs);

        bgPaint.setStyle(Paint.Style.STROKE);
        bgPaint.setStrokeCap(Paint.Cap.ROUND);
        bgPaint.setColor(0x30FFFFFF);

        fgPaint.setStyle(Paint.Style.STROKE);
        fgPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    public void setProgress(float[] values) {
        if (values.length == 6) {
            progress = values;
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float size = Math.min(getWidth(), getHeight());
        float center = size / 2f;

        ringStroke = size / 26f;
        float ringStep = ringStroke * 1.55f;
        float minRadius = ringStroke * 2.2f;

        startRadius = center - ringStroke;
        float radius = startRadius;

        bgPaint.setStrokeWidth(ringStroke);
        fgPaint.setStrokeWidth(ringStroke);

        for (int i = 0; i < 6; i++) {

            if (radius < minRadius) break;

            RectF rect = new RectF(
                    center - radius,
                    center - radius,
                    center + radius,
                    center + radius
            );

            bgPaint.setColor(bgColors[i]);
            canvas.drawArc(rect, -90, 360, false, bgPaint);

            fgPaint.setColor(colors[i]);
            canvas.drawArc(
                    rect,
                    -90,
                    360 * (progress[i] / 100f),
                    false,
                    fgPaint
            );
            radius -= ringStep;
        }
    }


    public interface OnRingClickListener {
        void onRingClick(int index);
    }

    private OnRingClickListener listener;

    public void setOnRingClickListener(OnRingClickListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (event.getAction() != MotionEvent.ACTION_DOWN)
            return true;

        float size = Math.min(getWidth(), getHeight());
        float center = size / 2f;

        float dx = event.getX() - center;
        float dy = event.getY() - center;

        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        float ringStep = ringStroke * 1.55f;
        float minRadius = ringStroke * 2.2f;

        float radius = startRadius;

        for (int i = 0; i < 6; i++) {

            if (radius < minRadius) break;

            float outer = radius + ringStroke / 2f;
            float inner = radius - ringStroke / 2f;

            if (distance <= outer && distance >= inner) {
                if (listener != null) listener.onRingClick(i);
                return true;
            }
            radius -= ringStep;
        }
        return true;
    }
}
