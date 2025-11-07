package es.ucm.fdi.pad.collabup.controlador;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import es.ucm.fdi.pad.collabup.R;
import es.ucm.fdi.pad.collabup.modelo.Etiqueta;
import es.ucm.fdi.pad.collabup.modelo.collabView.CollabItem;
import es.ucm.fdi.pad.collabup.modelo.collabView.DAOCollabItemImp;
import es.ucm.fdi.pad.collabup.modelo.interfaz.OnOperationCallback;

//Llamado cuando vayamos a añadir evento
public class CreateCollabItemActivity extends AppCompatActivity {

    // ---------Atributos vista
    private EditText eTxtNombreCollabItem, eTxtDescripcionCollabItem, eTxtFechaCollabItem;
    private Button btnCrearCollabItem;

    //---------- Atributos necesarios
    private String idC; //id del collab en el que estamos
    DAOCollabItemImp daoci;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_collabitem);

        daoci = new DAOCollabItemImp();

        // Inicializar vistas
        eTxtNombreCollabItem = findViewById(R.id.eTxtNombreCollabItem);
        eTxtDescripcionCollabItem = findViewById(R.id.eTxtDescripcionCollabItem);
        btnCrearCollabItem = findViewById(R.id.btnCrearCollabItem);
        eTxtFechaCollabItem = findViewById(R.id.eTxtFechaCollabItem);

        //Valores que llegan
        idC = getIntent().getStringExtra("idC");
        if (idC == "") {
            idC = ""; // o algún valor por defecto
        }
        //todo faltan


        // Configurar listener del botón
        btnCrearCollabItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                crearNuevoCollabItem();
            }
        });
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

        List<String> uasig = null; //todo MODULO COLLAB NECESITO ESTOS PARÁMETROS
        List<Etiqueta> easig = null;
        String idC = "G1rScmUcdWhg4T0D7HfA";

        // Validaciones
        if (nombre.isEmpty()) {
            eTxtNombreCollabItem.setError("El nombre es requerido");
            return;
        }

        // Creamos el objeto CollabItem
        CollabItem nuevoCollabItem = new CollabItem(nombre, descripcion, fecha, uasig, easig, idC);

        //Lo añadimos a la base de datos
        daoci.crear(nuevoCollabItem, new OnOperationCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(CreateCollabItemActivity.this, "CollabItem creado con éxito", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(CreateCollabItemActivity.this, "Error al crear: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

}
