package com.example.bluetooth;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

public class BluetoothActivity extends AppCompatActivity {

     private static final String TAG = "Bluetooth Activity";
    BluetoothAdapter mBluetoothAdapter;
    Button btnEnableDisable_Discoverable;
    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
    public DeviceListAdapter mDeviceListAdapter;
    ListView IvNewDevices;

    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver(){

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)){
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,mBluetoothAdapter.ERROR);

                switch(state){

                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "onReceive: STATE OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "mBroadcastReceiver1: ON");
                        break;
                     case BluetoothAdapter.STATE_TURNING_ON:
                         Log.d(TAG,  "mBroadcastReceiver1: STATE TURNING ON");
                         break;
                }
            }
        }

    };

    private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver(){

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)){
                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE,BluetoothAdapter.ERROR);

                switch(mode){

                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Enabled.");
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Disabled. Able to receive connections.");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability Disabled. Not able to receive connections.");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d(TAG,  "mBroadcastReceiver2: Connecting...");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d(TAG,  "mBroadcastReceiver2: Connected");
                        break;
                }
            }
        }

    };
    private BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG,"OnReceive: ACTION FOUND.");

            if (action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mBTDevices.add(device);
                Log.d(TAG,"onReceive: " + device.getName() + ":" + device.getAddress());
                mDeviceListAdapter = new DeviceListAdapter(context,R.layout.activity_discover_device, mBTDevices);
                IvNewDevices.setAdapter(mDeviceListAdapter);
            }
        }
    };


    @Override
    protected void onDestroy(){
        Log.d(TAG,"onDestroy: called.");
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        Button btOnOff=(Button) findViewById(R.id.btOnOff);
        btnEnableDisable_Discoverable = (Button) findViewById(R.id.btnDiscoverable_on_off);
        IvNewDevices = (ListView) findViewById(R.id.IvNewDevices);
        mBTDevices = new ArrayList<>();

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        btOnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"onClick: enabling/disabling bluetooth.");
                enableDisableBt();
            }
        });

    }

    public void enableDisableBt(){

          if(mBluetoothAdapter == null){
              Log.d(TAG,"enableDisableBt: Does not have BT capabilities.");
          }
          if(mBluetoothAdapter.isEnabled()){
              Log.d(TAG,"isEnabled: Enabling BT");
              Intent enableBtIntent=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
              startActivity(enableBtIntent);

              IntentFilter BtIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
              registerReceiver(mBroadcastReceiver1, BtIntent);
          }
          if(mBluetoothAdapter.isEnabled()){
              Log.d(TAG,"isEnabled: Disabling BT");
              mBluetoothAdapter.disable();

              IntentFilter BtIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
              registerReceiver(mBroadcastReceiver1, BtIntent);
          }
    }

     public void btnEnableDisable_Discoverable(View view){
        Log.d(TAG,"btnEnableDisable_Discoverable: Making device discoverable for 300 seconds");

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,300);
        startActivity(discoverableIntent);

         IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
         registerReceiver(mBroadcastReceiver2, intentFilter);
    }

    public void btnDiscover(View view) {
        Log.d(TAG,"btnDiscover: Looking for unpaired devices.");

        if(mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.cancelDiscovery();
            Log.d(TAG,"btnDiscover: Cancel discovery.");

            checkBTpermissions();

            mBluetoothAdapter.startDiscovery();

            IntentFilter discoverDeviceIntent= new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3,discoverDeviceIntent);
        }
        if(!mBluetoothAdapter.isDiscovering()){
            checkBTpermissions();

            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDeviceIntent= new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3,discoverDeviceIntent);
        }
    }

    private void checkBTpermissions() {
        if(Build.VERSION.SDK_INT>Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck= this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");

            if(permissionCheck != 0){
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},1000);
            }
            else{
                Log.d(TAG,"check:BTPermission: No need to check permissions.SDK version < LOLLIPOP.");
            }
        }
    }
}
