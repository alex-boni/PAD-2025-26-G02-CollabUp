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

import java.util.ArrayList;
import java.util.List;

import es.ucm.fdi.pad.collabup.R;
import es.ucm.fdi.pad.collabup.modelo.adapters.CardAdapter;
import es.ucm.fdi.pad.collabup.modelo.CollabItem;
import es.ucm.fdi.pad.collabup.modelo.adapters.CardAdapter;

public class CollabListFragment extends Fragment {

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_collab_list_layout, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewCollab);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        List<CollabItem> items = new ArrayList<>();
        items.add(new CollabItem("Titulo del Collab 1", "Descripción del collab 1", R.drawable.ic_launcher_foreground));
        items.add(new CollabItem("Titulo del Collab 2", "Descripción del collab 2", R.drawable.ic_launcher_foreground));
        items.add(new CollabItem("Titulo del Collab 3", "Descripción del collab 3", R.drawable.ic_launcher_foreground));
        items.add(new CollabItem("Titulo del Collab 4", "Descripción del collab 4", R.drawable.ic_launcher_foreground));
        items.add(new CollabItem("Titulo del Collab 5", "Descripción del collab 5", R.drawable.ic_launcher_foreground));
        items.add(new CollabItem("Titulo del Collab 6", "Descripción del collab 6", R.drawable.ic_launcher_foreground));
        items.add(new CollabItem("Titulo del Collab 6", "Descripción del collab 6", R.drawable.ic_launcher_foreground));
        items.add(new CollabItem("Titulo del Collab 6", "Descripción del collab 6", R.drawable.ic_launcher_foreground));
        items.add(new CollabItem("Titulo del Collab 6", "Descripción del collab 6", R.drawable.ic_launcher_foreground));
        items.add(new CollabItem("Titulo del Collab 6", "Descripción del collab 6", R.drawable.ic_launcher_foreground));
        items.add(new CollabItem("Titulo del Collab 6", "Descripción del collab 6", R.drawable.ic_launcher_foreground));
        items.add(new CollabItem("Titulo del Collab 6", "Descripción del collab 6", R.drawable.ic_launcher_foreground));

        CardAdapter adapter = new CardAdapter(items);
        recyclerView.setAdapter(adapter);
        FloatingActionButton fab = view.findViewById(R.id.fabCreateCollab);
        fab.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Crear nuevo Collab", Toast.LENGTH_SHORT).show();
            // getParentFragmentManager().beginTransaction()
            //     .replace(R.id.fragmentContainer, CollabFormFragment.newInstance())
            //     .addToBackStack(null)
            //     .commit();
        });
        return view;
    }
}
