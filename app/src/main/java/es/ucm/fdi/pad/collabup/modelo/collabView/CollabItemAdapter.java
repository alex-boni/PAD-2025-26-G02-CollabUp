package es.ucm.fdi.pad.collabup.modelo.collabView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CollabItemAdapter extends RecyclerView.Adapter<CollabItemAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(CollabItem item);
    }

    private List<CollabItem> items;
    private OnItemClickListener listener;

    public CollabItemAdapter(List<CollabItem> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void setItems(List<CollabItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        CollabItem item = items.get(position);
        holder.textView.setText(item.getNombre());
        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }
    }
}
