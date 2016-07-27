package lab.wifiscanner;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("MissingPermission")
public class MainActivity extends Activity implements View.OnClickListener {
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
        final LocationFinder LF =new LocationFinder(this);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                showDialog(
                        "SSID: " + data.get(position).SSID + "\n"
                                + "BSSID: " + data.get(position).BSSID + "\n"
                                + "Capabilities: " + data.get(position).capabilities + "\n"
                                + "Frequency: " + data.get(position).frequency + "\n"
                                + "Level: " + data.get(position).level + "\n"
                        +"Longitude: " +LF.getlongitude()+"\n"
                        +"Latitude: "+ LF.getlatitude()

                );

            }
        });

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
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.scan_button) {
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
    private void showDialog(String s){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Dialog");
        builder.setMessage(s);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

}
