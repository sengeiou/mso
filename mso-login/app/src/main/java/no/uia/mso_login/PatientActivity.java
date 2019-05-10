package no.uia.mso_login;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Random;

public class PatientActivity extends AppCompatActivity {

    private final static String TAG = PatientActivity.class.getSimpleName();
    private String patientName;

    GraphView graph;
    LineGraphSeries<DataPoint> series;
    private int y = 60;
    private int hrValue;
    private final static int xMax = 100;
    private int x = xMax;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient);

        Intent intent = getIntent();
        patientName = intent.getStringExtra("name");

        // TODO: define action as static string
        IntentFilter filter = new IntentFilter("MQTT_ON_RECIEVE");
        this.registerReceiver(new PatientActivity.Receiver(), filter);

        TextView tvName = findViewById(R.id.patient_name);
        tvName.setText(patientName);
        initializeGraph();
    }

    private class Receiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");

            // Make sure message is correctly formatted
            if(!isFormattedCorrectly(message))
                return;

            String temp = message.substring(message.indexOf("[") + 1);
            String name = temp.substring(0, temp.indexOf("]"));
            temp = temp.substring(temp.indexOf("[") + 1);
            String value = temp.substring(0, temp.indexOf("]"));

            if(name.equals(patientName)) {
                if(value.charAt(0)=='H') {
                    Toast.makeText(PatientActivity.this, name +
                            R.string.needs_medical_attention, Toast.LENGTH_LONG).show();
                } else if (isInteger(value)){
                    TextView hr = findViewById(R.id.heart_rate);
                    hr.setText(value);
                    try {
                        hrValue = Integer.parseInt(value);
                        addPointToGraph(hrValue);
                    }
                    catch (NumberFormatException e) {
                        hrValue = 0;
                    }
                }
            }
        }
    }

    public void buttonSimulate_onClick(View view) {
        Random r = new Random();
        int low = -3;
        int high = 3;
        int result = r.nextInt(high-low) + low;
        y-=result;
        addPointToGraph(y);
        TextView hr = findViewById(R.id.heart_rate);
        hr.setText(String.valueOf(y));
    }

    private void addPointToGraph(int value) {
        graph.removeAllSeries();

        x+=1;
        graph.getViewport().setMinX(x-xMax+5);
        graph.getViewport().setMaxX(x+5);
        series.appendData(new DataPoint(x, value), true, xMax, true);
        graph.addSeries(series);
    }

    private Boolean isInteger(String s) {
        return isInteger(s,10);
    }

    private Boolean isInteger(String s, int radix) {
        if(s.isEmpty()) return false;
        for(int i = 0; i < s.length(); i++) {
            if(i == 0 && s.charAt(i) == '-') {
                if(s.length() == 1) return false;
                else continue;
            }
            if(Character.digit(s.charAt(i),radix) < 0) return false;
        }
        return true;
    }

    private void initializeGraph() {
        graph = (GraphView) findViewById(R.id.graph);
        graph.setCursorMode(true);
        series = new LineGraphSeries<DataPoint>();
        graph.addSeries(series);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(5);
        graph.getViewport().setMaxX(xMax + 5);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(30);
        graph.getViewport().setMaxY(220);
    }

    private Boolean isFormattedCorrectly(String message) {
        /*
            // message formatting: [ID][PatientName][DataType][DataValue]
            String temp = message.substring(message.indexOf("[") + 1);
            String id = temp.substring(0, temp.indexOf("]"));
            Log.i(TAG, "MQTT: id: " + id);
            temp = temp.substring(temp.indexOf("[") + 1);
            String name = temp.substring(0, temp.indexOf("]"));
            Log.i(TAG, "MQTT: name: " + name);
            temp = temp.substring(temp.indexOf("[") + 1);
            String dataType = temp.substring(0, temp.indexOf("]"));
            Log.i(TAG, "MQTT: dataType: " + dataType);
            temp = temp.substring(temp.indexOf("[") + 1);
            String value = temp.substring(0, temp.indexOf("]"));
            Log.i(TAG, "MQTT: value: " + value);
        */

        if(message.split("\\]",-1).length-1 != 2) {
            if (message.split("\\[",-1).length-1 != 2) {
                Log.i(TAG, "MQTT: Error in recieved message: " + message);
                return false;
            }
            Log.i(TAG, "MQTT: Error in recieved message: " + message);
            return false;
        }
        return true;
    }
}
