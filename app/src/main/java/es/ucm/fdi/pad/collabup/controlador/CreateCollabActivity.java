package es.ucm.fdi.pad.collabup.controlador;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date; // Import java.util.Date

import es.ucm.fdi.pad.collabup.R;
import es.ucm.fdi.pad.collabup.modelo.Collab; // Importa el modelo

public class CreateCollabActivity extends AppCompatActivity {

    private EditText eTxtNombreCollab;
    private EditText eTxtDescripcionCollab;
    private Button btnCrearCollab;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_collab);

        // Inicializar vistas
        eTxtNombreCollab = findViewById(R.id.eTxtNombreCollab);
        eTxtDescripcionCollab = findViewById(R.id.eTxtDescripcionCollab);
        btnCrearCollab = findViewById(R.id.btnCrearCollab);

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Configurar listener del botón
        btnCrearCollab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                crearNuevoCollab();
            }
        });
    }

    private void crearNuevoCollab() {
        String nombre = eTxtNombreCollab.getText().toString().trim();
        String descripcion = eTxtDescripcionCollab.getText().toString().trim();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Validaciones
        if (nombre.isEmpty()) {
            eTxtNombreCollab.setError("El nombre es requerido");
            return;
        }
        if (descripcion.isEmpty()) {
            eTxtDescripcionCollab.setError("La descripción es requerida");
            return;
        }
        if (currentUser == null) {
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear el objeto Collab
        String uidCreador = currentUser.getUid();
        Collab nuevoCollab = new Collab(nombre, descripcion, uidCreador, new Date());

        // Guardar en Firestore en la colección "collabs"
        // .add() crea un documento con un ID automático
        db.collection("collabs").add(nuevoCollab)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(CreateCollabActivity.this, "Collab creado con éxito", Toast.LENGTH_SHORT).show();
                    finish(); // Cierra la actividad y vuelve al fragmento
                })
                .addOnFailureListener(e -> Toast.makeText(CreateCollabActivity.this, "Error al crear: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}