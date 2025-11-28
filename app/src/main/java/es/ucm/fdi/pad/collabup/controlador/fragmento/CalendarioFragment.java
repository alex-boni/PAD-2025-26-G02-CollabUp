package es.ucm.fdi.pad.collabup.controlador.fragmento;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import es.ucm.fdi.pad.collabup.R;
import es.ucm.fdi.pad.collabup.modelo.Collab;
import es.ucm.fdi.pad.collabup.modelo.CollabItem;
import es.ucm.fdi.pad.collabup.modelo.Usuario;
import es.ucm.fdi.pad.collabup.modelo.adapters.CollabItemAdapter;
import es.ucm.fdi.pad.collabup.modelo.interfaz.OnDataLoadedCallback;

public class CalendarioFragment extends Fragment {

    private CalendarView calendarView;
    private RecyclerView recyclerView;
    private CollabItemAdapter adapter;

    //Argumentos opcionales (para poder reutilizar este fragment)
    private static final String ARG_COLLAB_ID = "idC";
    private static final String ARG_COLLABVIEW_ID = "idCV";
    private static final String ARG_ITEMS_ID = "idItems";
    private static final String TITULO_DEFECTO = "Calendario Collab View";
    private String idC;
    private String idCV;
    private List<String> idItems;
    private boolean general = true; //para saber si estamos en el calendario general o no

    List<CollabItem> lci = new ArrayList<>();


    //Para tener el usuario:
    private FirebaseAuth mAuth;
    private FirebaseUser usuarioFirebase;
    private Usuario usuario;
    private String titulo;

    public static CalendarioFragment newInstance() {
        return new CalendarioFragment();
    }

    public static CalendarioFragment newInstance(String collabId, String collabViewId, ArrayList<String> itemIds) {
        CalendarioFragment fragment = new CalendarioFragment();
        Bundle args = new Bundle();
        args.putString(ARG_COLLAB_ID, collabId);
        args.putString(ARG_COLLABVIEW_ID, collabViewId);
        args.putStringArrayList(ARG_ITEMS_ID, itemIds);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendario, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        configurarIdiomaCalendario();

        //Layouts (orden importante)
        calendarView = view.findViewById(R.id.calendarView);
        TextView tvTitulo = view.findViewById(R.id.tituloCalendario);
        TextView tvSubtitulo = view.findViewById(R.id.subtituloCalendario);
        recyclerView = view.findViewById(R.id.recyclerItemsDia);

        Toolbar toolbar = view.findViewById(R.id.toolbar);

        ajustesCalendario();
        lecturaArgumentos();


        if (general) {
            toolbar.setVisibility(View.GONE);
            cargarCalendarioGeneral();
        } else {
            //Oculto estos, que solo quiero que se enseñen en el modo general.
            tvTitulo.setVisibility(View.GONE);
            tvSubtitulo.setVisibility(View.GONE);

            toolbar.setTitle(titulo != null ? titulo : TITULO_DEFECTO);
            toolbar.setNavigationOnClickListener(v -> { //para volver atrás
                if (getParentFragmentManager() != null) {
                    getParentFragmentManager().popBackStack();
                }
            });
            cargarCalendarioCV();
        }


        //Creamos el adapter de la lista de items
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new CollabItemAdapter(new ArrayList<>(), item -> {
            //Pasamos los parámetros necesarios
            CollabItemFragment fragment = CollabItemFragment.newInstance(
                    item.getIdI(),
                    item.getIdC()
            );
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentApp, fragment)
                    .addToBackStack(null)
                    .commit();
        });


        recyclerView.setAdapter(adapter);
    }

    private void lecturaArgumentos() {
        if (getArguments() != null) {
            idC = getArguments().getString(ARG_COLLAB_ID);
            idCV = getArguments().getString(ARG_COLLABVIEW_ID);
            idItems = getArguments().getStringArrayList(ARG_ITEMS_ID);

            if (idC != null && idCV != null && idItems != null) { //si no son nulos, estamos en el modo collab view
                general = false;
            }
        }
    }

    private void cargarCalendarioGeneral() {
        //Sacamos las cosas del usuario
        mAuth = FirebaseAuth.getInstance();
        usuarioFirebase = mAuth.getCurrentUser();
        usuario = new Usuario();
        usuario.obtener(usuarioFirebase.getUid(), new OnDataLoadedCallback<Usuario>() {
            @Override
            public void onSuccess(Usuario data) {
                usuario = data;

                // Cargar items del día actual (para que al inicio salgan los items de hoy)
                Calendar hoy = Calendar.getInstance();
                cargarItems(hoy);
                //filtrarItemsDia(hoy);

                // Listener al cambiar de día -> se abre lista de items
                calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
                    Calendar cal = Calendar.getInstance();
                    cal.set(year, month, dayOfMonth);
                    filtrarItemsDia(cal);
                });
            }

            @Override
            public void onFailure(Exception e) {
                if (isAdded()) {
                    Toast.makeText(getContext(), "Error al cargar perfil: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void cargarItems(Calendar cal) {

        // Cargo los items de ese día del usuario de cualquier collab (de ese usuario)
        //Saco collabs del usuario:
        new Collab().obtenerCollabsDelUsuario(usuario.getUID(), new OnDataLoadedCallback<ArrayList<Collab>>() {
            @Override
            public void onSuccess(ArrayList<Collab> listaCollabs) {
                List<CollabItem> todosItems = new ArrayList<>(); //auxiliar por firebase
                AtomicInteger contador = new AtomicInteger(0); //por firebase

                // Aquí tenemos todas las collabs del usuario -> sacamos los items asignados a ese usuario
                for (Collab collab : listaCollabs) {
                    new CollabItem().obtenerCollabItemsUsr(usuario.getUID(), collab.getId(), new OnDataLoadedCallback<List<CollabItem>>() {

                        @Override
                        public void onSuccess(List<CollabItem> data) {
                            todosItems.addAll(data);
                            if (contador.incrementAndGet() == listaCollabs.size()) {
                                lci = todosItems; //solo aqui actualizo esta lista
                                filtrarItemsDia(cal);
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(getContext(), CollabItem.CollabItemConstants.ERROR_CARGA_ITEMS + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Error al cargar collabs del usuario: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarCalendarioCV() {
        //Me llega la lista de los ids de los collab items
        AtomicInteger contador = new AtomicInteger(0); //por firebase

        for (String idItem : idItems) {
            CollabItem item = new CollabItem();
            item.setIdI(idItem);
            item.setIdC(idC);
            item.obtener(idItem, new OnDataLoadedCallback<CollabItem>() {
                @Override
                public void onSuccess(CollabItem data) {
                    lci.add(data);
                    if (contador.incrementAndGet() == idItems.size()) {
                        Toast.makeText(getContext(), CollabItem.CollabItemConstants.CONF_COLLABITEM_CARGADOS, Toast.LENGTH_SHORT).show();
                        Calendar hoy = Calendar.getInstance();
                        filtrarItemsDia(hoy);

                        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
                            Calendar cal = Calendar.getInstance();
                            cal.set(year, month, dayOfMonth);
                            filtrarItemsDia(cal);
                        });
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(getContext(), CollabItem.CollabItemConstants.ERROR_CARGAR_ITEM + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    private void filtrarItemsDia(Calendar fSel) {
        List<CollabItem> filtrados = new ArrayList<>();
        for (CollabItem item : lci) {
            if (item.getFecha() != null) {
                Calendar itemCal = Calendar.getInstance();
                itemCal.setTime(item.getFecha().toDate());
                if (itemCal.get(Calendar.YEAR) == fSel.get(Calendar.YEAR) &&
                        itemCal.get(Calendar.MONTH) == fSel.get(Calendar.MONTH) &&
                        itemCal.get(Calendar.DAY_OF_MONTH) == fSel.get(Calendar.DAY_OF_MONTH)) {
                    filtrados.add(item);
                }
            }
        }
        adapter.setItems(filtrados);
    }

    private void ajustesCalendario() {
        calendarView.setFirstDayOfWeek(Calendar.MONDAY);
        calendarView.setWeekDayTextAppearance(R.style.CalendarWeekDay);
        calendarView.setDateTextAppearance(R.style.CalendarDate);
    }

    private void configurarIdiomaCalendario() {
        Locale locale = new Locale("es", "ES");
        Locale.setDefault(locale);
        Configuration config = getResources().getConfiguration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    public void setAdapter(RecyclerView.Adapter<?> adapter) {
        this.adapter = (CollabItemAdapter) adapter;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public CollabItemAdapter.OnItemClickListener cambiarAItem() {
        return item -> {
            CollabItemFragment fragment = CollabItemFragment.newInstance(
                    item.getIdI(),
                    item.getIdC()
            );
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentApp, fragment)
                    .addToBackStack(null)
                    .commit();
        };
    }
}