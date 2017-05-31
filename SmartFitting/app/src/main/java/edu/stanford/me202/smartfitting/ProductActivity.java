package edu.stanford.me202.smartfitting;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ProductActivity extends AppCompatActivity implements ItemClickListener {
    private final static String BLETAG = "Bluetooth";
    private final static String TAG = ProductActivity.class.getSimpleName();

    private static final String room = "1";
    private static final String[] sizes = {"S","M","L","XL"};

    private ArrayList<String> imageURLArray;
    private ArrayList<String> imageKeyArray;
    private HorizontalAdapter adapter;
    private DatabaseReference mDatabase;

    private String rfid;
    private Stock stock;
    private Catalog catalog;
    private String imageURL;
    private String productNum;

    private Dialog scanDialog;
    private TextView scandialog_hint1;
    private TextView scandialog_hint2;
    private Button laterBtn;

    private Typeface ralewaylight;
    private Typeface ralewaysemibold;

    private BluetoothLEService bleService;
    private String bledata = "";

    @BindView(R.id.horizontal_recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.product_image)
    ImageView imageView;
    @BindView(R.id.scanAnother)
    Button scanAnother;
    @BindView(R.id.request_button)
    Button requestButton;
    @BindView(R.id.text_product_name)
    TextView nameText;
    @BindView(R.id.text_product_price)
    TextView priceText;

    @BindView(R.id.color_menu)
    FloatingActionMenu colorMenu;
    @BindView(R.id.color_button1)
    FloatingActionButton colorButton1;
    @BindView(R.id.color_button2)
    FloatingActionButton colorButton2;
    @BindView(R.id.color_button3)
    FloatingActionButton colorButton3;
    private ArrayList<FloatingActionButton> colorButtonArray;

    @BindView(R.id.size_menu)
    FloatingActionMenu sizeMenu;
    @BindView(R.id.size_button1)
    FloatingActionButton sizeButton1;
    @BindView(R.id.size_button2)
    FloatingActionButton sizeButton2;
    @BindView(R.id.size_button3)
    FloatingActionButton sizeButton3;
    @BindView(R.id.size_button4)
    FloatingActionButton sizeButton4;
    private ArrayList<FloatingActionButton> sizeButtonArray;

    @BindView(R.id.sizeText)
    TextView productSize;
    @BindView(R.id.colorText)
    TextView productColor;

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

                // dismiss dialog
                scanDialog.dismiss();

                if (bledata.equals("e")){
                    // unbind BLE Service
                    LocalBroadcastManager.getInstance(ProductActivity.this).unregisterReceiver(BluetoothReceiver);
                    unbindService(mServiceConnection);
                    bleService = null;

                    // go back to welcome activity
                    Intent intent1 = new Intent(ProductActivity.this, WelcomeActivity.class);
                    startActivity(intent1);
                } else {
                    Log.d(BLETAG, "Refreshing page with data: " + bledata);
                    // audio feedback
                    MediaPlayer rfidBeep = MediaPlayer.create(ProductActivity.this, R.raw.beep);
                    rfidBeep.start();

                    rfid = new String(new char[8]).replace("\0", bledata);
                    mDatabase.child("stockroom").child(rfid).addListenerForSingleValueEvent(readStockListener);
                }
            }

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
        setContentView(R.layout.activity_product);
        ButterKnife.bind(this);

        // configure fonts
        ralewaylight = Typeface.createFromAsset(getAssets(), "fonts/ralewaylight.ttf");
        ralewaysemibold = Typeface.createFromAsset(getAssets(), "fonts/ralewaysemibold.ttf");
        // create scanDialog object
        scanDialog = new Dialog(ProductActivity.this);

        // interpret rfid number passed in
        productNum = getIntent().getStringExtra(WelcomeActivity.PRODUCTNUM);
        Log.d(TAG, "IntentExtra from WelcomeActivity: " + productNum);
        rfid = new String(new char[8]).replace("\0", productNum);
        Log.d(TAG, "RFID tag number is now " + rfid);

        imageURLArray = new ArrayList<>();
        imageKeyArray = new ArrayList<>();

        colorButtonArray = new ArrayList<>();
        colorButtonArray.add(colorButton1);
        colorButtonArray.add(colorButton2);
        colorButtonArray.add(colorButton3);

        sizeButtonArray = new ArrayList<>();
        sizeButtonArray.add(sizeButton1);
        sizeButtonArray.add(sizeButton2);
        sizeButtonArray.add(sizeButton3);
        sizeButtonArray.add(sizeButton4);

        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // configure BLE
        /* Service start: this service will live and die with this activity; unless we
           control the lifecycle of this this by using startService(intent), stopService(intent)
         */
        Intent intent = new Intent(ProductActivity.this,BluetoothLEService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);

        IntentFilter filter = new IntentFilter(bleService.ACTION_DATA_AVAILABLE);
        filter.addAction(bleService.ACTION_GATT_CONNECTED );
        filter.addAction(bleService.ACTION_GATT_DISCONNECTED );

        LocalBroadcastManager.getInstance(ProductActivity.this).registerReceiver(BluetoothReceiver,filter);

        LinearLayoutManager manager = new LinearLayoutManager(ProductActivity.this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(manager);

        adapter = new HorizontalAdapter(imageURLArray, this);
        adapter.setItemClickListener(this);
        recyclerView.setAdapter(adapter);

        mDatabase.child("stockroom").child(rfid).addListenerForSingleValueEvent(readStockListener);

        scanAnother.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // connect to BLE once entering this activity
                String scannerID = "D5:3C:ED:8E:F2:08";
                // Initialize bluetooth service
                bleService.initialize();
                // Try to connect to bluetooth of this address
                bleService.connect(scannerID);

                // send flag
                String string = "s";
                byte[] b = string.getBytes();
                bleService.writeRXCharacteristic(b);
                Log.d(BLETAG, "Sent letter S flag");

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
                    }
                });
            }
        });

        requestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ProductActivity.this);
                builder.setTitle(R.string.requestconfirm);
                builder.setIcon(R.drawable.request);
                builder.setCancelable(false);
                // show customers their request confirmation
                builder.setMessage(catalog.getName() + "\nSize: " + stock.getSize() + "\nColor: " + stock.getColor());

                builder.setPositiveButton("CONFIRM", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // request via Firebase
                        CustomerRequest request = new CustomerRequest(room, stock.getCatalog(), stock.getColor(), stock.getSize(), imageURL);
                        mDatabase.child("request").push().setValue(request).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(ProductActivity.this, "Request Sent", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                }).show();


            }
        });

        colorMenu.setOnMenuButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (colorMenu.isOpened()){
                    colorMenu.close(true);
                } else{
                    for (int i = 0; i < colorButtonArray.size(); i++) {
                        colorButtonArray.get(i).setVisibility(View.VISIBLE);
                    }
                    int[] colorCode = catalog.getColorCode();
                    int difference = colorButtonArray.size() - colorCode.length;
                    if (difference > 0) {
                        for (int i = 0; i < difference; i++) {
                            colorButtonArray.get(colorButtonArray.size() - i - 1).setVisibility(View.GONE);
                        }
                    }
                    colorMenu.open(true);
                }
                if (sizeMenu.isOpened()){
                    sizeMenu.close(true);
                }
            }
        });

        sizeMenu.setOnMenuButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sizeMenu.isOpened()){
                    sizeMenu.close(true);
                } else{
                    sizeMenu.open(true);
                }
                if (colorMenu.isOpened()){
                    colorMenu.close(true);
                }
            }
        });
    }

    ValueEventListener readStockListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            stock = dataSnapshot.getValue(Stock.class);
            productSize.setText(stock.getSize());
            productColor.setText(stock.getColor());
            mDatabase.child("catalog").child(stock.getCatalog()).addListenerForSingleValueEvent(readCatalogListener);

            for (int i = 0; i < sizeButtonArray.size(); i++) {
                final int n = i;
                sizeButtonArray.get(i).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sizeMenu.close(true);
                        String size = sizes[n];
                        stock.setSize(size);
                        productSize.setText(size);
                    }
                });
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            // Getting Post failed, log a message
            Log.w(TAG, "readStock:onCancelled", databaseError.toException());
            // ...
        }
    };

    ValueEventListener readCatalogListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            catalog = dataSnapshot.getValue(Catalog.class);
            GenericTypeIndicator<List<String>> list = new GenericTypeIndicator<List<String>>() {};
            catalog.setColors(dataSnapshot.child("color").getValue(list));
            GenericTypeIndicator<Map<String, String>> map = new GenericTypeIndicator<Map<String, String>>() {};
            catalog.setImageURLs(dataSnapshot.child("image").getValue(map));
            mDatabase.child("recommendation").child(catalog.getType()).child(stock.getColor()).addListenerForSingleValueEvent(readRecommendationListener);

            nameText.setText(catalog.getName());
            priceText.setText("$" + String.format("%.2f", catalog.getPrice()));

            int[] colorCode = catalog.getColorCode();
            for (int i = 0; i < colorCode.length; i++) {
                colorButtonArray.get(i).setColorNormal(colorCode[i]);
                final int n = i;
                colorButtonArray.get(i).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        colorMenu.close(true);
                        String color = catalog.getColors().get(n);
                        stock.setColor(color);
                        productColor.setText(color);
                        mDatabase.child("recommendation").child(catalog.getType()).child(stock.getColor()).addListenerForSingleValueEvent(readRecommendationListener);
                    }
                });
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            // Getting Post failed, log a message
            Log.w(TAG, "readCatalog:onCancelled", databaseError.toException());
            // ...
        }
    };

    ValueEventListener readRecommendationListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            imageURL = catalog.getImageURLs().get(stock.getColor());
            Picasso.with(ProductActivity.this)
                    .load(imageURL)
                    .into(imageView);
            imageURLArray = new ArrayList<>();
            imageKeyArray = new ArrayList<>();
            for (DataSnapshot recommendationSnapshot: dataSnapshot.getChildren()) {
                if (!recommendationSnapshot.getKey().equals(stock.getKey())) {
                    imageURLArray.add(recommendationSnapshot.getValue().toString());
                    imageKeyArray.add(recommendationSnapshot.getKey());
                }
            }
            adapter.data = imageURLArray;
            adapter.notifyDataSetChanged();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            // Getting Post failed, log a message
            Log.w(TAG, "readRecommendation:onCancelled", databaseError.toException());
            // ...
        }
    };

    @Override
    public void onClick(View view, final int position) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog);

        ImageView imagePreview = (ImageView) dialog.findViewById(R.id.image_dialog);
        Picasso.with(this)
                .load(imageURLArray.get(position))
                .into(imagePreview);

        Stock selected = new Stock(imageKeyArray.get(position), stock.getSize());
        mDatabase.child("catalog").child(selected.getCatalog()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Catalog selectedCatalog = dataSnapshot.getValue(Catalog.class);
                TextView productName = (TextView) dialog.findViewById(R.id.text_dialog_name);
                productName.setText(selectedCatalog.getName());
                TextView productPrice = (TextView) dialog.findViewById(R.id.text_dialog_price);
                productPrice.setText("$" + String.format("%.2f", selectedCatalog.getPrice()));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "seeDetail:onCancelled", databaseError.toException());
            }
        });

        Button detailsButton = (Button) dialog.findViewById(R.id.button_dialog_details);
        detailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                stock = new Stock(imageKeyArray.get(position), stock.getSize());
                mDatabase.child("catalog").child(stock.getCatalog()).addListenerForSingleValueEvent(readCatalogListener);
            }
        });
        dialog.show();
    }
}