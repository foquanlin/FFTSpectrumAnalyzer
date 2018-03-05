package fftpack.aig.uol.ca.fftspectrumanalyzer;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class SoundRecordAndAnalysisActivity extends AppCompatActivity {

    private GraphView graphView; // frequency spectrum
    private TextView bicep_txtview; // bicep text view
    private EditText bicep_frq_et; // bicep frequency edit text
    private TextView triceps_txtview; // triceps text view
    private EditText triceps_frq_et; // triceps frequency edit text
    private TextView forearm_txtview; // forearm text view
    private EditText forearm_frq_et; // forearm frequency edit text
    private ImageView bitmap; // bitmap image
    private Display display; // display of device
    private Button update_frq_button;
    private Button replay_recording_button;
    private Button start_recording_button;

    private LineGraphSeries<DataPoint> frq_series , min_series , max_series; // data for the graph
    private BarGraphSeries<DataPoint> bicep_series , triceps_series , forearm_series;
    private double x_frq, y_frq , x_bicep , y_bicep , x_min , y_min , x_max , y_max; // x_frq and y_frq coordinates
    private int BICEP_FRQ = 1000 , TRICEPS_FRQ = 2000 , FOREARM_FRQ = 4000 , MAX_MAGNITUDE = 400 , MIN_MAGNITUDE = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) { // when app in launched
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound_record_and_analysis); // call the UI
        setUpVariables();
        setUpGraph();
    }

    private void setUpVariables() { // initializing all views with their respective ids
        graphView = (GraphView) findViewById(R.id.frequency_spectrum);
        bicep_txtview = (TextView) findViewById(R.id.bicep_tv);
        bicep_frq_et = (EditText) findViewById(R.id.bicep_frq_et);
        triceps_txtview = (TextView) findViewById(R.id.triceps_tv);
        triceps_frq_et = (EditText) findViewById(R.id.triceps_frq_et);
        forearm_txtview = (TextView) findViewById(R.id.forearm_tv);
        forearm_frq_et = (EditText) findViewById(R.id.forearm_frq_et);
        bitmap = (ImageView) findViewById(R.id.bitmap_iv);
        frq_series = new LineGraphSeries<DataPoint>();
        min_series = new LineGraphSeries<DataPoint>();
        max_series = new LineGraphSeries<DataPoint>();
        bicep_series = new BarGraphSeries<DataPoint>();
        triceps_series = new BarGraphSeries<DataPoint>();
        forearm_series = new BarGraphSeries<DataPoint>();
        update_frq_button = (Button) findViewById(R.id.update_frq_btn);
        replay_recording_button = (Button) findViewById(R.id.replay_recording_btn);
        start_recording_button = (Button) findViewById(R.id.start_recording_btn);
        start_recording_button.setTag(1);
    }

    private void setUpGraph() {
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
        graphView.getViewport().setMaxX(21);
        graphView.getViewport().setYAxisBoundsManual(true); // set range of y-axis
        graphView.getViewport().setMinY(0.0);
        graphView.getViewport().setMaxY(500);
        graphView.getViewport().setScalable(true); // make zooming and scrolling active x-axis
        graphView.getViewport().setScalableY(true); // make zooming and scrolling active y-axis
        x_frq = 0; // first x_frq value
        for(int i = 0 ; i < 1000 ; i++) { // display the wanted function through the loop
            x_frq = x_frq + 0.1f;
            y_frq = 2 * Math.pow(x_frq, 2); // TODO: Implement real data
            frq_series.appendData(new DataPoint(x_frq, y_frq) , true , 1000);
        }
        graphView.addSeries(frq_series); // add the data to the graph

        // setting up min line
        x_min = 0;
        Paint min_paint = new Paint();
        setDashPaint(min_series , min_paint , Color.GREEN);
        for(int j = 0 ; j < 1000 ; j++) {
            x_min = x_min + 0.1f;
            y_min = MIN_MAGNITUDE;
            min_series.appendData(new DataPoint(x_min , y_min) , true , 1000);
        }
        graphView.addSeries(min_series);

        // setting up max line
        x_max = 0;
        Paint max_paint = new Paint();
        setDashPaint(max_series , max_paint , Color.RED);
        for(int j = 0 ; j < 1000 ; j++) {
            x_max = x_max + 0.1f;
            y_max = MAX_MAGNITUDE;
            max_series.appendData(new DataPoint(x_max , y_max) , true , 1000);
        }
        graphView.addSeries(max_series);

        // set target frq for bicep
        setTargetFrequencyLines(bicep_series , Color.CYAN , BICEP_FRQ);

        // set target frq for triceps
        setTargetFrequencyLines(triceps_series , Color.YELLOW , TRICEPS_FRQ);

        // set target frq for forearm
        setTargetFrequencyLines(forearm_series , Color.MAGENTA , FOREARM_FRQ);
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
        series.appendData(new DataPoint(frq / 1000 , 1000) , true , 1);
        series.setDataWidth(0.2f);
        series.setColor(color);
        graphView.addSeries(series);
    }

    // update frequency button
    public void updateFrequency(View view) {
        if(!TextUtils.isEmpty(bicep_frq_et.getText().toString())) {
            this.BICEP_FRQ = Integer.parseInt(bicep_frq_et.getText().toString());
            DataPoint[] dataPoint = new DataPoint[] {
                    new DataPoint(BICEP_FRQ / 1000 , 1000)
            };
            bicep_series.resetData(dataPoint);
            graphView.addSeries(bicep_series);
        } else {}
        if(!TextUtils.isEmpty(triceps_frq_et.getText().toString())) {
            this.TRICEPS_FRQ = Integer.parseInt(triceps_frq_et.getText().toString());
            DataPoint[] dataPoint = new DataPoint[] {
                    new DataPoint(TRICEPS_FRQ / 1000 , 1000)
            };
            triceps_series.resetData(dataPoint);
            graphView.addSeries(triceps_series);
        } else {}
        if(!TextUtils.isEmpty(forearm_frq_et.getText().toString())) {
            this.FOREARM_FRQ = Integer.parseInt(forearm_frq_et.getText().toString());
            DataPoint[] dataPoint = new DataPoint[] {
                    new DataPoint(FOREARM_FRQ / 1000 , 1000)
            };
            forearm_series.resetData(dataPoint);
            graphView.addSeries(forearm_series);
        } else {}
    }

    public void generateData(int frq , BarGraphSeries<DataPoint> series) {
        series.resetData(new DataPoint[]{
                new DataPoint(frq / 1000, 1000)
        });
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
    public void replayRecording(View view) {
        Toast.makeText(this , "To be implemented" , Toast.LENGTH_SHORT).show();
    }
}