package lab.wifiscanner;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, ActivityCompat.OnRequestPermissionsResultCallback {
    public WifiManager wifi;
    public List<ScanResult> results;
    public Button scanBtn;
    public ListView list;
    public ListViewAdapter adapter;
    public ArrayList<ScanResult> data = new ArrayList<>();
    public boolean isScanning;
    public LocationManager manager;
    public View view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        checkPermission();
    }

    public void init() {
        isScanning = false;
        wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        scanBtn = (Button) findViewById(R.id.scan_button);
        list = (ListView) findViewById(R.id.list_item);
        scanBtn.setOnClickListener(this);
        adapter = new ListViewAdapter(this, data);
        list.setAdapter(adapter);
        view = findViewById(R.id.main_layout);
        manager = (LocationManager) getSystemService(LOCATION_SERVICE);
    }

    public void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Snackbar.make(view, "You need to allow access to GPS", Snackbar.LENGTH_LONG).setAction("Retry", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
                    }
                }).show();
                return;
            }
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            return;
        }
        new LocationFinder(view, manager);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 0 && grantResults.length >= 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                new LocationFinder(view, manager);
            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Snackbar.make(view, "You need to allow access to GPS again", Snackbar.LENGTH_LONG).setAction("Retry", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
                        }
                    }).show();
                } else {
                    Snackbar.make(view, "Please making GPS enabled in the setting manually", Snackbar.LENGTH_LONG).setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent settingGPS = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                            startActivity(settingGPS);
                        }
                    }).show();
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (isScanning) {
            scanBtn.setText(String.valueOf("Scan Now"));
            scanBtn.setTextColor(Color.BLACK);
            isScanning = false;
        } else {
            scanBtn.setText(String.valueOf("Stop"));
            scanBtn.setTextColor(Color.RED);
            isScanning = true;
        }
        // Cần tạo 1 luồng riêng vì ta cần cập nhật giao diện khi quét được Wifi,
        // nếu không ứng dụng sẽ bị treo
        new Thread(new Runnable() {
            @Override
            public void run() {
                autoScan();
            }
        }).start();
    }

    private void autoScan() {
        while (isScanning) {
            if (!wifi.isWifiEnabled()) {
                showMessage("Wifi is disabled... making it enabled");
                wifi.setWifiEnabled(true);
            }
            data.clear();
            wifi.startScan();
            results = wifi.getScanResults();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for (ScanResult result : results) {
                        data.add(result);
                        adapter.notifyDataSetChanged();
                    }
                }
            });
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                showMessage(e.toString());
            }
        }
    }

    // Hiển thị thông báo, cần phải tạo luồng riêng
    private void showMessage(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
