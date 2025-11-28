package es.ucm.fdi.pad.collabup.modelo.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import es.ucm.fdi.pad.collabup.R;
import es.ucm.fdi.pad.collabup.modelo.CollabItem;

public class CollabItemAdapter extends RecyclerView.Adapter<CollabItemAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(CollabItem item);
    }

    private List<CollabItem> items;
    private OnItemClickListener listener;

    public CollabItemAdapter(List<CollabItem> items) {
        this.items = items;
    }

    public CollabItemAdapter(List<CollabItem> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void setItems(List<CollabItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    public void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.collab_item_detail_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        CollabItem item = items.get(position);
        holder.tvNombre.setText(item.getNombre());
        if (item.getDescripcion() != null) {
            holder.tvDescripcion.setText(item.getDescripcion());
            holder.tvDescripcion.setVisibility(View.VISIBLE);
        } else {
            holder.tvDescripcion.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
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
}
