package es.ucm.fdi.pad.collabup.controlador.fragmento;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.Timestamp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import es.ucm.fdi.pad.collabup.R;
import es.ucm.fdi.pad.collabup.modelo.Usuario;
import es.ucm.fdi.pad.collabup.modelo.collabView.CollabItem;
import es.ucm.fdi.pad.collabup.modelo.interfaz.OnDataLoadedCallback;
import es.ucm.fdi.pad.collabup.modelo.interfaz.OnOperationCallback;

//VISTA DEL COLLABITEM INDIVIDUAL -> Incluirá botones de editar y eliminar
public class CollabItemFragment extends Fragment {

    //------------- Atributos vista
    private EditText eTxtNombreCollabItem, eTxtDescripcionCollabItem, eTxtFechaCollabItem;

    private Button btnEditarCollabItem, btnGuardarCollabItem, btnEliminarCollabItem, btnSeleccionMiembros, btnSeleccionCV;
    private ListView lvUsrsAsigCollabItem, lvCvAsigCollabItem;
    private MaterialToolbar toolbar;

    //------------
    private String idI; //el que estoy viendo
    private CollabItem ci; //collabitem seleccionado

    private String idC; //la collab del item

    //ATRIBUTOS DE SELECCIÓN (miembros y collab views)
    private List<String> miembros = new ArrayList<>(); //elementos posibles
    private List<String> cv = new ArrayList<>();
    private List<String> miembrosElegidos = new ArrayList<>(); //los asignados actualmente
    private List<String> cvElegidas = new ArrayList<>();
    private Map<String, String> idNombreMiembros = new HashMap<>(); //para los nombres
    private Map<String, String> idNombreCv = new HashMap<>();


    public CollabItemFragment() {
    }

    public static CollabItemFragment newInstance(String idI, String idC) {
        CollabItemFragment fragment = new CollabItemFragment();
        Bundle args = new Bundle();
        args.putString("idI", idI);
        args.putString("idC", idC);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_collabitem, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Textos
        eTxtNombreCollabItem = view.findViewById(R.id.eTxtNombreCollabItem);
        eTxtDescripcionCollabItem = view.findViewById(R.id.eTxtDescripcionCollabItem);
        eTxtFechaCollabItem = view.findViewById(R.id.eTxtFechaCollabItem);

        // Listas
        lvUsrsAsigCollabItem = view.findViewById(R.id.lvUsrsAsigCollabItem);
        lvCvAsigCollabItem = view.findViewById(R.id.lvCVAsigCollabItem);

        // Botones
        btnEditarCollabItem = view.findViewById(R.id.btnEditarCollabItem);
        btnGuardarCollabItem = view.findViewById(R.id.btnGuardarCollabItem);
        btnEliminarCollabItem = view.findViewById(R.id.btnEliminarCollabItem);
        btnSeleccionMiembros = view.findViewById(R.id.btnSeleccionMiembros);
        btnSeleccionCV = view.findViewById(R.id.btnSeleccionCV);

        toolbar = view.findViewById(R.id.toolbarCollabItem);
        toolbar.setNavigationOnClickListener(v -> {
            getParentFragmentManager().popBackStack(); //para poder volver atrás
        });


        // Recuperar argumentos
        Bundle bundle = getArguments();
        if (bundle != null) {
            idI = bundle.getString("idI");
            idC = bundle.getString("idC");
        }

        // Configuramos botones y listas (igual que antes)
        btnEditarCollabItem.setOnClickListener(v -> {
            setEditable(true);
            btnEditarCollabItem.setVisibility(View.GONE);
            btnGuardarCollabItem.setVisibility(View.VISIBLE);
        });

        btnGuardarCollabItem.setOnClickListener(v -> {
            modificarCollabItem();
            setEditable(false);
        });

        btnEliminarCollabItem.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Eliminar CollabItem")
                    .setMessage("¿Estás seguro de que quieres eliminar este CollabItem?")
                    .setPositiveButton("Sí", (dialog, which) -> eliminarCollabItem())
                    .setNegativeButton("No", null)
                    .show();
        });

        btnSeleccionMiembros.setOnClickListener(v -> seleccionarMiembros());
        btnSeleccionCV.setOnClickListener(v -> seleccionarCV());

        //cargamos el item y se muestra en la pantalla
        cargarCollabItem();
    }

    //--------------- FUNCIONES DE CARGAR DATOS

    private void cargarCollabItem() {
        ci = new CollabItem();
        ci.setIdI(idI);
        ci.setIdC(idC);
        ci.obtener(idI, new OnDataLoadedCallback<CollabItem>() {
            @Override
            public void onSuccess(CollabItem ciRet) {
                if (ciRet != null) {
                    ci = ciRet;
                    miembros = ci.getUsuariosAsignados();
                    cv = ci.getcvAsignadas();
                    AtomicInteger contador = new AtomicInteger(0);
                    for (String idUsr : miembros) {
                        obtenerNombreMiembroCollab(idUsr, () -> {
                            if (contador.incrementAndGet() == miembros.size()) {
                                mostrarDatosCollabItem(); //cuando todos los miembros están cargados
                            }
                        });
                    }
                } else eTxtNombreCollabItem.setText("Item no encontrado");
            }

            @Override
            public void onFailure(Exception e) {
                eTxtNombreCollabItem.setText("Error al cargar: " + e.getMessage());
            }
        });
    }

    private void obtenerNombreMiembroCollab(String idU, Runnable callback) {
        new Usuario().obtener(idU, new OnDataLoadedCallback<Usuario>() {
            @Override
            public void onSuccess(Usuario data) {
                idNombreMiembros.put(data.getUID(), data.getNombre());
                callback.run();
            }

            @Override
            public void onFailure(Exception e) {
                eTxtNombreCollabItem.setText("Error al cargar usuario: " + e.getMessage());
            }
        });
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

        List<String> usrsAsig = new ArrayList<>(miembrosElegidos);
        List<String> collabAsig = new ArrayList<>(cvElegidas);

        if (nombre.isEmpty()) {
            Toast.makeText(requireContext(), "Por favor ponle un nombre al CollabItem", Toast.LENGTH_SHORT).show();
            return null;
        }

        CollabItem ci = new CollabItem(nombre, descripcion, fecha, usrsAsig, idC, collabAsig);
        ci.setIdI(idI);

        return ci;
    }

    //--------- FUNCIONES DE SELECCION

    private void seleccionarMiembros() {
        boolean[] seleccionados = new boolean[miembros.size()];
        for (int i = 0; i < miembros.size(); i++)
            seleccionados[i] = miembrosElegidos.contains(miembros.get(i));


        String[] nombres = miembros.stream()
                .map(id -> idNombreMiembros.getOrDefault(id, id))
                .toArray(String[]::new);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Selecciona los miembros");
        builder.setMultiChoiceItems(nombres, seleccionados,
                (dialog, which, isChecked) -> seleccionados[which] = isChecked);
        builder.setPositiveButton("OK", (dialog, which) -> {
            miembrosElegidos.clear();
            for (int i = 0; i < seleccionados.length; i++)
                if (seleccionados[i]) miembrosElegidos.add(miembros.get(i));
            actualizarListaUsuarios();
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void seleccionarCV() {
        boolean[] cvseleccionados = new boolean[cv.size()];
        for (int i = 0; i < cv.size(); i++)
            cvseleccionados[i] = cvElegidas.contains(cv.get(i));

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Selecciona Collab Views");

        String[] nombres = cv.stream()
                .map(id -> idNombreCv.getOrDefault(id, id))
                .toArray(String[]::new);

        builder.setMultiChoiceItems(nombres, cvseleccionados,
                (dialog, which, isChecked) -> cvseleccionados[which] = isChecked);
        builder.setPositiveButton("OK", (dialog, which) -> {
            cvElegidas.clear();
            for (int i = 0; i < cvseleccionados.length; i++)
                if (cvseleccionados[i]) cvElegidas.add(cv.get(i));
            actualizarListaCV();
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }


    //Funcion para hacer los elementos generales de forma eficiente y escalable
    private void setEditable(boolean editable) {
        View root = requireView().findViewById(R.id.rootLayoutCollabItem); // el id del layout raíz de activity_collabitem.xml
        setEditableRecursive(root, editable);

        //mostramos o ocultamos botones de la selección
        if (editable) {
            btnSeleccionMiembros.setVisibility(View.VISIBLE);
            btnSeleccionCV.setVisibility(View.VISIBLE);
        } else {
            btnSeleccionMiembros.setVisibility(View.GONE);
            btnSeleccionCV.setVisibility(View.GONE);
        }
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


    //---- ACTUALIZACIONES DE INFORMACION
    private void actualizarListaUsuarios() {
        List<String> nombresAsignados = new ArrayList<>();
        for (String id : miembrosElegidos) {
            String nombre = idNombreMiembros.getOrDefault(id, id); // ponemos id en caso raro de que no haya nombre
            nombresAsignados.add(nombre);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1,
                nombresAsignados);
        lvUsrsAsigCollabItem.setAdapter(adapter);

        btnSeleccionMiembros.setText(nombresAsignados.isEmpty() ? "Seleccionar miembros" :
                "Miembros: " + nombresAsignados.size());
    }

    private void actualizarListaCV() {
        List<String> nombrescvAsignados = new ArrayList<>();
        for (String id : cvElegidas) {
            String nombre = idNombreCv.getOrDefault(id, id); // ponemos id en caso raro de que no haya nombre
            nombrescvAsignados.add(nombre);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1,
                nombrescvAsignados);
        lvCvAsigCollabItem.setAdapter(adapter);

        btnSeleccionCV.setText(nombrescvAsignados.isEmpty() ? "Seleccionar Collab Views" : "Collab Views: " + nombrescvAsignados.size());
    }


    //Función que se usa para mostrar los datos del item al que estamos viendo una vez ya tenemos
    //la información
    private void mostrarDatosCollabItem() {
        eTxtNombreCollabItem.setText(ci.getNombre());
        eTxtDescripcionCollabItem.setText(ci.getDescripcion());
        if (ci.getFecha() != null) {
            Date date = ci.getFecha().toDate();
            String fechaStr = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date);
            eTxtFechaCollabItem.setText(fechaStr);
        }
        if (ci.getUsuariosAsignados() != null) { //usuarios asignados
            miembrosElegidos.clear();
            miembrosElegidos.addAll(ci.getUsuariosAsignados());
            boolean[] seleccionados = new boolean[miembros.size()];
            for (int i = 0; i < miembros.size(); i++) {
                seleccionados[i] = miembrosElegidos.contains(miembros.get(i));
            }
            actualizarListaUsuarios();
        }
        if (ci.getcvAsignadas() != null) { //collabViews asignadas
            cvElegidas.clear();
            cvElegidas.addAll(ci.getcvAsignadas());
            boolean[] cvseleccionados = new boolean[cv.size()];
            for (int i = 0; i < cv.size(); i++) {
                cvseleccionados[i] = cvElegidas.contains(cv.get(i));
            }
            actualizarListaCV();
        }
    }

    //-------------- FUNCIONES EDITAR Y ELIMINAR

    private void modificarCollabItem() {

        CollabItem ciActualizado = obtenerCollabItemDePantalla();
        ciActualizado.modificar(ciActualizado, new OnOperationCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(requireContext(), "CollabItem actualizado correctamente", Toast.LENGTH_SHORT).show();
                setEditable(false);
                btnGuardarCollabItem.setVisibility(View.GONE);
                btnEditarCollabItem.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(requireContext(), "Error al actualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void eliminarCollabItem() {
        CollabItem ciEliminar = new CollabItem();
        ciEliminar.setIdI(this.idI);
        ciEliminar.setIdC(this.idC);
        ciEliminar.setcvAsignadas(Arrays.asList(idC));

        ciEliminar.eliminar(new OnOperationCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(requireContext(), "CollabItem eliminado correctamente", Toast.LENGTH_SHORT).show();
                getParentFragmentManager().popBackStack(); // cierro el fragment y vuelvo a la lista
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(requireContext(), "Error al eliminar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
}
