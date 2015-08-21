package com.firebirdberlin.tinytimetracker;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import java.lang.Runnable;
import java.util.ArrayList;
import java.util.List;
import android.support.v4.app.ListFragment;

public class StatsFragment extends ListFragment {
    List<String> svalues = new ArrayList<String>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.stats_fragment, container, false);


        refresh();
        // use the SimpleCursorAdapter to show the
        // elements in a ListView
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, svalues);
        setListAdapter(adapter);
        return v;
    }

    public void refresh() {
        ArrayAdapter adapter = (ArrayAdapter) getListAdapter();
        if (adapter != null) {
            adapter.clear();
        }

        LogDataSource datasource = new LogDataSource(getActivity());
        //datasource.open();
        String ssid = Settings.getTrackedSSID(getActivity());
        List<LogEntry> values = datasource.getAllEntries(ssid);
        String lastDate = "";
        for (LogEntry e : values) {
            String curDate = e.startAsDateString();
            if (! curDate.equals(lastDate)) {
                lastDate = curDate;
                svalues.add(curDate);
            }
            svalues.add("\t\t" + e.toString());
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        datasource.close();
    }
}