package es.ucm.fdi.pad.collabup.controlador.fragmento;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import es.ucm.fdi.pad.collabup.R;

//VISTA DEL COLLABITEM INDIVIDUAL -> Incluirá botones de editar y eliminar
public class CollabItemActivity extends AppCompatActivity {

    //--------------Atributos base de datos
    private FirebaseFirestore db;

    //------------- Atributos vista
    private EditText eTxtNombreCollabItem, eTxtDescripcionCollabItem, eTxtFechaCollabItem;

    private Button btnEditarCollabItem, btnGuardarCollabItem, btnEliminarCollabItem;
    private ListView lvUsrsAsigCollabItem, lvEtiqCollabItem;

    //------------
    private String idI; //id del item que estoy viendo

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
        idI = getIntent().getStringExtra("idI"); //todo MODULO COLLAB pasar parametros
        if (idI == null) {
            idI = ""; // o algún valor por defecto
            idI = "kUzMBWEq7JkErjPwwqLL"; //para hacerlo ahora
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

}
