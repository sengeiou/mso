/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package no.uia.mso_login;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class PatientMainActivity extends Activity {
    // Change target device name here:
    private final static String targetDeviceName = "RTA";
    private final static String TAG = PatientMainActivity.class.getSimpleName();

    // Bluetooth
    private BluetoothLeService mBluetoothLeService;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final long SCAN_PERIOD = 10000; // Stops scanning after 10 seconds.
    private static int PERMISSION_REQUEST_CODE = 1;
    private boolean mConnected = false;
    private String deviceName;
    private String deviceAddress;

    // Program state
    private enum State {
        DISCONNECTED,
        CONNECTED,
        SCANNING
    }
    private State state;
    private boolean currentlyConnected = false;
    private boolean readingAccData = false;
    private boolean editingPatientName = false;

    // UUIDs
    public final static UUID UUID_TX_CHARACTERISTICS =
            UUID.fromString(GattAttributes.TX_CHARACTERISTICS);
    public final static UUID UUID_RX_CHARACTERISTICS =
            UUID.fromString(GattAttributes.RX_CHARACTERISTICS);

    // Characteristics
    private BluetoothGattCharacteristic bluetoothGattCharacteristicTX;
    private BluetoothGattCharacteristic bluetoothGattCharacteristicRX;

    // UI references
    private TextView statusTextView;
    private TextView deviceNameTextView;
    private TextView deviceAddressTextView;
    private LinearLayout deviceAddressTextContainer;
    private TextView dataTextView;
    private LinearLayout dataTextContainer;
    private TextView heartRateTextView;
    private TextView heartRateInfoTextView;
    private TextView savedAddressTextView;
    private Button button;
    private CheckBox checkbox;
    private String heartRateData = null;
    private Button requestAccButton;
    private Button editButton;
    private EditText editTextUsername;
    private TextView tvUsername;
    private TextView tvPatientName;

    // MQTT options
    // TODO: Use static string values for topic (inside strings.xml)
    private final static String topicTX = "patient";
    private String username;
    private String patientName;
    private Boolean mqttEnabled = false;

    // MSO_LOG
    private ListView msoLogListView; // links to UI
    private ArrayList<String> msoLogArrayList;
    ListAdapterMsoLog msoLogAdapter;
    private int msoLoggedItemsCounter = 0; // count logged items

    // Accelerometer accGraph
    // TODO: Add heartRateGraph for both pulse (hr) and accelerometer data (acc)
    GraphView accGraph; // link to UI
    LineGraphSeries<DataPoint> accSeries;
    private int accValue;
    private final static int accXMaxValue = 300; // accCurrentXValue axis max size
    private int accCurrentXValue = accXMaxValue;

    // Heart Rate BpmGraph
    GraphView heartRateGraph;
    LineGraphSeries<DataPoint> heartRateSeries;
    private final static int heartRateXMaxValue = 100;
    private int heartRateXValue = heartRateXMaxValue;
    int hrValue = 0;

    // Heart Rate simulation
    private int mHRSimulationTimeInterval = 500; // 600 ms
    private Handler mHRSimulationHandler;
    private boolean simulatingHeartRate = false;
    int y = 70;
    private boolean invert = false;
    private TextView tvSimulate;

    // Shared preferences
    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mEditor;

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "BLE: ERROR: Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(deviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Coarse location permission granted");
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_patient);
        mHandler = new Handler();

        // Get username from login screen
        Intent intent = getIntent();
        username = intent.getStringExtra("username");
        if(!username.equals(""))
            mqttEnabled = true;

        // Graph(s)
        initializeGraph();

        initializeBluetooth();
        state = State.DISCONNECTED;
        setUiReferences();
        updateUiText();

        // MSO_LOG
        msoLogArrayList = new ArrayList<>();
        msoLogAdapter = new ListAdapterMsoLog(this, R.layout.custom_list_item_log, msoLogArrayList);
        msoLogListView.setAdapter(msoLogAdapter);

        // Shared preferences
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mEditor = mPreferences.edit();
        mEditor.apply();

        // HR Simulation
        mHRSimulationHandler = new Handler();
    }

    public void loadSharedPreferences() {
        patientName = mPreferences.getString("name", "");
        if(patientName.equals("")) {
            tvPatientName.setText("Vennligst oppgi ditt fulle navn.");
            tvPatientName.setTextColor(getResources().getColor(R.color.colorSerious));
        } else {
            tvPatientName.setText(patientName);
            tvPatientName.setTextColor(getResources().getColor(R.color.colorPrimary));
        }
    }

    private void saveSharedPreferences() {
        mEditor.putString("name", patientName);
        mEditor.apply();
    }

    private void initializeGraph() {
        // accelerometer accGraph
        accGraph = (GraphView) findViewById(R.id.acc_graph);
        accGraph.setCursorMode(true);
        accSeries = new LineGraphSeries<DataPoint>();
        accGraph.addSeries(accSeries);
        accGraph.getViewport().setXAxisBoundsManual(true);
        accGraph.getViewport().setMinX(5);
        accGraph.getViewport().setMaxX(accXMaxValue + 5);

        // Heart Rate heartRateGraph
        heartRateGraph = (GraphView) findViewById(R.id.bpm_graph);
        heartRateGraph.setCursorMode(true);
        heartRateSeries = new LineGraphSeries<DataPoint>();
        heartRateGraph.addSeries(heartRateSeries);
        heartRateGraph.getViewport().setXAxisBoundsManual(true);
        heartRateGraph.getViewport().setMinX(5);
        heartRateGraph.getViewport().setMaxX(heartRateXMaxValue + 5);
        heartRateGraph.getViewport().setYAxisBoundsManual(true);
        heartRateGraph.getViewport().setMinY(30);
        heartRateGraph.getViewport().setMaxY(220);
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

        addPointToHeartRateGraph(y);
        TextView hr = findViewById(R.id.heart_rate);
        hr.setText(String.valueOf(y));
        hrValue = y;
        sendMessageTroughMqttService(topicTX, formatMqttMessage(String.valueOf(hrValue)));
        determineHeartRateInfoText();
    }

    private void addPointToAccGraph(int value) {
        accGraph.removeAllSeries();
        accCurrentXValue +=1;
        accGraph.getViewport().setMinX(accCurrentXValue - accXMaxValue +5);
        accGraph.getViewport().setMaxX(accCurrentXValue +5);
        accSeries.appendData(new DataPoint(accCurrentXValue, value), true, accXMaxValue, true);
        accGraph.addSeries(accSeries);
    }

    private void addPointToHeartRateGraph(int value) {
        heartRateGraph.removeAllSeries();
        heartRateXValue +=1;
        heartRateGraph.getViewport().setMinX(heartRateXValue - heartRateXMaxValue +5);
        heartRateGraph.getViewport().setMaxX(heartRateXValue +5);
        heartRateSeries.appendData(new DataPoint(heartRateXValue, value), true, heartRateXMaxValue, true);
        heartRateGraph.addSeries(heartRateSeries);
    }

    private void initializeBluetooth() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSION_REQUEST_CODE);

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
            finish();

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter == null)
            finish();

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        scanLeDevice(false);
        updateUiText();
        loadSharedPreferences();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopSimulation();
        scanLeDevice(false);
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(!mConnected) {
                        MSO_LOG("Unable to find any devices.");
                        mScanning = false;
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                        invalidateOptionsMenu();
                        state = State.DISCONNECTED;
                        updateUiText();
                    }
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            state = State.SCANNING;
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        invalidateOptionsMenu();
        updateUiText();
    }

    protected void validateDevice(BluetoothDevice device) {
        // Abort if currently connecting
        if (currentlyConnected)
            return;

        // Abort if currently connected
        if (mConnected)
            return;

        // Abort if an unknown device is discovered
        if (device.getName() == null)
            return;

        // Check if this device is black listed
        if (checkbox.isChecked()) {
            if (device.getAddress().equals(deviceAddress))
                return;
        }

        // Check device name
        if (device.getName().equals(targetDeviceName)) {
            currentlyConnected = true;
            MSO_LOG("Trying to connect to device with address: " + device.getAddress());
            connectToDevice(device);
        }
    }

    protected void connectToDevice(BluetoothDevice device) {
        deviceAddress = device.getAddress();
        deviceName = device.getName();

        if (mScanning) {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            mScanning = false;
        }

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(deviceAddress);
            MSO_LOG("Success: Connected to device.");
        } else {
            MSO_LOG("Error: Unable to connect to device.");
            currentlyConnected = false;
            state = State.DISCONNECTED;
        }

        updateUiText();
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            validateDevice(device);
                        }
                    });
                }
            };

    private void readGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null)
            return;

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                if (gattCharacteristic.getUuid().equals(UUID_TX_CHARACTERISTICS)) {
                    bluetoothGattCharacteristicTX =
                            gattService.getCharacteristic(UUID_TX_CHARACTERISTICS);

                    mBluetoothLeService.setCharacteristicNotification(
                            gattCharacteristic, true);
                } else if (gattCharacteristic.getUuid().equals(UUID_RX_CHARACTERISTICS))
                    bluetoothGattCharacteristicRX =
                            gattService.getCharacteristic(UUID_RX_CHARACTERISTICS);
            }
        }
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
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

    // interpret and display data received from device
    private void displayData(String data) {
        if (data == null) {
            return;
        }

        // Show raw data in MSO_LOG
        MSO_LOG("Data received from device: " + data);

        // Show raw data in info text (top of UI)
        dataTextContainer.setVisibility(View.VISIBLE);
        dataTextView.setText(data);

        // Check what type of data has arrived
        if (data.charAt(0) == 'B') {
            // Received HR data
            heartRateTextView.setText(data.substring(5));
            heartRateData = data.substring(5);
            heartRateInfoTextView.setVisibility(View.VISIBLE);

            // Indicate whether HR value is normal or not
            if(isInteger(heartRateData)) {
                hrValue = Integer.parseInt(heartRateData);
                addPointToHeartRateGraph(hrValue);
                determineHeartRateInfoText();
            }
            sendMessageTroughMqttService(topicTX, formatMqttMessage(heartRateData));
            return;
        } else if (data.charAt(0) == 'P') {
            // Received alert that HR is not updating
            if (heartRateData != null) {
                heartRateInfoTextView.setVisibility(View.VISIBLE);
                heartRateInfoTextView.setText(R.string.pulse_is_not_updated);
                heartRateInfoTextView.setTextColor(getResources().getColor(R.color.colorWarning));
                // TODO: inform personnel that heart rate data currently isn`t updating
                sendMessageTroughMqttService(topicTX, formatMqttMessage("--"));
                return;
            }
        } else if (data.charAt(0) == 'A') {
            // Received accelerometer data
            String accData = data.substring(5);
            if(isInteger(accData)) {
                accValue = Integer.parseInt(accData);
                addPointToAccGraph(accValue);
            }
            return;
        }

        // Physical button pressed on device?
        if (data.equals("HELP")) { // Emergency request
            sendMessageTroughMqttService(topicTX, formatMqttMessage("H"));
        }
        else if (data.equals("help")) { // Assistance request
            sendMessageTroughMqttService(topicTX, formatMqttMessage("h"));
        }
    }

    private void determineHeartRateInfoText() {
        if(hrValue > 100) { // high HR
            heartRateInfoTextView.setTextColor(getResources().getColor(R.color.colorSerious));
            heartRateInfoTextView.setText(R.string.pulse_is_high);
        } else if(hrValue < 50) { // low HR
            heartRateInfoTextView.setTextColor(getResources().getColor(R.color.colorSerious));
            heartRateInfoTextView.setText(R.string.pulse_is_low);
        } else { // normal HR
            heartRateInfoTextView.setTextColor(getResources().getColor(R.color.colorHeartRate));
            heartRateInfoTextView.setText(R.string.pulse_is_normal);
        }
    }

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                invalidateOptionsMenu();
                state = State.CONNECTED;
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                invalidateOptionsMenu();
                state = State.DISCONNECTED;
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                readGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }

            if (mConnected)
                state = State.CONNECTED;
            else {
                currentlyConnected = false;
                if (deviceAddress != null)
                    checkbox.setVisibility(View.VISIBLE);
            }
            updateUiText();
        }
    };

    private void setUiReferences() {
        // Dynamic text elements
        statusTextView = findViewById(R.id.status);
        dataTextView = findViewById(R.id.data);
        heartRateTextView = findViewById(R.id.heart_rate);
        heartRateInfoTextView = findViewById(R.id.heart_rate_info);
        deviceAddressTextView = findViewById(R.id.device_address);
        deviceNameTextView = findViewById(R.id.device_name);
        savedAddressTextView = findViewById(R.id.saved_address);
        requestAccButton = findViewById(R.id.accButton);
        editButton = findViewById(R.id.edit_button);
        editTextUsername = findViewById(R.id.edit_text_username);
        tvPatientName = findViewById(R.id.your_name);
        tvUsername = findViewById(R.id.your_username);
        tvUsername.setText(username);
        tvSimulate = findViewById(R.id.simulate);

        // Button
        button = findViewById(R.id.button);

        // Checkbox
        checkbox = findViewById(R.id.checkBox);

        // Containers
        deviceAddressTextContainer = findViewById(R.id.device_address_container);
        dataTextContainer = findViewById(R.id.data_container);

        // MSO_LOG
        msoLogListView = (ListView)findViewById(R.id.listview_log);
    }

    public void bluetoothControlBtn_onClick(View view) {
        if (mConnected)
            state = State.CONNECTED;

        switch (state) {
            case SCANNING: {
                // Stop scan pressed
                state = State.DISCONNECTED;
                scanLeDevice(false);
                MSO_LOG("Scan was stopped by user.");
                break;
            }
            case CONNECTED: {
                // Disconnect pressed
                MSO_LOG("Disconnected by user.");
                state = State.DISCONNECTED;
                mBluetoothLeService.disconnect();
                scanLeDevice(false);
                if (deviceAddress != null)
                    checkbox.setVisibility(View.VISIBLE);
                break;
            }
            case DISCONNECTED: {
                // Connect pressed
                MSO_LOG("Scan initiated by user.");
                state = State.SCANNING;
                scanLeDevice(true);
                if (checkbox.isChecked()) {
                    clearUiText();
                }
                break;
            }
        }
        updateUiText();
    }

    private void updateUiText() {
        switch (state) {
            case SCANNING: {
                statusTextView.setText(R.string.scanning);
                button.setText(R.string.stop_scanning);
                requestAccButton.setVisibility(View.GONE);
                if (checkbox.isChecked() && deviceAddress != null)
                    savedAddressTextView.setText(R.string.saved);
                accGraph.setVisibility(View.GONE);
                readingAccData = false;
                break;
            }
            case DISCONNECTED: {
                statusTextView.setText(R.string.disconnected);
                requestAccButton.setVisibility(View.GONE);
                dataTextContainer.setVisibility(View.GONE);
                if (deviceAddress != null) {
                    savedAddressTextView.setText(R.string.saved);
                    button.setText(R.string.connect);
                } else
                    button.setText(R.string.start_scan);
                accGraph.setVisibility(View.GONE);
                readingAccData = false;
                break;
            }
            case CONNECTED: {
                checkbox.setVisibility(View.GONE);
                requestAccButton.setVisibility(View.VISIBLE);
                statusTextView.setText(R.string.connected);
                button.setText(R.string.disconnect);
                savedAddressTextView.setText(null);
                heartRateGraph.setVisibility(View.VISIBLE);
                break;
            }
        }

        if (deviceAddress != null) {
            deviceAddressTextContainer.setVisibility(View.VISIBLE);
            deviceNameTextView.setText(deviceName);
            deviceAddressTextView.setText(deviceAddress);
        } else {
            deviceAddressTextContainer.setVisibility(View.GONE);
            dataTextContainer.setVisibility(View.GONE);
            checkbox.setVisibility(View.GONE);
        }
    }

    private void clearUiText() {
        deviceNameTextView.setText(null);
        deviceAddressTextView.setText(null);
        dataTextView.setText(null);
        savedAddressTextView.setText(null);
    }

    private String formatMqttMessage(String message) {
        // TODO: Use XML formatting instead with < and /> (follow HL7 FHIR standard)
        return "[" + username + "][" + patientName + "][" + message + "]";
    }

    private void sendMessageTroughMqttService(String topic, String message) {
        if(!mqttEnabled)
            return;

        MSO_LOG("Sending MQTT message: " + message);

        // Send message to MQTT background service trough a broadcast
        Intent intent = new Intent();
        intent.setAction("MQTT_ON_TRANSMIT");
        intent.putExtra("topic", topic);
        intent.putExtra("message", message);
        sendBroadcast(intent);
    }

    public void buttonHelp_onClick(View view) {
        if(!mqttEnabled) {
            Toast.makeText(this, "Du er i offline-modus.", Toast.LENGTH_SHORT).show();
            return;
        }
        sendMessageTroughMqttService(topicTX, formatMqttMessage("H"));
    }

    public void buttonAssistance_onClick(View view) {
        if(!mqttEnabled) {
            Toast.makeText(this, "Du er i offline-modus.", Toast.LENGTH_SHORT).show();
            return;
        }
        sendMessageTroughMqttService(topicTX, formatMqttMessage("h"));
    }

    private void MSO_LOG(String message) {
        msoLoggedItemsCounter++; // count logged items

        // Format message and add to array list
        String LOG_MESSAGE = "#" + String.valueOf(msoLoggedItemsCounter) + ": " + message;
        msoLogArrayList.add(LOG_MESSAGE);
        msoLogAdapter.notifyDataSetChanged();
    }

    public void requestAccData_onClick(View view) {
        if(!mConnected) {
            MSO_LOG("Not currently connected to device.");
            accGraph.setVisibility(View.GONE);
            return;
        }

        if(!readingAccData) {
            readingAccData = true;
            accGraph.setVisibility(View.VISIBLE);
            sendDataToDevice("ACC_START");
        } else {
            readingAccData = false;
            accGraph.setVisibility(View.GONE);
            sendDataToDevice("ACC_STOP");
        }
    }

    private void sendDataToDevice(String transmitData) {
        MSO_LOG("Transmitting data to device: " + transmitData);

        final byte[] insertSomething = transmitData.getBytes();
        byte[] txBytes = new byte[insertSomething.length];
        System.arraycopy(insertSomething, 0, txBytes, 0, insertSomething.length);

        if (bluetoothGattCharacteristicRX != null) {
            bluetoothGattCharacteristicRX.setValue(txBytes);
            mBluetoothLeService.writeCharacteristic(bluetoothGattCharacteristicRX);
            mBluetoothLeService.setCharacteristicNotification(bluetoothGattCharacteristicTX,
                    true);
        }
    }

    public void editPatientName_onClick(View view) {
        if(!editingPatientName) {
            editingPatientName = true;
            editButton.setText(getResources().getString(R.string.save_patient_name));
            editTextUsername.setVisibility(View.VISIBLE);
        } else {
            editingPatientName = false;
            editButton.setText(getResources().getString(R.string.edit));

            if(editTextUsername.getText().toString().equals("")) {
                Toast.makeText(this, "Dette navnet er ikke gyldig.", Toast.LENGTH_SHORT).show();
                return;
            } else {
                patientName = editTextUsername.getText().toString();
                tvPatientName.setText(patientName);
                saveSharedPreferences();
                tvPatientName.setTextColor(getResources().getColor(R.color.colorPrimary));
            }
            editTextUsername.setVisibility(View.GONE);
        }
    }

    public void buttonSimulate_onClick(View view) {
        if(!simulatingHeartRate) {
            simulatingHeartRate = true;
            heartRateGraph.setVisibility(View.VISIBLE);
            startSimulation();
            tvSimulate.setText(getResources().getString(R.string.stop_simulation));
            heartRateInfoTextView.setVisibility(View.VISIBLE);
        } else {
            simulatingHeartRate = false;
            stopSimulation();
            tvSimulate.setText(getResources().getString(R.string.simulate_graph));
            heartRateInfoTextView.setText(getResources().getString(R.string.pulse_is_not_updated));
            heartRateInfoTextView.setTextColor(getResources().getColor(R.color.colorWarning));
        }
    }
}
