package com.firebirdberlin.tinytimetracker;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class TwoColumnListAdapter extends ArrayAdapter<String> {
    private Context mContext = null;
    private List<String> values1 = null;
    private List<String> values2 = null;
    private List<Long> ids = null;


    public TwoColumnListAdapter(Context context, int viewid, List<String> values1,
                                List<String> values2) {
        super(context, viewid, R.id.text1, values1);
        mContext = context;
        this.values1 = values1;
        this.values2 = values2;
        this.ids = new ArrayList<Long>();
    }

    @Override
    public View getView( int position, View convertView, ViewGroup parent ) {
        super.getView(position, convertView, parent);
        LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
        View v = inflater.inflate(R.layout.list_2_columns, parent, false);
        TextView text1 = ( TextView ) v.findViewById ( R.id.text1 );
        TextView text2 = ( TextView ) v.findViewById ( R.id.text2 );

        if (position < values1.size()) {
            text1.setText(values1.get(position));
            text2.setText(values2.get(position));
        }

        return v;
    }

    @Override
    public void clear() {
        super.clear();
        values1.clear();
        values2.clear();
    }

    public void addRight(String value) {
        values2.add(value);
    }

    public void addRight(String value, long id) {
        values2.add(value);
        ids.add(id);
    }


    public long getIDAtPosition(int position) {
        if ( position < ids.size() ) {
            return ids.get(position);
        }
        return -1L;
    }

    public void removeItemAtPosition(int position) {
        if (position < values1.size()) values1.remove(position);
        if (position < values2.size()) values2.remove(position);
        if ( position < ids.size() ) ids.remove(position);
        notifyDataSetChanged();
    }

}
