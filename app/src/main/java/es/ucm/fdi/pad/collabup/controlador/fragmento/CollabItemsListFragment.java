package es.ucm.fdi.pad.collabup.controlador.fragmento;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import es.ucm.fdi.pad.collabup.R;
import es.ucm.fdi.pad.collabup.modelo.CollabItem;
import es.ucm.fdi.pad.collabup.modelo.adapters.CollabItemAdapter;
import es.ucm.fdi.pad.collabup.modelo.interfaz.OnDataLoadedCallback;

//Para ver todos los collabItems de una collab específica
public class CollabItemsListFragment extends Fragment {

    private ArrayList<CollabItem> listaItems = new ArrayList<>();
    private CollabItemAdapter adapter;
    private String collabId;

    RecyclerView rvCollabItems;


    public CollabItemsListFragment() {
    }

    public static CollabItemsListFragment newInstance(String collabId) {
        CollabItemsListFragment fragment = new CollabItemsListFragment();
        Bundle args = new Bundle();
        args.putString("collabId", collabId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_collab_items_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Recuperamos los argumentos
        Bundle bundle = getArguments();
        if (bundle != null) {
            collabId = bundle.getString("collabId");
        }

        rvCollabItems = view.findViewById(R.id.rvCollabItems);
        rvCollabItems.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new CollabItemAdapter(listaItems, item -> {
            CollabItemFragment fragment = CollabItemFragment.newInstance(
                    item.getIdI(),
                    collabId
            );
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentApp, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        rvCollabItems.setAdapter(adapter);

        cargarCollabItems();
    }


    private void cargarCollabItems() {
        CollabItem model = new CollabItem();
        model.setIdC(collabId);
        List<String> lista = new ArrayList<>();
        model.setcvAsignadas(lista);
        model.obtenerCollabItemsCollab(collabId, new OnDataLoadedCallback<ArrayList<CollabItem>>() {
            @Override
            public void onSuccess(ArrayList<CollabItem> items) {
                if (items != null && !items.isEmpty()) {
                    listaItems.clear();
                    listaItems.addAll(items);
                    adapter.setItems(listaItems);
                } else {
                    Toast.makeText(requireContext(), "No hay tareas para mostrar", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(requireContext(), "Error al cargar tareas: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
