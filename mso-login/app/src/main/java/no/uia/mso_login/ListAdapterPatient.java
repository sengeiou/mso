package no.uia.mso_login;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
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
        String strId = "#" + String.valueOf(id);
        String username = "(" + getItem(position).getUsername() + ")";
        String heartRate = getItem(position).getHeartRate();
        String patientName = getItem(position).getName();
        boolean emergencyRequest = getItem(position).getEmergencyRequest();
        boolean assistanceRequest = getItem(position).getAssistanceRequest();
        boolean fallRequest = getItem(position).isFallRequest();

        Patient patient = new Patient(id, username, patientName, heartRate);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource, parent, false);

        TextView tvName = (TextView) convertView.findViewById(R.id.patient_name);
        TextView tvUsername = (TextView) convertView.findViewById(R.id.patient_username);
        TextView tvHeartRate = (TextView) convertView.findViewById(R.id.heart_rate);
        TextView tvId = (TextView) convertView.findViewById(R.id.patient_id);
        TextView tvPulseInfo = (TextView) convertView.findViewById(R.id.heart_rate_info);
        LinearLayout lyEmergencyRequest = (LinearLayout) convertView.findViewById(R.id.emergency_request);
        LinearLayout lyAssistanceRequest = (LinearLayout) convertView.findViewById(R.id.assistance_request);
        LinearLayout lyFallRequest = (LinearLayout) convertView.findViewById(R.id.fall_request);

        int hrValue = 0;
        if(App.isInteger(heartRate)){
            hrValue = Integer.parseInt(heartRate);
        }

        // TODO: manually putting color HEX values is not ideal
        if(hrValue==0) {
            tvPulseInfo.setText(R.string.not_updating);
            tvPulseInfo.setTextColor(Color.parseColor("#F08A5D"));
        } else if(hrValue<50) {
            tvPulseInfo.setText(R.string.pulse_is_low);
            tvPulseInfo.setTextColor(Color.parseColor("#EF5184"));
        } else if(hrValue>100) {
            tvPulseInfo.setText(R.string.pulse_is_high);
        } else {
            tvPulseInfo.setText(R.string.pulse_is_normal);
            tvPulseInfo.setTextColor(Color.parseColor("#008577"));
        }

        // Request from patient
        if(emergencyRequest)
            lyEmergencyRequest.setVisibility(View.VISIBLE);
        if(assistanceRequest)
            lyAssistanceRequest.setVisibility(View.VISIBLE);
        if(fallRequest)
            lyFallRequest.setVisibility(View.VISIBLE);

        tvId.setText(strId);
        tvUsername.setText(username);
        tvName.setText(patientName);
        tvHeartRate.setText(heartRate);

        v = convertView;
        vgParent = parent;
        return convertView;
    }
}
