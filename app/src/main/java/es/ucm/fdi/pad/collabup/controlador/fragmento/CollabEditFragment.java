package es.ucm.fdi.pad.collabup.controlador.fragmento;

import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
// Agrega la importación de Glide si lo usas para cargar imágenes por URI
// import com.bumptech.glide.Glide;

import es.ucm.fdi.pad.collabup.R;
import es.ucm.fdi.pad.collabup.modelo.Collab;
import es.ucm.fdi.pad.collabup.modelo.interfaz.OnDataLoadedCallback;
import es.ucm.fdi.pad.collabup.modelo.interfaz.OnOperationCallback;

public class CollabEditFragment extends Fragment {

    private static final String ARG_COLLAB_ID = "collab_id_to_edit";
    // Definición en EditCollabFragment o en una interfaz de constantes
    private static final String RESULT_KEY = "collab_updated";

    private String collabId;
    private Collab currentCollab;
    private Uri selectedImageUri = null; // Para la nueva imagen
    private Toolbar editCollabToolbar;
    private TextInputLayout tilCollabName;
    private TextInputLayout tilCollabDescription;
    private TextInputEditText etCollabName;
    private TextInputEditText etCollabDescription;
    private ImageView ivCollabImage;
    private Button btnSelectImage;
    private Button btnCancel;
    private Button btnUpdate;
    private FirebaseAuth mAuth;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;


    public CollabEditFragment() {
    }

    public static CollabEditFragment newInstance(String collabId) {
        Fragment fragment = new CollabEditFragment();
        Bundle args = new Bundle();
        args.putString(ARG_COLLAB_ID, collabId);
        fragment.setArguments(args);
        return (CollabEditFragment) fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            collabId = getArguments().getString(ARG_COLLAB_ID);
        }
        mAuth = FirebaseAuth.getInstance();
        pickMedia = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {
                try {
                    requireContext().getContentResolver().takePersistableUriPermission(
                            uri,
                            android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                    );
                } catch (SecurityException e) {
                    // No se pueden tomar permisos persistentes, la URI será temporal
                }
                selectedImageUri = uri;
                ivCollabImage.setImageURI(uri);
                Toast.makeText(getContext(), "Imagen seleccionada", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "No se seleccionó ninguna imagen", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_collab_edit_layout, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        setupToolbar();
        setupListeners();

        if (collabId != null) {
            loadExistingCollabDetails(collabId);
        } else {
            Toast.makeText(getContext(), "Error: No se puede editar sin ID de Collab.", Toast.LENGTH_LONG).show();
            getParentFragmentManager().popBackStack();
        }
    }

    // --- Métodos de Inicialización y Lógica ---

    private void initializeViews(View view) {

        editCollabToolbar = view.findViewById(R.id.editCollabToolbar);
        tilCollabName = view.findViewById(R.id.tilCollabName);
        tilCollabDescription = view.findViewById(R.id.tilCollabDescription);
        etCollabName = view.findViewById(R.id.etCollabName);
        etCollabDescription = view.findViewById(R.id.etCollabDescription);
        ivCollabImage = view.findViewById(R.id.ivCollabImage);
        btnSelectImage = view.findViewById(R.id.btnSelectImage);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnUpdate = view.findViewById(R.id.btnUpdate);
    }

    private void setupToolbar() {
        editCollabToolbar.setNavigationOnClickListener(v -> cancelEditing());

        // Si quieres que el menú del detalle aparezca también en edición, configúralo aquí
        // (Aunque para edición pura, se suele dejar solo el botón de guardar en el cuerpo del fragmento)
    }

    private void setupListeners() {
        btnSelectImage.setOnClickListener(v -> selectImage()); // Lógica de selección de imagen
        btnCancel.setOnClickListener(v -> cancelEditing());
        btnUpdate.setOnClickListener(v -> updateCollab());
    }

    private void selectImage() {
        pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    private void cancelEditing() {
        // Vuelve al fragmento anterior (CollabDetailFragment)
        if (getParentFragmentManager() != null) {
            getParentFragmentManager().popBackStack();
        }
    }

    // --- Lógica de Edición ---

    private void loadExistingCollabDetails(String id) {
        Collab dao = new Collab();
        dao.obtener(id, new OnDataLoadedCallback<Collab>() {
            @Override
            public void onSuccess(Collab data) {
                if (isAdded()) { // Verificar que el fragmento sigue activo
                    currentCollab = data;
                    fillFormWithData(data);
                }
            }

            @Override
            public void onFailure(Exception e) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Error al cargar Collab: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    getParentFragmentManager().popBackStack(); // Volver si falla la carga
                }
            }
        });
    }

    private void fillFormWithData(Collab collab) {
        etCollabName.setText(collab.getNombre());
        etCollabDescription.setText(collab.getDescripcion());
        if(collab.getImageUri() != null){
            try {
                requireContext().getContentResolver().takePersistableUriPermission(
                        Uri.parse(collab.getImageUri()),
                        android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                );
                ivCollabImage.setImageURI(Uri.parse(collab.getImageUri()));
                selectedImageUri = Uri.parse(collab.getImageUri());
            } catch (SecurityException e) {
                // No se pueden tomar permisos persistentes, la URI será temporal
            }

        }
    }

    private void updateCollab() {
        clearErrors();
        String name = etCollabName.getText() != null ? etCollabName.getText().toString().trim() : "";
        String description = etCollabDescription.getText() != null ? etCollabDescription.getText().toString().trim() : "";

        boolean isValid = validateForm(name, description);
        if (isValid) {

            String imageUriString = selectedImageUri != null ? selectedImageUri.toString() : null;
            if (currentCollab != null) {
                currentCollab.setNombre(name);
                currentCollab.setDescripcion(description);
                if (imageUriString != null)
                    currentCollab.setImageUri(imageUriString);
            }
            Collab daoCollab = new Collab();
            daoCollab.modificar(currentCollab, new OnOperationCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(getContext(), "Collab actualizado con éxito.", Toast.LENGTH_SHORT).show();

                    // Enviar resultado al fragmento de
                     Bundle result = new Bundle();
                     result.putString("collab_id", currentCollab.getId());
                     getParentFragmentManager().setFragmentResult(RESULT_KEY, result);

                    if (getParentFragmentManager() != null) {
                        getParentFragmentManager().popBackStack();
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(getContext(), "Error al actualizar Collab: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }
    private boolean validateForm(String name, String description) {
        boolean isValid = true;

        if (name.isEmpty()) {
            tilCollabName.setError("El nombre del Collab es obligatorio");
            isValid = false;
        }

        if (description.isEmpty()) {
            tilCollabDescription.setError("La descripción es obligatoria");
            isValid = false;
        }

        return isValid;
    }

    private void clearErrors() {
        tilCollabName.setError(null);
        tilCollabDescription.setError(null);
    }
}