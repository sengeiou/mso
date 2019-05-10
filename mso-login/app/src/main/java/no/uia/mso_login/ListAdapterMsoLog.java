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

public class ListAdapterMsoLog extends ArrayAdapter<String> {
    private static final String TAG = "ListAdapterMsoLog";
    private View v;
    private ViewGroup vgParent;

    private Context mContext;
    int mResource;

    public ListAdapterMsoLog(Context context, int resource, ArrayList<String> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
    }

    @Override
    public String getItem(int position) {
        return super.getItem(position);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String text = getItem(position);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource, parent, false);

        TextView tvText = (TextView) convertView.findViewById(R.id.text);

        tvText.setText(text);

        v = convertView;
        vgParent = parent;
        return convertView;
    }
}
