package es.ucm.fdi.pad.collabup.controlador.fragmento;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import es.ucm.fdi.pad.collabup.R;
import es.ucm.fdi.pad.collabup.modelo.collabView.CollabItem;
import es.ucm.fdi.pad.collabup.modelo.interfaz.OnOperationCallback;

//Llamado cuando vayamos a añadir evento
public class CreateCollabItemFragment extends Fragment {

    // ---------Atributos vista
    private EditText eTxtNombreCollabItem, eTxtDescripcionCollabItem, eTxtFechaCollabItem;
    Button btnSeleccionMiembros, btnSeleccionCV;
    private Button btnCrearCollabItem;
    private MaterialToolbar toolbar;


    //---------- Atributos necesarios
    private String idC; //id del collab en el que estamos
    private ArrayList<String> miembros; //miembros del collab
    private boolean[] seleccionados; //miembros seleccionados para el item
    private List<String> miembrosElegidos = new ArrayList<>(); // auxiliar

    private ArrayList<String> miembrosNombres; //nombres de los miembros del collab

    private ArrayList<String> cv; //collabviews del collab
    private ArrayList<String> cvNombres; //Nombres de collabviews del collab
    private boolean[] cvseleccionados; //cv seleccionados para el item
    private List<String> cvElegidas = new ArrayList<>(); // auxiliar

    public CreateCollabItemFragment() {
    }


    //Crea fragment, mete los argumentos necesarios y devuelve el fragmento listo para usarse.
    public static CreateCollabItemFragment newInstance(String idC, ArrayList<String> miembros, ArrayList<String>
            miembrosNombres, ArrayList<String> cv, ArrayList<String> cvNombres) {
        CreateCollabItemFragment fragment = new CreateCollabItemFragment();
        Bundle args = new Bundle();
        args.putString("idC", idC);
        args.putStringArrayList("miembros", miembros);
        args.putStringArrayList("miembrosNombres", miembrosNombres);
        args.putStringArrayList("cv", cv);
        args.putStringArrayList("cvNombres", cvNombres);
        fragment.setArguments(args);
        return fragment;
    }

    //Infla el xml del fragmento, devuelve la vista para que android la devuelva en la pantalla.
    //(como set content view)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_collabitem, container, false);
    }


    //Inicializamos las vistas, preparamos las acciones de los botones, y recuperamos los argumentos del otro fragment
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        eTxtNombreCollabItem = view.findViewById(R.id.eTxtNombreCollabItem);
        eTxtDescripcionCollabItem = view.findViewById(R.id.eTxtDescripcionCollabItem);
        eTxtFechaCollabItem = view.findViewById(R.id.eTxtFechaCollabItem);
        btnCrearCollabItem = view.findViewById(R.id.btnCrearCollabItem);
        btnSeleccionMiembros = view.findViewById(R.id.btnSeleccionMiembros);
        btnSeleccionCV = view.findViewById(R.id.btnSeleccionCV);

        toolbar = view.findViewById(R.id.toolbarCollabItem);

        toolbar.setNavigationOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });


        // Recuperamos argumentos del fragment
        Bundle bundle = getArguments();
        if (bundle != null) {
            idC = bundle.getString("idC");
            miembros = bundle.getStringArrayList("miembros");
            miembrosNombres = bundle.getStringArrayList("miembrosNombres");
            cv = bundle.getStringArrayList("cv");
            cvNombres = bundle.getStringArrayList("cvNombres");
        }

        if (miembros != null) seleccionados = new boolean[miembros.size()];
        if (cv != null) cvseleccionados = new boolean[cv.size()];

        btnSeleccionMiembros.setOnClickListener(v -> seleccionarMiembros());
        btnSeleccionCV.setOnClickListener(v -> seleccionarCV());

        btnCrearCollabItem.setOnClickListener(v -> crearNuevoCollabItem());
    }

    private void seleccionarMiembros() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Selecciona los miembros");

        //aqui van los nombres para que se muestren los nombres
        builder.setMultiChoiceItems(miembrosNombres.toArray(new String[0]), seleccionados,
                (dialog, which, isChecked) -> seleccionados[which] = isChecked
        );

        builder.setPositiveButton("OK", (dialog, which) -> {
            miembrosElegidos.clear();
            for (int i = 0; i < seleccionados.length; i++)
                if (seleccionados[i]) miembrosElegidos.add(miembros.get(i));

            btnSeleccionMiembros.setText(miembrosElegidos.isEmpty() ?
                    "Seleccionar miembros" : "Miembros: " + miembrosElegidos.size());
        });

        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void seleccionarCV() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Selecciona las CollabViews");

        builder.setMultiChoiceItems(cvNombres.toArray(new String[0]), cvseleccionados,
                (dialog, which, isChecked) -> cvseleccionados[which] = isChecked
        );

        builder.setPositiveButton("OK", (dialog, which) -> {
            cvElegidas.clear();
            for (int i = 0; i < cvseleccionados.length; i++)
                if (cvseleccionados[i]) cvElegidas.add(cv.get(i));

            btnSeleccionCV.setText(cvElegidas.isEmpty() ?
                    "Seleccionar CollabViews" : "CollabViews: " + cvElegidas.size());
        });

        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void crearNuevoCollabItem() {
        String nombre = eTxtNombreCollabItem.getText().toString().trim();
        String descripcion = eTxtDescripcionCollabItem.getText().toString().trim();
        String fechastr = eTxtFechaCollabItem.getText().toString().trim();

        if (nombre.isEmpty()) {
            eTxtNombreCollabItem.setError("El nombre es requerido");
            return;
        }

        Timestamp fecha = null;
        if (!fechastr.isEmpty()) {
            try {
                Date date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(fechastr);
                fecha = new Timestamp(date);
            } catch (Exception ignored) {
            }
        }

        CollabItem nuevo = new CollabItem(nombre, descripcion, fecha,
                miembrosElegidos, idC, cvElegidas);

        nuevo.crear(new OnOperationCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(requireContext(), "CollabItem creado", Toast.LENGTH_SHORT).show();
                requireActivity().getSupportFragmentManager().popBackStack();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

