package es.ucm.fdi.pad.collabup.controlador.fragmento;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import es.ucm.fdi.pad.collabup.R;
import es.ucm.fdi.pad.collabup.modelo.Collab;
import es.ucm.fdi.pad.collabup.modelo.interfaz.OnOperationCallback;

public class CreateCollabFragment extends Fragment {

    private static final String RESULT_KEY = "collab_created";

    private TextInputLayout tilCollabName;
    private TextInputLayout tilCollabDescription;
    private TextInputLayout tilInviteUser;
    private TextInputEditText etCollabName;
    private TextInputEditText etCollabDescription;
    private TextInputEditText etInviteUser;
    private ImageView ivCollabImage;
    private Button btnSelectImage;
    private Button btnCancel;
    private Button btnCreate;
    private Toolbar createCollabToolbar;

    private Uri selectedImageUri = null;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;
    private FirebaseAuth mAuth;

    public CreateCollabFragment() {
    }

    public static CreateCollabFragment newInstance() {
        return new CreateCollabFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_collab, container, false);

        initializeViews(view);
        setupListeners();

        return view;
    }

    private void initializeViews(View view) {
        tilCollabName = view.findViewById(R.id.tilCollabName);
        tilCollabDescription = view.findViewById(R.id.tilCollabDescription);
        tilInviteUser = view.findViewById(R.id.tilInviteUser);
        etCollabName = view.findViewById(R.id.etCollabName);
        etCollabDescription = view.findViewById(R.id.etCollabDescription);
        etInviteUser = view.findViewById(R.id.etInviteUser);
        ivCollabImage = view.findViewById(R.id.ivCollabImage);
        btnSelectImage = view.findViewById(R.id.btnSelectImage);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnCreate = view.findViewById(R.id.btnCreate);
        createCollabToolbar= view.findViewById(R.id.createCollabToolbar);
    }

    private void setupListeners() {
        btnSelectImage.setOnClickListener(v -> selectImage());
        btnCancel.setOnClickListener(v -> cancelCreation());
        btnCreate.setOnClickListener(v -> createCollab());
        createCollabToolbar.setNavigationOnClickListener(v -> cancelCreation());
    }

    private void selectImage() {
        pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    //se usará en un futuro
    private void cancelCreation() {
        if (getParentFragmentManager() != null) {
            getParentFragmentManager().popBackStack();
        }
    }

    private void createCollab() {
        clearErrors();

        String name = etCollabName.getText() != null ? etCollabName.getText().toString().trim() : "";
        String description = etCollabDescription.getText() != null ? etCollabDescription.getText().toString().trim() : "";
        String inviteUser = etInviteUser.getText() != null ? etInviteUser.getText().toString().trim() : "";

        boolean isValid = validateForm(name, description);

        if (isValid) {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            String userId = currentUser.getUid();

            String imageUriString = selectedImageUri != null ? selectedImageUri.toString() : null;
            Collab nuevoCollab = new Collab(name, description, imageUriString, userId);

            nuevoCollab.crear(new OnOperationCallback() {
                @Override
                public void onSuccess() {
                    Bundle result = new Bundle();
                    result.putString("collabId", nuevoCollab.getId());
                    getParentFragmentManager().setFragmentResult(RESULT_KEY, result);
                    
                    if (getParentFragmentManager() != null) {
                        getParentFragmentManager().popBackStack();
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(getContext(), "Error al crear Collab: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
