package es.ucm.fdi.pad.collabup.controlador;

import android.content.Intent; // Importado
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull; // Importado
import androidx.appcompat.app.AppCompatActivity;
import es.ucm.fdi.pad.collabup.R;

// Imports de Firebase Authentication
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class LoginController extends AppCompatActivity implements View.OnClickListener {

    // Variables para los elementos de la interfaz
    private EditText eTxtEmail;
    private EditText eTxtPass;
    private Button btnLogin;

    // Variable para emplear Firebase Authentication
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);

        // Instancio las variables
        eTxtEmail = findViewById(R.id.eTxtEmail);
        eTxtPass = findViewById(R.id.eTxtPass);
        btnLogin = findViewById(R.id.btnLogin);

        // Inicializo Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // ClickListener al unico boton
        btnLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        // Constantes para los elementos, añadiendo trim() para limpiar espacios
        String email = eTxtEmail.getText().toString().trim();
        String pass = eTxtPass.getText().toString().trim();

        // Para iniciar el logon primero compruebo que no esten vacios los campos
        if (!email.isEmpty() && !pass.isEmpty()) {
            // Lanzo el metodo que continua con el proceso de login
            login(email, pass);
        } else {
            Toast.makeText(this, "Por favor, rellene todos los campos", Toast.LENGTH_SHORT).show();
        }
    }

    private void login(String email, String pass) {
        // Empleo el servicio de Firebase Authentication para comprobar y ejecutar el login
        mAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Si se puede iniciar sesión, inicio la actividad principal de la app
                            startActivity(new Intent(LoginController.this, AppController.class));
                            finish(); // Cierra esta actividad
                        } else {
                            // Si hubo algún error, compruebo las excepciones que pudo almacenar task
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthInvalidUserException e) {
                                // Email no encontrado
                                Toast.makeText(LoginController.this, "Email no encontrado.", Toast.LENGTH_LONG).show();
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                // Contraseña incorrecta
                                Toast.makeText(LoginController.this, "Contraseña incorrecta.", Toast.LENGTH_LONG).show();
                            } catch (FirebaseAuthException e) {
                                // Otros errores de Firebase (ej. red)
                                Toast.makeText(LoginController.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                                // Errores inesperados
                                e.printStackTrace();
                                Toast.makeText(LoginController.this, "Error inesperado.", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
    }
}