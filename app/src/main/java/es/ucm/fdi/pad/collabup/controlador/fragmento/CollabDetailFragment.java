package es.ucm.fdi.pad.collabup.controlador.fragmento;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

import es.ucm.fdi.pad.collabup.R;
import es.ucm.fdi.pad.collabup.modelo.Collab;
import es.ucm.fdi.pad.collabup.modelo.Usuario;
import es.ucm.fdi.pad.collabup.modelo.adapters.MemberAdapter;
import es.ucm.fdi.pad.collabup.modelo.interfaz.OnDataLoadedCallback;
import es.ucm.fdi.pad.collabup.modelo.interfaz.OnOperationCallback;

public class CollabDetailFragment extends Fragment {

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
    private Button btnAddMember;
    private ImageView ivCollabImage;


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
        ivCollabImage = view.findViewById(R.id.ivCollabImage);

        btnAddMember = view.findViewById(R.id.btnAddMember);

        listaMiembros = new ArrayList<>();
        memberAdapter = new MemberAdapter(listaMiembros);
        rvMembers.setAdapter(memberAdapter);

        setupToolbar();

        if (collabId != null) {
            cargarDetallesDelCollabDesdeFirestore(collabId);
        }

        btnAddMember.setOnClickListener(v -> {
            mostrarDialogoAgregarMiembro();
        });
    }


    private void setupToolbar() {
        detailToolbar.setNavigationOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        // Manejo del menú (Editar, Archivar, etc.)
        detailToolbar.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if(itemId == R.id.action_view_more){
                if(currentCollab != null){
                    CollabDetailFragment detailFragment = CollabDetailFragment.newInstance(collabId);
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.fragmentApp, detailFragment)
                            .addToBackStack("collab_detail_tag")
                            .commit();
                }
                return true;
            } else if (itemId == R.id.action_edit) {
                // Lógica de edición
                if (collabId != null) {
                    Fragment editFragment = CollabEditFragment.newInstance(collabId);
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.fragmentApp, editFragment)
                            .addToBackStack("collab_detail_tag")
                            .commit();
                }
                return true;
            } else if (itemId == R.id.action_delete) {
                // Lógica de eliminación
                deleteCollab();
                return true;
            } else if (itemId == R.id.action_exit) {
                // Lógica para salir del Collab
                exitCollab();
                return true;
            }
            return false;
        });
    }
    private void deleteCollab() {
        if (currentCollab == null) {
            Toast.makeText(getContext(), "Error: No se ha cargado el Collab", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseAuth mAuth= FirebaseAuth.getInstance();
        FirebaseUser usuarioActual = mAuth.getCurrentUser();
        if (usuarioActual == null) return;
        if (!currentCollab.getCreadorId().equals(usuarioActual.getUid())) {
            Toast.makeText(getContext(), "Error: Solo el creador puede eliminar el Collab", Toast.LENGTH_SHORT).show();
        }else{
            showDeleteConfirmationDialog();
        }
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Eliminar Collab");
        builder.setMessage("¿Estás seguro de que deseas eliminar este Collab? Esta acción no se puede deshacer.");

        builder.setPositiveButton("Eliminar", (dialog, which) -> {
            // Lógica para eliminar el Collab
            currentCollab.eliminar(new OnOperationCallback() {
                @Override
                public void onSuccess() {
                    if (isAdded()) {
                        Toast.makeText(getContext(), "Collab eliminado", Toast.LENGTH_SHORT).show();
                        getParentFragmentManager().popBackStack();
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    if (isAdded()) {
                        Toast.makeText(getContext(), "Error al eliminar Collab: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> {
            dialog.dismiss();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void exitCollab() {
        if (currentCollab == null) {
            Toast.makeText(getContext(), "Error: No se ha cargado el Collab", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseAuth mAuth= FirebaseAuth.getInstance();
        FirebaseUser usuarioActual = mAuth.getCurrentUser();
        if (usuarioActual == null) {
            Toast.makeText(getContext(), "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }
        if(currentCollab.getMiembros().size()==1 && currentCollab.getMiembros().contains(usuarioActual.getUid())){
            Toast.makeText(getContext(), "Eres el último miembro. Si sales, el Collab se eliminará.", Toast.LENGTH_SHORT).show();
            showDeleteConfirmationDialog();
            return;
        }
        if(currentCollab.getCreadorId().equals(usuarioActual.getUid())){
            Toast.makeText(getContext(), "Eres el creador, se asignara a otro creador al collab", Toast.LENGTH_SHORT).show();
            newCreatorAssignment(usuarioActual.getUid());
            showExitConfirmationDialog();
            return;
        }
    }

    private void showExitConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Salir del Collab");
        builder.setMessage("¿Estás seguro de que deseas salir de este Collab?");

        builder.setPositiveButton("Salir", (dialog, which) -> {
            // Lógica para salir del Collab
            FirebaseAuth mAuth= FirebaseAuth.getInstance();
            FirebaseUser usuarioActual = mAuth.getCurrentUser();
            if (usuarioActual == null) return;
            currentCollab.removerMiembro(usuarioActual.getUid(), new OnOperationCallback() {
                @Override
                public void onSuccess() {
                    if (isAdded()) {
                        Toast.makeText(getContext(), "Has salido del Collab", Toast.LENGTH_SHORT).show();
                        getParentFragmentManager().popBackStack();
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    if (isAdded()) {
                        Toast.makeText(getContext(), "Error al salir del Collab: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> {
            dialog.dismiss();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void newCreatorAssignment(String exitingCreatorId) {
        ArrayList<String> miembros = currentCollab.getMiembros();
        String newCreatorId = null;
        for (String miembroId : miembros) {
            if (!miembroId.equals(exitingCreatorId)) {
                newCreatorId = miembroId;
                break;
            }
        }
        if (newCreatorId != null) {
            currentCollab.setCreadorId(newCreatorId, new OnOperationCallback() {
                @Override
                public void onSuccess() {
                    // Creador asignado correctamente
                }

                @Override
                public void onFailure(Exception e) {
                    if (isAdded()) {
                        Toast.makeText(getContext(), "Error al asignar nuevo creador: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void cargarDetallesDelCollabDesdeFirestore(String id) {
        Collab dao = new Collab();
        dao.obtener(id, new OnDataLoadedCallback<Collab>() {
            @Override
            public void onSuccess(Collab data) {
                if (isAdded()) {
                    currentCollab = data;
                    tvCollabTitle.setText(currentCollab.getNombre());
                    tvCollabDescription.setText(currentCollab.getDescripcion());
                    if (currentCollab.getImageUri() != null && !currentCollab.getImageUri().isEmpty()) {
                        try {
                            ivCollabImage.setImageURI(Uri.parse(currentCollab.getImageUri()));
                        } catch (Exception e) {
                            ivCollabImage.setImageResource(R.drawable.logo); // Imagen por defecto
                        }
                    } else {
                        ivCollabImage.setImageResource(R.drawable.logo); // Imagen por defecto
                    }
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