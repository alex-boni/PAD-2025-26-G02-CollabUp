package es.ucm.fdi.pad.collabup.controlador;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.Timestamp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import es.ucm.fdi.pad.collabup.R;
import es.ucm.fdi.pad.collabup.modelo.Etiqueta;
import es.ucm.fdi.pad.collabup.modelo.collabView.CollabItem;
import es.ucm.fdi.pad.collabup.modelo.interfaz.OnOperationCallback;

//Llamado cuando vayamos a añadir evento
public class CreateCollabItemActivity extends AppCompatActivity {

    // ---------Atributos vista
    private EditText eTxtNombreCollabItem, eTxtDescripcionCollabItem, eTxtFechaCollabItem;
    Button btnSeleccionMiembros, btnSeleccionCV;
    private Button btnCrearCollabItem;

    //---------- Atributos necesarios
    private String idC; //id del collab en el que estamos
    private ArrayList<String> miembros; //miembros del collab
    private boolean[] seleccionados; //miembros seleccionados para el item
    private List<String> miembrosElegidos = new ArrayList<>(); // auxiliar

    private ArrayList<String> cv; //collabviews del collab
    private boolean[] cvseleccionados; //cv seleccionados para el item
    private List<String> cvElegidas = new ArrayList<>(); // auxiliar


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_collabitem);

        // Inicializar vistas
        eTxtNombreCollabItem = findViewById(R.id.eTxtNombreCollabItem);
        eTxtDescripcionCollabItem = findViewById(R.id.eTxtDescripcionCollabItem);
        btnCrearCollabItem = findViewById(R.id.btnCrearCollabItem);
        eTxtFechaCollabItem = findViewById(R.id.eTxtFechaCollabItem);
        btnSeleccionMiembros = findViewById(R.id.btnSeleccionMiembros);
        btnSeleccionCV = findViewById(R.id.btnSeleccionCV);

        Toolbar toolbar = findViewById(R.id.toolbarCollabItem);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        //parámetros que llegan: idCollab, miembros del collab, collabViews
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            idC = bundle.getString("idC");
            miembros = bundle.getStringArrayList("miembros");
            cv = bundle.getStringArrayList("cv"); //todo faltan collabViews
            //todo por ahora hasta que llegue
            cv = new ArrayList<>();
        } else {
            idC = "ryO2NPfO9YaaWfNkhibD"; //por defecto //todo revisar
        }

        if (miembros != null) {
            seleccionados = new boolean[miembros.size()];
        }
        if (cv != null) {
            cvseleccionados = new boolean[cv.size()];
        }


        //Para seleccionar los miembros asignados al collabItem
        btnSeleccionMiembros.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Selecciona los miembros");

            builder.setMultiChoiceItems(
                    miembros.toArray(new String[0]), // elementos
                    seleccionados,                         // checkboxes
                    (dialog, which, isChecked) -> {
                        seleccionados[which] = isChecked;
                    }
            );

            builder.setPositiveButton("OK", (dialog, which) -> {
                miembrosElegidos.clear();

                for (int i = 0; i < seleccionados.length; i++) {
                    if (seleccionados[i]) {
                        miembrosElegidos.add(miembros.get(i));
                    }
                }

                btnSeleccionMiembros.setText(
                        miembrosElegidos.isEmpty()
                                ? "Seleccionar miembros"
                                : "Miembros: " + miembrosElegidos.size()
                );
            });

            builder.setNegativeButton("Cancelar", null);

            builder.show();
        });

        //Para seleccionar las CollabViews asignados al collabItem
        btnSeleccionCV.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Selecciona las Collab Views");

            builder.setMultiChoiceItems(
                    cv.toArray(new String[0]), // elementos
                    cvseleccionados,                         // checkboxes
                    (dialog, which, isChecked) -> {
                        cvseleccionados[which] = isChecked;
                    }
            );

            builder.setPositiveButton("OK", (dialog, which) -> {
                cvElegidas.clear();

                for (int i = 0; i < cvseleccionados.length; i++) {
                    if (cvseleccionados[i]) {
                        cvElegidas.add(cv.get(i));
                    }
                }

                btnSeleccionCV.setText(
                        cvElegidas.isEmpty()
                                ? "Seleccionar Collab Views"
                                : "Collab Views: " + cvElegidas.size()
                );
            });

            builder.setNegativeButton("Cancelar", null);

            builder.show();
        });

        // Configurar listener del botón
        btnCrearCollabItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                crearNuevoCollabItem();
            }
        });
    }

    //Para volver atrás
    @Override
    public boolean onSupportNavigateUp() {
        finish();  // vuelve a la vista anterior
        return true;
    }

    //Crear item en collab
    public void crearNuevoCollabItem() {

        String nombre = eTxtNombreCollabItem.getText().toString().trim();
        String descripcion = eTxtDescripcionCollabItem.getText().toString().trim();
        String fechastr = eTxtFechaCollabItem.getText().toString().trim();
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()); //todo posible cambiar
        Date date = null; // convierte el String a Date
        Timestamp fecha = null;
        if (!fechastr.isEmpty()) {
            try {
                date = formato.parse(fechastr);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            fecha = new Timestamp(date);
        }

        List<Etiqueta> easig = null;

        // Validaciones
        if (nombre.isEmpty()) {
            eTxtNombreCollabItem.setError("El nombre es requerido");
            return;
        }

        // Creamos el objeto CollabItem
        CollabItem nuevoCollabItem = new CollabItem(nombre, descripcion, fecha, miembrosElegidos, easig, cvElegidas);
        //Lo añadimos a la base de datos
        nuevoCollabItem.crear(new OnOperationCallback() {
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
