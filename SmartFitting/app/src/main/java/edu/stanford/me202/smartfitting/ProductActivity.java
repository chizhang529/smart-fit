package edu.stanford.me202.smartfitting;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
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

    private final static String TAG = ProductActivity.class.getSimpleName();

    private static final String rfid = "11111111";
    private static final String room = "1";
    private static final String[] sizes = {"S","M","L","XL"};

    private ArrayList<String> imageURLArray;
    private ArrayList<String> imageKeyArray;
    private HorizontalAdapter adapter;
    private DatabaseReference mDatabase;

    private Stock stock;
    private Catalog catalog;
    private String imageURL;

    @BindView(R.id.horizontal_recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.product_image)
    ImageView imageView;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);

        ButterKnife.bind(this);

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

        LinearLayoutManager manager = new LinearLayoutManager(ProductActivity.this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(manager);

        adapter = new HorizontalAdapter(imageURLArray, this);
        adapter.setItemClickListener(this);
        recyclerView.setAdapter(adapter);

        mDatabase.child("stockroom").child(rfid).addListenerForSingleValueEvent(readStockListener);
        
        requestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
            mDatabase.child("catalog").child(stock.getCatalog()).addListenerForSingleValueEvent(readCatalogListener);

            for (int i = 0; i < sizeButtonArray.size(); i++) {
                final int n = i;
                sizeButtonArray.get(i).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sizeMenu.close(true);
                        stock.setSize(sizes[n]);
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
                        stock.setColor(catalog.getColors().get(n));
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
