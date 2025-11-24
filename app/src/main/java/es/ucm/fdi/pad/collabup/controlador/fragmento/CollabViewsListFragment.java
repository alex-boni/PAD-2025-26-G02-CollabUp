package es.ucm.fdi.pad.collabup.controlador.fragmento;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import es.ucm.fdi.pad.collabup.R;
import es.ucm.fdi.pad.collabup.modelo.adapters.CollabViewListAdapter;
import es.ucm.fdi.pad.collabup.modelo.collabView.AbstractCollabView;
import es.ucm.fdi.pad.collabup.modelo.collabView.Calendario;
import es.ucm.fdi.pad.collabup.modelo.collabView.CollabView;
import es.ucm.fdi.pad.collabup.modelo.collabView.Lista;
import es.ucm.fdi.pad.collabup.modelo.collabView.Registry;
import es.ucm.fdi.pad.collabup.modelo.collabView.TablonNotas;

public class CollabViewsListFragment extends Fragment {

    private static final String ARG_COLLAB_ID = "collab_id";
    private String collabId;

    private RecyclerView recyclerView;
    private CollabViewListAdapter adapter;
    private List<CollabView> items;
    private FirebaseFirestore db;

    public CollabViewsListFragment() {}

    public static CollabViewsListFragment newInstance(String collabId) {
        CollabViewsListFragment fragment = new CollabViewsListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_COLLAB_ID, collabId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            collabId = getArguments().getString(ARG_COLLAB_ID);
        }
        db = FirebaseFirestore.getInstance();
        Registry<CollabView> reg = Registry.getOrCreateRegistry(CollabView.class);
        reg.register(Lista.class);
        reg.register(Calendario.class);
        reg.register(TablonNotas.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_collab_views_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerCollabViews);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        items = new ArrayList<>();
        adapter = new CollabViewListAdapter(getContext(), items);
        recyclerView.setAdapter(adapter);

        // Cargamos los datos inicialmente
        if (collabId != null) {
            cargarCollabViewsDesdeFirestore();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (collabId != null) {
            cargarCollabViewsDesdeFirestore();
        }
    }

    private void cargarCollabViewsDesdeFirestore() {
        if (collabId == null) {
            Toast.makeText(getContext(), "ERROR: collabId es NULO", Toast.LENGTH_LONG).show();
            return;
        }

        db.collection("collabs")
                .document(collabId)
                .collection("collabViews")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalDocs = queryDocumentSnapshots.size();
                    items.clear();
                    Registry<CollabView> reg = Registry.getOrCreateRegistry(CollabView.class);
                    int itemsAdded = 0; // Contador de éxitos

                    for (var doc : queryDocumentSnapshots.getDocuments()) {
                        String uid = doc.getId();
                        String name = doc.getString("name");

                        String type = doc.getString("type");
                        java.util.Map<String, Object> settings = (java.util.Map<String, Object>) doc.get("settings");
                        if (settings == null) settings = new java.util.HashMap<>();

                        if (type == null && settings.containsKey("type")) {
                            type = (String) settings.get("type");
                        }

                        if (type == null) {
                            System.out.println("Documento " + uid + " ignorado: No tiene 'type'");
                            continue;
                        }

                        try {
                            Class<? extends CollabView> viewClass = reg.get(type);
                            if (viewClass != null) {
                                CollabView cvStatic = (CollabView) viewClass.getMethod("getStaticInstance").invoke(null);
                                if (cvStatic != null) {
                                    CollabView cv = cvStatic.build(collabId, uid, name, settings);
                                    items.add(cv);
                                    itemsAdded++;
                                }
                            } else {
                                // 4. ERROR ESPECÍFICO: Tipo desconocido
                                System.out.println("Tipo desconocido en Registry: " + type);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    adapter.notifyDataSetChanged();

                    if (totalDocs > 0 && itemsAdded == 0) {
                        Toast.makeText(getContext(), "Error: Hay docs pero falló el parseo (Clases/Registry)", Toast.LENGTH_LONG).show();
                    } else if (itemsAdded > 0) {
                        Toast.makeText(getContext(), "Éxito: Se han cargado " + itemsAdded + " Collab Views", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Fallo Firestore: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}