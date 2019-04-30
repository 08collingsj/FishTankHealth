package embeddedsystems.watertankcompanion;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 0;
    private static final int REQUEST_DISCOVER_BT = 0;
    private Date lastConnection;
    private ProgressDialog progress;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    String address = null;
    TextView mStatusBlueTv, mPairedTv;
    ImageView mBlueIv;
    Button mOnBtn, mOffBtn, mDiscoverBtn, mPairedBtn;
    BluetoothAdapter mBlueAdapter;
    BluetoothSocket mBlueSocket;

    private boolean isBtConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent newint = getIntent();
        address = newint.getStringExtra(DeviceList.EXTRA_ADDRESS); //receive the address of the bluetooth device
        setContentView(R.layout.activity_main);

        mStatusBlueTv = (TextView) findViewById(R.id.statusBluetoothTv);
        mPairedTv = (TextView) findViewById(R.id.pairedTV);
        mBlueIv = (ImageView) findViewById(R.id.bluetoothIv);
        mOnBtn = (Button) findViewById(R.id.onBtn);
        mOffBtn = (Button) findViewById(R.id.offBtn);
        mDiscoverBtn = (Button) findViewById(R.id.discoverableBtn);
        mPairedBtn = (Button) findViewById(R.id.pairedBtn);

        mBlueAdapter = BluetoothAdapter.getDefaultAdapter();
        new ConnectBt().execute();
        //check if bluetooth is available
        if (mBlueAdapter == null)
            mStatusBlueTv.setText("BlueTooth is not available");
        else
            mStatusBlueTv.setText("BlueTooth is not available");

        //set bluetooth state image
        if (mBlueAdapter.isEnabled()){
            mBlueIv.setImageResource(R.drawable.ic_action_on);
        }
        else
            mBlueIv.setImageResource(R.drawable.ic_action_off);

        mOnBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if (!mBlueAdapter.isEnabled()){
                    showToast("Turning on BlueTooth");
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intent, REQUEST_ENABLE_BT);
                }
                else
                    showToast("Bluetooth is already on");
            }
        });

        mDiscoverBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if (!mBlueAdapter.isDiscovering())
                {
                    showToast("Making device discoverable");
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    startActivityForResult(intent, REQUEST_DISCOVER_BT);
                }
            }
        });

        mOffBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if (mBlueAdapter.isEnabled())
                {
                    mBlueAdapter.disable();
                    showToast("Turning bluetooth off");
                    mBlueIv.setImageResource(R.drawable.ic_action_off);
                }
                else
                    showToast("Bluetooth is off.");
            }
        });

        mPairedBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                if (mBlueAdapter.isEnabled())
                {
                    mPairedTv.setText("Paired devices");
                    Set<BluetoothDevice> devices = mBlueAdapter.getBondedDevices();
                    for (BluetoothDevice device: devices)
                    {
                        mPairedTv.append("\nDevice: " + device.getName() + ", " + device);
                    }
                }
                else{
                    showToast("Turn on bluetooth to get paired devices");
            }
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        switch (requestCode){
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK)
                {
                    mBlueIv.setImageResource(R.drawable.ic_action_on);
                    showToast("Bluetooth is on");
                }
                else
                {
                    showToast("Bluetooth cant be turned on");
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    private void showToast(String msg) {
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
    }
    private class ConnectBt extends AsyncTask<Void, Void, Void>
    {
        private boolean ConnectSuccess = true;

        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(MainActivity.this, "Connecting...", "Please wait.");
        }
        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try
            {
                if (mBlueSocket == null || !isBtConnected)
                {
                    mBlueAdapter = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice dispositivo = mBlueAdapter.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    mBlueSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    mBlueSocket.connect();//start connection
                }
            }
            catch (IOException e)
            {
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess)
            {
                showToast("Connection Failed. Is it a SPP Bluetooth? Try again.");
                finish();
            }
            else
            {
                showToast("Connected.");
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }
}
/*
1 - check if bluetooth is available
 */