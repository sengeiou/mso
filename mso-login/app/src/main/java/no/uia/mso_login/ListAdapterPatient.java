package no.uia.mso_login;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ListAdapterPatient extends ArrayAdapter<Patient> {
    private static final String TAG = "ListAdapterPatient";
    private View v;
    private ViewGroup vgParent;

    private Context mContext;
    int mResource;


    public ListAdapterPatient(Context context, int resource, ArrayList<Patient> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
    }

    @Override
    public Patient getItem(int position) {
        return super.getItem(position);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int id = getItem(position).getId();
        String username = getItem(position).getUsername();
        String heartRate = getItem(position).getHeartRate();

        Patient patient = new Patient(id, username, heartRate);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource, parent, false);

        TextView tvName = (TextView) convertView.findViewById(R.id.name);
        TextView tvHeartRate = (TextView) convertView.findViewById(R.id.heart_rate);

        tvName.setText(username);
        tvHeartRate.setText(heartRate);

        Log.i(TAG, "MQTT: Name: " + username + " at position " + position);

        v = convertView;
        vgParent = parent;
        return convertView;
    }
}
