package e.paulk.blescanner;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    protected static final ParcelUuid RADIOLAND_BEACON = ParcelUuid.fromString(
            "00001803-494C-4F47-4943-544543480000");

    protected static final String BEACON_NAME = "Radioland iBeacon";

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String[] appPermissions = {
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private static final int PERMISSION_REQUEST_CODE = 30;
    private static final int REQUEST_ENABLE_BT = 10;

    private List<String> listPermissionNeeded;

    private RecyclerView.Adapter mAdapter;

    ArrayList<BleDevice> devices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        devices = new ArrayList<>();
        mAdapter = new MyAdapter(devices);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!checkPermissions()){
            requestPermission();
        }
        else {
            scanStart();
        }
    }

    public static String formatStringToUUID(String str) {
        String temp = str.substring(0, 8);
        temp = temp.concat("-");
        temp = temp.concat(str.substring(8, 12));
        temp = temp.concat("-");
        temp = temp.concat(str.substring(12, 16));
        temp = temp.concat("-");
        temp = temp.concat(str.substring(16, 20));
        temp = temp.concat("-");
        temp = temp.concat(str.substring(20));
        return temp;
    }

    ScanCallback mScanCallBack = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            String ble_name;
            String ble_identifier;
            String uuid = "null";
            String data = "null";
            int ble_rssi;
            BleDevice currentDevice = new BleDevice();
            int ble_tx = 0;

            if (result != null) {
                ScanRecord scanRecord = result.getScanRecord();
                BluetoothDevice bluetoothDevice = result.getDevice();
                if (scanRecord != null) {
                    ble_rssi = result.getRssi();
                    ble_name = scanRecord.getDeviceName();
                    ble_identifier = bluetoothDevice.getAddress();
                    byte[] bytes = scanRecord.getBytes();

                    SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
                    Calendar c = Calendar.getInstance();
                    String currentTime = df.format(c.getTime());
                    if (bytes != null) {
                        currentDevice.setDevice_rawData(bytes);
                        data = currentDevice.getDevice_convertedRawData();
                        currentDevice.setDevice_UUID(data.substring(18,50));
                        uuid = currentDevice.getDevice_UUID();
                    }
                    currentDevice.setDevice_RSSI(ble_rssi);
                    currentDevice.setDevice_name(ble_name);
                    currentDevice.setDevice_identifier(ble_identifier);
                    currentDevice.setLastUpdateTime(currentTime);

                    if (devices.isEmpty()) {
                        devices.add(currentDevice);
                        Log.i(TAG, "First time" );
                    } else {
                        int i = 0;
                        int count = 0;
                        boolean isDifferent = true;
                        while (i < devices.size() && isDifferent) {
                            BleDevice recordedDevice = devices.get(i);
                            Log.i(TAG, "recorded: " +
                                    String.valueOf(recordedDevice.getDevice_identifier()) +
                                    " current: " + String.valueOf(ble_identifier));
                            if (recordedDevice.getDevice_identifier().equals(ble_identifier)) {
                                recordedDevice.setDevice_RSSI(ble_rssi);
                                recordedDevice.setDevice_rawData(bytes);
                                recordedDevice.setLastUpdateTime(currentTime);
                                Log.i(TAG, "same");
                                isDifferent = false;
                            } else {
                                count++;
                                Log.i(TAG, "diff");
                                isDifferent = true;
                            }
                            if (count == devices.size()) {
                                Log.i(TAG, "No matching record, adding into the arraylist");
                                devices.add(currentDevice);
                            }
                            i++;
                        }
                    }
                    mAdapter.notifyDataSetChanged();
                    Log.i(TAG, "name: " + ble_name + " RSSI: " + String.valueOf(ble_rssi) +
                            " Tx: " + String.valueOf(ble_tx) + " identifier: " + ble_identifier +
                            " uuid: " + uuid + " data: " + data + " LastUpdate: " + currentTime);
                    Log.i(TAG, "adapter: " + String.valueOf(mAdapter.getItemCount()) +
                            "array: " + devices.size());

                }
            }
        }
    };

    public static String getDataFromByteArray(byte[] byteArray) {
        if (byteArray == null) {
            return null;
        }
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[byteArray.length * 2];
        for (int j = 0; j < byteArray.length; j++) {
            int v = byteArray[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        String str = new String(hexChars);
        return str;
    }

    private void scanStart() {
        Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
        //Request turing on the Bluetooth of the device
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(
                Context.BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        ScanSettings settings = new ScanSettings.Builder().setScanMode(
                ScanSettings.SCAN_MODE_LOW_LATENCY).build();

        List<ScanFilter> scanFilters = new ArrayList<>();
        //ScanFilter filter_uuid = new ScanFilter.Builder().setServiceUuid(RADIOLAND_BEACON).build();
        ScanFilter filter_name = new ScanFilter.Builder().setDeviceName(BEACON_NAME).build();
        ScanFilter filter_uuid = new ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString(
                "00001803-494C-4F47-4943-544543480000")).build();
        //scanFilters.add(filter_uuid);
        scanFilters.add(filter_name);

        BluetoothLeScanner bleScanner = mBluetoothAdapter.getBluetoothLeScanner();
        if (bleScanner != null) {
            bleScanner.startScan(scanFilters, settings, mScanCallBack);
        }
    }

    private void stopScan() {
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(
                Context.BLUETOOTH_SERVICE);
        bluetoothManager.getAdapter().getBluetoothLeScanner().stopScan(mScanCallBack);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //Adding new context when the user rejected the request of turning on the Bluetooth
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_CANCELED) {
                Log.i(TAG, "User rejected the request of turning on the Bluetooth.");
                Toast.makeText(this, "Please turn on the Bluetooth",
                        Toast.LENGTH_SHORT);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                Toast.makeText(this, "Refreshing scan result", Toast.LENGTH_SHORT).show();
                stopScan();
                //remove all the data
                devices.clear();
                mAdapter.notifyDataSetChanged();
                scanStart();
                return true;
            case R.id.menu_stopScan:
                Toast.makeText(this, "Stop scan", Toast.LENGTH_SHORT).show();
                stopScan();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean checkPermissions() {
        listPermissionNeeded = new ArrayList<>();
        for (String perm : appPermissions) {
            if (ActivityCompat.checkSelfPermission(this,
                    perm) != PackageManager.PERMISSION_GRANTED) {
                listPermissionNeeded.add(perm);
            }
        }
        if (listPermissionNeeded.isEmpty()) {
            return true;
        }
        else {
            return false;
        }
    }

    private void startPermissionRequest() {
        ActivityCompat.requestPermissions(this,
                listPermissionNeeded.toArray(new String[listPermissionNeeded.size()]),
                PERMISSION_REQUEST_CODE);
    }

    private void requestPermission() {
        for (String perm : listPermissionNeeded) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    perm)) {
                Log.i(TAG, "Displaying permission rationale.");
                startPermissionRequest();
            }
            else {
                Log.i(TAG, "Requesting permission");
                startPermissionRequest();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");

        if (requestCode == PERMISSION_REQUEST_CODE) {
            HashMap<String, Integer> permissionResults = new HashMap<>();
            int deniedCount = 0;

            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    permissionResults.put(permissions[i], grantResults[i]);
                    deniedCount++;
                }
            }

            if (deniedCount == 0) {
                scanStart();
            }
        }
    }
}
