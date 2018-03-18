package backend;

/**
 * Created by lucien on 2018-03-17.
 */

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import android.util.DisplayMetrics;

@SuppressLint("AppCompatCustomView")
public class HorizontalScaleImageView extends ImageView {
    Paint paintScaleDisplay;
    Bitmap bitmapScale;
    Canvas canvasScale;
    private int viewWidth;
    private int viewHeight;

    public HorizontalScaleImageView(Context context) {
        super(context);
    }

    public HorizontalScaleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HorizontalScaleImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void init() {
        if (!isInEditMode()) {
            bitmapScale = Bitmap.createBitmap(viewWidth, 150, Bitmap.Config.ARGB_8888);

            paintScaleDisplay = new Paint();
            paintScaleDisplay.setColor(Color.WHITE);
            paintScaleDisplay.setStyle(Paint.Style.FILL);
//			paintScaleDisplay.setTextSize(3.0f);

            canvasScale = new Canvas(bitmapScale);
            setImageBitmap(bitmapScale);
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (canvasScale == null) {
            return;
        }

        int linesBig = viewWidth / 21;
        int linesSmall = viewWidth / 21;
        canvasScale.drawLine(0, 25, viewWidth, 25, paintScaleDisplay);
        for (int i = 0, j = 0; i < viewWidth; i = i + linesBig, j++) {
            for (int k = i; k < (i + linesBig); k = k + linesSmall) {
                canvasScale.drawLine(k, 30, k, 25, paintScaleDisplay);
            }
            canvasScale.drawLine(i, 40, i, 25, paintScaleDisplay);
            String text = Integer.toString(j) + " ";
            if (j % 5 == 0) {
                float textSize = paintScaleDisplay.getTextSize();
                paintScaleDisplay.setTextSize(textSize * 2.1f);
                float x = i;
                float w = 31.5f;
                if (i != 0) x -= 12.5f;
                if (i < 10 * linesBig) w = 17.5f;
                canvasScale.drawText(text, x, 65, paintScaleDisplay);
                paintScaleDisplay.setTextSize(textSize * 1.3f);
                canvasScale.drawText("kHz", x + w, 65, paintScaleDisplay);
                paintScaleDisplay.setTextSize(textSize);
            }
        }
//		canvas.drawBitmap(bitmapScale, 0, 0, paintScaleDisplay);





    }

    @Override
    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
        super.onSizeChanged(xNew, yNew, xOld, yOld);
        viewWidth = xNew;
        viewHeight = yNew;
        init();
        Log.d("DimensionScale", viewWidth + " x " + viewHeight);
    }
}

