package lab.wifiscanner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public WifiManager wifi;
    public List<ScanResult> results;
    public Button scanButton;
    public ListView list;
    public ListViewAdapter adapter;
    public ArrayList<ScanResult> data = new ArrayList<>();
    public BroadcastReceiver receiver;
    public IntentFilter filter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        scanButton.setOnClickListener(this);
        adapter = new ListViewAdapter(this, data);
        list.setAdapter(adapter);
        registerReceiver(receiver, filter);
    }

    public void init() {
        wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        scanButton = (Button) findViewById(R.id.scan_button);
        list = (ListView) findViewById(R.id.list_item);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                results = wifi.getScanResults();
            }
        };
        filter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
    }

    @Override
    public void onClick(View v) {
        if (!wifi.isWifiEnabled()) {
            Toast.makeText(this, "Wifi is disabled...making it enabled", Toast.LENGTH_LONG).show();
            wifi.setWifiEnabled(true);
        }
        Toast.makeText(this, "Scanning....", Toast.LENGTH_SHORT).show();
        data.clear();
        wifi.startScan();
        results = wifi.getScanResults();
        Toast.makeText(this, results.size() + " result", Toast.LENGTH_SHORT).show();
        for (ScanResult result : results) {
            data.add(result);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }
}
