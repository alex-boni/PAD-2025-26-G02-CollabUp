package es.ucm.fdi.pad.collabup.controlador;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import es.ucm.fdi.pad.collabup.R;

// Imports de la nueva interfaz DAO y el modelo Usuario
import es.ucm.fdi.pad.collabup.modelo.Usuario;
import es.ucm.fdi.pad.collabup.modelo.interfaz.OnOperationCallback;

// Imports de Firebase Authentication
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

public class SingupController extends AppCompatActivity implements View.OnClickListener {

    // Variables para los elementos de la interfaz
    private EditText eTxtNombre;
    private EditText eTxtUsuario;
    private EditText eTxtEmailRegistro;
    private EditText eTxtPassword;
    private Button btnSingup;

    // Variable para emplear Firebase Authentication
    private FirebaseAuth mAuth;

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

        // Inicializo Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Establezco listener al unico boton
        btnSingup.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        // Obtengo los datos insertados en los EditText
        String email = eTxtEmailRegistro.getText().toString().trim();
        String password = eTxtPassword.getText().toString().trim();
        String nombre = eTxtNombre.getText().toString().trim();
        String usuario = eTxtUsuario.getText().toString().trim();

        // Si los campos no están vacíos se realiza el signup
        if (!email.isEmpty() && !password.isEmpty() && !nombre.isEmpty() && !usuario.isEmpty()) {
            // Validar contraseña (ej. > 6 caracteres)
            if (password.length() < 6) {
                Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
                return;
            }
            signup(email, password, nombre, usuario);
        } else {
            Toast.makeText(this, "Por favor, rellene todos los campos", Toast.LENGTH_SHORT).show();
        }
    }

    // Metodo para registrar el usuario
    private void signup(String emailRegistro, String password, String nombre, String usuarioAlias) {
        // Empleo el servicio de Firebase Authentication para realizar el signup
        mAuth.createUserWithEmailAndPassword(emailRegistro, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Si se logró el signup, instancio mi objeto Usuario para insertar los datos extra en la bd de cloud firestore
                            String uid = mAuth.getCurrentUser().getUid();

                            Usuario usuario = new Usuario();
                            usuario.setUID(uid);
                            usuario.setEmail(emailRegistro);
                            usuario.setNombre(nombre);
                            usuario.setUsuario(usuarioAlias);

                            // Llamamos al método crear() de nuestro DAO
                            usuario.crear(new OnOperationCallback() {
                                @Override
                                public void onSuccess() {
                                    // Datos guardados en Firestore con éxito
                                    Toast.makeText(SingupController.this, "Registro completado.", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(SingupController.this, AppController.class));
                                    finish();
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    // Error al guardar en Firestore
                                    Toast.makeText(SingupController.this, "Error al guardar datos: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });

                        } else {
                            // Si el registro falla, mostramos un mensaje al usuario.
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                Toast.makeText(SingupController.this, "Contraseña no válida.", Toast.LENGTH_LONG).show();
                            } catch (FirebaseAuthUserCollisionException e) {
                                Toast.makeText(SingupController.this, "El email ya está en uso.", Toast.LENGTH_LONG).show();
                            } catch (FirebaseAuthException e) {
                                Toast.makeText(SingupController.this, "Error de registro: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                                Toast.makeText(SingupController.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
    }
}