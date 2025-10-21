package es.ucm.fdi.pad.collabup.controlador;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import es.ucm.fdi.pad.collabup.R;

// Imports de Firebase Authentication
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // Variables para los elementos de la interfaz
    private Button btnIrLogin;
    private Button btnIrSingup;

    // Variable para emplear Firebase Authentication
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnIrLogin = findViewById(R.id.btnIrLogin);
        btnIrSingup = findViewById(R.id.btnIrSingup);

        btnIrLogin.setOnClickListener(this);
        btnIrSingup.setOnClickListener(this);

        // Inicializo Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        /*
         * Esta sección comprueba si hay un usuario autenticado.
         * Si lo hay, inicia directamente la actividad principal de la app (AppController).
         */
        user = mAuth.getCurrentUser();
        if (user != null) {
            startActivity(new Intent(this, AppController.class));
            finish(); // Cierra MainActivity para que el usuario no pueda volver con el botón de retroceso.
        }
    }

    @Override
    public void onClick(View view) {
        // El switch reemplaza al 'when' de Kotlin para manejar los clics.
        if (view.getId() == R.id.btnIrSingup) {
            startActivity(new Intent(this, SingupController.class));
        } else if (view.getId() == R.id.btnIrLogin) {
            startActivity(new Intent(this, LoginController.class));
        }
    }
}