package fftpack.aig.uol.ca.fftspectrumanalyzer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.SystemClock;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;

import FFTLibrary.RealDoubleFFT;

import static android.graphics.Paint.Style.FILL;
import static android.graphics.Paint.Style.FILL_AND_STROKE;

public class SoundRecordAndAnalysisActivity extends AppCompatActivity {

    private GraphView graphView; // frequency spectrum
    private TextView bicep_txtview; // bicep text view
    private EditText bicep_frq_et; // bicep frequency edit text
    private TextView triceps_txtview; // triceps text view
    private EditText triceps_frq_et; // triceps frequency edit text
    private TextView forearm_txtview; // forearm text view
    private EditText forearm_frq_et; // forearm frequency edit text
    private ImageView bitmap , bicep_iv , tricep_iv , forearm_iv; // bitmap image
    private Display display; // display of device
    private Point size;
    private Button update_frq_button;
    private Button replay_recording_button;
    private Button start_recording_button;
//    private FrequencyGraph frequencyGraph;
    private ImageView spectrum;
    private Paint paintScale , paintAxis , paintSpectrumDisplay , paintBicep , paintTricep , paintForearm;
    private Bitmap bitmapScale , bitmap_bicep , bitmap_tricep , bitmap_forearm;
    private Canvas canvasScale , canvas_bicep , canvas_tricep , canvas_forearm;
    private int width, height, width_bitmap , height_bitmap;
//    private DisplayMetrics displayM;
    private float xmax , xmin , ymax , ymin;

    private LineGraphSeries<DataPoint> min_series , max_series , sound_series; // data for the graph
    private BarGraphSeries<DataPoint> bicep_series , triceps_series , forearm_series;
    private double x_frq, y_frq , x_bicep , y_bicep , x_min , y_min , x_max , y_max; // x_frq and y_frq coordinates
    private int BICEP_FRQ = 1000 , TRICEPS_FRQ = 2000 , FOREARM_FRQ = 4000 , MAX_MAGNITUDE = 90 , MIN_MAGNITUDE = 10 , margin = 200;

    RecordAudio recordTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) { // when app in launched
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound_record_and_analysis); // call the UI
        setUpVariables();
        setUpMuscles();
        requestRecordAudioPermission();
    }

    private void setUpVariables() { // initializing all views with their respective ids
        bicep_txtview = (TextView) findViewById(R.id.bicep_tv);
        bicep_frq_et = (EditText) findViewById(R.id.bicep_frq_et);
        triceps_txtview = (TextView) findViewById(R.id.triceps_tv);
        triceps_frq_et = (EditText) findViewById(R.id.triceps_frq_et);
        forearm_txtview = (TextView) findViewById(R.id.forearm_tv);
        forearm_frq_et = (EditText) findViewById(R.id.forearm_frq_et);
        update_frq_button = (Button) findViewById(R.id.update_frq_btn);
        replay_recording_button = (Button) findViewById(R.id.replay_recording_btn);
        start_recording_button = (Button) findViewById(R.id.start_recording_btn);
        start_recording_button.setTag(1);
        bicep_frq_et.setHint(String.valueOf(BICEP_FRQ));
        triceps_frq_et.setHint(String.valueOf(TRICEPS_FRQ));
        forearm_frq_et.setHint(String.valueOf(FOREARM_FRQ));
        bicep_iv = (ImageView) findViewById(R.id.bicep_iv);
        tricep_iv = (ImageView) findViewById(R.id.tricep_iv);
        forearm_iv = (ImageView) findViewById(R.id.forearm_iv);
//        spectrum = (ImageView) findViewById(R.id.spectrum_imageview);
        display = getWindowManager().getDefaultDisplay();
        width = display.getWidth();
        height = display.getHeight() / 4;
    }

    private void setUpMuscles() {
        bitmap_bicep = Bitmap.createBitmap(4 , 130 , Bitmap.Config.ARGB_8888);
        bitmap_tricep = Bitmap.createBitmap(4 , 130 , Bitmap.Config.ARGB_8888);
        bitmap_forearm = Bitmap.createBitmap(5 , 125 , Bitmap.Config.ARGB_8888);
        canvas_bicep = new Canvas(bitmap_bicep);
        canvas_bicep.drawColor(Color.GREEN);
        canvas_tricep = new Canvas(bitmap_tricep);
        canvas_tricep.drawColor(Color.GREEN);
        canvas_forearm = new Canvas(bitmap_forearm);
        canvas_forearm.drawColor(Color.GREEN);
        bicep_iv.setImageBitmap(bitmap_bicep);
        tricep_iv.setImageBitmap(bitmap_tricep);
        forearm_iv.setImageBitmap(bitmap_forearm);
        bicep_iv.invalidate();
        tricep_iv.invalidate();
        forearm_iv.invalidate();
    }

    // update frequency button
    public void updateFrequency(View view) {
        if(!TextUtils.isEmpty(bicep_frq_et.getText().toString())) {
            this.BICEP_FRQ = Integer.parseInt(bicep_frq_et.getText().toString());
            bicep_frq_et.setHint(String.valueOf(BICEP_FRQ));
            bicep_frq_et.setText("");
            DataPoint[] dataPoint = new DataPoint[] {
                    new DataPoint(BICEP_FRQ , 1000)
            };
            bicep_series.resetData(dataPoint);
            graphView.addSeries(bicep_series);
        } else {}
        if(!TextUtils.isEmpty(triceps_frq_et.getText().toString())) {
            this.TRICEPS_FRQ = Integer.parseInt(triceps_frq_et.getText().toString());
            triceps_frq_et.setHint(String.valueOf(TRICEPS_FRQ));
            triceps_frq_et.setText("");
            DataPoint[] dataPoint = new DataPoint[] {
                    new DataPoint(TRICEPS_FRQ , 1000)
            };
            triceps_series.resetData(dataPoint);
            graphView.addSeries(triceps_series);
        } else {}
        if(!TextUtils.isEmpty(forearm_frq_et.getText().toString())) {
            this.FOREARM_FRQ = Integer.parseInt(forearm_frq_et.getText().toString());
            forearm_frq_et.setHint(String.valueOf(FOREARM_FRQ));
            forearm_frq_et.setText("");
            DataPoint[] dataPoint = new DataPoint[] {
                    new DataPoint(FOREARM_FRQ , 1000)
            };
            forearm_series.resetData(dataPoint);
            graphView.addSeries(forearm_series);
        } else {}
    }

    // start recording button
    public void startRecording(View view) {
        int status = (Integer) start_recording_button.getTag();
        if(status == 1) {
            start_recording_button.setText("Stop Recording"); // change text when clicked
            start_recording_button.setTag(0);
        }
        else {
            start_recording_button.setText("Start Recording"); // change text when clicked
            start_recording_button.setTag(1);
        }
    }

    // replay recording button
    public void replayRecording(View view) { // TODO: to be implemented
        Snackbar.make(view, "Feature still not implemented", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    private void requestRecordAudioPermission() {
        //check API version, do nothing if API version < 23!
        int currentApiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentApiVersion > android.os.Build.VERSION_CODES.LOLLIPOP){

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {

                    // Show an expanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                } else {

                    // No explanation needed, we can request the permission.

                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Log.d("Activity", "Granted!");

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Log.d("Activity", "Denied!");
                    finish();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private class RecordAudio extends AsyncTask<Void , double[] , Boolean> {
        private ImageView spectrum;
        private Paint paintScale , paintAxis, paintSpectrumDisplay , seriesPaint;
        private Bitmap bitmapScale;
        private Canvas canvasScale;
        private int width, height, width_bitmap , height_bitmap;
        private RealDoubleFFT transformer;
        private AudioRecord audioRecord;
        private DisplayMetrics displayM;
        private float xmax , xmin , ymax , ymin;
        private final double[] CANCELLED = {100};
        int blockSize = /*2048;// = */256;
        boolean started = false;
        boolean CANCELLED_FLAG = false;
        double[][] cancelledResult = {{100}};
        int mPeakPos;
        double mHighestFreq;
        private double[] real , imaginary , magnitude , frequency;
        int sampleRate = 44100;
        int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
        int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
        DataPoint[] dataPoints;
        private Runnable mTimer;
        private boolean bicepActive , tricepActive , forearmActive;
        private int THRESHOLD = 10;


        public final static String IO_FILENAME= "KISDataREC";
        public FileOutputStream fOut;
        public FileInputStream fIn;
        public File file;
        public InputStreamReader myInReader;
        public OutputStreamWriter myOutWriter;
        public boolean isRecording = false , wasRecording = false , wasRepaying = false , isReplaying = false;


        public RecordAudio(/*ImageView spectrum,*/ Paint paintScale, Paint paintAxis, Paint paintSpectrumDisplay , Bitmap bitmapScale, Canvas canvasScale, int width_bitmap, int height_bitmap, float xmax, float xmin, float ymax, float ymin, int width , int height) {
//            this.spectrum = spectrum;
            this.paintScale = paintScale;
            this.paintAxis = paintAxis;
            this.paintSpectrumDisplay = paintSpectrumDisplay;
            this.bitmapScale = bitmapScale;
            this.canvasScale = canvasScale;
            this.width_bitmap = width_bitmap;
            this.height_bitmap = height_bitmap;
            this.xmax = xmax;
            this.xmin = xmin;
            this.ymax = ymax;
            this.ymin = ymin;
            this.width = width;
            this.height = height;

            bicepActive = false;
            tricepActive = false;
            forearmActive = false;

        }

        private DataPoint[] generateData(double[][] progress) {
            int count = 128;
            DataPoint[] values = new DataPoint[count];
            for(int i = 0 ; i < count ; i++) {
                double x = (21000 / 128) * i;
                double y = progress[0][i];
                DataPoint v = new DataPoint(x , y);
                values[i] = v;
                canvas_bicep.drawColor(Color.GREEN);
                canvas_tricep.drawColor(Color.GREEN);
                canvas_forearm.drawColor(Color.GREEN);
                if (magnitude[i] > THRESHOLD) {
                    if(frequency[i] > (BICEP_FRQ - margin) && frequency[i] < (BICEP_FRQ + margin)) {
                        bicepActive = true;
                        canvas_bicep.drawColor(Color.RED);
                    }
                    else {
                        bicepActive = false;
                        canvas_bicep.drawColor(Color.GREEN);
                    }
                    if(frequency[i] > (TRICEPS_FRQ - margin) && frequency[i] < (TRICEPS_FRQ + margin)) {
                        tricepActive = true;
                        canvas_tricep.drawColor(Color.RED);
                    }
                    else {
                        tricepActive = false;
                        canvas_tricep.drawColor(Color.GREEN);
                    }
                    if(frequency[i] > (FOREARM_FRQ - margin) && frequency[i] < (FOREARM_FRQ + margin)) {
                        forearmActive = true;
                        canvas_forearm.drawColor(Color.RED);
                    }
                    else {
                        forearmActive = false;
                        canvas_forearm.drawColor(Color.GREEN);
                    }
                }
            }
            return values;
        }

        private void setUpGraph() {
            graphView = (GraphView) findViewById(R.id.frequency_spectrum);
            min_series = new LineGraphSeries<DataPoint>();
            max_series = new LineGraphSeries<DataPoint>();
            bicep_series = new BarGraphSeries<DataPoint>();
            triceps_series = new BarGraphSeries<DataPoint>();
            forearm_series = new BarGraphSeries<DataPoint>();
            sound_series = new LineGraphSeries<DataPoint>();
            display = getWindowManager().getDefaultDisplay(); // get device screen info
            int height = display.getHeight(); // get height of screen
            int graphViewHeight = height / 4; // graph is 1/4 of the screen's height
            graphView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT , graphViewHeight)); // set graph's width and height
            graphView.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() { // set labels for x_frq and y_frq axis
                @Override
                public String formatLabel(double value , boolean isValueX) {
                    if (isValueX) {
                        // show normal x_frq values
                        return super.formatLabel(value, isValueX) + "kHz";
                    } else {
                        // show currency for y_frq values
                        return super.formatLabel(value, isValueX) + "dB";
                    }
                }
            });
            graphView.getViewport().setXAxisBoundsManual(true); // set range of x-axis
            graphView.getViewport().setMinX(0.0);
            graphView.getViewport().setMaxX(21000);
            graphView.getViewport().setYAxisBoundsManual(true); // set range of y-axis
            graphView.getViewport().setMinY(0.0);
            graphView.getViewport().setMaxY(100);
            graphView.getViewport().setScalable(true); // make zooming and scrolling active x-axis
            graphView.getViewport().setScalableY(true); // make zooming and scrolling active y-axis

//            // setting up min line
//            x_min = 0;
//            Paint min_paint = new Paint();
//            setDashPaint(min_series , min_paint , Color.GREEN);
//            for(int j = 0 ; j < 21000 ; j++) {
//                x_min = j;
//                y_min = MIN_MAGNITUDE;
//                min_series.appendData(new DataPoint(x_min , y_min) , true , 21000);
//            }
//            graphView.addSeries(min_series);
//
//            // setting up max line
//            x_max = 0;
//            Paint max_paint = new Paint();
//            setDashPaint(max_series , max_paint , Color.RED);
//            for(int j = 0 ; j < 21000 ; j++) {
//                x_max = j;
//                y_max = MAX_MAGNITUDE;
//                max_series.appendData(new DataPoint(x_max , y_max) , true , 21000);
//            }
//            graphView.addSeries(max_series);

            // set target frq for bicep
            setTargetFrequencyLines(bicep_series , Color.CYAN , BICEP_FRQ);

            // set target frq for triceps
            setTargetFrequencyLines(triceps_series , Color.YELLOW , TRICEPS_FRQ);

            // set target frq for forearm
            setTargetFrequencyLines(forearm_series , Color.MAGENTA , FOREARM_FRQ);

            seriesPaint = new Paint();
            seriesPaint.setColor(Color.BLUE);
            seriesPaint.setStyle(FILL_AND_STROKE);
            seriesPaint.setStrokeWidth(6);
        }

        // set max and min magnitude dashed lines
        public void setDashPaint(LineGraphSeries series , Paint paint , int color) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(3);
            paint.setColor(color);
            DashPathEffect dashPathEffect = new DashPathEffect(new float[]{8, 5}, 0);
            paint.setPathEffect(dashPathEffect);
            paint.setAntiAlias(true);
            series.setCustomPaint(paint);
        }

        // set target frequencies
        public void setTargetFrequencyLines(BarGraphSeries series , int color , int frq) {
            series.appendData(new DataPoint(frq , 1000) , true , 1);
            series.setDataWidth(200f);
            series.setColor(color);
            graphView.addSeries(series);
        }

//        public void setUpSpectrum() {
//            spectrum.setLayoutParams(new LinearLayout.LayoutParams(width , height));
//            bitmapScale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//            width_bitmap = bitmapScale.getWidth();
//            height_bitmap = bitmapScale.getHeight();
//            xmax = width_bitmap;
//            ymax = height_bitmap;
//            xmin = 0;
//            ymin = 0;
//            paintScale = new Paint();
//            paintScale.setColor(Color.GRAY);
//            paintScale.setStyle(FILL);
//            paintAxis = new Paint();
//            paintAxis.setColor(Color.BLACK);
//            paintAxis.setStyle(FILL);
//            paintAxis.setStrokeWidth(5);
//            paintSpectrumDisplay = new Paint();
//            paintSpectrumDisplay.setColor(Color.GREEN); //color of the spectrum
//            paintSpectrumDisplay.setStyle(FILL_AND_STROKE);
//            paintSpectrumDisplay.setStrokeWidth(12f);
//            paintBicep = new Paint();
//            paintBicep.setColor(Color.CYAN);
//            paintBicep.setStyle(FILL_AND_STROKE);
//            paintBicep.setStrokeWidth(3);
//            paintTricep = new Paint();
//            paintTricep.setColor(Color.YELLOW);
//            paintTricep.setStyle(FILL_AND_STROKE);
//            paintTricep.setStrokeWidth(3);
//            paintForearm = new Paint();
//            paintForearm.setColor(Color.MAGENTA);
//            paintForearm.setStyle(FILL_AND_STROKE);
//            paintForearm.setStrokeWidth(3);
//            canvasScale = new Canvas(bitmapScale);
//            canvasScale.drawColor(Color.WHITE);
//            spectrum.setImageBitmap(bitmapScale);
//            spectrum.invalidate();
//
//            if (canvasScale == null) {
//                return;
//            }
//
//            int linesBigWidth = (width - 80) / 21;
//            int linesSmallWidth = (width - 80) / 21;
//            int linesBigHeight = (height - 40) / 21;
//            int linesSmallHeight = (height - 40) / 21;
//            canvasScale.drawLine(80, height_bitmap - 40, width, height_bitmap - 40, paintAxis);
//            canvasScale.drawLine(80 , height_bitmap - 40 , 80 , 0 ,paintAxis);
//            for (int i = 80, j = 0; i < width; i = i + linesBigWidth, j++) {
//                for (int k = i; k < (i + linesBigWidth); k = k + linesSmallWidth) {
//                    canvasScale.drawLine(k, height_bitmap - 40, k, height_bitmap - 35, paintScale);
//                }
//                canvasScale.drawLine(i, height_bitmap - 40, i, height_bitmap - 25, paintScale);
//                String text = Integer.toString(j) + " ";
//                if (j % 5 == 0) {
//                    canvasScale.drawLine(i, height_bitmap - 40, i, 0, paintScale);
//                    float textSize = paintScale.getTextSize();
//                    paintScale.setTextSize(textSize * 2.1f);
//                    float x = i;
//                    float w = 31.5f;
//                    if (i != 0) x -= 12.5f;
//                    if (i < 10 * linesBigWidth) w = 17.5f;
//                    canvasScale.drawText(text, x, height_bitmap - 5, paintScale);
//                    paintScale.setTextSize(textSize * 1.3f);
//                    canvasScale.drawText("kHz", x + w, height_bitmap - 5, paintScale);
//                    paintScale.setTextSize(textSize);
//                }
//            }
//            for (int i = 40, j = 0; i < height; i = i + linesBigHeight, j++) {
//                for (int k = i; k < (i + linesBigHeight); k = k + linesSmallHeight) {
//                    canvasScale.drawLine(80, height_bitmap - k, 65, height_bitmap - k, paintScale);
//                }
////            canvasScale.drawLine(80, i, 75 , i, paintScale);
//                String text = Integer.toString(j * 40) + " ";
//                if (j % 5 == 0) {
//                    canvasScale.drawLine(80 , height_bitmap - i , width , height_bitmap - i , paintScale);
//                    float textSize = paintScale.getTextSize();
//                    paintScale.setTextSize(textSize * 2.1f);
//                    float y = height_bitmap - i;
//                    float w = 41.5f;
//                    if (i != 0) y += 12.5f;
//                    if (i < 10 * linesBigHeight) w = 37.5f;
//                    canvasScale.drawText(text, 5, y, paintScale);
//                    paintScale.setTextSize(textSize * 1.3f);
//                    canvasScale.drawText("dB", 5 + w, y, paintScale);
//                    paintScale.setTextSize(textSize);
//                }
//            }
//        }

        private double[] freqMagnitude(double [] toTransform) {
            real = new double[blockSize];
            imaginary = new double[blockSize];
            magnitude = new double[blockSize / 2];
            frequency = new double[blockSize / 2];

            for (int i = 0 ; i < blockSize / 2 ; i++) {
                real[i] = toTransform[i * 2];
                imaginary[i] = toTransform[(i * 2) + 1];
            }

            for (int i = 0 ; i < blockSize / 2 ; i++) {
                magnitude[i] = 0.7 * (Math.sqrt((real[i] * real[i]) + (imaginary[i] * imaginary[i]))); // magnitude is calculated by the square root of (imaginary^2 + real^2)
                frequency[i] = i * (sampleRate) / (blockSize); // calculated the frequency
            }

            return magnitude;
        }

        public File init_writeFile(){
            final File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS + "/KIS/"); // Get the directory for the user's public pictures directory.
            if(!path.exists()) { // Make sure the path directory exists.
                path.mkdirs(); // Make it, if it doesn't exit
            }
            File file = new File(path, RecordAudio.IO_FILENAME);
            return file;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            transformer = new RealDoubleFFT(blockSize);
            dataPoints = new DataPoint[]{new DataPoint(1 , 1)};
//            setUpMuscles();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            Log.d("Recording doBackground", voids.toString());
            file = init_writeFile(); // initiation of file writing
            try {
                if(!file.exists()) // check if file doesn't exist
                    file.createNewFile(); // create a new file

            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fOut = new FileOutputStream(file); // output file
                myOutWriter = new OutputStreamWriter(fOut); // output writer?

                fIn = new FileInputStream(file); // input file
                myInReader = new InputStreamReader(fIn); // input reader?
            } catch (IOException e) {
                e.printStackTrace();
            }
            int  bufferSize = AudioRecord.getMinBufferSize(sampleRate , channelConfiguration , audioEncoding);
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.DEFAULT , sampleRate , channelConfiguration , audioEncoding , bufferSize);
            int state = audioRecord.getState();
            int bufferReadResult = 0;
            int counter = 0;
            long total = 0;
            boolean run = true;
            short[] buffer = new short[blockSize];
            byte[] buff = new byte[2 * blockSize];
            double[] toTransform = new double[blockSize];
            try {
                audioRecord.startRecording();
                started = true;
            } catch (IllegalStateException e) {
                Log.e("Recording failed" , e.toString());
            }
            while (started) {
                if(isCancelled() || (CANCELLED_FLAG == true)) {
                    started = false;
//                    publishProgress(cancelledResult);
                    Log.d("doInBackground" , "Cancelling the RecordTask");
                    break;
                }
                else {
                    if(!isReplaying) {
                        bufferReadResult = audioRecord.read(buffer , 0 , blockSize);
                        if (isRecording) { // if is recording
                            ByteBuffer.wrap(buff).asShortBuffer().put(buffer); // write to buffer?
                            wasRecording = true;
                            try {
                                if (total + bufferReadResult > 4294967295L) { // Write as many bytes as we can before hitting the max size
                                    for (int i = 0; i < bufferReadResult && total <= 4294967295L; i++, total++) {
                                        fOut.write(buff[i]);
                                    }
                                    isRecording = false; // is recording is false because file limit is reached
                                    Log.v("File ", "hit file limit");
                                } else {
                                    fOut.write(buff, 0, bufferReadResult); // Write out the entire read buffer
                                }
                                total += bufferReadResult;

                            } catch (IOException ex) {
                            } finally { //return new Object[]{ex};
                                if (!isRecording && wasRecording && fOut != null)
                                    try {
                                        fOut.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                            }
                        }
                    }
                    else if(isReplaying) {
                    SystemClock.sleep(75);
                    try {
                        fIn = new FileInputStream(file);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    try {
                        if (total <= buff.length) { //if short record
                            fIn.read(buff, 0, (int) total);
                            bufferReadResult = (int) total;
                            isReplaying = false;
                        } else { //if long record
                            if (counter < 10) {
                                fIn.read(buff, counter * buff.length, buff.length);
                                counter++;
                            }else
                            if (counter == blockSize-1) {
                                fIn.read(buff, counter * buff.length, (int) (total - buff.length * counter));
                                counter = 0;
                                isReplaying = false;
                            }
                            bufferReadResult = blockSize;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }finally {
                        if (!isReplaying && wasRepaying && fIn != null)
                            try {
                                fIn.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                    }
                    ByteBuffer.wrap(buff).asShortBuffer().get(buffer);
                }
                    for (int i = 0 ; i < blockSize && i < bufferReadResult ; i++) {
                        toTransform[i] = (double) buffer[i] / 32768.0; // signed 16 bit
                    }

                    transformer.ft(toTransform);
                    publishProgress(freqMagnitude(toTransform));
                }
            }
            return true;
        }

        @Override
        protected void onProgressUpdate(double[]...progress) {
            Log.e("RecordingProgress", "Displaying in progress");
//            canvasScale.drawColor(Color.WHITE);
//            mTimer = new Runnable() {
//                @Override
//                public void run() {
//                    graphView.removeAllSeries();
//                    sound_series.resetData(generateData(progress));
//                }
//            };
            graphView.removeSeries(sound_series);
            sound_series.resetData(generateData(progress));
//            frq_series.resetData(dataPoints);
//            setUpSpectrum();
            double mMaxFFTSample = 150.0;
            Log.d("Test:", Integer.toString(progress[0].length));

//            int lower = 1;
//            int upper = 1;
//            double freqGap = (((double) sampleRate) / ((double) blockSize));
//            int line_position_Bicep = 12 * (Math.round((float)((double) BICEP_FRQ / freqGap))) + 80; // line indicating bicep frequency
//            int line_position_Triceps = 12 * (Math.round((float)((double)TRICEPS_FRQ / freqGap))) + 80; // line indicating triceps frequency
//            int line_position_Forearm = 12 * (Math.round((float)((double) FOREARM_FRQ / freqGap))) + 80; // line indicating forearm frequency
//
//            for (int i = 0 ; i < progress[0].length ; i++) {
//                    int fs = 12 * i + 80;
//                    lower = (int) ((height - 40) - progress[0][i] * 10);
//                    upper = height - 40;
//                    canvasScale.drawLine(fs, lower, fs, upper , paintSpectrumDisplay);
//            }

            sound_series = new LineGraphSeries<>(generateData(progress));
//            sound_series.setColor(Color.BLUE);
            sound_series.setCustomPaint(seriesPaint);
            sound_series.setBackgroundColor(Color.rgb(0 , 157 , 255));
            sound_series.setDrawBackground(true);
            graphView.addSeries(sound_series);
//            canvasScale.drawLine(line_position_Bicep , height_bitmap - 40 , line_position_Bicep , 0 , paintBicep);
//            canvasScale.drawLine(line_position_Triceps , height_bitmap - 40 , line_position_Triceps , 0 , paintTricep);
//            canvasScale.drawLine(line_position_Forearm , height_bitmap - 40 , line_position_Forearm , 0 , paintForearm);


//            spectrum.invalidate();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            try {
                audioRecord.stop();
            } catch (IllegalStateException e) {
                Log.e("Stop failed", e.toString());
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        recordTask = new RecordAudio(/*spectrum ,*/ paintScale , paintAxis , paintSpectrumDisplay , bitmapScale , canvasScale , width_bitmap , height_bitmap , xmax , xmin , ymax , ymin , width , height);
        recordTask.setUpGraph();
//        recordTask.setUpSpectrum();
        recordTask.execute();
    }
}