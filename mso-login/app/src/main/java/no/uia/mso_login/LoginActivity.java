package no.uia.mso_login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;

public class LoginActivity extends AppCompatActivity {
    // UI References
    private EditText usernameEditText;
    private EditText passwordEditText;
    private CheckBox personnelCheckBox;
    private CheckBox offlineCheckBox;

    private Boolean personnel = false;
    private Boolean offline = false;

    private String username;
    private String password;

    private final static String TAG = LoginActivity.class.getSimpleName();
    private MqttAndroidClient client;

    // Shared preferences
    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();

        setUiReferences();

        // Shared preferences
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mEditor = mPreferences.edit();
        mEditor.apply();
    }

    public void loadSharedPreferences() {
        // TODO: use constants to avoid typos
        username = mPreferences.getString("username", "");
        password = mPreferences.getString("password", "");
        personnel = mPreferences.getBoolean("personnel", false);
        offline = mPreferences.getBoolean("offline", false);

        usernameEditText.setText(username);
        passwordEditText.setText(password);
        personnelCheckBox.setChecked(personnel);
        offlineCheckBox.setChecked(offline);
    }

    private void saveSharedPreferences() {
        // TODO: use constants to avoid typos
        mEditor.putString("username", username);
        mEditor.putString("password", password);
        mEditor.putBoolean("personnel", personnel);
        mEditor.putBoolean("offline", offline);
        mEditor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        stopService();
        loadSharedPreferences();
    }

    private void setUiReferences() {
        usernameEditText = findViewById(R.id.edit_username);
        passwordEditText = findViewById(R.id.edit_password);
        personnelCheckBox = findViewById(R.id.checkBox_personnel);
        offlineCheckBox = findViewById(R.id.checkBox_ignoreMqtt);
    }

    public void LoginBtn_onClick(View view) {
        personnel = personnelCheckBox.isChecked();
        offline = offlineCheckBox.isChecked();
        username = usernameEditText.getText().toString();
        password = passwordEditText.getText().toString();

        saveSharedPreferences();

        if(offline) { // skipping MQTT connection
            Toast.makeText(this, R.string.logged_in, Toast.LENGTH_SHORT).show();
            goToHomePage();
            return;
        }

        if(usernameEditText.getText().toString().equals("")) {
            Toast.makeText(this, R.string.missing_username,
                    Toast.LENGTH_SHORT).show();
            return;
        } else if(passwordEditText.getText().toString().equals("")) {
            Toast.makeText(this, R.string.missing_password,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        startService();
    }

    private void goToHomePage() {
        if(personnel) { // personnel page
            Intent intent = new Intent(LoginActivity.this, PersonnelMainActivity.class);
            startActivity(intent);
        } else { // Patient page
            Intent intent = new Intent(LoginActivity.this, PatientMainActivity.class);
            intent.putExtra("username", "");
            startActivity(intent);
        }
    }

    public void startService() {
        Intent serviceIntent = new Intent(this, MqttService.class);
        serviceIntent.putExtra("username", username);
        serviceIntent.putExtra("password", password);
        serviceIntent.putExtra("personnel", personnel);
        startService(serviceIntent);
    }

    public void stopService() {
        Intent serviceIntent = new Intent(this, MqttService.class);
        stopService(serviceIntent);
    }
}
