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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import es.ucm.fdi.pad.collabup.R;
import es.ucm.fdi.pad.collabup.modelo.Collab;
import es.ucm.fdi.pad.collabup.modelo.collabView.AbstractCollabView;
import es.ucm.fdi.pad.collabup.modelo.collabView.Calendario;
import es.ucm.fdi.pad.collabup.modelo.collabView.CollabItem;
import es.ucm.fdi.pad.collabup.modelo.collabView.CollabView;
import es.ucm.fdi.pad.collabup.modelo.interfaz.OnDataLoadedCallback;
import es.ucm.fdi.pad.collabup.modelo.interfaz.OnOperationCallback;

//Llamado cuando vayamos a añadir evento
public class CreateCollabItemFragment extends Fragment {

    // ---------Atributos vista
    private EditText eTxtNombreCollabItem, eTxtDescripcionCollabItem, eTxtFechaCollabItem;
    private Button btnSeleccionMiembros, btnSeleccionCV;
    private ListView lvUsrsAsigCollabItem, lvCvAsigCollabItem;
    private Button btnCrearCollabItem;
    private MaterialToolbar toolbar;


    //---------- Atributos necesarios
    private String idC; //id del collab en el que estamos
    private Collab c; // collab desde donde se añade el item
    private ArrayList<String> miembros; //miembros del collab
    private List<String> miembrosElegidos = new ArrayList<>(); // auxiliar
    private ArrayList<String> cv = new ArrayList<>(); //collabviews del collab
    private List<String> cvElegidas = new ArrayList<>(); // auxiliar
    private Map<String, String> idNombreMiembros = new HashMap<>(); //para los nombres
    private Map<String, CollabView> idCv = new HashMap<>();

    public CreateCollabItemFragment() {
    }


    //Crea fragment, mete los argumentos necesarios y devuelve el fragmento listo para usarse.
    public static CreateCollabItemFragment newInstance(String idC) {
        CreateCollabItemFragment fragment = new CreateCollabItemFragment();
        Bundle args = new Bundle();
        args.putString("idC", idC);
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

        // Listas
        lvUsrsAsigCollabItem = view.findViewById(R.id.lvUsrsAsigCollabItem);
        lvCvAsigCollabItem = view.findViewById(R.id.lvCVAsigCollabItem);

        toolbar = view.findViewById(R.id.toolbarCollabItem);

        toolbar.setNavigationOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });


        // Recuperamos argumentos del fragment
        Bundle bundle = getArguments();
        if (bundle != null) {
            idC = bundle.getString("idC");
        }

        btnSeleccionMiembros.setOnClickListener(v -> seleccionarMiembros());
        btnSeleccionCV.setOnClickListener(v -> seleccionarCV());
        btnCrearCollabItem.setOnClickListener(v -> crearNuevoCollabItem());
        btnSeleccionMiembros.setEnabled(false);
        btnSeleccionCV.setEnabled(false);
        btnCrearCollabItem.setEnabled(false);

        cargarDatos();
    }

    private void cargarDatos() {
        //Necesitamos saber los miembros que tiene el collab, y las collab views del collab
        //Cargo la collab
        c = new Collab();
        c.obtener(idC, new OnDataLoadedCallback<Collab>() {
            @Override
            public void onSuccess(Collab data) {
                c = data;
                miembros = data.getMiembros();

                new CollabItem().obtenerNombresMiembrosCollab(miembros, new OnDataLoadedCallback<Map<String, String>>() {
                    @Override
                    public void onSuccess(Map<String, String> data) {
                        idNombreMiembros.putAll(data);
                        //Saco lista de collab views del collab
                        AbstractCollabView aux = new Calendario();
                        aux.setCollabId(idC);

                        btnSeleccionMiembros.setEnabled(true);
                        btnCrearCollabItem.setEnabled(true);
                        btnSeleccionCV.setEnabled(true);
                        mostrarDatosCollabItem();
/*
                        aux.obtenerListado(new OnDataLoadedCallback<ArrayList<CollabView>>() {

                            @Override
                            public void onSuccess(ArrayList<CollabView> data) {
                                if(data.isEmpty()){
                                    btnSeleccionMiembros.setEnabled(true);
                                    btnCrearCollabItem.setEnabled(true);
                                    btnSeleccionCV.setEnabled(true);
                                    mostrarDatosCollabItem();
                                    //todo salir de esto no?
                                }
                                for (CollabView auxCV : data) {
                                    cv.add(auxCV.getUid()); //lista con los ids de los collabViews
                                    idCv.put(auxCV.getUid(), auxCV);
                                }

                                btnSeleccionMiembros.setEnabled(true);
                                btnCrearCollabItem.setEnabled(true);
                                btnSeleccionCV.setEnabled(true);
                                mostrarDatosCollabItem();

                            }

                            @Override
                            public void onFailure(Exception e) {
                                Toast.makeText(requireContext(), "Error al cargar collabViews: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

 */


                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(requireContext(), "Error al cargar miembros: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(requireContext(), "Error al cargar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

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
            miembrosElegidos = new CollabItem().obtenerIdsObjSeleccionados(miembros, seleccionados);
            actualizarListaUsuarios();
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void mostrarDatosCollabItem() {
        boolean[] seleccionados = new boolean[miembros.size()];
        for (int i = 0; i < miembros.size(); i++) {
            seleccionados[i] = miembrosElegidos.contains(miembros.get(i));
        }
        actualizarListaUsuarios();
        actualizarListaCV();
    }


    private void actualizarListaUsuarios() {
        List<String> nombresAsignados = new CollabItem().obtenerNombresDeMapaId(idNombreMiembros, miembrosElegidos);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1,
                nombresAsignados);
        lvUsrsAsigCollabItem.setAdapter(adapter);
        btnSeleccionMiembros.setText(nombresAsignados.isEmpty() ? "Seleccionar miembros" :
                "Miembros: " + nombresAsignados.size());
    }


    private void seleccionarCV() {
        boolean[] cvseleccionados = new boolean[cv.size()];
        for (int i = 0; i < cv.size(); i++)
            cvseleccionados[i] = cvElegidas.contains(cv.get(i));


        String[] nombres = cv.stream()
                .map(id -> {
                    CollabView cvObj = idCv.get(id);
                    return (cvObj != null) ? cvObj.getName() : null;
                })
                .toArray(String[]::new);


        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Selecciona Collab Views");
        builder.setMultiChoiceItems(nombres, cvseleccionados,
                (dialog, which, isChecked) -> cvseleccionados[which] = isChecked);
        builder.setPositiveButton("OK", (dialog, which) -> {
            cvElegidas = new CollabItem().obtenerIdsObjSeleccionados(cv, cvseleccionados);
            actualizarListaCV();
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }


    private void actualizarListaCV() {
        List<String> nombresAsignados = new CollabItem().obtenerNombresDeMapaCVId(idCv, cvElegidas);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1,
                nombresAsignados);
        lvUsrsAsigCollabItem.setAdapter(adapter);
        btnSeleccionMiembros.setText(nombresAsignados.isEmpty() ? "Seleccionar miembros" :
                "Miembros: " + nombresAsignados.size());
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
                //Necesito añadirlo a la lista de collabItems asignados a cada view.
                // Actualizar cada CollabView
                AtomicInteger contador = new AtomicInteger(0); //por firebase
                if (cvElegidas.isEmpty()) {
                    requireActivity().getSupportFragmentManager().popBackStack();
                }
                for (String cvId : cvElegidas) {
                    CollabView cvaux = idCv.get(cvId);
                    cvaux.populate(nuevo, new OnOperationCallback() {
                        @Override
                        public void onSuccess() {
                            if (contador.incrementAndGet() == cvElegidas.size()) {// Todas las collabViews han sido actualizadas
                                requireActivity().getSupportFragmentManager().popBackStack();
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(requireContext(), "Error al actualizar CV: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            if (contador.incrementAndGet() == cvElegidas.size()) {
                                // Aunque haya errores, cerramos al final
                                requireActivity().getSupportFragmentManager().popBackStack();
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

