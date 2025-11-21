package es.ucm.fdi.pad.collabup.controlador.fragmento;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

import es.ucm.fdi.pad.collabup.R;
import es.ucm.fdi.pad.collabup.modelo.collabView.CollabItem;
import es.ucm.fdi.pad.collabup.modelo.interfaz.OnDataLoadedCallback;

//Para ver todos los collabItems de una collab específica
public class CollabItemsListFragment extends Fragment {
    private ListView lvCollabItems;
    private ArrayList<CollabItem> listaItems = new ArrayList<>();
    private ArrayAdapter<String> adapter; // Adapter simple solo con nombres de items
    private String collabId;
    private ArrayList<String> miembros;
    private ArrayList<String> miembrosNombres; //nombres de los miembros del collab
    private ArrayList<String> collabViews;
    private ArrayList<String> cvNombres;

    private MaterialToolbar toolbar;


    public CollabItemsListFragment() {
    }

    public static CollabItemsListFragment newInstance(String collabId, ArrayList<String> miembros,
                                                      ArrayList<String> miembrosNombres, ArrayList<String> collabViews,
                                                      ArrayList<String> cvNombres) {
        CollabItemsListFragment fragment = new CollabItemsListFragment();
        Bundle args = new Bundle();
        args.putString("collabId", collabId);
        args.putStringArrayList("miembros", miembros);
        args.putStringArrayList("miembrosNombres", miembrosNombres);
        args.putStringArrayList("cv", collabViews);
        args.putStringArrayList("cvNombres", collabViews);
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

        toolbar = view.findViewById(R.id.toolbarCollabItem);
        toolbar.setNavigationOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        lvCollabItems = view.findViewById(R.id.lvCollabItems);

        // Recuperamos los argumentos
        Bundle bundle = getArguments();
        if (bundle != null) {
            collabId = bundle.getString("collabId");
            miembros = bundle.getStringArrayList("miembros");
            miembrosNombres = bundle.getStringArrayList("miembrosNombres");
            collabViews = bundle.getStringArrayList("cv");
            cvNombres = bundle.getStringArrayList("cvNombres");
        } else {
            collabId = "ryO2NPfO9YaaWfNkhibD";
            miembros = new ArrayList<>();
            miembrosNombres = new ArrayList<>();
            collabViews = new ArrayList<>();
        }

        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, new ArrayList<>());
        lvCollabItems.setAdapter(adapter);

        cargarCollabItems();

        lvCollabItems.setOnItemClickListener((parent, itemView, position, id) -> {
            CollabItem itemSeleccionado = listaItems.get(position);
            CollabItemFragment fragment = CollabItemFragment.newInstance(
                    itemSeleccionado.getIdI(),
                    collabId,
                    miembros,
                    miembrosNombres,
                    collabViews,
                    cvNombres
            );
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentApp, fragment) // el contenedor donde lo vamos a mostrar
                    .addToBackStack(null) // para poder volver atrás
                    .commit();
        });
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

                    ArrayList<String> nombres = new ArrayList<>();
                    for (CollabItem ci : items) nombres.add(ci.getNombre());

                    adapter.clear();
                    adapter.addAll(nombres);
                    adapter.notifyDataSetChanged();
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


    @Override
    public void onResume() {
        super.onResume();
        cargarCollabItems(); // recargamos la lista siempre que se muestre el activity
    }
}
