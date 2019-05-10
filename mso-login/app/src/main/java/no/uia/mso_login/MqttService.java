package no.uia.mso_login;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;

import static no.uia.mso_login.App.MQTT_CHANNEL_ID;

public class MqttService extends Service {
    private static String server = "m24.cloudmqtt.com";
    private static String port = "14782";
    private static String serverUri = "tcp://" + server + ":" + port;
    private String username;
    private String password;
    private String topicRX = "patient";
    private String topicTX;

    private Boolean personnel;
    private Boolean loggedIn = false;

    private Boolean mqttConnected = false;

    private final static String TAG = "MqttService";
    private MqttAndroidClient client;

    @Override
    public void onCreate() {
        super.onCreate();

        // TODO: define action as static string
        IntentFilter filter = new IntentFilter("MQTT_ON_TRANSMIT");
        this.registerReceiver(new MqttService.Receiver(), filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO: user constants to avoid typos
        username = intent.getStringExtra("username");
        password = intent.getStringExtra("password");
        personnel = intent.getBooleanExtra("personnel", false);

        Intent notificationIntent = new Intent(this, LoginActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, MQTT_CHANNEL_ID )
                .setContentTitle("Tilkoplet via CloudMQTT")
                .setContentText("Brukernavn: " + username)
                .setSmallIcon(R.drawable.ic_favorite_border_black_24dp)
                .setContentIntent(pendingIntent)
                .build();

        if(!mqttConnected)
            mqttConnect();

        startForeground(1, notification);

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSelf();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void mqttConnect() {
        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(this.getApplicationContext(), serverUri,
                clientId);

        MqttConnectOptions options = new MqttConnectOptions();
        options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);
        options.setUserName(username);
        options.setPassword(password.toCharArray());

        try {
            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Log.i(TAG, "MQTT: Client was successfully connected to MQTT Broker.");
                    mqttConnected = true;
                    loggedIn = true;
                    mqttSubscribe();
                    goToHomePage();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.i(TAG, "MQTT: ERROR: Client was unable to connect to MQTT Broker.");
                    mqttConnected = false;
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void goToHomePage() {
        loggedIn = true;
        if(personnel) { // personnel page
            Intent intent = new Intent(this, PersonnelMainActivity.class);
            startActivity(intent);
        } else { // Patient page
            Intent intent = new Intent(this, PatientMainActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
        }
    }

    public void sendMqttMessage(String topic, String data) {
        if(!mqttConnected) {
            Log.i(TAG, "MQTT: MQTT Service not connected." + data);
            return;
        }

        byte[] encodedPayload = new byte[0];
        try {
            encodedPayload = data.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodedPayload);
            client.publish(topic, message);
            Log.i(TAG, "MQTT: Sending data:" + data);
        } catch (UnsupportedEncodingException | MqttException e) {
            e.printStackTrace();
        }
    }

    private void mqttSubscribe() {
        int qos = 1;
        try {
            IMqttToken subToken = client.subscribe(topicRX, qos);
            subToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // The message was published
                    Log.i(TAG, "MQTT: Successfully subscribed to topic: " + topicRX);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    // The subscription could not be performed, maybe the user was not
                    // authorized to subscribe on the specified topic e.g. using wildcards
                    Log.i(TAG, "MQTT: ERROR: Unable to subscribe to topic: " + topicRX);
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Log.i(TAG, "MQTT: Connection lost.");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                String m = new String(message.getPayload());
                Log.i(TAG, "MQTT: Message arrived from Broker: " + m);
                sendMessageToActivity(m);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Log.i(TAG, "MQTT: Message was successfully sent to Broker.");
            }
        });
    }

    private void sendMessageToActivity(String msg) {
        Intent intent = new Intent();
        intent.setAction("MQTT_ON_RECIEVE");
        intent.putExtra("message",msg);
        sendBroadcast(intent);
    }

    public Boolean getLoggedIn() {
        return loggedIn;
    }

    private class Receiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO: Use constants to avoid typos
            String topic = intent.getStringExtra("topic");
            String message = intent.getStringExtra("message");

            if(topic == null){
                Log.i(TAG, "MQTT: Failed to transmit. Missing topic.");
                return;
            }
            else if(message == null){
                Log.i(TAG, "MQTT: Failed to transmit. Message has no body.");
            }

            Log.i(TAG, "MQTT: Data ready to transmit: " + topic + "/" + message);
            sendMqttMessage(topic, message);
        }
    }
}
