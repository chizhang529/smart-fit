package edu.stanford.me202.smartfitting;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.icu.util.TimeUnit;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.dd.CircularProgressButton;
//import com.geniusforapp.fancydialog.FancyAlertDialog;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WelcomeActivity extends AppCompatActivity {
    private final static String BLETAG = "Bluetooth";
    private final static String TAG = WelcomeActivity.class.getSimpleName();

    private BluetoothLEService bleService;
    private String bledata = "";

    @BindView(R.id.welcometext)
    TextView welcomeText;
    @BindView(R.id.uniqlotext)
    TextView uniqloText;
    @BindView(R.id.enjoyText)
    TextView enjoyText;
    @BindView(R.id.scanButton)
    CircularProgressButton scanButton;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BluetoothLEService.LocalBinder binder = (BluetoothLEService.LocalBinder)service;
            bleService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bleService = null;
        }
    };

    public BroadcastReceiver BluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(bleService.ACTION_DATA_AVAILABLE)){
                bledata += intent.getStringExtra(bleService.EXTRA_DATA);

                if (bledata.endsWith("@")) {
                    Log.d(BLETAG, "Raw data: " + bledata);
                    String delim = "@";
                    String dataInfo = bledata.split(delim)[0];

                    // reset data string
                    bledata = "";
                }
            }

            // Bluetooth Connected
            if (intent.getAction().equals(bleService.ACTION_GATT_CONNECTED)){
                // TODO: if needed, tell Arduino that BLE is connected
                Log.d(BLETAG, "CONNECTED");
                String string = "Good check: connected!";
                byte[] b = string.getBytes();
                bleService.writeRXCharacteristic(b);
            }

            // Bluetooth Disconnected
            if (intent.getAction().equals(bleService.ACTION_GATT_DISCONNECTED)){

            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        ButterKnife.bind(this);

//        // remove title bar
//        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        // remove notification bar
//        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        // set content view AFTER above sequence (to avoid crash)
//        this.setContentView(R.layout.activity_welcome);

        // display welcome text
        Typeface ralewaylight = Typeface.createFromAsset(getAssets(), "fonts/ralewaylight.ttf");
        Typeface ralewaysemibold = Typeface.createFromAsset(getAssets(), "fonts/ralewaysemibold.ttf");
        welcomeText.setTypeface(ralewaylight);
        uniqloText.setTypeface(ralewaysemibold);
        enjoyText.setTypeface(ralewaylight);

        scanButton.setIndeterminateProgressMode(true);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanButton.setProgress(50);
//                FancyAlertDialog.Builder alert = new FancyAlertDialog.Builder(WelcomeActivity.this)
//                        //.setImageRecourse(R.drawable.success)
//                        .setTextTitle("scan your tag")
//                        .setTitleFont("fonts/ralewaysemibold.ttf")
//                        .setTextSubTitle("please let us know what you are trying on")
//                        .setSubTitleFont("fonts/ralewaylight.ttf")
//                        .setNegativeButtonText("LATER")
//                        .setNegativeColor(R.color.crimson)
//                        .setOnNegativeClicked(new FancyAlertDialog.OnNegativeClicked() {
//                            @Override
//                            public void OnClick(View view, Dialog dialog) {
//                            dialog.dismiss();
//                            scanButton.setProgress(0);
//
//                            String string = "Good check again!";
//                            byte[] b = string.getBytes();
//                            bleService.writeRXCharacteristic(b);
//                            }
//                        })
//                        .build();
//                alert.show();

                String scannerID = "D5:3C:ED:8E:F2:08";
                // Initialize bluetooth service
                bleService.initialize();
                // Try to connect to bluetooth of this address
                bleService.connect(scannerID);

                // TODO: Pesudo-click switch
                Intent intent = new Intent(WelcomeActivity.this, ProductActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        /* Service start: this service will live and die with this activity; unless we
           control the lifecycle of this this by using startService(intent), stopService(intent)
         */
        Intent intent = new Intent(WelcomeActivity.this,BluetoothLEService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);

        IntentFilter filter = new IntentFilter(bleService.ACTION_DATA_AVAILABLE);
        filter.addAction(bleService.ACTION_GATT_CONNECTED );
        filter.addAction(bleService.ACTION_GATT_DISCONNECTED );

        LocalBroadcastManager.getInstance(this).registerReceiver(BluetoothReceiver,filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(BluetoothReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        bleService = null;
    }
}
