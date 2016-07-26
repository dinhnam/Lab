package lab.wifiscanner;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
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
    public Button scanBtn;
    public ListView list;
    public ListViewAdapter adapter;
    public ArrayList<ScanResult> data = new ArrayList<>();
    public boolean isScanning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        scanBtn.setOnClickListener(this);
        adapter = new ListViewAdapter(this, data);
        list.setAdapter(adapter);
    }

    public void init() {
        isScanning = false;
        wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        scanBtn = (Button) findViewById(R.id.scan_button);
        list = (ListView) findViewById(R.id.list_item);
        /* Ở phiên bản android có API >= 23
         * Cần phải kiểm tra quyền truy cập vị trí (1 trong 2 quyền bên dưới) để lấy được danh sách Wifi*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
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
