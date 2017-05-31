package edu.stanford.me202.smartfitting;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.dd.CircularProgressButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WelcomeActivity extends AppCompatActivity {
    private final static String BLETAG = "Bluetooth";
    private final static String TAG = WelcomeActivity.class.getSimpleName();
    public final static String PRODUCTNUM = "productNum";

    private static final String email = "jangwon90@gmail.com";
    private static final String password = "12345678";

    private FirebaseAuth mAuth;

    private BluetoothLEService bleService;
    private String bledata = "";

    private TextView scandialog_hint1;
    private TextView scandialog_hint2;
    private Button laterBtn;

    private Typeface ralewaylight;
    private Typeface ralewaysemibold;

    private Dialog scanDialog;

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
                // get data from BLE module
                bledata = intent.getStringExtra(bleService.EXTRA_DATA);
                Log.d(BLETAG, "Raw data: " + bledata);

                // TODO: decisions when receiving data
                if (bledata.equals("e")) {
                    // first disconnect BLE Service
                    bleService.disconnect();

                    if (scanButton.getProgress() != 0) {
                        scanDialog.dismiss();
                        scanButton.setProgress(-1);
                        scanButton.setProgress(0);
                    }
                } else {
                    scanDialog.dismiss();
                    scanButton.setProgress(100);

                    // unbind BLE Service
                    LocalBroadcastManager.getInstance(WelcomeActivity.this).unregisterReceiver(BluetoothReceiver);
                    unbindService(mServiceConnection);
                    bleService = null;

                    // interpret product number and jump to product activity
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(WelcomeActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.d(TAG, "signInWithEmail:success");
                                        Intent intent = new Intent(WelcomeActivity.this, ProductActivity.class);
                                        Log.d(TAG, "Sending IntentExtra [" + bledata + "] to ProductActivity!");
                                        intent.putExtra(PRODUCTNUM, bledata);
                                        startActivity(intent);
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                                        Toast.makeText(WelcomeActivity.this, "Authentication failed",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                }
            }
            // TODO: decisions when receiving data

            // Bluetooth Connected
            if (intent.getAction().equals(bleService.ACTION_GATT_CONNECTED)){
                Log.d(BLETAG, "Bluetooth Service is connected");
            }

            // Bluetooth Disconnected
            if (intent.getAction().equals(bleService.ACTION_GATT_DISCONNECTED)){
                Log.d(BLETAG, "Bluetooth Service is disconnected");
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        ButterKnife.bind(this);
        // get an instance for Firebase
        mAuth = FirebaseAuth.getInstance();

        // display welcome text
        ralewaylight = Typeface.createFromAsset(getAssets(), "fonts/ralewaylight.ttf");
        ralewaysemibold = Typeface.createFromAsset(getAssets(), "fonts/ralewaysemibold.ttf");
        welcomeText.setTypeface(ralewaylight);
        uniqloText.setTypeface(ralewaysemibold);
        enjoyText.setTypeface(ralewaylight);

        // set welcomeText for connecting BLE before sending data
        welcomeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // connect to BLE once entering this activity
                String scannerID = "D5:3C:ED:8E:F2:08";
                // Initialize bluetooth service
                bleService.initialize();
                // Try to connect to bluetooth of this address
                bleService.connect(scannerID);
            }
        });

        scanDialog = new Dialog(WelcomeActivity.this);

        // set up scanButton functionality
        scanButton.setProgress(0);
        scanButton.setIndeterminateProgressMode(true);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanButton.setProgress(50);

                // TODO: CONNECT BLE WHEN PRESSING START but No s TO SEND
                // connect to BLE once entering this activity
                String scannerID = "D5:3C:ED:8E:F2:08";
                // Initialize bluetooth service
                bleService.initialize();
                // Try to connect to bluetooth of this address
                bleService.connect(scannerID);
                // TODO: CONNECT BLE WHEN PRESSING START

                // show the dialog
                scanDialog.setContentView(R.layout.dialog_rfid);
                scanDialog.setCancelable(false);
                scanDialog.show();
                Window window = scanDialog.getWindow();
                window.setLayout(450, 450);
                // set font for texts
                scandialog_hint1 = (TextView) scanDialog.findViewById(R.id.scandialog_hint1);
                scandialog_hint2 = (TextView) scanDialog.findViewById(R.id.scandialog_hint2);
                scandialog_hint1.setTypeface(ralewaysemibold);
                scandialog_hint2.setTypeface(ralewaylight);
                // set button for later scanning
                laterBtn = (Button) scanDialog.findViewById(R.id.laterBtn);
                laterBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        scanDialog.dismiss();
                        scanButton.setProgress(0);
                    }
                });

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // TODO: COMMENT THIS PART TO DISABLE BLE
        /* Service start: this service will live and die with this activity; unless we
           control the lifecycle of this this by using startService(intent), stopService(intent)
         */
        Intent intent = new Intent(WelcomeActivity.this,BluetoothLEService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);

        IntentFilter filter = new IntentFilter(bleService.ACTION_DATA_AVAILABLE);
        filter.addAction(bleService.ACTION_GATT_CONNECTED );
        filter.addAction(bleService.ACTION_GATT_DISCONNECTED );

        LocalBroadcastManager.getInstance(this).registerReceiver(BluetoothReceiver,filter);
        // re-initialize the button and dismiss dialog
        scanDialog.dismiss();
        scanButton.setProgress(0);
        // TODO: COMMENT THIS PART TO DISABLE BLE
    }

//    @Override
//    protected void onStop() {
//        super.onStop();
//        LocalBroadcastManager.getInstance(this).unregisterReceiver(BluetoothReceiver);
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        unbindService(mServiceConnection);
//        bleService = null;
//    }
}