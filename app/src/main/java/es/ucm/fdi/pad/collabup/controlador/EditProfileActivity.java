package es.ucm.fdi.pad.collabup.controlador;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

import es.ucm.fdi.pad.collabup.R;
import es.ucm.fdi.pad.collabup.modelo.Usuario;
import es.ucm.fdi.pad.collabup.modelo.interfaz.OnDataLoadedCallback;
import es.ucm.fdi.pad.collabup.modelo.interfaz.OnOperationCallback;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView imgProfile;
    private Button btnChangeImage, btnSaveChanges;
    private EditText eTxtPresentation;

    // Cambiamos Spinner por MaterialAutoCompleteTextView
    private MaterialAutoCompleteTextView autoCompleteProvincia, autoCompleteLocalidad;

    private Toolbar editProfileToolbar;

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

        // Nuevas referencias a los AutoCompleteTextView
        autoCompleteProvincia = findViewById(R.id.autoCompleteProvincia);
        autoCompleteLocalidad = findViewById(R.id.autoCompleteLocalidad);

        editProfileToolbar = findViewById(R.id.editProfileToolbar);

        editProfileToolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance();

        // Configurar Listas Desplegables
        setupDropdowns();

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

                // Lógica para pre-rellenar los campos de ubicación
                // Se asume que se guardó como "Provincia, Localidad"
                if (data.getUbicacion() != null && data.getUbicacion().contains(",")) {
                    String[] partes = data.getUbicacion().split(",");
                    if (partes.length >= 1) {
                        String provincia = partes[0].trim();
                        // El false evita que se despliegue el filtro al setear texto
                        autoCompleteProvincia.setText(provincia, false);
                        // Cargar las localidades correspondientes a esa provincia
                        cargarLocalidades(provincia);
                    }
                    if (partes.length >= 2) {
                        String localidad = partes[1].trim();
                        autoCompleteLocalidad.setText(localidad, false);
                    }
                }
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

        // Ahora obtenemos el texto directamente del AutoCompleteTextView
        String provincia = autoCompleteProvincia.getText().toString();
        String localidad = autoCompleteLocalidad.getText().toString();

        String ubicacion = "";
        if(!provincia.isEmpty() && !localidad.isEmpty()){
            ubicacion = provincia + ", " + localidad;
        } else if (!provincia.isEmpty()) {
            ubicacion = provincia;
        }

        // Usamos un Map para actualizar solo los campos modificados
        Map<String, Object> updates = new HashMap<>();
        updates.put("presentacion", presentacion);
        updates.put("ubicacion", ubicacion);

        if (newImageUrl != null) {
            updates.put("urlFoto", newImageUrl);
        }

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
     * Configura los adaptadores para los campos de autocompletado (Provincia y Localidad).
     */
    private void setupDropdowns() {
        // 1. Configurar PROVINCIAS
        String[] provinciasArray = getResources().getStringArray(R.array.provincias);
        ArrayAdapter<String> provinciaAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, provinciasArray);

        autoCompleteProvincia.setAdapter(provinciaAdapter);

        // Listener: Cuando el usuario selecciona una provincia de la lista
        autoCompleteProvincia.setOnItemClickListener((parent, view, position, id) -> {
            String provinciaSeleccionada = (String) parent.getItemAtPosition(position);

            // Limpiar localidad anterior porque ha cambiado la provincia
            autoCompleteLocalidad.setText("");

            // Cargar las localidades de esa provincia
            cargarLocalidades(provinciaSeleccionada);
        });
    }

    /**
     * Carga el adaptador de localidades basado en la provincia seleccionada.
     */
    private void cargarLocalidades(String provinciaSeleccionada) {
        int idArrayLocalidades = R.array.localidades_default; // Por defecto

        // Lógica simple de mapeo (Asegúrate de tener estos arrays en strings.xml/arrays.xml)
        if (provinciaSeleccionada.equals("Madrid")) {
            idArrayLocalidades = R.array.localidades_madrid;
        } else if (provinciaSeleccionada.equals("Barcelona")) {
            idArrayLocalidades = R.array.localidades_barcelona;
        }
        // Puedes añadir más 'else if' para otras provincias aquí

        String[] localidadesArray = getResources().getStringArray(idArrayLocalidades);
        ArrayAdapter<String> localidadAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, localidadesArray);

        autoCompleteLocalidad.setAdapter(localidadAdapter);
    }
}
