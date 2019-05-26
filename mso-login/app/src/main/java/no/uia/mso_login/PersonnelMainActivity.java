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

import java.util.ArrayList;

public class PersonnelMainActivity extends AppCompatActivity {
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
                intent.putExtra("name", patient.getName());
                startActivity(intent);
            }
        });
    }

    private class Receiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            message = intent.getStringExtra("message");
            Log.i(TAG, "MQTT: Received data from MQTT service: " + message);

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

            // Make sure message is correct
            if(message.split("\\]",-1).length-1 != 2) {
                if (message.split("\\[",-1).length-1 != 2) {
                    Log.i(TAG, "MQTT: Error in recieved message: " + message);
                    return;
                }
                Log.i(TAG, "MQTT: Error in recieved message: " + message);
                return;
            }

            String temp = message.substring(message.indexOf("[") + 1);
            String name = temp.substring(0, temp.indexOf("]"));
            temp = temp.substring(temp.indexOf("[") + 1);
            String value = temp.substring(0, temp.indexOf("]"));

            for(Patient p : arrayList) {
                if(p.getName().equals(name)){
                    // Patient already exist

                    if(value.charAt(0)=='H') {
                        Toast.makeText(PersonnelMainActivity.this, name +
                                " trenger akutt nødhjelp.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    p.setHeartRate(value);
                    adapter.notifyDataSetChanged();
                    return;
                }
            }

            // Add new Patient
            Patient patient = new Patient(patientCount, name, value);
            arrayList.add(patient);
            adapter.notifyDataSetChanged();

            patientCount++;
            discoveredPatients.setText(String.valueOf(patientCount));
        }
    }

    public void addPatientBtn_onClick(View view) {
        // Add new Patient
        patientCount++;
        Patient patient = new Patient(patientCount, "patient" + Integer.toString(patientCount), "--");
        arrayList.add(patient);
        adapter.notifyDataSetChanged();
        discoveredPatients.setText(String.valueOf(patientCount));
    }
}
