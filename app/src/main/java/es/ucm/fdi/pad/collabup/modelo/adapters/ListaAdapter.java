package es.ucm.fdi.pad.collabup.modelo.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import es.ucm.fdi.pad.collabup.R;
import es.ucm.fdi.pad.collabup.modelo.CollabItem;

/**
 * Adaptador específico para la CollabView Lista
 */
public class ListaAdapter extends RecyclerView.Adapter<ListaAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(CollabItem item);
    }

    private List<CollabItem> items;
    private OnItemClickListener listener;

    public ListaAdapter(List<CollabItem> items) {
        this.items = items;
    }

    public void setItems(List<CollabItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.collab_item_detail_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CollabItem item = items.get(position);
        holder.tvNombre.setText(item.getNombre());
        if (item.getDescripcion() != null && !item.getDescripcion().isEmpty()) {
            holder.tvDescripcion.setText(item.getDescripcion());
            holder.tvDescripcion.setVisibility(View.VISIBLE);
        } else {
            holder.tvDescripcion.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre;
        TextView tvDescripcion;

        public ViewHolder(View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvItemNombre);
            tvDescripcion = itemView.findViewById(R.id.tvItemDescripcion);
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}

