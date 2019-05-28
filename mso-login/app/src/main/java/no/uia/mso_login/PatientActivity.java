package no.uia.mso_login;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Calendar;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class PatientActivity extends AppCompatActivity {
    private final static String TAG = PatientActivity.class.getSimpleName();
    private String patientUsername;
    private String patientName;
    private static String patientFilename = "";
    private static String filename = "patients.txt";
    private ArrayList<Patient> arrayList;
    private int patientCount = 0;

    GraphView graph;
    LineGraphSeries<DataPoint> series;
    private int y = 70;
    private int  hrValue = 0;
    private final static int xMax = 100;
    private int x = xMax;
    boolean invert = false;

    private Button newNoteButton;
    private EditText editTextNote;
    private TextView tvPulseInfo;
    private LinearLayout llEditPatientName;
    private EditText etPatientName;
    private TextView tvUsername;
    private TextView tvPatientName;
    private TextView tvSimulate;
    private LinearLayout llRequest;

    // Program state
    private boolean editingPatientName = false;
    private boolean addingNewNote = false;

    // MSO_LOG
    private ListView msoLogListView; // links to UI
    private ArrayList<String> msoLogArrayList;
    ListAdapterMsoLog msoLogAdapter;
    private int msoLoggedItemsCounter = 0; // count logged items

    // Heart Rate simulation
    private int mHRSimulationTimeInterval = 500; // 600 ms
    private Handler mHRSimulationHandler;
    private boolean simulatingHeartRate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient);

        Intent intent = getIntent();
        patientUsername = intent.getStringExtra("username");
        patientName = intent.getStringExtra("name");
        if(!patientUsername.equals(""))
            patientFilename = "patient" + patientUsername + ".txt";
        boolean request = intent.getBooleanExtra("request", false);

        // TODO: define action as static string
        IntentFilter filter = new IntentFilter("MQTT_ON_RECIEVE");
        this.registerReceiver(new PatientActivity.Receiver(), filter);

        tvUsername = findViewById(R.id.patient_username);
        tvUsername.setText(patientUsername);

        tvPatientName = findViewById(R.id.patient_name);
        tvPatientName.setText(patientName);
        initializeGraph();

        newNoteButton = findViewById(R.id.new_note_button);
        editTextNote = findViewById(R.id.edit_text_note);
        editTextNote.setVisibility(View.GONE);
        tvPulseInfo = findViewById(R.id.heart_rate_info);
        tvSimulate = findViewById(R.id.simulate);

        // MSO_LOG
        msoLogListView = (ListView)findViewById(R.id.listview_notes);
        msoLogArrayList = new ArrayList<>();
        msoLogAdapter = new ListAdapterMsoLog(this, R.layout.custom_list_item_log, msoLogArrayList);
        msoLogListView.setAdapter(msoLogAdapter);

        loadPatientNotes();

        msoLogListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int pos, long id) {
                msoLogArrayList.remove(pos);
                msoLogAdapter.notifyDataSetChanged();
                Toast.makeText(PatientActivity.this, "Slettet notat.",
                        Toast.LENGTH_LONG).show();
                return true;
            }
        });

        // File IO for patient list
        arrayList = new ArrayList<>();
        loadPatientDataFromFile();

        llEditPatientName = (LinearLayout) findViewById(R.id.edit_patient_name_linear_layout);
        etPatientName = (EditText) findViewById(R.id.patient_name_edit_text);
        llRequest = (LinearLayout) findViewById(R.id.request);

        if(request)
            llRequest.setVisibility(View.VISIBLE);

        mHRSimulationHandler = new Handler();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopSimulation();
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                generateRandomHeartRate(); //this function can change value of mHRSimulationTimeInterval.
            } finally {
                // 100% guarantee that this always happens, even if
                // your update method throws an exception
                mHRSimulationHandler.postDelayed(mStatusChecker, mHRSimulationTimeInterval);
            }
        }
    };

    void startSimulation() {
        mStatusChecker.run();
    }

    void stopSimulation() {
        mHRSimulationHandler.removeCallbacks(mStatusChecker);
    }

    private class Receiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");

            // Make sure message is correctly formatted
            if(!isFormattedCorrectly(message))
                return;

            String temp = message.substring(message.indexOf("[") + 1);
            String username = temp.substring(0, temp.indexOf("]"));
            temp = temp.substring(temp.indexOf("[") + 1);
            String patientName = temp.substring(0, temp.indexOf("]"));
            temp = temp.substring(temp.indexOf("[") + 1);
            String value = temp.substring(0, temp.indexOf("]"));

            Log.i(TAG, "MQTT: Userame: " + username + " this username = " + patientUsername);
            Log.i(TAG, "MQTT: Full patient name: " + patientName);
            Log.i(TAG, "MQTT: Value: " + value);

            if(username.equals(patientUsername)) {
                if(value.charAt(0)=='H') {
                    llRequest.setVisibility(View.VISIBLE);
                    return;
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
                    updatePulseInfoText();
                } else if (value.charAt(0)=='-') {
                    tvPulseInfo.setText(getResources().getString(R.string.pulse_is_not_updated));
                    tvPulseInfo.setTextColor(getResources().getColor(R.color.colorWarning));
                }
            }

            if (value.charAt(0)=='H') {
                for(Patient p: arrayList) {
                    if(username.equals(p.getUsername())) {
                        Toast.makeText(PatientActivity.this, p.getName() +
                                " trenger akutt nødhjelp.", Toast.LENGTH_LONG).show();
                        break;
                    }
                }
            }
        }
    }

    private void updatePulseInfoText() {
        if(hrValue < 50) {
            tvPulseInfo.setTextColor(getResources().getColor(R.color.colorSerious));
            tvPulseInfo.setText(getResources().getString(R.string.pulse_is_low));
        } else if(hrValue > 100) {
            tvPulseInfo.setTextColor(getResources().getColor(R.color.colorSerious));
            tvPulseInfo.setText(getResources().getString(R.string.pulse_is_high));
        } else {
            tvPulseInfo.setTextColor(getResources().getColor(R.color.colorPrimary));
            tvPulseInfo.setText(getResources().getString(R.string.pulse_is_normal));
        }
    }

    public void buttonSimulate_onClick(View view) {
        if(!simulatingHeartRate) {
            simulatingHeartRate = true;
            startSimulation();
            tvSimulate.setText(getResources().getString(R.string.stop_simulation));
        } else {
            simulatingHeartRate = false;
            stopSimulation();
            tvSimulate.setText(getResources().getString(R.string.simulate_graph));
            tvPulseInfo.setText(getResources().getString(R.string.pulse_is_not_updated));
            tvPulseInfo.setTextColor(getResources().getColor(R.color.colorWarning));
        }
    }

    private void generateRandomHeartRate() {
        if(hrValue!=0)
            y=hrValue;

        int low = -2; // for dynamic HR use -2, for stable HR use -1
        int high = 2;
        // Some extra logic so random value does not exceed possible values
        if(y < 60) {
            invert = false;
        }
        else if(y > 110) {
            invert = true;
        }

        Random r = new Random();
        int result = r.nextInt(high-low) + low;
        if(invert)
            y+=result;
        else
            y-=result;

        addPointToGraph(y);
        TextView hr = findViewById(R.id.heart_rate);
        hr.setText(String.valueOf(y));
        hrValue = y;
        updatePulseInfoText();
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
        if(message.split("\\]",-1).length-1 != 3) {
            if (message.split("\\[",-1).length-1 != 3) {
                Log.i(TAG, "MQTT: Error in recieved message: " + message);
                return false;
            }
            Log.i(TAG, "MQTT: Error in recieved message: " + message);
            return false;
        }
        return true;
    }

    public void newNoteBtn_onClick(View view) {
        if(!addingNewNote) {
            addingNewNote = true;
            editTextNote.setVisibility(View.VISIBLE);
            editTextNote.setText("");
            newNoteButton.setText(getResources().getString(R.string.save_patient_name));
        } else {
            addingNewNote = false;

            if(editTextNote.getText().toString().equals(""))
                return;
            else {
                MSO_LOG(editTextNote.getText().toString());
            }

            editTextNote.setVisibility(View.GONE);
            newNoteButton.setText(getResources().getString(R.string.add_note));
        }
    }

    private void MSO_LOG(String message) {
        msoLoggedItemsCounter++; // count logged items

        Date currentTime = Calendar.getInstance().getTime();

        // Format message and add to array list
        String LOG_MESSAGE = "#"
                + String.valueOf(msoLoggedItemsCounter)
                + " "
                + currentTime.toString().substring(0, 16)
                + ": "
                + message;
        msoLogArrayList.add(LOG_MESSAGE);
        msoLogAdapter.notifyDataSetChanged();
        savePatientNotes();
    }

    private void writeToFile(String filename, String data, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(filename, Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
            Log.i(TAG, "MSO: Success: Wrote " + data + " to the file " + filename);
        }
        catch (IOException e) {
            Log.i(TAG, "MSO: Error: File write failed: " + e.toString());
        }
    }

    private String readFromFile(String filename, Context context) {
        String ret = "";

        try {
            InputStream inputStream = context.openFileInput(filename);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.i(TAG, "FILEIO: Error: File not found: " + e.toString());
        } catch (IOException e) {
            Log.i(TAG, "FILEIO: Error: Can not read file: " + e.toString());
        }

        return ret;
    }

    private void savePatientNotes() {
        StringBuilder sb = new StringBuilder();
        for(String s: msoLogArrayList) {
            sb.append("<");
            sb.append(s);
            sb.append("/>");
        }
        String data = sb.toString();
        writeToFile(patientFilename, data, App.getAppContext());
    }

    private void loadPatientNotes() {
        // Read from file
        String data = readFromFile(patientFilename, App.getAppContext());

        // Count notes
        String findStr = "/>";
        int count = data.split(findStr, -1).length-1;
        Log.i(TAG, "FILEIO: Number of notes in file " + count);

        // Clear list
        msoLogArrayList.clear();
        msoLogAdapter.notifyDataSetChanged();
        msoLoggedItemsCounter = count;

        // Parse patient data from file
        for(int i = 0; i < count; i++) {
            Log.i(TAG, "FILEIO: Parsing note " + (i + 1) + "/" + count);

            data = data.substring(data.indexOf("<") + 1);
            String temp = data.substring(0, data.indexOf("/>"));
            Log.i(TAG, "FILEIO: temp is currently: " + temp);
            msoLogArrayList.add(temp);
        }

        // update UI
        msoLogAdapter.notifyDataSetChanged();
    }

    private void savePatientDataToFile() {
        StringBuilder sb = new StringBuilder();
        for(Patient p: arrayList) {
            sb.append("<Patient>\n");

            // Patient ID
            sb.append("     <id value=");
            sb.append(p.getId());
            sb.append("/>\n");

            // Patient name
            sb.append("     <name value=");
            sb.append(p.getName());
            sb.append("/>\n");

            // Patient username
            sb.append("     <username value=");
            sb.append(p.getUsername());
            sb.append("/>\n");

            sb.append("</Patient>\n");
        }

        String data = sb.toString();
        writeToFile(filename, data, App.getAppContext());
    }

    private void loadPatientDataFromFile() {
        // TODO: This is the same logic as in PersonellMainActivity, which is unfortunate
        // Read from file
        String data = readFromFile(filename, App.getAppContext());

        // Count patients
        String findStr = "<Patient>";
        int count = data.split(findStr, -1).length-1;

        // Clear list
        arrayList.clear();
        patientCount = 0;

        // Parse patient data from file
        for(int i = 0; i < count; i++) {
            int id = 0;
            String name = "";
            String username = "";

            data = data.substring(data.indexOf("id value="));
            // Find ID
            String idTemp = data.substring(data.indexOf("=") + 1, data.indexOf("/>"));
            if(isInteger(idTemp)){
                id = Integer.parseInt(idTemp);
            } else
                continue;

            data = data.substring(data.indexOf("name value="));
            // Find name
            String nameTemp = data.substring(data.indexOf("=") + 1, data.indexOf("/>"));
            if(nameTemp.equals("null")){
                name = "Eksempelnavn";
            } else
                name = nameTemp;

            data = data.substring(data.indexOf("username value="));
            // Find username
            String usernameTemp = data.substring(data.indexOf("=") + 1, data.indexOf("/>"));
            if(usernameTemp.equals("null")){
                username = "patient" + Integer.toString(patientCount + 1);
            } else {
                username = usernameTemp;
            }

            patientCount++;
            Patient patient = new Patient(patientCount, username, name,"--");
            arrayList.add(patient);
        }
    }

    public void buttonEditPatientName_onClick(View view) {
        if(!editingPatientName) {
            editingPatientName = true;
            llEditPatientName.setVisibility(View.VISIBLE);
            etPatientName.setText(patientName);
        }
    }

    public void buttonSavePatientName_onClick(View view) {
        String newName = etPatientName.getText().toString();
        if(!newName.equals("")) {
            patientName = newName;

            for(Patient p: arrayList) {
                if(p.getUsername().equals(patientUsername)){
                    // Patient found
                    p.setName(patientName);
                    tvPatientName.setText(patientName);
                    savePatientDataToFile();
                }
            }
        } else {
            Toast.makeText(PatientActivity.this, "Ugyldig navn.",
                    Toast.LENGTH_LONG).show();
        }

        editingPatientName = false;
        llEditPatientName.setVisibility(View.GONE);
    }
}
