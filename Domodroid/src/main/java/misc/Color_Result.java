package misc;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;

public class Color_Result extends View {
    public float[] hsvCurrent = {1, 1, 1};
    private final Paint mPaint;
    public String color = null;
    public String colorrgb = null;

    public Color_Result(Context context) {
        super(context);
        mPaint = new Paint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        try {
            if (color == null && colorrgb == null) {
                mPaint.setColor(Color.HSVToColor(hsvCurrent));
            } else if (color == null && colorrgb != null) {
                String[] parsecolor = colorrgb.split(",");
                int red = Integer.parseInt(parsecolor[0]);
                int green = Integer.parseInt(parsecolor[1]);
                int blue = Integer.parseInt(parsecolor[2]);
                mPaint.setColor(Color.rgb(red, green, blue));
            } else if (color != null && colorrgb == null) {
                mPaint.setColor(Color.parseColor(color));
            }
            canvas.drawRect(0, 10, 100, 100, mPaint);
        } catch (Exception e) {
            Log.e("", e.toString());
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(100, 120);
    }

    public void refresh() {
        invalidate();
    }
}