package es.ucm.fdi.pad.collabup.controlador.fragmento;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import es.ucm.fdi.pad.collabup.R;
import es.ucm.fdi.pad.collabup.modelo.adapters.ListaAdapter;

public class ListaFragment extends Fragment {
    private RecyclerView.Adapter<?> adapter;
    private String titulo;

    public void setAdapter(RecyclerView.Adapter<?> adapter) {
        this.adapter = adapter;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public static ListaFragment newInstance() {
        return new ListaFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_lista, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Configurar RecyclerView con el adapter recibido
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        if (adapter != null) {
            recyclerView.setAdapter(adapter);

            if (adapter instanceof ListaAdapter) {
                ((ListaAdapter) adapter).setOnItemClickListener(item1 -> {
                    CollabItemFragment fragment = CollabItemFragment.newInstance(
                            item1.getIdI(),
                            item1.getIdC()
                    );
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragmentApp, fragment)
                            .addToBackStack(null)
                            .commit();
                });
            }
        }
    }
}
