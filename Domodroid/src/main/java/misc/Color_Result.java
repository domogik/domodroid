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
    public String colorCMYK = null;
    public String colorCII = null;

    public Color_Result(Context context) {
        super(context);
        mPaint = new Paint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        try {
            if (color == null && colorrgb == null && colorCMYK == null && colorCII == null) {
                mPaint.setColor(Color.HSVToColor(hsvCurrent));
            } else if (colorrgb != null) {
                String[] parsecolor = colorrgb.split(",");
                int red = Integer.parseInt(parsecolor[0]);
                int green = Integer.parseInt(parsecolor[1]);
                int blue = Integer.parseInt(parsecolor[2]);
                mPaint.setColor(Color.rgb(red, green, blue));
            } else if (colorCMYK != null) {
                String[] parsecolor = colorCMYK.split(",");
                int C = Integer.parseInt(parsecolor[0]);
                int M = Integer.parseInt(parsecolor[1]);
                int Y = Integer.parseInt(parsecolor[2]);
                int K = Integer.parseInt(parsecolor[2]);
                int red = ((255 - C) * (255 - K)) / 255;
                int green = ((255 - M) * (255 - K)) / 255;
                int blue = ((255 - Y) * (255 - K)) / 255;
                mPaint.setColor(Color.rgb(red, green, blue));
            } else if (colorCII != null) {
                Log.e("Color log", colorCII);
                switch (colorCII.toLowerCase()) {
                    case "b":
                        colorCII = "blue";
                        break;
                    case "br":
                        colorCII = "maroon";
                        break;
                    case "g":
                        colorCII = "green";
                        break;
                    case "o":
                        colorCII = "#FF6600";
                        break;
                    case "bk":
                        colorCII = "black";
                        break;
                    case "r":
                        colorCII = "red";
                        break;
                    case "w":
                        colorCII = "white";
                        break;
                    case "y":
                        colorCII = "yellow";
                        break;
                }
                mPaint.setColor(Color.parseColor(colorCII));
            } else if (color != null) {
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