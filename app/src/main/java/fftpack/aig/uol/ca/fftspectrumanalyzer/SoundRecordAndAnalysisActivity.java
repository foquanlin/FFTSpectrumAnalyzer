package fftpack.aig.uol.ca.fftspectrumanalyzer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.NumberFormat;

import javax.sql.DataSource;

public class SoundRecordAndAnalysisActivity extends AppCompatActivity {

    private GraphView graphView;
    private TextView bicep_txtview;
    private EditText bicep_frq_et;
    private TextView triceps_txtview;
    private EditText triceps_frq_et;
    private TextView forearm_txtview;
    private EditText forearm_frq_et;
    private ImageView bitmap;
    private Display display;

    private LineGraphSeries<DataPoint> series;
    private double x , y;
    private NumberFormat nf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound_record_and_analysis);
        setUpVariables();
        setUpGraph();
    }

    private void setUpVariables() {
        graphView = (GraphView) findViewById(R.id.frequency_spectrum);
        bicep_txtview = (TextView) findViewById(R.id.bicep_tv);
        bicep_frq_et = (EditText) findViewById(R.id.bicep_frq_et);
        triceps_txtview = (TextView) findViewById(R.id.triceps_tv);
        triceps_frq_et = (EditText) findViewById(R.id.triceps_frq_et);
        forearm_txtview = (TextView) findViewById(R.id.forearm_tv);
        forearm_frq_et = (EditText) findViewById(R.id.forearm_frq_et);
        bitmap = (ImageView) findViewById(R.id.bitmap_iv);
        series = new LineGraphSeries<DataPoint>();
    }

    private void setUpGraph() {
        display = getWindowManager().getDefaultDisplay();
        int height = display.getHeight();
        int graphViewHeight = height / 4;
        graphView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT , graphViewHeight));
        graphView.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value , boolean isValueX) {
                if (isValueX) {
                    // show normal x values
                    return super.formatLabel(value, isValueX) + "KHz";
                } else {
                    // show currency for y values
                    return super.formatLabel(value, isValueX) + "dB";
                }
            }
        });
        graphView.getViewport().setXAxisBoundsManual(true);
        graphView.getViewport().setMinX(0.0);
        graphView.getViewport().setMaxX(21);
        graphView.getViewport().setYAxisBoundsManual(true);
        graphView.getViewport().setMinY(0.0);
        graphView.getViewport().setMaxY(500);
        x = 0;
        for(int i = 0 ; i < 1000 ; i++) {
            x = x + 0.1f;
            y = 2 * Math.pow(x , 2);
            series.appendData(new DataPoint(x , y) , true , 1000);
        }
        graphView.addSeries(series);
    }
}
