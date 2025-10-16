package es.ucm.fdi.pad.collabup.controlador;


import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import es.ucm.fdi.pad.collabup.R;
// Se necesita la clase AuxFirebase para interactuar con Firebase.
// import com.example.ourleagues.modelo.herramienta.AuxFirebase;
// Imports para las excepciones específicas de Firebase Auth.
// import com.google.android.gms.tasks.OnCompleteListener;
// import com.google.android.gms.tasks.Task;
// import com.google.firebase.auth.AuthResult;
// import com.google.firebase.auth.FirebaseAuthEmailException;
// import com.google.firebase.auth.FirebaseAuthException;
// import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;

public class LoginController extends AppCompatActivity implements View.OnClickListener {

    // Variables para los elementos de la interfaz
    private EditText eTxtEmail;
    private EditText eTxtPass;
    private Button btnLogin;

    // Se necesita una instancia de AuxFirebase para usar los servicios de Firebase.
    // private final AuxFirebase auxFirebase = new AuxFirebase();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);

        // Instancio las variables
        eTxtEmail = findViewById(R.id.eTxtEmail);
        eTxtPass = findViewById(R.id.eTxtPass);
        btnLogin = findViewById(R.id.btnLogin);

        // ClickListener al unico boton
        btnLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        // Constantes para los elementos
        String email = eTxtEmail.getText().toString();
        String pass = eTxtPass.getText().toString();

        // Para iniciar el logon primero compruebo que no esten vacios los campos
        if (!email.isEmpty() && !pass.isEmpty()) {
            // Lanzo el metodo que continua con el proceso de login
            login(email, pass);
        } else {
            Toast.makeText(this, "Por favor, rellene todos los campos", Toast.LENGTH_SHORT).show();
        }
    }

    private void login(String email, String pass) {
        /*
         * La siguiente sección contiene la lógica de autenticación con Firebase.
         * Se comenta porque requiere la configuración de Firebase y la clase AuxFirebase.
         *
         * 1. Llama a `signInWithEmailAndPassword` para verificar las credenciales.
         * 2. Añade un listener que se ejecuta cuando la tarea finaliza.
         * 3. Si la tarea tuvo éxito (`isSuccessful`), navega a AppController.
         * 4. Si falla, captura y maneja las excepciones para mostrar un mensaje de error adecuado.
         */
        /*
        auxFirebase.getAuth().signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        // Si se puede iniciar sesión, inicio la actividad principal de la app
                        startActivity(new Intent(LoginController.this, AppController.class));
                        finish();
                    } else {
                        // Si hubo algún error, compruebo las excepciones que pudo almacenar task
                        try {
                            throw task.getException();
                        } catch (FirebaseAuthInvalidCredentialsException e) {
                            Toast.makeText(LoginController.this, "Contraseña incorrecta", Toast.LENGTH_LONG).show();
                        } catch (FirebaseAuthEmailException e) {
                            Toast.makeText(LoginController.this, "Email no encontrado", Toast.LENGTH_LONG).show();
                        } catch (FirebaseAuthException e) {
                            Toast.makeText(LoginController.this, "Datos incorrectos", Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        */
    }
}