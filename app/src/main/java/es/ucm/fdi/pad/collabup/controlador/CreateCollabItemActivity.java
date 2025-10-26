package es.ucm.fdi.pad.collabup.controlador;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import es.ucm.fdi.pad.collabup.R;
import es.ucm.fdi.pad.collabup.modelo.Etiqueta;
import es.ucm.fdi.pad.collabup.modelo.collabView.CollabItem;
import es.ucm.fdi.pad.collabup.modelo.interfaz.OnDataLoadedCallback;

//Llamado cuando vayamos a añadir evento
public class CreateCollabItemActivity extends AppCompatActivity {

    //Atributos base de datos
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    // ---------Atributos vista
    private EditText eTxtNombreCollabItem;
    private EditText eTxtDescripcionCollabItem;
    private EditText eTxtFechaCollabItem;
    private Button btnCrearCollabItem;

    private String idC; //id del collab en el que estamos


    //---------------- FUNCIONES BASE DE DATOS

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_collabitem);

        // Inicializar vistas
        eTxtNombreCollabItem = findViewById(R.id.eTxtNombreCollabItem);
        eTxtDescripcionCollabItem = findViewById(R.id.eTxtDescripcionCollabItem);
        btnCrearCollabItem = findViewById(R.id.btnCrearCollabItem);
        eTxtFechaCollabItem = findViewById(R.id.eTxtFechaCollabItem);

        idC = getIntent().getStringExtra("idC"); //llega de arriba
        if (idC == "") {
            idC = ""; // o algún valor por defecto
        }

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Configurar listener del botón
        btnCrearCollabItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                crearNuevoCollabItem();
            }
        });
    }

    public void obtener(String identificador, OnDataLoadedCallback<CollabItem> callback) {

    }

    //Crear item en collab
    public void crearNuevoCollabItem() {

        String nombre = eTxtNombreCollabItem.getText().toString().trim();
        String descripcion = eTxtDescripcionCollabItem.getText().toString().trim();
        String fechastr = eTxtFechaCollabItem.getText().toString().trim();
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()); //todo posible cambiar
        Date date = null; // convierte el String a Date
        try {
            date = formato.parse(fechastr);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        Timestamp fecha = new Timestamp(date);

        List<String> uasig = null;
        List<Etiqueta> easig = null;
        String idC = "AqxjtEgzCnkKLuEZcX59"; //todo MODULO COLLAB necesito: id collab

        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Validaciones
        if (nombre.isEmpty()) {
            eTxtNombreCollabItem.setError("El nombre es requerido");
            return;
        }
        /*
        if (currentUser == null) {
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

         */

        // Crear el objeto CollabItem
        //String uidCreador = currentUser.getUid();
        CollabItem nuevoCollabItem = new CollabItem(nombre, descripcion, fecha, uasig, easig, idC);

        // Guardar en Firestore en la colección "collabItem" dentro de su "collabs"
        // .add() crea un documento con un ID automático
        db.collection("collabs")
                .document(idC)
                .collection("collabItem")
                .add(nuevoCollabItem)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(CreateCollabItemActivity.this, "CollabItem creado con éxito", Toast.LENGTH_SHORT).show();
                    finish(); // Cierra la actividad y vuelve al fragmento
                })
                .addOnFailureListener(e -> Toast.makeText(CreateCollabItemActivity.this, "Error al crear: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    public void modificar() {

    }


    public void eliminar() {

    }


    public void obtenerListado() {

    }
}
