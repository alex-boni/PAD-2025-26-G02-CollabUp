package es.ucm.fdi.pad.collabup.controlador.fragmento;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import es.ucm.fdi.pad.collabup.R;
import es.ucm.fdi.pad.collabup.modelo.Collab;
import es.ucm.fdi.pad.collabup.modelo.Usuario;
import es.ucm.fdi.pad.collabup.modelo.adapters.MemberAdapter;
import es.ucm.fdi.pad.collabup.modelo.interfaz.OnDataLoadedCallback;
import es.ucm.fdi.pad.collabup.modelo.interfaz.OnOperationCallback;

public class CollabDetailFragment extends Fragment {

    // 1. RENOMBRAR la clave del argumento
    private static final String ARG_COLLAB_ID = "collab_id";
    private static final String RESULT_KEY = "collab_updated";

    // Variables para almacenar los datos
    private String collabId;
    private Collab currentCollab;
    private MemberAdapter memberAdapter;
    private ArrayList<Usuario> listaMiembros;

    // Componentes del Layout
    private Toolbar detailToolbar;
    private TextView tvCollabTitle;
    private TextView tvCollabDescription;
    private TextView tvCollabCreator;
    private RecyclerView rvMembers;
    private Button btnViewAllTasks;
    private Button btnAddCollabItem;
    private FloatingActionButton fabAddMember;


    public CollabDetailFragment() {
    }

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
        getParentFragmentManager().setFragmentResultListener(RESULT_KEY, this, (requestKey, result) -> {
            if (requestKey.equals(RESULT_KEY) && collabId != null) {
                // El Collab ha sido actualizado, recargamos los datos desde Firestore
                Toast.makeText(getContext(), "Detectada actualización de Collab. Recargando datos...", Toast.LENGTH_SHORT).show();
                cargarDetallesDelCollabDesdeFirestore(collabId);
            }
        });
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
        btnViewAllTasks = view.findViewById(R.id.btnViewAllCollabItems);
        btnAddCollabItem = view.findViewById(R.id.btnAddCollabItem);
        fabAddMember = view.findViewById(R.id.fabAddMember);

        listaMiembros = new ArrayList<>();
        memberAdapter = new MemberAdapter(listaMiembros);
        rvMembers.setAdapter(memberAdapter);

        setupToolbar();

        if (collabId != null) {
            cargarDetallesDelCollabDesdeFirestore(collabId);
        }


        btnViewAllTasks.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Navegando a Tareas...", Toast.LENGTH_SHORT).show();

            //Para mostrar los nombres pero mantener la lógica con los ids
            MiembrosData data = obtenerIdsYNombres(listaMiembros);
            ArrayList<String> miembrosIds = data.ids;
            ArrayList<String> miembrosNombres = data.nombres;

            //todo faltan ids collab views del collab
            ArrayList<String> cvIds = new ArrayList<>();
            ArrayList<String> cvNombres = new ArrayList<>();

            if (collabId != null) {
                CollabItemsListFragment fragment = CollabItemsListFragment.newInstance(
                        collabId,
                        miembrosIds,
                        miembrosNombres,
                        cvIds,
                        cvNombres
                );

                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentApp, fragment)
                        .addToBackStack(null) // para poder volver atrás
                        .commit();
            }
        });

        btnAddCollabItem.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Abriendo formulario Tarea...", Toast.LENGTH_SHORT).show();

            //Para mostrar los nombres pero mantener la lógica con los ids
            MiembrosData data = obtenerIdsYNombres(listaMiembros);
            ArrayList<String> miembrosIds = data.ids;
            ArrayList<String> miembrosNombres = data.nombres;

            //todo faltan ids collab views del collab
            ArrayList<String> cvIds = new ArrayList<>();
            ArrayList<String> cvNombres = new ArrayList<>();

            CreateCollabItemFragment fragment = CreateCollabItemFragment.newInstance(
                    currentCollab.getId(),
                    miembrosIds,
                    miembrosNombres,
                    cvIds,
                    cvNombres
            );

            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentApp, fragment)  // Ponemos nuestras cosas en el contenedor del fragment
                    .addToBackStack(null) // para poder volver atrás
                    .commit();
        });

        fabAddMember.setOnClickListener(v -> {
            mostrarDialogoAgregarMiembro();
        });
    }

    //Clase auxiliar para obtener ids y el nombre de la lista de miembros de forma general
    public class MiembrosData {
        public ArrayList<String> ids;
        public ArrayList<String> nombres;

        public MiembrosData(ArrayList<String> ids, ArrayList<String> nombres) {
            this.ids = ids;
            this.nombres = nombres;
        }
    }

    //Función que obtiene una lista con los ids de los miembros y otra con sus nombres
    private MiembrosData obtenerIdsYNombres(List<Usuario> listaMiembros) {
        ArrayList<String> miembrosIds = new ArrayList<>();
        ArrayList<String> miembrosNombres = new ArrayList<>();

        for (Usuario u : listaMiembros) {
            miembrosIds.add(u.getUID());
            miembrosNombres.add(u.getNombre());
        }

        return new MiembrosData(miembrosIds, miembrosNombres);
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
                if (collabId != null) {
                    // Navegar al fragmento de edición, pasándole el ID
                    Fragment editFragment = CollabEditFragment.newInstance(collabId);

                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.fragmentApp, editFragment) // R.id.fragmentApp es el contenedor principal
                            .addToBackStack("collab_detail_tag")
                            .commit();
                }
                return true;
            } else if (itemId == R.id.action_delete) {
                // Lógica de eliminación
                Toast.makeText(getContext(), "Eliminar Collab", Toast.LENGTH_SHORT).show();
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
        Toast.makeText(getContext(), "Cargando Collab con ID: " + id, Toast.LENGTH_SHORT).show();

        dao.obtener(id, new OnDataLoadedCallback<Collab>() {
            @Override
            public void onSuccess(Collab data) {
                if (isAdded()) {
                    currentCollab = data;
                    tvCollabTitle.setText(currentCollab.getNombre());
                    tvCollabDescription.setText(currentCollab.getDescripcion());
                    Usuario daoUsuario = new Usuario();
                    daoUsuario.obtener(data.getCreadorId(), new OnDataLoadedCallback<Usuario>() {
                        @Override
                        public void onSuccess(Usuario data) {
                            if (isAdded()) {
                                tvCollabCreator.setText("Creado por: " + data.getNombre());
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            if (isAdded()) {
                                Toast.makeText(getContext(), "Error al cargar creador: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    cargarMiembros(currentCollab.getMiembros());
                }
            }

            @Override
            public void onFailure(Exception e) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Error al cargar Collab: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    private void cargarMiembros(ArrayList<String> miembrosIds) {
        listaMiembros.clear();

        if (miembrosIds == null || miembrosIds.isEmpty()) {
            memberAdapter.notifyDataSetChanged();
            return;
        }

        Usuario daoUsuario = new Usuario();
        for (String miembroId : miembrosIds) {
            daoUsuario.obtener(miembroId, new OnDataLoadedCallback<Usuario>() {
                @Override
                public void onSuccess(Usuario usuario) {
                    if (isAdded()) {
                        listaMiembros.add(usuario);
                        memberAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    if (isAdded()) {
                        Toast.makeText(getContext(), "Error al cargar miembro: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void mostrarDialogoAgregarMiembro() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Añadir Miembro");

        final EditText input = new EditText(getContext());
        input.setHint("Nombre de usuario");
        builder.setView(input);

        builder.setPositiveButton("Añadir", (dialog, which) -> {
            String nombreUsuario = input.getText().toString().trim();
            if (!nombreUsuario.isEmpty()) {
                buscarYAgregarUsuario(nombreUsuario);
            } else {
                Toast.makeText(getContext(), "Por favor, introduce un nombre de usuario", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void buscarYAgregarUsuario(String nombreUsuario) {
        Usuario daoUsuario = new Usuario();
        daoUsuario.buscarPorNombreUsuario(nombreUsuario, new OnDataLoadedCallback<Usuario>() {
            @Override
            public void onSuccess(Usuario usuario) {
                if (isAdded() && currentCollab != null) {
                    agregarMiembroAlCollab(usuario.getUID(), usuario.getNombre());
                }
            }

            @Override
            public void onFailure(Exception e) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Usuario no encontrado: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void agregarMiembroAlCollab(String usuarioId, String nombreUsuario) {
        if (currentCollab == null) {
            Toast.makeText(getContext(), "Error: No se ha cargado el Collab", Toast.LENGTH_SHORT).show();
            return;
        }

        currentCollab.agregarMiembro(usuarioId, new OnOperationCallback() {
            @Override
            public void onSuccess() {
                if (isAdded()) {
                    Toast.makeText(getContext(), nombreUsuario + " añadido al Collab", Toast.LENGTH_SHORT).show();
                    cargarDetallesDelCollabDesdeFirestore(collabId);
                }
            }

            @Override
            public void onFailure(Exception e) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Error al añadir miembro: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}