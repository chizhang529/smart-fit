package com.example.jangwonsuh.smartfittingmerchant;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private ArrayList<CustomerRequest> data;
    private ArrayList<String> dataKey;

    private DatabaseReference mDatabase;
    private MerchantRecyclerAdapter adapter;
    private Vibrator vibrator;
    private ProgressDialog progressDialog;

    @BindView(R.id.my_recycler_view)
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        data = new ArrayList<>();
        dataKey = new ArrayList<>();

        mDatabase = FirebaseDatabase.getInstance().getReference();
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        progressDialog = new ProgressDialog(MainActivity.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LinearLayoutManager manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);

        adapter = new MerchantRecyclerAdapter(data,this);
        recyclerView.setAdapter(adapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        mDatabase.child("request").addValueEventListener(readRequestListener);
    }

    ValueEventListener readRequestListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            dataKey = new ArrayList<>();
            data = new ArrayList<>();
            for (DataSnapshot requestSnapshot: dataSnapshot.getChildren()) {
                dataKey.add(requestSnapshot.getKey());
                data.add(requestSnapshot.getValue(CustomerRequest.class));
            }
            if(data.size() > adapter.data.size()) {
                Toast.makeText(MainActivity.this, getString(R.string.toast_main_new_request), Toast.LENGTH_SHORT).show();
                vibrator.vibrate(400);
            }
            adapter.data = data;
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
    protected void onDestroy() {
        super.onDestroy();
        mDatabase.child("request").removeEventListener(readRequestListener);
    }

    ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(final RecyclerView.ViewHolder viewHolder, int swipeDir) {
//            Show dialog when row is swiped to delete or cancel.
            new AlertDialog.Builder(MainActivity.this)
                    .setMessage(getString(R.string.dialog_main_delete_message))
                    .setPositiveButton(getString(R.string.button_dialog_accept), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            progressDialog.setMessage(getString(R.string.text_progress_wait)); // Setting Message
                            progressDialog.setTitle(getString(R.string.text_progress_deleting)); // Setting Title
                            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); // Progress Dialog Style Spinner
                            progressDialog.show(); // Display Progress Dialog
                            progressDialog.setCancelable(false);
                            String key = dataKey.get(viewHolder.getLayoutPosition());
                            mDatabase.child("request").child(key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    progressDialog.dismiss();
                                    if (task.isSuccessful()) {
                                        Toast.makeText(MainActivity.this, getString(R.string.toast_main_deleted), Toast.LENGTH_SHORT).show();
                                    } else {
                                        Log.w(TAG, "deleteRide:failure", task.getException());
                                        Toast.makeText(MainActivity.this, getString(R.string.toast_main_delete_failed), Toast.LENGTH_SHORT).show();
                                        vibrator.vibrate(400);
                                    }
                                }
                            });
                        }
                    })
                    .setNegativeButton(getString(R.string.button_dialog_cancel), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            adapter.notifyDataSetChanged();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
    };
}
