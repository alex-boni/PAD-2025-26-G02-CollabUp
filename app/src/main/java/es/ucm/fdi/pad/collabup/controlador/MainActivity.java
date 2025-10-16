package es.ucm.fdi.pad.collabup.controlador;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import es.ucm.fdi.pad.collabup.R;
// Se necesita la clase AuxFirebase para interactuar con Firebase.
// import com.example.ourleagues.modelo.herramienta.AuxFirebase;
// Import para el usuario de Firebase.
// import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // Variables para los elementos de la interfaz
    private Button btnIrLogin;
    private Button btnIrSingup;

    /*
     * Se comenta la lógica de Firebase ya que requiere una configuración
     * previa en el proyecto y la clase AuxFirebase.
     */
    // private AuxFirebase auxFirebase = new AuxFirebase();
    // private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnIrLogin = findViewById(R.id.btnIrLogin);
        btnIrSingup = findViewById(R.id.btnIrSingup);

        btnIrLogin.setOnClickListener(this);
        btnIrSingup.setOnClickListener(this);

        /*
         * Esta sección comprueba si hay un usuario autenticado.
         * Si lo hay, inicia directamente la actividad principal de la app (AppController).
         * Se comenta porque depende de la configuración de Firebase.
         */
        // user = auxFirebase.getAuth().getCurrentUser();
        // if (user != null) {
        //     startActivity(new Intent(this, AppController.class));
        //     finish(); // Cierra MainActivity para que el usuario no pueda volver con el botón de retroceso.
        // }
    }

    @Override
    public void onClick(View view) {
        // El switch reemplaza al 'when' de Kotlin para manejar los clics.
        // La comparación se hace sobre el ID del elemento pulsado.
        if (view.getId() == R.id.btnIrSingup) {
            startActivity(new Intent(this, SingupController.class));
        } else if (view.getId() == R.id.btnIrLogin) {
            startActivity(new Intent(this, LoginController.class));
        }
    }
}