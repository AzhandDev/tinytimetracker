package com.firebirdberlin.tinytimetracker;

import android.app.ListActivity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.PorterDuff;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemLongClickListener;
import java.util.LinkedHashSet;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class AddTrackerActivity extends ListActivity {
    private static String TAG = TinyTimeTracker.TAG + ".AddTrackerActivity";
    private TrackerEntry tracker = null;
    private LogDataSource datasource = null;
    private AccessPointAdapter accessPointAdapter = null;
    private ArrayList<AccessPoint> accessPoints = new ArrayList<AccessPoint>();
    private Button button_wifi = null;
    private EditText edit_tracker_verbose_name = null;

    private final int RED = Color.parseColor("#AAC0392B");
    private final int BLUE = Color.parseColor("#3498db");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_tracker_activity);
        registerForContextMenu(this.getListView());

        datasource = new LogDataSource(this);
        Intent intent = getIntent();
        long tracker_id = intent.getLongExtra("tracker_id", -1L);
        if (tracker_id > -1L) {
            tracker = datasource.getTracker(tracker_id);
        }
        init();
    }

    private void init() {
        edit_tracker_verbose_name = (EditText) findViewById(R.id.edit_tracker_verbose_name);
        button_wifi = (Button) findViewById(R.id.button_wifi);
        setWifiIconColor(BLUE);

        if (tracker != null) {
            edit_tracker_verbose_name.setText(tracker.getVerboseName());

            accessPoints = (ArrayList<AccessPoint>) datasource.getAllAccessPoints(tracker.getID());
        }

        accessPointAdapter = new AccessPointAdapter(this, R.layout.list_2_lines, accessPoints);
        setListAdapter(accessPointAdapter);
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu_access_points, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.action_add:
                onChooseWifi(button_wifi);
                return true;
            case R.id.action_delete:
                AccessPoint accessPoint = accessPoints.remove(info.position);
                datasource.delete(accessPoint);
                accessPointAdapter.notifyDataSetChanged();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    public void onChooseWifi(View v) {
        final LinkedList<AccessPoint> accessPoints = new LinkedList<AccessPoint>();
        final AccessPointAdapter adapter = new AccessPointAdapter(this, R.layout.list_2_lines,
                                                                  accessPoints);

        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        List<ScanResult> networkList = wifiManager.getScanResults();
        if (networkList == null) {
            return;
        }

        for (ScanResult network : networkList) {
            if (accessPointAdapter.indexOfBSSID(network.BSSID) == -1) {
                AccessPoint accessPoint = new AccessPoint(network.SSID, network.BSSID);
                adapter.add(accessPoint);
            }
        }

        new AlertDialog.Builder(this)
            .setTitle("Active WiFi networks")
            .setIcon(R.drawable.ic_wifi)
            .setAdapter(adapter,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {
                                AccessPoint accessPoint = adapter.getItem(item);
                                String ssid = accessPoint.ssid;
                                String bssid = accessPoint.bssid;

                                if (edit_tracker_verbose_name.length() == 0) {
                                    edit_tracker_verbose_name.setText(ssid);
                                }

                                accessPointAdapter.add(accessPoint);
                                setWifiIconColor(BLUE);

                                dialog.dismiss();
                            }
                        })
            .setNegativeButton(android.R.string.no, null).show();
    }

    public void onClickOk(View v) {
        String verbose_name = edit_tracker_verbose_name.getText().toString();

        if (validateInputs(verbose_name) == false) {
            return;
        }

        if (tracker == null) {
            tracker = new TrackerEntry(verbose_name, "WLAN");
        } else {
            tracker.setVerboseName(verbose_name);
        }

        // when saving the account the deprecated fields shall no longer contain useful data
        tracker.setSSID("_deprecated_");
        datasource.save(tracker);
        long tracker_id = tracker.getID();
        for (int i = 0; i < accessPoints.size(); i++ ) {
            AccessPoint ap = accessPoints.get(i);
            ap.setTrackerID(tracker_id);
            datasource.save(ap);

        }
        datasource.close();
        this.finish();
    }

    private boolean validateInputs(String verbose_name) {
        if (verbose_name.isEmpty()) {
            edit_tracker_verbose_name.setBackgroundColor(RED);
            edit_tracker_verbose_name.invalidate();
            return false;
        }

        if (accessPoints.size() == 0) {
            setWifiIconColor(RED);
            return false;
        }

        TrackerEntry other = datasource.getTracker(verbose_name);
        if (tracker == null && other != null) { // a tracker with this name already exists
            edit_tracker_verbose_name.setBackgroundColor(RED);
            edit_tracker_verbose_name.invalidate();
            return false;
        }

        if (tracker != null && other != null && other.getID() != tracker.getID()) {
            edit_tracker_verbose_name.setBackgroundColor(RED);
            edit_tracker_verbose_name.invalidate();
            return false;
        }
        return true;
    }

    private void setWifiIconColor(int color){
        Resources res = getResources();
        Drawable icon = res.getDrawable(R.drawable.ic_wifi);
        icon.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        button_wifi.setBackgroundResource(R.drawable.ic_wifi);
        button_wifi.invalidate();

        edit_tracker_verbose_name.setBackgroundColor(Color.TRANSPARENT);
        edit_tracker_verbose_name.invalidate();
    }

    public static void open(Context context) {
        Intent myIntent = new Intent(context, AddTrackerActivity.class);
        context.startActivity(myIntent);
    }

    public static void open(Context context, long tracker_id) {
        Intent myIntent = new Intent(context, AddTrackerActivity.class);
        myIntent.putExtra("tracker_id", tracker_id);
        context.startActivity(myIntent);
    }

}
