package es.ucm.fdi.pad.collabup.controlador;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import es.ucm.fdi.pad.collabup.R;
import es.ucm.fdi.pad.collabup.modelo.Etiqueta;
import es.ucm.fdi.pad.collabup.modelo.collabView.CollabItem;
import es.ucm.fdi.pad.collabup.modelo.interfaz.OnDataLoadedCallback;
import es.ucm.fdi.pad.collabup.modelo.interfaz.OnOperationCallback;

//VISTA DEL COLLABITEM INDIVIDUAL -> Incluirá botones de editar y eliminar
public class CollabItemActivity extends AppCompatActivity {

    //------------- Atributos vista
    private EditText eTxtNombreCollabItem, eTxtDescripcionCollabItem, eTxtFechaCollabItem;

    private Button btnEditarCollabItem, btnGuardarCollabItem, btnEliminarCollabItem;
    private ListView lvUsrsAsigCollabItem, lvEtiqCollabItem;

    //------------
    private String idI; //el que estoy viendo
    private String idC; //la collab del item

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
        //(Para volver atrás)
        Button btnVolver = findViewById(R.id.btnVolverCollabItem);
        btnVolver.setOnClickListener(v -> finish());

        //Valores que llegan de arriba
        //todo mirar si se pasan con bundles (mejor)
        idI = getIntent().getStringExtra("idI");
        if (idI == null) {
            idI = ""; // o algún valor por defecto
            idI = "u9WP085b5Sf9jJloCBL9"; //para hacerlo ahora
        }
        idC = getIntent().getStringExtra("idC");
        if (idC == null) {
            idC = ""; // o algún valor por defecto
            idC = "Bt8zGlf5fevw4Tqej0Kn"; //para hacerlo ahora
        }


        //-----------Acciones botones
        //Editar
        btnEditarCollabItem.setOnClickListener(v -> {
            setEditable(true);
            btnEditarCollabItem.setVisibility(View.GONE);
            btnGuardarCollabItem.setVisibility(View.VISIBLE);
        });

        // Guardar
        btnGuardarCollabItem.setOnClickListener(v -> {
            modificarCollabItem();
            setEditable(false);
        });

        //Eliminar
        btnEliminarCollabItem.setOnClickListener(v -> {
            // Muestro diálogo de confirmación
            new androidx.appcompat.app.AlertDialog.Builder(CollabItemActivity.this)
                    .setTitle("Eliminar CollabItem")
                    .setMessage("¿Estás seguro de que quieres eliminar este CollabItem?")
                    .setPositiveButton("Sí", (dialog, which) -> {
                        eliminarCollabItem(); //elimino collabitem
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        CollabItem collabItemModel = new CollabItem();
        collabItemModel.setIdI(idI);
        collabItemModel.setCollabsAsignadas(Arrays.asList(idC));
        collabItemModel.cargarDatosCollabItem(new OnDataLoadedCallback<CollabItem>() {
            @Override
            public void onSuccess(CollabItem item) {
                if (item != null) {
                    mostrarDatosCollabItem(item);
                } else {
                    eTxtNombreCollabItem.setText("Item no encontrado");
                }
            }

            @Override
            public void onFailure(Exception e) {
                eTxtNombreCollabItem.setText("Error al cargar: " + e.getMessage());
            }
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

    private CollabItem obtenerCollabItemDePantalla() {

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

        //todo usuarios y etiquetas
        List<String> usrsAsig = new ArrayList<>();
        List<String> collabAsig = new ArrayList<>();
        List<Etiqueta> etAsig = new ArrayList<>();


        if (nombre.isEmpty()) {
            Toast.makeText(this, "Por favor ponle un nombre al CollabItem", Toast.LENGTH_SHORT).show();
            return null;
        }

        CollabItem ci = new CollabItem(nombre, descripcion, fecha, usrsAsig, etAsig, collabAsig);
        ci.setIdI(idI);

        return ci;
    }

    //-------------- Funciones base de datos
    //Función que se usa para mostrar los datos del item al que estamos viendo una vez ya tenemos
    //la información
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

    private void modificarCollabItem() {

        CollabItem ciActualizado = obtenerCollabItemDePantalla();

        ciActualizado.modificar(ciActualizado, new OnOperationCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(CollabItemActivity.this, "CollabItem actualizado correctamente", Toast.LENGTH_SHORT).show();
                setEditable(false);
                btnGuardarCollabItem.setVisibility(View.GONE);
                btnEditarCollabItem.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(CollabItemActivity.this, "Error al actualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void eliminarCollabItem() {
        CollabItem ciEliminar = new CollabItem();
        ciEliminar.setIdI(this.idI);
        ciEliminar.setCollabsAsignadas(Arrays.asList(idC));

        ciEliminar.eliminar(new OnOperationCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(CollabItemActivity.this, "CollabItem eliminado correctamente", Toast.LENGTH_SHORT).show();
                finish(); // cierro la activity y vuelvo a la lista
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(CollabItemActivity.this, "Error al eliminar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
}
