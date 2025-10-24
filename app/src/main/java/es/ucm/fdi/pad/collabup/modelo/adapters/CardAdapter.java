package es.ucm.fdi.pad.collabup.modelo.adapters;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import es.ucm.fdi.pad.collabup.R;
import es.ucm.fdi.pad.collabup.modelo.Collab;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {

    private List<Collab> items;

    public CardAdapter(List<Collab> items) {
        this.items = items;
    }

    public static class CardViewHolder extends RecyclerView.ViewHolder {
        public ImageView image;
        public TextView title;
        public TextView description;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.cardImage);
            title = itemView.findViewById(R.id.cardTitle);
            description = itemView.findViewById(R.id.cardDescription);
        }
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_collab, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        Collab item = items.get(position);
        
        if (item.getImageUri() != null && !item.getImageUri().isEmpty()) {
            holder.image.setImageURI(Uri.parse(item.getImageUri()));
        } else {
            holder.image.setImageResource(R.drawable.ic_launcher_foreground);
        }
        
        holder.title.setText(item.getNombre());
        holder.description.setText(item.getDescripcion());
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

