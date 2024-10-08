package com.example.shopease;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

    private Context context;
    private List<DataClass> dataList;

    public MyAdapter(Context context, List<DataClass> dataList) {
        this.context = context;
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Glide.with(context).load(dataList.get(position).getImagemProduto()).into(holder.recImage);
        holder.recNome.setText(dataList.get(position).getNomeProduto());
        holder.recQuantidade.setText(dataList.get(position).getQuantidadeProduto());
        holder.recCategoria.setText(dataList.get(position).getCategoria());

        holder.recCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, DetailActivity.class);
                intent.putExtra("Image", dataList.get(holder.getAbsoluteAdapterPosition()).getImagemProduto());
                intent.putExtra("NomeProduto", dataList.get(holder.getAbsoluteAdapterPosition()).getNomeProduto());
                intent.putExtra("QuantidadeProduto", dataList.get(holder.getAbsoluteAdapterPosition()).getQuantidadeProduto());
                intent.putExtra("NomeUsuario", dataList.get(holder.getAbsoluteAdapterPosition()).getNomeUsuario());
                intent.putExtra("key", dataList.get(holder.getAbsoluteAdapterPosition()).getKey());

                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public void searchDataList(ArrayList<DataClass> searchList){
        dataList = searchList;
        notifyDataSetChanged();
    }
}

class MyViewHolder extends RecyclerView.ViewHolder{

    ImageView recImage;
    TextView recNome, recQuantidade, recCategoria;
    CardView recCard;
    public MyViewHolder(@NonNull View itemView){
        super(itemView);

        recImage = itemView.findViewById(R.id.recImage);
        recCard = itemView.findViewById(R.id.recCard);
        recNome = itemView.findViewById(R.id.recNome);
        recQuantidade = itemView.findViewById(R.id.recQuantidade);
        recCategoria = itemView.findViewById(R.id.recCategoria);

    }
}