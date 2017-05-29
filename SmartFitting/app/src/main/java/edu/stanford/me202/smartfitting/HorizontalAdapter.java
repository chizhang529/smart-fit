package edu.stanford.me202.smartfitting;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Jang Won Suh on 5/28/2017.
 */

public class HorizontalAdapter extends RecyclerView.Adapter<HorizontalAdapter.MyViewHolder> {

    ArrayList<String> data;
    private Context ctx;

    public HorizontalAdapter(ArrayList<String> data, Context ctx) {
        this.data = data;
        this.ctx = ctx;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.imageView)
        public ImageView imageView;

        public MyViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.content_scrolling, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        Picasso.with(ctx)
                .load(data.get(position))
                .into(holder.imageView);

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
        return data.size();
    }
}
