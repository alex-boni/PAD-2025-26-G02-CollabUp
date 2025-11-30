package es.ucm.fdi.pad.collabup.modelo.adapters;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import es.ucm.fdi.pad.collabup.R;
import es.ucm.fdi.pad.collabup.modelo.Collab;
import es.ucm.fdi.pad.collabup.modelo.interfaz.OnCollabClickListener;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {

    private List<Collab> items;
    private final OnCollabClickListener listener;

    public CardAdapter(List<Collab> items, OnCollabClickListener listener) {
        this.listener = listener;
        this.items = items;
    }

    public static class CardViewHolder extends RecyclerView.ViewHolder {
        public ImageView image;
        public ImageView ivFavorite;
        public TextView title;
        public TextView description;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.cardImage);
            title = itemView.findViewById(R.id.cardTitle);
            description = itemView.findViewById(R.id.cardDescription);
            ivFavorite = itemView.findViewById(R.id.ivFavorite);
        }
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.collab_card_layout, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        Collab item = items.get(position);

        if (item.getImageUri() != null && !item.getImageUri().isEmpty()) {
            try {
                Glide.with(holder.itemView.getContext()).load(item.getImageUri()).into(holder.image);
            } catch (SecurityException e) {
                holder.image.setImageResource(R.drawable.logo);
            }
        } else {
            holder.image.setImageResource(R.drawable.logo);
        }
        if (item.esFavorito()) {
            holder.ivFavorite.setImageResource(R.drawable.ic_favorite_filled);
        } else {
            holder.ivFavorite.setImageResource(R.drawable.ic_favorite);
        }
        holder.ivFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    int actualPosition = holder.getBindingAdapterPosition();
                    listener.onFavoriteClick(item, actualPosition);
                }
            }
        });
        holder.title.setText(item.getNombre());
        holder.description.setText(item.getDescripcion());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    // Llamar al listener y pasar el objeto 'item' (el Collab actual)
                    listener.onCollabClick(item);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addItem(Collab item) {
        items.add(0, item);
        notifyItemInserted(0);
    }
}

