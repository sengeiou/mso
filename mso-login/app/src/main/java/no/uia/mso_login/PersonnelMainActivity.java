package no.uia.mso_login;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class PersonnelMainActivity extends AppCompatActivity {
    private static String filename = "patients.txt";

    private int patientCount = 0;
    private ListView mListView;
    TextView discoveredPatients;

    String message = "Empty";

    private ArrayList<Patient> arrayList;
    ListAdapterPatient adapter;

    private final static String TAG = PersonnelMainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_personnel);
        getSupportActionBar().hide();

        // TODO: define action as static string
        IntentFilter filter = new IntentFilter("MQTT_ON_RECIEVE");
        this.registerReceiver(new Receiver(), filter);

        discoveredPatients = (TextView) findViewById(R.id.discovered_patients);
        discoveredPatients.setText("0");

        mListView = (ListView)findViewById(R.id.listview);
        arrayList = new ArrayList<>();

        adapter = new ListAdapterPatient(this, R.layout.custom_list_item_patient, arrayList);
        mListView.setAdapter(adapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Patient patient = adapter.getItem(position);
                Intent intent = new Intent(PersonnelMainActivity.this,
                        PatientActivity.class);
                // Based on item add info to intent
                intent.putExtra("emergencyRequest", patient.getEmergencyRequest());
                intent.putExtra("assistanceRequest", patient.getAssistanceRequest());
                intent.putExtra("fallRequest", patient.isFallRequest());
                patient.setEmergencyRequest(false);
                patient.setFallRequest(false);
                patient.setAssistanceRequest(false);
                intent.putExtra("username", patient.getUsername());
                intent.putExtra("name", patient.getName());
                savePatientDataToFile();
                startActivity(intent);
            }
        });

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int pos, long id) {
                if(!arrayList.get(pos).getEmergencyRequest()) {
                    arrayList.remove(pos);
                    patientCount--;
                    discoveredPatients.setText(String.valueOf(patientCount));
                    adapter.notifyDataSetChanged();
                    Toast.makeText(PersonnelMainActivity.this, "Slettet pasient.",
                            Toast.LENGTH_LONG).show();
                    savePatientDataToFile();
                }
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPatientDataFromFile();
    }

    private class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            message = intent.getStringExtra("message");
            Log.i(TAG, "MQTT: Received data from MQTT service: " + message);

            // Make sure message is correct
            if(message.split("\\]",-1).length-1 != 3) {
                if (message.split("\\[",-1).length-1 != 3) {
                    Log.i(TAG, "MQTT: Error in recieved message: " + message);
                    return;
                }
                Log.i(TAG, "MQTT: Error in recieved message: " + message);
                return;
            }

            // Current format (check PatientMainActivity): [username][full patient name][value]

            String temp = message.substring(message.indexOf("[") + 1);
            String username = temp.substring(0, temp.indexOf("]"));
            temp = temp.substring(temp.indexOf("[") + 1);
            String patientName = temp.substring(0, temp.indexOf("]"));
            temp = temp.substring(temp.indexOf("[") + 1);
            String value = temp.substring(0, temp.indexOf("]"));

            Log.i(TAG, "MQTT: Userame: " + username);
            Log.i(TAG, "MQTT: Full patient name: " + patientName);
            Log.i(TAG, "MQTT: Value: " + value);

            if(username.equals(""))
                return;
            else if(patientName.equals(""))
                patientName = username;

            for(Patient p: arrayList) {
                if(p.getUsername().equals(username)){
                    // Patient already exist

                    if(value.charAt(0)=='H') {
                        Toast.makeText(PersonnelMainActivity.this, p.getName() +
                                " trenger akutt nødhjelp.", Toast.LENGTH_LONG).show();
                        p.setEmergencyRequest(true);
                        adapter.notifyDataSetChanged();
                        return;
                    } else if (value.charAt(0)=='h') {
                        p.setAssistanceRequest(true);
                        adapter.notifyDataSetChanged();
                        return;
                    } else if (value.charAt(0)=='F') {
                        p.setFallRequest(true);
                        adapter.notifyDataSetChanged();
                        return;
                    }

                    p.setHeartRate(value);
                    adapter.notifyDataSetChanged();
                    return;
                }
            }

            // Add new Patient
            patientCount++;
            Patient patient = new Patient(patientCount, username, patientName, value);

            if(value.charAt(0)=='H') {
                Toast.makeText(PersonnelMainActivity.this, patientName +
                        " trenger akutt nødhjelp.", Toast.LENGTH_LONG).show();
                patient.setHeartRate("--");
                patient.setEmergencyRequest(true);
            } else if (value.charAt(0)=='h') {
                patient.setHeartRate("--");
                patient.setAssistanceRequest(true);
            } else if (value.charAt(0)=='F') {
                patient.setFallRequest(true);
                adapter.notifyDataSetChanged();
                return;
            }

            arrayList.add(patient);
            adapter.notifyDataSetChanged();
            discoveredPatients.setText(String.valueOf(patientCount));
            savePatientDataToFile();
        }
    }

    public void addPatientBtn_onClick(View view) {
        // Add new Patient
        patientCount++;
        Patient patient = new Patient(patientCount,
                generateNewUsername(),
                "Eksempelnavn",
                "--");
        arrayList.add(patient);
        adapter.notifyDataSetChanged();
        discoveredPatients.setText(String.valueOf(patientCount));
        savePatientDataToFile();
    }

    private void writeToFile(String data, Context context) {
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

    private String readFromFile(Context context) {

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
        writeToFile(data, App.getAppContext());
    }

    private void loadPatientDataFromFile() {
        // Read from file
        String data = readFromFile(App.getAppContext());

        // Count patients
        String findStr = "<Patient>";
        int count = data.split(findStr, -1).length-1;
        Log.i(TAG, "FILEIO: Number of patients stored in file: " + count);

        // Clear list
        arrayList.clear();
        adapter.notifyDataSetChanged();
        patientCount = 0;

        // Parse patient data from file
        for(int i = 0; i < count; i++) {
            int id = 0;
            String name = "";
            String username = "";

            Log.i(TAG, "FILEIO: Parsing patient " + (i + 1) + "/" + count);

            data = data.substring(data.indexOf("id value="));
            // Find ID
            String idTemp = data.substring(data.indexOf("=") + 1, data.indexOf("/>"));
            if(App.isInteger(idTemp)){
                id = patientCount + 1;
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

        // update UI
        adapter.notifyDataSetChanged();
        discoveredPatients.setText(String.valueOf(patientCount));
    }

    private String generateNewUsername() {
        int numberOfTests = 100;
        int patientId = 1;
        String generatedUsername = "patient" + String.valueOf(patientId);

        boolean usernameIsOccupied = false;
        for(int i = 0; i < numberOfTests; i++) {
            for(Patient p: arrayList) {
                if(p.getUsername().equals(generatedUsername)) {
                    usernameIsOccupied = true;
                }
            }
            if(usernameIsOccupied) {
                patientId++;
                generatedUsername = "patient" + String.valueOf(patientId);
                usernameIsOccupied = false;
            } else {
                return generatedUsername;
            }

        }
        return generatedUsername;
    }
}
