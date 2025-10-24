package es.ucm.fdi.pad.collabup.controlador.fragmento;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import es.ucm.fdi.pad.collabup.R;
import es.ucm.fdi.pad.collabup.modelo.Collab;
import es.ucm.fdi.pad.collabup.modelo.adapters.CardAdapter;
import es.ucm.fdi.pad.collabup.modelo.interfaz.OnDataLoadedCallback;

public class CollabListFragment extends Fragment {

    private static final String RESULT_KEY = "collab_created";

    public CollabListFragment() {
    }

    public static CollabListFragment newInstance(String param1, String param2) {
        CollabListFragment fragment = new CollabListFragment();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    private RecyclerView recyclerView;
    private CardAdapter adapter;
    private List<Collab> items;
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_collab_list_layout, container, false);

        mAuth = FirebaseAuth.getInstance();
        
        recyclerView = view.findViewById(R.id.recyclerViewCollab);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        items = new ArrayList<>();
        adapter = new CardAdapter(items);
        recyclerView.setAdapter(adapter);

        cargarCollabsDesdeFirestore();
        setupFragmentResultListener();

        FloatingActionButton fab = view.findViewById(R.id.fabCreateCollab);
        fab.setOnClickListener(v ->
            getParentFragmentManager().beginTransaction()
                .replace(R.id.fragmentApp, CreateCollabFragment.newInstance())
                .addToBackStack(null)
                .commit()
        );
        return view;
    }

    private void cargarCollabsDesdeFirestore() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userId;
        
        if (currentUser == null) {
            userId = "temp_user_dev";
            Toast.makeText(getContext(), "Modo desarrollo: cargando collabs de usuario temporal", Toast.LENGTH_SHORT).show();
        } else {
            userId = currentUser.getUid();
        }

        Collab collabDAO = new Collab();
        collabDAO.obtenerCollabsDelUsuario(userId, new OnDataLoadedCallback<ArrayList<Collab>>() {
            @Override
            public void onSuccess(ArrayList<Collab> collabs) {
                items.clear();
                items.addAll(collabs);
                adapter.notifyDataSetChanged();
                
                if (collabs.isEmpty()) {
                    Toast.makeText(getContext(), "No tienes Collabs aún. ¡Crea uno!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Error al cargar Collabs: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupFragmentResultListener() {
        getParentFragmentManager().setFragmentResultListener(RESULT_KEY, this, (requestKey, result) -> {
            String collabId = result.getString("collabId");
            
            if (collabId != null) {
                cargarCollabsDesdeFirestore();
            }
        });
    }
}

