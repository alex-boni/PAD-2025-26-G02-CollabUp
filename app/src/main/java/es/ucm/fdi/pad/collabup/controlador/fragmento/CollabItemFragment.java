package es.ucm.fdi.pad.collabup.controlador.fragmento;

import android.os.Bundle;
import android.util.Log;
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
    private boolean editable = false; //modo de edición o no.

    //ATRIBUTOS DE SELECCIÓN (miembros y collab views)
    private List<String> miembros = new ArrayList<>(); //elementos posibles
    private List<String> cv = new ArrayList<>();
    private List<String> miembrosElegidos = new ArrayList<>(); //los asignados actualmente
    private List<String> cvElegidas = new ArrayList<>();
    private Map<String, String> idNombreMiembros = new HashMap<>(); //para los nombres
    private Map<String, CollabView> idCv = new HashMap<>();


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
        eTxtFechaCollabItem.setOnClickListener(v -> {
            if (editable) { //quiero que se abra el picker solo si estamos en modo de edición
                mostrarDatePicker();
            }
        });

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
        toolbar.setTitle("Detalles del Collab Item");
        toolbar.setNavigationOnClickListener(v -> {
            getParentFragmentManager().popBackStack(); //para poder volver atrás
        });


        // Recupero argumentos
        Bundle bundle = getArguments();
        if (bundle != null) {
            idI = bundle.getString("idI");
            idC = bundle.getString("idC");
        }

        // Botones:
        btnEditarCollabItem.setOnClickListener(v -> {
            setEditable(true);
            btnEditarCollabItem.setVisibility(View.GONE);
            btnGuardarCollabItem.setVisibility(View.VISIBLE);
        });

        btnGuardarCollabItem.setOnClickListener(v -> {
            modificarCollabItem();
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
        setEditable(false); //para que en principio no se puedan editar
    }

    private void mostrarDatePicker() {
        final java.util.Calendar calendar = java.util.Calendar.getInstance();
        int year = calendar.get(java.util.Calendar.YEAR);
        int month = calendar.get(java.util.Calendar.MONTH);
        int day = calendar.get(java.util.Calendar.DAY_OF_MONTH);

        android.app.DatePickerDialog datePicker = new android.app.DatePickerDialog(
                requireContext(),
                (view, yearSelected, monthSelected, daySelected) -> {
                    // +1 porque los meses van del 0–11
                    String fecha = String.format(Locale.getDefault(),
                            "%02d/%02d/%04d",
                            daySelected,
                            monthSelected + 1,
                            yearSelected
                    );
                    eTxtFechaCollabItem.setText(fecha);
                },
                year, month, day
        );

        datePicker.show();
    }

    //--------------- FUNCIONES DE CARGAR DATOS

    private void cargarCollabItem() {
        ci = new CollabItem();
        ci.setIdI(idI);
        ci.setIdC(idC);

        Collab c = new Collab(); //Necesito saber los miembros que tiene la collab para poder enseñarlos.
        c.obtener(idC, new OnDataLoadedCallback<Collab>() {
            @Override
            public void onSuccess(Collab data) {
                miembros = data.getMiembros();

                //Ahora si obtengo el collab item que estamos viendo
                ci.obtener(idI, new OnDataLoadedCallback<CollabItem>() {
                    @Override
                    public void onSuccess(CollabItem ciRet) {
                        if (ciRet != null) {
                            ci = ciRet;
                            ci.obtenerNombresMiembrosCollab(miembros, new OnDataLoadedCallback<Map<String, String>>() {
                                @Override
                                public void onSuccess(Map<String, String> data) {
                                    idNombreMiembros.putAll(data);
                                    //Saco lista de collab views del collab
                                    AbstractCollabView aux = new Calendario();
                                    aux.setCollabId(idC);

                                    aux.obtenerListado(new OnDataLoadedCallback<ArrayList<CollabView>>() {

                                        @Override
                                        public void onSuccess(ArrayList<CollabView> data) {
                                            for (CollabView auxCV : data) {
                                                cv.add(auxCV.getUid()); //lista con los ids de los collabViews
                                                idCv.put(auxCV.getUid(), auxCV);
                                            }
                                            mostrarDatosCollabItem();
                                        }

                                        @Override
                                        public void onFailure(Exception e) {
                                            Toast.makeText(requireContext(), "Error al cargar collabViews: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    Toast.makeText(requireContext(), "Error al cargar miembros: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                        } else {
                            Toast.makeText(requireContext(), "Item no encontrado", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(requireContext(), "Error al cargar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(requireContext(), "Error al cargar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
            miembrosElegidos = ci.obtenerIdsObjSeleccionados(miembros, seleccionados);
            actualizarListaUsuarios();
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
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


    //Funcion para poder editar los elementos SOLO cuando corresponda
    private void setEditable(boolean editable) {
        this.editable = editable;

        setFieldEditable(eTxtNombreCollabItem, editable);
        setFieldEditable(eTxtDescripcionCollabItem, editable);
        setFieldEditable(eTxtFechaCollabItem, editable);

        btnSeleccionMiembros.setVisibility(editable ? View.VISIBLE : View.GONE);
        btnSeleccionCV.setVisibility(editable ? View.VISIBLE : View.GONE);
    }

    private void setFieldEditable(EditText editText, boolean editable) {
        editText.setFocusable(editable);
        editText.setFocusableInTouchMode(editable);
        editText.setClickable(editable);
    }

    //---- ACTUALIZACIONES DE INFORMACION
    private void actualizarListaUsuarios() {
        List<String> nombresAsignados = ci.obtenerNombresDeMapaId(idNombreMiembros, miembrosElegidos);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1,
                nombresAsignados);
        lvUsrsAsigCollabItem.setAdapter(adapter);
        setListViewHeightBasedOnItems(lvUsrsAsigCollabItem);
        btnSeleccionMiembros.setText(nombresAsignados.isEmpty() ? "Seleccionar miembros" :
                "Miembros: " + nombresAsignados.size());
    }

    private void actualizarListaCV() {
        List<String> nombresAsignados = new CollabItem().obtenerNombresDeMapaCVId(idCv, cvElegidas);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1,
                nombresAsignados);
        lvCvAsigCollabItem.setAdapter(adapter);
        setListViewHeightBasedOnItems(lvCvAsigCollabItem);
        btnSeleccionCV.setText(nombresAsignados.isEmpty() ? "Seleccionar Collab Views" :
                "CollabViews: " + nombresAsignados.size());
    }


    //Función que se usa para mostrar los datos del item al que estamos viendo una vez ya tenemos
    //la información
    private void mostrarDatosCollabItem() {
        Log.d("Calendario", "en mostrar item");
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

    //Por la vista
    private void setListViewHeightBasedOnItems(ListView listView) {
        ArrayAdapter adapter = (ArrayAdapter) listView.getAdapter();
        if (adapter == null) return;

        int totalHeight = 0;
        for (int i = 0; i < adapter.getCount(); i++) {
            View listItem = adapter.getView(i, null, listView);
            listItem.measure(
                    View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (adapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    //-------------- FUNCIONES EDITAR Y ELIMINAR

    private void modificarCollabItem() {
        List<String> prevCV = new ArrayList<>(ci.getcvAsignadas()); //saco las que había asignadas antes

        CollabItem ciActualizado = obtenerCollabItemDePantalla();
        //Si se ha seleccionado algún collabView y el item no tiene fecha no puede ser.
        Timestamp fecha = ciActualizado.getFecha();
        for (String cvId : ciActualizado.getcvAsignadas()) {
            CollabView cvaux = idCv.get(cvId);
            if (cvaux instanceof Calendario && fecha == null) {
                Toast.makeText(requireContext(),
                        "No puedes asignar este CollabItem a un Calendario sin fecha",
                        Toast.LENGTH_LONG).show();
                return; // se detiene la edición
            }
        }

        List<String> nuevasCV = new ArrayList<>(ciActualizado.getcvAsignadas());//saco las nuevas

        // Calculamos diferencias para actualizar el collabitem
        List<String> cvAañadir = new ArrayList<>(nuevasCV);
        cvAañadir.removeAll(prevCV);
        List<String> cvAEliminar = new ArrayList<>(prevCV);
        cvAEliminar.removeAll(nuevasCV);

        //actualizamos collab item de firebase
        ciActualizado.modificar(ciActualizado, new OnOperationCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(requireContext(), "CollabItem actualizado correctamente", Toast.LENGTH_SHORT).show();

                AtomicInteger operacionesPendientes = new AtomicInteger(cvAañadir.size() + cvAEliminar.size());

                if (operacionesPendientes.get() == 0) { //no hay nada que actualizar
                    finalizarModificacion(ciActualizado);
                    return;
                }

                //Añadimos los items a nuevas
                for (String cvId : cvAañadir) {
                    CollabView cvaux = idCv.get(cvId);
                    cvaux.populate(ciActualizado, new OnOperationCallback() {
                        @Override
                        public void onSuccess() {
                            if (operacionesPendientes.decrementAndGet() == 0) { //todos los cv han sido actualziados
                                finalizarModificacion(ciActualizado);
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(requireContext(), "Error al actualizar CV: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            if (operacionesPendientes.decrementAndGet() == 0) { //todos los cv han sido actualziados
                                finalizarModificacion(ciActualizado);
                            }
                        }
                    });
                }

                //Eliminamos los collab views que se han quitado
                for (String cvId : cvAEliminar) {
                    CollabView cvaux = idCv.get(cvId);
                    cvaux.remove(ciActualizado, new OnOperationCallback() {
                        @Override
                        public void onSuccess() {
                            if (operacionesPendientes.decrementAndGet() == 0) { //todos los cv han sido actualziados
                                finalizarModificacion(ciActualizado);
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(requireContext(), "Error al actualizar CV: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            if (operacionesPendientes.decrementAndGet() == 0) { //todos los cv han sido actualziados
                                finalizarModificacion(ciActualizado);
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(requireContext(), "Error al actualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void finalizarModificacion(CollabItem ciActualizado) {
        Toast.makeText(requireContext(), "CollabItem actualizado correctamente", Toast.LENGTH_SHORT).show();
        setEditable(false);
        btnGuardarCollabItem.setVisibility(View.GONE);
        btnEditarCollabItem.setVisibility(View.VISIBLE);
        requireActivity().getSupportFragmentManager().popBackStack();
    }


    private void eliminarCollabItem() {
        CollabItem ciEliminar = obtenerCollabItemDePantalla();
        ciEliminar.setIdI(this.idI);
        ciEliminar.setIdC(this.idC);

        ciEliminar.eliminar(new OnOperationCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(requireContext(), "CollabItem eliminado correctamente", Toast.LENGTH_SHORT).show();

                AtomicInteger operacionesPendientes = new AtomicInteger(ciEliminar.getcvAsignadas().size());
                if (ciEliminar.getcvAsignadas().isEmpty()) {
                    getParentFragmentManager().popBackStack(); // cierro el fragment y vuelvo a la lista
                    return;
                }
                for (String cvId : ciEliminar.getcvAsignadas()) {
                    CollabView cvaux = idCv.get(cvId);
                    cvaux.remove(ciEliminar, new OnOperationCallback() {
                        @Override
                        public void onSuccess() {
                            if (operacionesPendientes.decrementAndGet() == 0) { //todos los cv han sido actualziados
                                getParentFragmentManager().popBackStack(); // cierro el fragment y vuelvo a la lista
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(requireContext(), "Error al actualizar CV: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            if (operacionesPendientes.decrementAndGet() == 0) { //todos los cv han sido actualziados
                                getParentFragmentManager().popBackStack(); // cierro el fragment y vuelvo a la lista
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(requireContext(), "Error al eliminar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
}
