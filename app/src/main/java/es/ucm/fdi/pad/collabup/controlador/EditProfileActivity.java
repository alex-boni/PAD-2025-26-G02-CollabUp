package es.ucm.fdi.pad.collabup.controlador;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide; // Importar Glide
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import es.ucm.fdi.pad.collabup.R;
import es.ucm.fdi.pad.collabup.modelo.Usuario;
import es.ucm.fdi.pad.collabup.modelo.interfaz.OnDataLoadedCallback;
import es.ucm.fdi.pad.collabup.modelo.interfaz.OnOperationCallback;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView imgProfile;
    private Button btnChangeImage, btnSaveChanges;
    private EditText eTxtPresentation;
    private Spinner spinnerProvincia, spinnerLocalidad;

    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private Usuario usuarioActual;
    private Uri imageUri; // URI de la nueva imagen seleccionada

    // Lanzador para seleccionar una imagen de la galería
    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    // Imagen seleccionada, guardamos su URI y la mostramos
                    imageUri = uri;
                    Glide.with(this).load(imageUri).into(imgProfile);
                }
            });

    // --- LANZADORES DE ACTIVIDAD ---
    // Lanzador para solicitar permisos de lectura
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permiso concedido, lanzar el selector de imágenes
                    pickImageLauncher.launch("image/*");
                } else {
                    Toast.makeText(this, "Permiso de galería denegado", Toast.LENGTH_SHORT).show();
                }
            });



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Inicializar Vistas
        imgProfile = findViewById(R.id.imgProfile);
        btnChangeImage = findViewById(R.id.btnChangeImage);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);
        eTxtPresentation = findViewById(R.id.eTxtPresentation);
        spinnerProvincia = findViewById(R.id.spinnerProvincia);
        spinnerLocalidad = findViewById(R.id.spinnerLocalidad);

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance();

        // Configurar Spinners
        setupSpinners();

        // Cargar datos del usuario
        loadUserData();

        // --- LISTENERS ---
        btnChangeImage.setOnClickListener(v -> checkPermissionAndPickImage());
        btnSaveChanges.setOnClickListener(v -> saveProfileChanges());
    }

    /**
     * Carga los datos actuales del usuario (bio, ubicación, foto) en la vista.
     */
    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            finish(); // No debería estar aquí sin estar logueado
            return;
        }

        usuarioActual = new Usuario();
        usuarioActual.obtener(user.getUid(), new OnDataLoadedCallback<Usuario>() {
            @Override
            public void onSuccess(Usuario data) {
                usuarioActual = data;
                // Rellenar campos
                eTxtPresentation.setText(data.getPresentacion());

                // Cargar foto de perfil si existe
                if (data.getUrlFoto() != null && !data.getUrlFoto().isEmpty()) {
                    Glide.with(EditProfileActivity.this)
                            .load(data.getUrlFoto())
                            .into(imgProfile);
                }

                // TODO: Lógica para seleccionar el item correcto en los spinners
                // basado en data.getUbicacion()
            }
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(EditProfileActivity.this, "Error al cargar datos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Inicia el proceso de selección de imagen, comprobando permisos primero.
     */
    private void checkPermissionAndPickImage() {
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            // Permiso ya concedido
            pickImageLauncher.launch("image/*");
        } else {
            // Solicitar permiso
            requestPermissionLauncher.launch(permission);
        }
    }

    /**
     * Guarda todos los cambios del perfil.
     * Si hay una nueva imagen, la sube a Storage primero.
     */
    private void saveProfileChanges() {
        if (imageUri != null) {
            // 1. Hay una imagen nueva -> Subir a Storage
            uploadImageAndSaveChanges();
        } else {
            // 2. No hay imagen nueva -> Guardar solo texto
            saveChangesToFirestore(null); // null significa "no cambiar URL de foto"
        }
    }

    /**
     * Sube la imagen (imageUri) a Firebase Storage y luego guarda todo.
     */
    private void uploadImageAndSaveChanges() {
        String uid = mAuth.getCurrentUser().getUid();
        // Crear una referencia única (ej. profile_images/UID.jpg)
        String fileName = "profile_images/" + uid + ".jpg";
        StorageReference storageRef = mStorage.getReference().child(fileName);

        Toast.makeText(this, "Subiendo imagen...", Toast.LENGTH_SHORT).show();

        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Imagen subida, ahora obtenemos la URL de descarga
                    storageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                        // URL obtenida, ahora guardamos todo en Firestore
                        saveChangesToFirestore(downloadUri.toString());
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al subir imagen: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Guarda los campos de texto y (opcionalmente) la nueva URL de la foto en Firestore.
     * @param newImageUrl La URL de la foto subida, o null si no se cambió.
     */
    private void saveChangesToFirestore(String newImageUrl) {
        String presentacion = eTxtPresentation.getText().toString();
        String provincia = spinnerProvincia.getSelectedItem().toString();
        String localidad = spinnerLocalidad.getSelectedItem().toString();
        String ubicacion = provincia + ", " + localidad;

        // Usamos un Map para actualizar solo los campos modificados
        Map<String, Object> updates = new HashMap<>();
        updates.put("presentacion", presentacion);
        updates.put("ubicacion", ubicacion);

        if (newImageUrl != null) {
            updates.put("urlFoto", newImageUrl);
        }

        // Usamos el método 'actualizarCampos' que crearemos en Usuario.java
        usuarioActual.actualizarCampos(updates, new OnOperationCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(EditProfileActivity.this, "Perfil actualizado", Toast.LENGTH_SHORT).show();
                finish(); // Volver a la pantalla anterior
            }
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(EditProfileActivity.this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Configura los adaptadores para los Spinners de ubicación.
     */
    private void setupSpinners() {
        // Adaptador para Provincias (desde arrays.xml)
        ArrayAdapter<CharSequence> provinciaAdapter = ArrayAdapter.createFromResource(this,
                R.array.provincias, android.R.layout.simple_spinner_item);
        provinciaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProvincia.setAdapter(provinciaAdapter);

        // Listener para Provincia
        spinnerProvincia.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Basado en la provincia, cambiamos el adaptador de Localidad
                String provinciaSeleccionada = parent.getItemAtPosition(position).toString();
                int idArrayLocalidades = R.array.localidades_default; // Por defecto

                if (provinciaSeleccionada.equals("Madrid")) {
                    idArrayLocalidades = R.array.localidades_madrid;
                } else if (provinciaSeleccionada.equals("Barcelona")) {
                    idArrayLocalidades = R.array.localidades_barcelona;
                } // ... añadir más 'else if'

                // Actualizar adaptador de Localidad
                ArrayAdapter<CharSequence> localidadAdapter = ArrayAdapter.createFromResource(EditProfileActivity.this,
                        idArrayLocalidades, android.R.layout.simple_spinner_item);
                localidadAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerLocalidad.setAdapter(localidadAdapter);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
}