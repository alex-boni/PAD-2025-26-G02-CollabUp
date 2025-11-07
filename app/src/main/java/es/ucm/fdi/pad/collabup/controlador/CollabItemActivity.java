package es.ucm.fdi.pad.collabup.controlador;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import es.ucm.fdi.pad.collabup.R;
import es.ucm.fdi.pad.collabup.modelo.Etiqueta;
import es.ucm.fdi.pad.collabup.modelo.collabView.CollabItem;

//VISTA DEL COLLABITEM INDIVIDUAL -> Incluirá botones de editar y eliminar
public class CollabItemActivity extends AppCompatActivity {

    //--------------Atributos base de datos
    private FirebaseFirestore db;

    //------------- Atributos vista
    private EditText eTxtNombreCollabItem, eTxtDescripcionCollabItem, eTxtFechaCollabItem;

    private Button btnEditarCollabItem, btnGuardarCollabItem, btnEliminarCollabItem;
    private ListView lvUsrsAsigCollabItem, lvEtiqCollabItem;

    //------------
    private CollabItem collabItem; //el que estoy viendo

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collabitem);

        //Textos
        eTxtNombreCollabItem = findViewById(R.id.eTxtNombreCollabItem);
        eTxtDescripcionCollabItem = findViewById(R.id.eTxtDescripcionCollabItem);
        eTxtFechaCollabItem = findViewById(R.id.eTxtFechaCollabItem);
        //Listas
        lvUsrsAsigCollabItem = findViewById(R.id.lvUsrsAsigCollabItem);
        lvEtiqCollabItem = findViewById(R.id.lvEtiqCollabItem);
        //Botones
        btnEditarCollabItem = findViewById(R.id.btnEditarCollabItem);
        btnGuardarCollabItem = findViewById(R.id.btnGuardarCollabItem);
        btnEliminarCollabItem = findViewById(R.id.btnEliminarCollabItem);

        //Valores que llegan de arriba

        /*
        idI = getIntent().getStringExtra("idI");
        if (idI == null) {
            idI = ""; // o algún valor por defecto
            idI = "YdpM0KRzLYP8ylrrvpwz"; //para hacerlo ahora
        }
         */
        //todo MODULO COLLAB pasar parametros
        collabItem = (CollabItem) getIntent().getSerializableExtra("collabItem");

        //Por ahora:
        String fechastr = "07/11/2025";
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()); //todo posible cambiar
        Date date = null; // convierte el String a Date
        try {
            date = formato.parse(fechastr);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        Timestamp fecha = new Timestamp(date);
        CollabItem collabItem = new CollabItem("Pruebas de CollabItem", "Aquí " +
                "hare todas las pruebas de collabItem", fecha, new ArrayList<String>(),
                new ArrayList<Etiqueta>(), "G1rScmUcdWhg4T0D7HfA");
        if (collabItem != null) {
            mostrarDatosCollabItem(collabItem);
        }

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();

        //-----------Acciones botones
        //Editar
        btnEditarCollabItem.setOnClickListener(v -> {
            setEditable(true);
            btnEditarCollabItem.setVisibility(View.GONE);
            btnGuardarCollabItem.setVisibility(View.VISIBLE);
        });

        // Guardar
        btnGuardarCollabItem.setOnClickListener(v -> {
            //todo update tabla
            setEditable(false);
            btnGuardarCollabItem.setVisibility(View.GONE);
            btnEditarCollabItem.setVisibility(View.VISIBLE);
        });

        //Eliminar
        btnEditarCollabItem.setOnClickListener(v -> {
            //todo eliminar item
        });

    }

    //Funcion para hacer los elementos generales de forma eficiente y escalable
    private void setEditable(boolean editable) {
        View root = findViewById(R.id.rootLayoutCollabItem); // el id del layout raíz de activity_collabitem.xml
        setEditableRecursive(root, editable);
    }

    private void setEditableRecursive(View view, boolean editable) {
        if (view instanceof EditText) {
            view.setEnabled(editable);
        } else if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                setEditableRecursive(group.getChildAt(i), editable);
            }
        }
    }

    //-------------- Funciones base de datos
    private void modificarCollabItem() {

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

    }

    private void mostrarDatosCollabItem(CollabItem item) {
        eTxtNombreCollabItem.setText(item.getNombre());
        eTxtDescripcionCollabItem.setText(item.getDescripcion());

        if (item.getFecha() != null) {
            Date date = item.getFecha().toDate();
            String fechaStr = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date);
            eTxtFechaCollabItem.setText(fechaStr);
        }

        if (item.getUsuariosAsignados() != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_list_item_1,
                    item.getUsuariosAsignados()
            );
            lvUsrsAsigCollabItem.setAdapter(adapter);
        }

        if (item.getEtiquetasItem() != null) {
            ArrayAdapter<Etiqueta> adapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_list_item_1,
                    item.getEtiquetasItem()
            );
            lvEtiqCollabItem.setAdapter(adapter);
        }
    }


    /*
    Esto no funciona porque necesito tb el id del collab, pero creo que es mejor simplemente
    pasar el collabitem por pantalla
    private void cargarDatosCollabItem() {
        db.collection("collabItems").document(idI)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String nombre = documentSnapshot.getString("nombre");
                        String descripcion = documentSnapshot.getString("descripcion");

                        // Si la fecha se guarda como Timestamp:
                        Timestamp timestamp = documentSnapshot.getTimestamp("fecha");
                        String fechaFormateada = "";
                        if (timestamp != null) {
                            Date date = timestamp.toDate();
                            SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                            fechaFormateada = formato.format(date);
                        }

                        // Mostrar en los EditText
                        eTxtNombreCollabItem.setText(nombre);
                        eTxtDescripcionCollabItem.setText(descripcion);
                        eTxtFechaCollabItem.setText(fechaFormateada);

                        // Si tienes una lista de usuarios asignados:
                        java.util.List<String> usuariosAsignados = (java.util.List<String>) documentSnapshot.get("usuariosAsignados");
                        if (usuariosAsignados != null) {
                            // Muestra los nombres en la lista o adaptador
                            mostrarUsuariosAsignados(usuariosAsignados);
                        }

                    } else {
                        eTxtNombreCollabItem.setText("No encontrado");
                    }
                })
                .addOnFailureListener(e -> {
                    eTxtNombreCollabItem.setText("Error al cargar");
                });
    }

    private void mostrarUsuariosAsignados(java.util.List<String> usuarios) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                usuarios
        );
        lvUsrsAsigCollabItem.setAdapter(adapter);

     */


}
