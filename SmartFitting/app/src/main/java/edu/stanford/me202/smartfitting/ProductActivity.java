package edu.stanford.me202.smartfitting;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ProductActivity extends AppCompatActivity {

    private final static String TAG = ProductActivity.class.getSimpleName();

    private static final String rfid = "11111111";
    private static final String room = "1";

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
    @BindView(R.id.color_button1)
    FloatingActionButton color_button1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);

        ButterKnife.bind(this);

        imageURLArray = new ArrayList<>();
        imageKeyArray = new ArrayList<>();

        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    protected void onResume() {
        super.onResume();

        LinearLayoutManager manager = new LinearLayoutManager(ProductActivity.this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(manager);

        adapter = new HorizontalAdapter(imageURLArray, this);
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
    }

    ValueEventListener readStockListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            stock = dataSnapshot.getValue(Stock.class);
            mDatabase.child("catalog").child(stock.getCatalog()).addValueEventListener(readCatalogListener);
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
            imageURL = catalog.getImageURLs().get(stock.getColor());
            Picasso.with(ProductActivity.this)
                    .load(imageURL)
                    .into(imageView);
            mDatabase.child("recommendation").child(catalog.getType()).child(stock.getColor()).addValueEventListener(readRecommendationListener);

            color_button1.setColorNormalResId(R.color.Black);

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
}
