package es.ucm.fdi.pad.collabup.controlador;


import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import es.ucm.fdi.pad.collabup.R;

// Se necesita la clase Usuario para crear el objeto y guardarlo en la base de datos.
// import com.example.ourleagues.modelo.Usuario;
// Se necesita la clase AuxFirebase para interactuar con Firebase.
// import com.example.ourleagues.modelo.herramienta.AuxFirebase;
// Imports para las excepciones específicas de Firebase Auth.
// import com.google.android.gms.tasks.OnCompleteListener;
// import com.google.android.gms.tasks.Task;
// import com.google.firebase.auth.AuthResult;
// import com.google.firebase.auth.FirebaseAuthEmailException;
// import com.google.firebase.auth.FirebaseAuthException;
// import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;

public class SingupController extends AppCompatActivity implements View.OnClickListener {

    // Variables para los elementos de la interfaz
    private EditText eTxtNombre;
    private EditText eTxtUsuario;
    private EditText eTxtEmailRegistro;
    private EditText eTxtPassword;
    private Button btnSingup;

    // Se necesita una instancia de AuxFirebase para usar los servicios de Firebase.
    // private final AuxFirebase auxFirebase = new AuxFirebase();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.singup_layout);

        // Instancio las variables de los elementos de la interfaz
        eTxtNombre = findViewById(R.id.eTxtNombre);
        eTxtUsuario = findViewById(R.id.eTxtUsuario);
        eTxtEmailRegistro = findViewById(R.id.eTxtEmailRegistro);
        eTxtPassword = findViewById(R.id.eTxtPassword);
        btnSingup = findViewById(R.id.btnSingup);

        // Establezco listener al unico boton
        btnSingup.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        // Obtengo los datos insertados en los EditText
        String email = eTxtEmailRegistro.getText().toString();
        String password = eTxtPassword.getText().toString();
        String nombre = eTxtNombre.getText().toString();
        String usuario = eTxtUsuario.getText().toString();

        // Si los campos no están vacíos se realiza el signup
        if (!email.isEmpty() && !password.isEmpty() && !nombre.isEmpty() && !usuario.isEmpty()) {
            signup(email, password);
        } else {
            Toast.makeText(this, "Por favor, rellene todos los campos", Toast.LENGTH_SHORT).show();
        }
    }

    // Metodo para registrar el usuario
    private void signup(String emailRegistro, String password) {
        /*
         * La siguiente sección contiene la lógica de registro con Firebase.
         * Se comenta porque requiere la configuración de Firebase y las clases AuxFirebase y Usuario.
         *
         * 1. Llama a `createUserWithEmailAndPassword` para crear un nuevo usuario.
         * 2. Si la creación es exitosa:
         * a. Crea un objeto `Usuario` con los datos adicionales (UID, email, nombre, etc.).
         * b. Llama al método `crear()` del objeto `Usuario` para guardar estos datos en Firestore.
         * c. Si se guarda correctamente, navega a AppController.
         * 3. Si la creación de usuario falla, captura las excepciones para mostrar mensajes de error.
         */
        /*
        auxFirebase.getAuth().createUserWithEmailAndPassword(emailRegistro, password)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        // Si se logró el signup, instancio mi objeto Usuario para insertar los datos extra en la bd de cloud firestore
                        Usuario usuario = new Usuario();
                        usuario.setUID(auxFirebase.getAuth().getCurrentUser().getUid());
                        usuario.setEmail(emailRegistro);
                        usuario.setNombre(eTxtNombre.getText().toString());
                        usuario.setUsuario(eTxtUsuario.getText().toString());

                        // El método crear() debería ser asíncrono o usar un callback para manejar el resultado.
                        // La implementación original en Kotlin puede no funcionar como se espera.
                        if (usuario.crear()) {
                            startActivity(new Intent(SingupController.this, AppController.class));
                            finish();
                        } else {
                            Toast.makeText(SingupController.this, "No se completó el registro en la base de datos", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        // Manejo de excepciones si el registro falla
                        try {
                            throw task.getException();
                        } catch (FirebaseAuthInvalidCredentialsException e) {
                            Toast.makeText(SingupController.this, "Contraseña no válida", Toast.LENGTH_LONG).show();
                        } catch (FirebaseAuthEmailException e) {
                            Toast.makeText(SingupController.this, "Email no válido o ya en uso", Toast.LENGTH_LONG).show();
                        } catch (FirebaseAuthException e) {
                            Toast.makeText(SingupController.this, "Credenciales no válidas", Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        */
    }
}