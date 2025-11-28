package es.ucm.fdi.pad.collabup.modelo.collabView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.Set;

import es.ucm.fdi.pad.collabup.R;

public class Lista extends AbstractCollabView {

    public Lista() {
        this.nombre = "Lista";
    }

    public static CollabView getStaticInstance() {
        return new Lista();
    }

    @Override
    protected Fragment getVistaGrande(RecyclerView.Adapter<?> adapter) {
        ListaFragment fragment = new ListaFragment();
        fragment.setAdapter(adapter);
        fragment.setTitulo(this.nombre != null ? this.nombre : "Lista");
        return fragment;
    }

    /**
     * Fragment para mostrar la vista de Lista.
     */
    public static class ListaFragment extends Fragment {
        private RecyclerView.Adapter<?> adapter;
        private String titulo;

        public void setAdapter(RecyclerView.Adapter<?> adapter) {
            this.adapter = adapter;
        }

        public void setTitulo(String titulo) {
            this.titulo = titulo;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_lista, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            // Configurar toolbar
            Toolbar toolbar = view.findViewById(R.id.toolbar);
            toolbar.setTitle(titulo != null ? titulo : "Lista");
            toolbar.setNavigationOnClickListener(v -> {
                if (getParentFragmentManager() != null) {
                    getParentFragmentManager().popBackStack();
                }
            });

            // Configurar RecyclerView con el adapter recibido
            RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
            recyclerView.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(requireContext()));
            if (adapter != null) {
                recyclerView.setAdapter(adapter);
            }
        }
    }

    @Override
    protected RecyclerView.Adapter<?> obtenerAdapter() {
        return new ListaAdapter(getListaCollabItems(), item -> {
            // TODO Click en item
        });
    }

    @Override
    protected View getPrevisualizacion(Context context) {
        ImageView iv = new ImageView(context);
        Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.lista_no_border_collabview);
        iv.setImageBitmap(bmp);
        iv.setAdjustViewBounds(true);
        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);

        return iv;
    }

    @Override
    protected Fragment getFragmentAjustes() {
        return null;
    }

    @Override
    public Set<CollabViewSetting> getStaticCreationSettings() {
        return Collections.emptySet();
    }

}
