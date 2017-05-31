package com.example.jangwonsuh.smartfittingmerchant;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by kilas on 2017/4/11.
 */

public class MerchantRecyclerAdapter extends RecyclerView.Adapter<MerchantRecyclerAdapter.Me202ViewHolder> {

    ArrayList<CustomerRequest> data = new ArrayList<>();
    private Context ctx;

    public MerchantRecyclerAdapter(ArrayList<CustomerRequest> data, Context ctx) {
        this.ctx=ctx;
        this.data=data;
    }

    public static class Me202ViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.text_row_room)
        TextView roomIndex;
        @BindView(R.id.text_row_color)
        TextView color;
        @BindView(R.id.text_row_size)
        TextView size;
        @BindView(R.id.text_row_catalog)
        TextView product_no;
        @BindView(R.id.image_row_product)
        ImageView product_image;

        public Me202ViewHolder(View itemView){
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    @Override
    public Me202ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_main_merchant, parent, false);
        Me202ViewHolder me202ViewHolder = new Me202ViewHolder(v);
        return me202ViewHolder;
    }

    @Override
    public void onBindViewHolder(Me202ViewHolder holder, int position){
        holder.roomIndex.setText(data.get(position).getRoom());
        holder.size.setText(data.get(position).getSize());
        holder.color.setText(data.get(position).getColor());
        holder.product_no.setText(data.get(position).getCatalog());
        Picasso.with(ctx)
                .load(data.get(position).getImageURL())
                .resize(400,400)
                .centerCrop()
                .into(holder.product_image);
    }

    @Override
    public int getItemCount(){
        return data.size();
    }
}
