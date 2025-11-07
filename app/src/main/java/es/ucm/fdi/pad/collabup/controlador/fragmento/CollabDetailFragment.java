package es.ucm.fdi.pad.collabup.controlador.fragmento;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.Toolbar; // Importar Toolbar
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import es.ucm.fdi.pad.collabup.R;
// Importa la clase Collab para manejar los datos
import es.ucm.fdi.pad.collabup.controlador.LoginController;
import es.ucm.fdi.pad.collabup.modelo.Collab;
import es.ucm.fdi.pad.collabup.modelo.interfaz.OnDataLoadedCallback;

public class CollabDetailFragment extends Fragment {

    // 1. RENOMBRAR la clave del argumento
    private static final String ARG_COLLAB_ID = "collab_id";

    // Variables para almacenar los datos
    private String collabId;
    private Collab currentCollab; // Para almacenar el objeto Collab después de cargarlo

    // Componentes del Layout
    private Toolbar detailToolbar;
    private TextView tvCollabTitle;
    private TextView tvCollabDescription;
    private TextView tvCollabCreator;
    private RecyclerView rvMembers;
    private Button btnViewAllTasks;
    private Button btnAddTarea;


    public CollabDetailFragment() {
        // Constructor público requerido
    }

    /**
     * Factory method para crear una nueva instancia pasando el ID del Collab.
     */
    public static CollabDetailFragment newInstance(String collabId) {
        CollabDetailFragment fragment = new CollabDetailFragment();
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

            if (collabId == null) {
                Toast.makeText(getContext(), "Error: ID de Collab no proporcionado.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_collab_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        detailToolbar = view.findViewById(R.id.detailToolbar);
        tvCollabTitle = view.findViewById(R.id.tvCollabTitle);
        tvCollabDescription = view.findViewById(R.id.tvCollabDescription);
        tvCollabCreator = view.findViewById(R.id.tvCollabCreator);
        rvMembers = view.findViewById(R.id.rvMembers);
        btnViewAllTasks = view.findViewById(R.id.btnViewAllTasks);
        btnAddTarea = view.findViewById(R.id.btnAddTarea);

        setupToolbar();

        if (collabId != null) {
            cargarDetallesDelCollabDesdeFirestore(collabId);
        }

        btnViewAllTasks.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Navegando a Tareas...", Toast.LENGTH_SHORT).show();
        });

        btnAddTarea.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Abriendo formulario Tarea...", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupToolbar() {
        detailToolbar.setNavigationOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        // Manejo del menú (Editar, Archivar, etc.) - Requiere implementar OnMenuItemClickListener
        detailToolbar.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_edit) {
                // Lógica de edición
                Toast.makeText(getContext(), "Editar Collab", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.action_archive) {
                // Lógica de archivo
                Toast.makeText(getContext(), "Archivar Collab", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.action_exit) {
                // Lógica para salir del Collab
                Toast.makeText(getContext(), "Salir del Collab", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
    }

    private void cargarDetallesDelCollabDesdeFirestore(String id) {
            Collab dao = new Collab();
            dao.obtener(id, new OnDataLoadedCallback<Collab>() {
                @Override
                public void onSuccess(Collab data) {
                    if(isAdded()){
                        currentCollab = data;
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    if(isAdded()){
                        Toast.makeText(getContext(), "Error al cargar Collab: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

        Toast.makeText(getContext(), "Cargando Collab con ID: " + id, Toast.LENGTH_SHORT).show();

        // Simular la actualización de la UI
        tvCollabTitle.setText("Detalles del Collab: " + id.substring(0, 5) + "...");
        tvCollabDescription.setText("Aquí va la descripción cargada de la base de datos.");
        tvCollabCreator.setText("Creado por: [Usuario]");
        btnViewAllTasks.setText("Ver Todas las Tareas (4)");

    }
}