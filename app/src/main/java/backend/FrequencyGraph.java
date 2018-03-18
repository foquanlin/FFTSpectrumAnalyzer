package backend;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by lucien on 2018-03-17.
 */

@SuppressLint("AppCompatCustomView")
public class FrequencyGraph extends ImageView {
    private Paint paintScale;
    private Bitmap bitmapScale;
    private Canvas canvasScale;
    private int width, height , width_bitmap , height_bitmap;
    private DisplayMetrics displayM;
    private float xmax , xmin , ymax , ymin;

    public FrequencyGraph(Context context) {
        super(context);
    }

    public FrequencyGraph(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FrequencyGraph(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void init() {
        if (!isInEditMode()) {
            displayM = this.getResources().getDisplayMetrics();
//            width = displayM.widthPixels; // get screen width
//            height = (displayM.heightPixels); // get screen height
            bitmapScale = Bitmap.createBitmap(width, 400, Bitmap.Config.ARGB_8888);
            width_bitmap = bitmapScale.getWidth();
            height_bitmap = bitmapScale.getHeight();
            xmax = width_bitmap;
            ymax = height_bitmap;
            xmin = 0;
            ymin = 0;
            paintScale = new Paint();
            paintScale.setColor(Color.BLACK);
            paintScale.setStyle(Paint.Style.FILL);
            canvasScale = new Canvas(bitmapScale);
//            canvasScale.scale(width_bitmap / (xmax - xmin), -height_bitmap / (ymax - ymin));
//            canvasScale.translate(-xmin, -ymax);
            canvasScale.drawColor(Color.RED);
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

        int linesBigWidth = width / 21;
        int linesSmallWidth = width / 21;
        int linesBigHeight = height / 21;
        int linesSmallHeight = height / 21;
        canvasScale.drawLine(80, height_bitmap - 40, width, height_bitmap - 40, paintScale);
        canvasScale.drawLine(80 , height_bitmap - 40 , 80 , 0 ,paintScale);
        for (int i = 80, j = 0; i < width; i = i + linesBigWidth, j++) {
            for (int k = i; k < (i + linesBigWidth); k = k + linesSmallWidth) {
                canvasScale.drawLine(k, height_bitmap - 40, k, height_bitmap - 35, paintScale);
            }
            canvasScale.drawLine(i, height_bitmap - 40, i, height_bitmap - 25, paintScale);
            String text = Integer.toString(j) + " ";
            if (j % 5 == 0) {
                float textSize = paintScale.getTextSize();
                paintScale.setTextSize(textSize * 2.1f);
                float x = i;
                float w = 31.5f;
                if (i != 0) x -= 12.5f;
                if (i < 10 * linesBigWidth) w = 17.5f;
                canvasScale.drawText(text, x, height_bitmap - 5, paintScale);
                paintScale.setTextSize(textSize * 1.3f);
                canvasScale.drawText("kHz", x + w, height_bitmap - 5, paintScale);
                paintScale.setTextSize(textSize);
            }
        }
        for (int i = 40, j = 0; i < height; i = i + linesBigHeight, j++) {
            for (int k = i; k < (i + linesBigHeight); k = k + linesSmallHeight) {
                canvasScale.drawLine(80, height_bitmap - k, 65, height_bitmap - k, paintScale);
            }
            canvasScale.drawLine(80, i, 75 , i, paintScale);
            String text = Integer.toString(j * 40) + " ";
            if (j % 5 == 0) {
                float textSize = paintScale.getTextSize();
                paintScale.setTextSize(textSize * 2.1f);
                float y = height_bitmap - i;
                float w = 41.5f;
                if (i != 0) y += 12.5f;
                if (i < 10 * linesBigHeight) w = 37.5f;
                canvasScale.drawText(text, 5, y, paintScale);
                paintScale.setTextSize(textSize * 1.3f);
                canvasScale.drawText("dB", 5 + w, y, paintScale);
                paintScale.setTextSize(textSize);
            }
        }
    }

    @Override
    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
        super.onSizeChanged(xNew, yNew, xOld, yOld);
        width = xNew;
        height = yNew;
        init();
        Log.d("DimensionScale", width + " x " + height);
    }
}
