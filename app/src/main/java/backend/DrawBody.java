package backend;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.DisplayMetrics;

/**
 * Created by lucien on 2018-03-01.
 */

public class DrawBody extends android.support.v7.widget.AppCompatImageView{

    private Bitmap bitmapBicep , bitmapTriceps , bitmapForearm; // bitmap for each muscle
    private Canvas canvasBicep , canvasTriceps , canvasForearm; // canvas for each muscle
    private Paint paintBicep , paintTriceps, paintForearm; // paint for each muscle
    private int width , height; // width & height of the display
    private DisplayMetrics display; // display of device
    private Context context; // context of app

    public DrawBody(Context context) { // constructor
        super(context);
        this.context = context;
        displayBody(); // call method to draw body
    }

    public void displayBody() { // displaying the body
        display = this.getResources().getDisplayMetrics(); // get the device's screen info
        width = display.widthPixels; // get the width in pixels
        height = display.heightPixels; // get the height in pixels
        bitmapBicep = Bitmap.createBitmap(15 , 50 , Bitmap.Config.ARGB_8888); // create the bicep bitmap
        bitmapTriceps = Bitmap.createBitmap(15 , 50 , Bitmap.Config.ARGB_8888); // create the triceps bitmap
        bitmapForearm = Bitmap.createBitmap(15 , 50 , Bitmap.Config.ARGB_8888); // create the forearm bitmap

        paintBicep = new Paint(); // initialize bicep paint
        paintBicep.setColor(Color.GREEN); // set color green to paint
        paintBicep.setStyle(Paint.Style.FILL_AND_STROKE); // set paint style
        paintBicep.setStrokeWidth(15); // set stroke width

        paintTriceps = new Paint(); // initialize triceps paint
        paintTriceps.setColor(Color.GREEN); // set color green to paint
        paintTriceps.setStyle(Paint.Style.FILL_AND_STROKE); // set paint style
        paintTriceps.setStrokeWidth(15); // set stroke width

        paintForearm = new Paint(); // initialize forearm paint
        paintForearm.setColor(Color.GREEN); // set color green to paint
        paintForearm.setStyle(Paint.Style.FILL_AND_STROKE); // set paint style
        paintForearm.setStrokeWidth(15); // set stroke width

        canvasBicep = new Canvas(bitmapBicep); // initializing the  bicep canvas to the corresponding bitmap
        canvasTriceps = new Canvas(bitmapTriceps); // initializing the triceps canvas to the correspondin bitmap
        canvasForearm = new Canvas(bitmapForearm); // initializing the forearm canvas to the correspondin bitmap

        setImageBitmap(bitmapBicep); // assign bicep bitmap to image
        setImageBitmap(bitmapTriceps); // assign triceps bitmap to image
        setImageBitmap(bitmapForearm); // assign forearm bitmap to image
    }
}
