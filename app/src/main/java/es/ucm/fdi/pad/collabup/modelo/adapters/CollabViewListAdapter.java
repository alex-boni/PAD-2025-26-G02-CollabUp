package es.ucm.fdi.pad.collabup.modelo.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import es.ucm.fdi.pad.collabup.R;
import es.ucm.fdi.pad.collabup.modelo.collabView.AbstractCollabView;
import es.ucm.fdi.pad.collabup.modelo.collabView.CollabView;

public class CollabViewListAdapter extends RecyclerView.Adapter<CollabViewListAdapter.ViewHolder> {

    private Context context;
    private List<CollabView> items;

    public CollabViewListAdapter(Context context, List<CollabView> items) {
        this.context = context;
        this.items = items;
    }

    // Usamos un FrameLayout como contenedor genérico
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public FrameLayout container;

        public ViewHolder(View v) {
            super(v);
            container = (FrameLayout) v;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Creamos un contenedor vacío programáticamente
        FrameLayout frame = new FrameLayout(parent.getContext());
        frame.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        return new ViewHolder(frame);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CollabView item = items.get(position);

        // 1. Limpiar cualquier vista anterior (porque el ViewHolder se recicla)
        holder.container.removeAllViews();

        // 2. MAGIA: Pedirle al objeto (Lista/Calendario) que genere su propia vista
        View itemView = item.getStaticAddCollabViewInListEntry(context);

        // 3. Añadir la vista generada al contenedor
        if (itemView != null) {
            holder.container.addView(itemView);
        }

        // 4. Gestionar el clic para abrir el detalle de esa vista
        holder.container.setOnClickListener(v -> {
            if (item instanceof AbstractCollabView) {
                AbstractCollabView abstractView = (AbstractCollabView) item;
                Fragment fragment = abstractView.getFullViewFragment();

                if (fragment != null && context instanceof AppCompatActivity) {
                    AppCompatActivity activity = (AppCompatActivity) context;
                    activity.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragmentApp, fragment)
                            .addToBackStack(null)
                            .commit();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}