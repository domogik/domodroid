package misc;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class Color_Result extends View {
    public float[] hsvCurrent = {1, 1, 1};
    private final Paint mPaint;
    public String color;

    public Color_Result(Context context) {
        super(context);
        mPaint = new Paint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (color == null) {
            mPaint.setColor(Color.HSVToColor(hsvCurrent));
        } else {
            mPaint.setColor(Color.parseColor(color));
        }
        canvas.drawRect(0, 10, 100, 100, mPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(100, 120);
    }

    public void refresh() {
        invalidate();
    }
}