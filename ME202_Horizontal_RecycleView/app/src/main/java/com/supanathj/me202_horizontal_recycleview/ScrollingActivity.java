package com.supanathj.me202_horizontal_recycleview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

public class ScrollingActivity extends AppCompatActivity {

    private RecyclerView vertical_recycler_view,horizontal_recycler_view;
    private ArrayList<String> horizontalList,verticalList;
    private HorizontalAdapter horizontalAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        horizontal_recycler_view= (RecyclerView) findViewById(R.id.horizontal_recycler_view);

        horizontalList=new ArrayList<>();
        horizontalList.add("horizontal 1");
        horizontalList.add("horizontal 2");
        horizontalList.add("horizontal 3");
        horizontalList.add("horizontal 4");
        horizontalList.add("horizontal 5");
        horizontalList.add("horizontal 6");
        horizontalList.add("horizontal 7");
        horizontalList.add("horizontal 8");
        horizontalList.add("horizontal 9");
        horizontalList.add("horizontal 10");


        horizontalAdapter=new HorizontalAdapter(horizontalList);

        LinearLayoutManager horizontalLayoutManagaer
                = new LinearLayoutManager(ScrollingActivity.this, LinearLayoutManager.HORIZONTAL, false);
        horizontal_recycler_view.setLayoutManager(horizontalLayoutManagaer);

        horizontal_recycler_view.setAdapter(horizontalAdapter);
    }



    public class HorizontalAdapter extends RecyclerView.Adapter<HorizontalAdapter.MyViewHolder> {

        private List<String> horizontalList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView txtView;
            public ImageView imageView;

            public MyViewHolder(View view) {
                super(view);
                  imageView = (ImageView) view.findViewById(R.id.imageView);
//                txtView = (TextView) view.findViewById(R.id.txtView);

            }
        }


        public HorizontalAdapter(List<String> horizontalList) {
            this.horizontalList = horizontalList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.content_scrolling, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position) {
            if(position%3 == 0){
                holder.imageView.setImageResource(R.drawable.item1);
            }
            else if(position%3 == 1){
                holder.imageView.setImageResource(R.drawable.item2);
            }
            else{
                holder.imageView.setImageResource(R.drawable.item3);
            }
//            holder.txtView.setText(horizontalList.get(position));
//
//            holder.txtView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Toast.makeText(ScrollingActivity.this,holder.txtView.getText().toString(),Toast.LENGTH_SHORT).show();
//                }
//            });
        }

        @Override
        public int getItemCount() {
            return horizontalList.size();
        }
    }
}