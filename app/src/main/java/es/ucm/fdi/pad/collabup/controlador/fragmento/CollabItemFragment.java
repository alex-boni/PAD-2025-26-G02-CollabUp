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
import java.util.List;
import java.util.Locale;

import es.ucm.fdi.pad.collabup.R;
import es.ucm.fdi.pad.collabup.modelo.Etiqueta;
import es.ucm.fdi.pad.collabup.modelo.collabView.CollabItem;
import es.ucm.fdi.pad.collabup.modelo.interfaz.OnDataLoadedCallback;
import es.ucm.fdi.pad.collabup.modelo.interfaz.OnOperationCallback;

//VISTA DEL COLLABITEM INDIVIDUAL -> Incluirá botones de editar y eliminar
public class CollabItemFragment extends Fragment {

    //------------- Atributos vista
    private EditText eTxtNombreCollabItem, eTxtDescripcionCollabItem, eTxtFechaCollabItem;

    private Button btnEditarCollabItem, btnGuardarCollabItem, btnEliminarCollabItem, btnSeleccionMiembros, btnSeleccionCV;
    private ListView lvUsrsAsigCollabItem, lvEtiqCollabItem, lvCvAsigCollabItem;
    private MaterialToolbar toolbar;

    //------------
    private String idI; //el que estoy viendo
    private String idC; //la collab del item

    //Selecciones:
    private List<String> miembros = new ArrayList<>(); //elementos posibles
    private List<String> cv = new ArrayList<>();

    private boolean[] seleccionados; //los seleccionados
    private boolean[] cvseleccionados;

    private List<String> miembrosElegidos = new ArrayList<>(); //los asignados actualmente
    private List<String> cvElegidas = new ArrayList<>();


    public CollabItemFragment() {
    }

    public static CollabItemFragment newInstance(String idI, String idC, ArrayList<String> miembros, ArrayList<String> collabViews) {
        CollabItemFragment fragment = new CollabItemFragment();
        Bundle args = new Bundle();
        args.putString("idI", idI);
        args.putString("idC", idC);
        args.putStringArrayList("miembros", miembros);
        args.putStringArrayList("collabViews", collabViews);
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
        lvEtiqCollabItem = view.findViewById(R.id.lvEtiqCollabItem);
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
            miembros = bundle.getStringArrayList("miembros");
            cv = bundle.getStringArrayList("collabViews");
            cv = new ArrayList<>(); // TODO: quitar cuando funcionen las collab views
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

        // Cargar datos del CollabItem
        CollabItem collabItemModel = new CollabItem();
        collabItemModel.setIdI(idI);
        collabItemModel.setIdC(idC);
        collabItemModel.setcvAsignadas(Arrays.asList(idC));
        collabItemModel.obtener(idI, new OnDataLoadedCallback<CollabItem>() {
            @Override
            public void onSuccess(CollabItem item) {
                if (item != null) mostrarDatosCollabItem(item);
                else eTxtNombreCollabItem.setText("Item no encontrado");
            }

            @Override
            public void onFailure(Exception e) {
                eTxtNombreCollabItem.setText("Error al cargar: " + e.getMessage());
            }
        });
    }

    private void seleccionarMiembros() {
        seleccionados = new boolean[miembros.size()];
        for (int i = 0; i < miembros.size(); i++)
            seleccionados[i] = miembrosElegidos.contains(miembros.get(i));

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Selecciona los miembros");
        builder.setMultiChoiceItems(miembros.toArray(new String[0]), seleccionados,
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
        cvseleccionados = new boolean[cv.size()];
        for (int i = 0; i < cv.size(); i++)
            cvseleccionados[i] = cvElegidas.contains(cv.get(i));

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Selecciona Collab Views");
        builder.setMultiChoiceItems(cv.toArray(new String[0]), cvseleccionados,
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
        List<Etiqueta> etAsig = new ArrayList<>(); //todo etiquetas

        if (nombre.isEmpty()) {
            Toast.makeText(requireContext(), "Por favor ponle un nombre al CollabItem", Toast.LENGTH_SHORT).show();
            return null;
        }

        CollabItem ci = new CollabItem(nombre, descripcion, fecha, usrsAsig, etAsig, idC, collabAsig);
        ci.setIdI(idI);

        return ci;
    }

    //----Modificación de listas
    private void actualizarListaUsuarios() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1,
                miembrosElegidos);
        lvUsrsAsigCollabItem.setAdapter(adapter);
        btnSeleccionMiembros.setText(miembrosElegidos.isEmpty() ? "Seleccionar miembros" : "Miembros: " + miembrosElegidos.size());
    }

    private void actualizarListaCV() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1,
                cvElegidas);
        lvCvAsigCollabItem.setAdapter(adapter);

        btnSeleccionCV.setText(cvElegidas.isEmpty() ? "Seleccionar Collab Views" : "Collab Views: " + cvElegidas.size());
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
        if (item.getUsuariosAsignados() != null) { //usuarios asignados
            miembrosElegidos.clear();
            miembrosElegidos.addAll(item.getUsuariosAsignados());
            seleccionados = new boolean[miembros.size()];
            for (int i = 0; i < miembros.size(); i++) {
                seleccionados[i] = miembrosElegidos.contains(miembros.get(i));
            }
            actualizarListaUsuarios();
        }
        if (item.getcvAsignadas() != null) { //collabViews asignadas
            cvElegidas.clear();
            cvElegidas.addAll(item.getcvAsignadas());
            cvseleccionados = new boolean[cv.size()];
            for (int i = 0; i < cv.size(); i++) {
                cvseleccionados[i] = cvElegidas.contains(cv.get(i));
            }
            actualizarListaCV();
        }
        if (item.getEtiquetasItem() != null) { //etiquetas
            ArrayAdapter<Etiqueta> adapter = new ArrayAdapter<>(
                    requireContext(),
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
